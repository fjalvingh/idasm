package to.etc.dec.idasm.disassembler.model;

import java.util.ArrayList;
import java.util.List;

final public class RegionModel {
	/**
	 * Regions, sorted by start address. No region can overlap.
	 */
	private List<Region> m_regionList = new ArrayList<>();

	public int getRegionCount() {
		return m_regionList.size();
	}

	public Region getRegionAt(int index) {
		return m_regionList.get(index);
	}

	public void addRegion(RegionType type, int start, int end) {
		if(start >= end)
			throw new IllegalArgumentException("start (" + start + ") must be less than end (" + end + ")");

		//-- Find any region that this overlaps with.
		int index = 0;
		while(index < m_regionList.size()) {
			Region region = m_regionList.get(index);
			if(end < region.getEnd()) {
				//-- We're before this region. We need to insert a new one before it.
				m_regionList.add(index, new Region(start, end, type));
				return;
			} else if(start >= region.getEnd()) {
				//-- Not in this region, at all. Just try the next one
				index++;
			} else if(start <= region.getStart() && end >= region.getEnd()) {    // Does the new region totally obscure the new one?
				//-- This replaces the region, at least at the start.
				index = setRegionAt(index, type, start, region.getEnd());
				start = region.getEnd();                                        // The rest of the thing goes here. Same regions will be merged later.
				index++;
			} else if(start < region.getStart()) {
				/*
				 * The new one starts before existing, but we also know it ends BEFORE the existing
				 * one ends (if not it would fully overlap). Add a region before the existing one, then
				 * be done.
				 */
				region.setStart(end);                        // The existing one now starts at our end
				index = insertRegionAt(index, type, start, end);
				return;
			} else {
				int oldEnd = region.getEnd();
				RegionType oldType = region.getType();
				if(start > region.getStart()) {
					//-- Make the existing one end at the new start,
					region.setEnd(start);                        // The existing one ends at the new one's start
					index++;
					region = null;							// No more region!
				}

				if(end >= oldEnd) {
					//-- We will have overwritten the 2nd part of the region. Just insert it,
					insertRegionAt(index, type, start, oldEnd);
					start = oldEnd;
				} else {
					//-- the new one ends inside the previous one,
					if(region == null) {
						//-- We overwrote the existing region with a start part. I.e. we split it in 3 parts (old, new, old)
						region = new Region();
						index = insertRegionAt(index, type, start, end);		// Insert the new region
						insertRegionAt(index + 1, oldType, end, oldEnd);	// Insert the tail
						return;
					} else {
						//-- It ends after the existing one..
						index = setRegionAt(index, type, start, oldEnd);		// Make the existing thing the new type
						start = oldEnd;
					}
				}
			}
			if(start >= end)
				return;
		}

		//-- If we still have a region left it needs to be added at the end..
		if(start < end) {
			m_regionList.add(new Region(start, end, type));
		}
	}

	private int insertRegionAt(int index, RegionType type, int start, int end) {
		m_regionList.add(index, new Region());
		return setRegionAt(index, type, start, end);
	}

	/**
	 * Replace the region at index. Merge adjacent regions.
	 */
	private int setRegionAt(int index, RegionType type, int start, int end) {
		Region r = m_regionList.get(index);
		r.update(type, start, end);

		//-- If the previous one directly touches this one AND has the same type - merge
		if(index > 0) {
			Region b = m_regionList.get(index - 1);
			if(b.getEnd() == r.getStart() && b.getType() == r.getType()) {
				//-- We can merge these, and remove r.
				b.update(type, b.getStart(), end);
				m_regionList.remove(index);
				index--;
				r = b;
			}
		}

		//-- Is the next one mergable?
		if(index < m_regionList.size() - 1) {
			Region a = m_regionList.get(index + 1);			// Get region after
			if(end == a.getStart() && a.getType() == type) {
				//-- We can merge the next one in here
				r.update(type, a.getStart(), end);
				m_regionList.remove(index + 1);
			}
		}
		return index;
	}


}

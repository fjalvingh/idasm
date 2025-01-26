package to.etc.dec.idasm.disassembler.model;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.dec.idasm.disassembler.util.Util;

import java.util.ArrayList;
import java.util.List;

final public class RegionModel {
	/**
	 * Regions, sorted by start address. No region can overlap.
	 */
	private List<Region> m_regionList = new ArrayList<>();

	private int m_currentRegionIndex;

	@Nullable
	private Region m_currentRegion;

	public RegionModel() {
		//-- Start with a full range region
		m_regionList.add(new Region(0, Integer.MAX_VALUE, RegionType.Code));
	}

	public void addRegion(RegionType type, int start, int end) {
		if(start >= end)
			throw new IllegalArgumentException("start (" + start + ") must be less than end (" + end + ")");

		//-- Find any region that this overlaps with.
		int index = 0;
		while(index < m_regionList.size()) {
			Region region = m_regionList.get(index);
			if(end <= region.getStart()) {
				//-- We're before this region. We need to insert a new one before it.
				insertRegionAt(index, type, start, end);
				//m_regionList.add(index, new Region(start, end, type));
				return;
			} else if(start >= region.getEnd()) {
				//-- Not in this region, at all. Just try the next one
				index++;
			} else if(start <= region.getStart() && end >= region.getEnd()) {    // Does the new region totally obscure the new one?
				//-- This replaces the region, at least at the start.
				index = setRegionAt(index, type, start, region.getEnd());
				region = m_regionList.get(index);
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
						//--
						index = setRegionAt(index, type, start, end);		// Make the existing thing the new type
						insertRegionAt(index + 1, oldType, end, oldEnd);
						start = oldEnd;
					}
				}
			}
			if(start >= end)
				return;
		}

		//-- If we still have a region left it needs to be added at the end..
		if(start < end) {
			insertRegionAt(index, type, start, end);
			//m_regionList.add(new Region(start, end, type));
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
				r.update(type, r.getStart(), a.getEnd());
				m_regionList.remove(index + 1);
			}
		}
		return index;
	}


	public void initializeFrom(List<Region> regionList) {
		m_regionList.clear();
		m_regionList.addAll(regionList);

		//-- Make all regions in holes
		int lastAddress = 0;
		int index = 0;
		while(index < m_regionList.size()) {
			Region region = m_regionList.get(index++);
			if(region.getStart() > lastAddress) {
				//-- Insert a code region.
				index = insertRegionAt(index - 1, RegionType.Code, lastAddress, region.getStart());
			}
			lastAddress = region.getEnd();
		}

		if(lastAddress < Integer.MAX_VALUE) {
			index = insertRegionAt(m_regionList.size(), RegionType.Code, lastAddress, Integer.MAX_VALUE);
		}
	}

	public List<Region> getRegionList() {
		return m_regionList;
	}

	/**
	 * Return the index of the Region containing the address.
	 */
	public int getRegionIndexByAddress(int address) {
		//-- We match on start address only.
		int index = Util.binarySearch(m_regionList, address, a -> a.getStart(), Integer::compareTo);
		if(index < 0) {
			index = -(index + 1);
		}

		/*
		 * If the address is the actual start address then this will have returned
		 * the actual region. Check its bounds.
		 */
		if(index < m_regionList.size()) {
			Region r = m_regionList.get(index);
			if(address >= r.getStart() && address < r.getEnd()) {
				return index;
			}
		}

		/*
		 * But usually it would find the "next" region, as that one is where
		 * a new one should be inserted..
		 */
		if(index > 0) {
			index--;
			Region r = m_regionList.get(index);
			if(address >= r.getStart() && address < r.getEnd()) {
				return index;
			}
		}

		throw new IllegalStateException("Region not correct at index " + index);
	}

	public int getRegionCount() {
		return m_regionList.size();
	}

	public Region getRegionByIndex(int index) {
		return m_regionList.get(index);
	}


	/*----------------------------------------------------------------------*/
	/*	CODING:	Walking regions												*/
	/*----------------------------------------------------------------------*/

	public Region updateAddress(int address) {
		Region currentRegion = m_currentRegion;
		if(currentRegion != null) {
			if(address < currentRegion.getEnd() && address >= currentRegion.getStart()) {
				return currentRegion;
			}
			m_currentRegion = null;
		}

		if(m_currentRegion == null) {
			m_currentRegionIndex = getRegionIndexByAddress(address);
			m_currentRegion = getRegionByIndex(m_currentRegionIndex);
		}
		if(m_currentRegion == null) {
			throw new IllegalStateException("Region not found");
		}
		return m_currentRegion;
	}

}

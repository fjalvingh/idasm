package to.etc.dec.idasm.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import to.etc.dec.idasm.disassembler.model.Region;
import to.etc.dec.idasm.disassembler.model.RegionModel;
import to.etc.dec.idasm.disassembler.model.RegionType;

import java.util.ArrayList;
import java.util.List;

public class RegionTests {
	private RegionModel m_model = new RegionModel();

	@Before
	public void setRegionModel() {
		m_model = new RegionModel();
	}

	public void defaultRegions() {
		m_model.addRegion(RegionType.StringAsciiWordBE, 100, 200);
		m_model.addRegion(RegionType.ByteData, 300, 400);
		m_model.addRegion(RegionType.StringAsciiC, 500, 600);
		m_model.addRegion(RegionType.WordData, 600, 700);
		m_model.addRegion(RegionType.ByteData, 700, 800);
		assertSorted();
	}

	@Test
	public void testAddAndRemove() throws Exception {
		assertRegions();

		m_model.addRegion(RegionType.StringAsciiWordBE, 100, 200);
		assertRegions(RegionType.StringAsciiWordBE, 100, 200);
		m_model.addRegion(RegionType.Code, 100, 200);
		assertRegions();
	}

	@Test
	public void testOverwriteFirstStartOnly() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 100, 150);	// Overwrite start of 1st code
		assertRegions(
			RegionType.LongData, 100, 150,				// <-- new one partially overwrites 1sy
			RegionType.StringAsciiWordBE, 150, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testOverwriteFirstEndOnlyAtExactEnd() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 150, 200);	// Overwrite end of 1st code
		assertRegions(
			RegionType.StringAsciiWordBE, 100, 150,
			RegionType.LongData, 150, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testOverwriteFirstOneInTheMiddle() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 150, 160);	// Put it splat in the middle
		assertRegions(
			RegionType.StringAsciiWordBE, 100, 150,
			RegionType.LongData, 150, 160,
			RegionType.StringAsciiWordBE, 160, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testRegionOverwriteFirstFully() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 100, 200);
		assertRegions(
			RegionType.LongData, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testRegionInsertAsFirst() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 50, 60);
		assertRegions(
			RegionType.LongData, 50, 60,				// <-- new one
			RegionType.StringAsciiWordBE, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testRegionInsertInTheMiddle() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 250, 260);
		assertRegions(
			RegionType.StringAsciiWordBE, 100, 200,
			RegionType.LongData, 250, 260,				// <-- new one
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}


	@Test
	public void testRegionsAreAsInitialized() {
		defaultRegions();
		assertRegions(
			RegionType.StringAsciiWordBE, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testOverwritePastEnd() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 150, 250);	// Middle of code, but past its end
		assertRegions(
			RegionType.StringAsciiWordBE, 100, 150,
			RegionType.LongData, 150, 250,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testOverwriteFullyFromStart() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 100, 400);	// Replace the 1st two blocks
		assertRegions(
			RegionType.LongData, 100, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.WordData, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testOverwriteFullyFromEnd() throws Exception {
		defaultRegions();
		m_model.addRegion(RegionType.LongData, 600, 900);	// Put it splat in the middle
		assertRegions(
			RegionType.StringAsciiWordBE, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.LongData, 600, 900
		);
	}

	private List<Region> expand(Object... list) {
		int lastAddress = 0;
		List<Region> res = new ArrayList<>();
		for(int i = 0; i < list.length; i += 3) {
			RegionType type = (RegionType) list[i];
			if(type == RegionType.Code)
				throw new IllegalStateException("Should not have code regions in the match list; these are automatic");
			int from = (Integer) list[i + 1];
			int to = (Integer) list[i + 2];
			if(from >= to) {
				Assert.fail("Region template " + (i / 3) + " start >= end");
			}
			if(lastAddress < from) {
				res.add(new Region(lastAddress, from, RegionType.Code));
			}
			res.add(new Region(from, to, type));
			lastAddress = to;
		}
		if(lastAddress < Integer.MAX_VALUE) {
			res.add(new Region(lastAddress, Integer.MAX_VALUE, RegionType.Code));
		}

		return res;
	}

	private void assertRegions(Object... list) {
		assertSorted();
		List<Region> expected = expand(list);

		//-- Do we have the same number of expected regions?
		if(expected.size() != m_model.getRegionCount()) {
			dumpCompare(expected);
			Assert.fail("The number of non-code regions (" + m_model.getRegionCount() + ") is not the same as the expected # (" + expected.size() + ")");
		}

		for(int i = 0; i < expected.size(); i += 3) {
			Region r = m_model.getRegionByIndex(i);
			Region e = expected.get(i);

			if(r.getType() != e.getType() || e.getStart() != r.getStart() || e.getEnd() != r.getEnd()) {
				dumpCompare(expected);
				Assert.fail("Region mismatch at index " + i);
			}
		}
	}

	private void dumpCompare(List<Region> expected) {
		int max = Math.max(m_model.getRegionCount(), expected.size());
		StringBuilder sb = new StringBuilder();
		for(int index = 0; index < max; index++) {
			sb.setLength(0);
			sb.append(String.format("[%-3d] ", index));

			if(index < expected.size()) {
				Region e = expected.get(index);

				sb.append(String.format("%20s %-3d %-10d ", e.getType().name(), e.getStart(), e.getEnd()));
			} else {
				sb.append(String.format("%36s", ""));
			}

			if(index < m_model.getRegionCount()) {
				Region region = m_model.getRegionByIndex(index);
				sb.append(String.format("%20s %-3d %-10d ", region.getType().name(), region.getStart(), region.getEnd()));
			} else {
				sb.append(String.format("%36s", ""));
			}
			System.out.println(sb.toString());
		}
	}

	/**
	 * Checks all regions are sorted and fully ajacent.
	 */
	private void assertSorted() {
		int start = -1;
		int lastAddress = 0;
		for(int i = 0; i < m_model.getRegionCount(); i++) {
			Region r = m_model.getRegionByIndex(i);
			if(r.getStart() != lastAddress) {
				dumpRegions();
				Assert.fail("Region " + i + " not ajacent with previous one: start=" + r.getStart() + " but expected was " + lastAddress);
			}
			lastAddress = r.getEnd();

			if(r.getStart() < start) {
				dumpRegions();
				Assert.fail("Region start " + r.getStart() + " is not in order (last was " + start + ")");
			}
			if(r.getEnd() <= r.getStart()) {
				dumpRegions();
				Assert.fail("Region start " + r.getStart() + " and region end (" + start + ") are incorrectly ordered");
			}
			start = r.getEnd();
		}
	}

	private void dumpRegions() {
		System.out.println("--- Region list ---");
		for(int i = 0; i < m_model.getRegionCount(); i++) {
			Region r = m_model.getRegionByIndex(i);
			System.out.println(r.getType() + " " + r.getStart() + " " + r.getEnd());
		}
	}


}

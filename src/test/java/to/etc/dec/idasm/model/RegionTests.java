package to.etc.dec.idasm.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import to.etc.dec.idasm.disassembler.model.Region;
import to.etc.dec.idasm.disassembler.model.RegionModel;
import to.etc.dec.idasm.disassembler.model.RegionType;

public class RegionTests {
	private RegionModel m_model = new RegionModel();

	@Before
	public void initialRegions() throws Exception {
		m_model.addRegion(RegionType.Code, 100, 200);
		m_model.addRegion(RegionType.ByteData, 300, 400);
		m_model.addRegion(RegionType.StringAsciiC, 500, 600);
		m_model.addRegion(RegionType.Code, 600, 700);
		m_model.addRegion(RegionType.ByteData, 700, 800);
		assertSorted();
	}

	@Test
	public void testOverwriteFirstStartOnly() throws Exception {
		m_model.addRegion(RegionType.LongDataLE, 100, 150);
		assertRegions(
			RegionType.LongDataLE, 100, 150,				// <-- new one
			RegionType.Code, 150, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.Code, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}


	@Test
	public void testRegionOverwriteFirstFully() throws Exception {
		m_model.addRegion(RegionType.LongDataLE, 100, 200);
		assertRegions(
			RegionType.LongDataLE, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.Code, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testRegionInsertAsFirst() throws Exception {
		m_model.addRegion(RegionType.LongDataLE, 50, 60);
		assertRegions(
			RegionType.LongDataLE, 50, 60,				// <-- new one
			RegionType.Code, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.Code, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	@Test
	public void testRegionInsertInTheMiddle() throws Exception {
		m_model.addRegion(RegionType.LongDataLE, 250, 260);
		assertRegions(
			RegionType.Code, 100, 200,
			RegionType.LongDataLE, 250, 260,				// <-- new one
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.Code, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}


	@Test
	public void testRegionsAreAsInitialized() {
		assertRegions(
			RegionType.Code, 100, 200,
			RegionType.ByteData, 300, 400,
			RegionType.StringAsciiC, 500, 600,
			RegionType.Code, 600, 700,
			RegionType.ByteData, 700, 800
		);
	}

	private void assertRegions(Object... list) {
		assertSorted();

		//-- Do we have the same number of expected regions?
		if(list.length / 3 != m_model.getRegionCount()) {
			dumpCompare(list);
			Assert.fail("The number of regions (" + m_model.getRegionCount() + ") is not the same as the expected # (" + list.length / 3 + ")");
		}

		int index = 0;
		for(int i = 0; i < list.length; i += 3) {
			RegionType type = (RegionType) list[i];
			int from = (Integer) list[i + 1];
			int to = (Integer) list[i + 2];
			if(from >= to) {
				Assert.fail("Region template " + index + " start >= end");
			}
			Region r = m_model.getRegionAt(index);
			if(r.getType() != type || r.getStart() != from || r.getEnd() != to) {
				dumpCompare(list);
				Assert.fail("Region " + index + " does not match");
			}
			index++;
		}
	}

	private void dumpCompare(Object[] list) {
		int max = Math.max(m_model.getRegionCount(), list.length / 3);
		StringBuilder sb = new StringBuilder();
		for(int index = 0; index < max; index++) {
			sb.setLength(0);
			sb.append(String.format("[%-3d] ", index));

			int p = index * 3;
			if(p < list.length) {
				RegionType type = (RegionType) list[p];
				int from = (Integer) list[p + 1];
				int to = (Integer) list[p + 2];

				sb.append(String.format("%15s %-3d %-3d ", type.name(), from, to));
			} else {
				sb.append(String.format("%24s", ""));
			}

			if(index < m_model.getRegionCount()) {
				Region region = m_model.getRegionAt(index);
				sb.append(String.format("%15s %-3d %-3d ", region.getType().name(), region.getStart(), region.getEnd()));
			} else {
				sb.append(String.format("%24s", ""));
			}
			System.out.println(sb.toString());
		}
	}

	private void assertSorted() {
		int start = -1;
		for(int i = 0; i < m_model.getRegionCount(); i++) {
			Region r = m_model.getRegionAt(i);
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
			Region r = m_model.getRegionAt(i);
			System.out.println(r.getType() + " " + r.getStart() + " " + r.getEnd());
		}
	}


}

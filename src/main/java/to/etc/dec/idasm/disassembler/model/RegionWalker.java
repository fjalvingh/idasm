package to.etc.dec.idasm.disassembler.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 *
 */
public class RegionWalker {
	private final RegionModel m_model;

	private int m_currentRegionIndex;

	@Nullable
	private Region m_currentRegion;

	public RegionWalker(RegionModel model) {
		m_model = model;
	}

	public Region updateAddress(int address) {
		Region currentRegion = m_currentRegion;
		if(currentRegion != null) {
			if(address < currentRegion.getEnd()) {
				return currentRegion;
			}
			m_currentRegion = null;
		}

		if(currentRegion == null) {
			m_currentRegionIndex = m_model.getRegionIndexByAddress(address);
			m_currentRegion = m_model.getRegionByIndex(m_currentRegionIndex);
		}
		return m_currentRegion;
	}
}

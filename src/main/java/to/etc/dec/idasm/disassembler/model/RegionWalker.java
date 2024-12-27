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

	public void updateAddress(int address) {
		if(m_currentRegion == null) {




		}


	}




}

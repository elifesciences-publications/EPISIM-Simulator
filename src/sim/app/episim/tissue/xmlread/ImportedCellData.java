package sim.app.episim.tissue.xmlread;

import java.util.HashMap;

public class ImportedCellData extends AbstractImportedObjectData {
	

	private static final String RATIOAXIS = "ratioAxis";
	private static final String ECCENTRICITY = "Eccentricity";
	private static final String SOLIDITY = "Solidity";
	private static final String NNUCLEI = "nNuclei";
	private static final String NUCLEIID = "nucleiID";
	private static final String RATIONUC2CP = "ratioNuc2Cp";
	private static final String NUCDENSITY = "nucDensity";
	private static final String MEANINT = "MeanInt";
	private static final String MEDIANINT = "MedianInt";
	private static final String QUANT75INT = "Quant75Int";
	private static final String CELLINTR = "cellIntR";
	private static final String CELLINTG = "cellIntG";
	private static final String CELLINTB = "cellIntB";
	private static final String MEMINTR = "memIntR";
	private static final String MEMINTG = "memIntG";
	private static final String MEMINTB = "memIntB";
	private static final String NUCINTR = "nucIntR";
	private static final String NUCINTG = "nucIntG";
	private static final String NUCINTB = "nucIntB";
	private static final String CYTINTR = "cytIntR";
	private static final String CYTINTG = "cytIntG";
	private static final String CYTINTB = "cytIntB";
	private static final String N_NEIGHBOUR = "nNeighbour";
	private static final String NEIGHBOUR_IDS = "neighbourID";

	
	private static final String TISSUEPARAMS = "TissueParams";
	private static final String IMAGE = "Image";
	private static final String EPIDERMIS = "Epidermis";
	private static final String MARKERPROFILE = "markerProfile";
	private static final String SURFACE = "Surface";
	private static final String BASALLAMINA = "BasalLamina";
	private static final String CELLS = "Cells";
	private static final String NUCLEI = "Nuclei";
	private static final String CELL = "Cell";
	private static final String NUCLEUS = "Nucleus";
	private static final String RESOLUTIONNM = "ResolutionNM";
	private static final String TISSUEID = "TissueID";
	private static final String MEANTHICKNESS = "MeanThickness";
	private static final String MAXTHICKNESS = "MaxThickness";
	private static final String ORIENTATION = "Orientation";
	private static final String PIXEL = "Pixel";
	private static final String MARKERPROFILEX = "markerProfileX";
	private static final String MARKERPROFILEY = "markerProfileY";
	private static final String LAYER = "Layer";
	private static final String ORIENTATIONX = "OrientationX";
	private static final String NNEIGHBOUR = "nNeighbour";
	private static final String NEIGHBOURID = "neighbourID";
	private static final String DISTBLABS = "DistBlAbs";
	private static final String NUCLEUSID = "nucleiID";
	private static final String DIST2BL = "Dist2BlAbs";
	private static final String RESOLUTION_IN_NM = "ResolutionNM";
	private static final String TISSUE = "TissueParams";
	private static final String TISSUE_IMAGE_ID = "TissueID";
	private static final String X = "X";
	private static final String Y = "Y";

	private double ratioAxis;
	private double eccentricity;
	private double solidity;
	private double nNuclei;
	private long[] nucleiID;
	private double nNeighbour;
	private long[] neighbourID;
	private double ratioNuc2Cp;
	private double nucDensity;
	private double meanInt;
	private double medianInt;
	private double quant75Int;
	private double cellIntR;
	private double cellIntG;
	private double cellIntB;
	private double memIntR;
	private double memIntG;
	private double memIntB;
	private double nucIntR;
	private double nucIntG;
	private double nucIntB;
	private double cytIntR;
	private double cytIntG;
	private double cytIntB;

	public ImportedCellData(long id) {
		super(id);
	}

	@Override
	public boolean setAttribute(String nodeName,
			HashMap<String, String> attNameValue) {
		if (super.setAttribute(nodeName, attNameValue)) {
			return true;
		} else if (nodeName.equals(RATIOAXIS)) {
			this.ratioAxis = stod(attNameValue.get("value"));
		} else if (nodeName.equals(ECCENTRICITY)) {
			this.eccentricity = stod(attNameValue.get("value"));
		} else if (nodeName.equals(SOLIDITY)) {
			this.solidity = stod(attNameValue.get("value"));
		} else if (nodeName.equals(NNUCLEI)) {
			this.nNuclei = stod(attNameValue.get("value"));
		} else if (nodeName.equals(NUCLEIID)) {
			this.nucleiID = new long[attNameValue.size()];
			int i = 0;
			for(String s : attNameValue.values()){
				nucleiID[i++] = stol(s);
			}
		} else if (nodeName.equals(N_NEIGHBOUR)) {
			this.nNeighbour = stod(attNameValue.get("value"));
		} else if (nodeName.equals(NEIGHBOUR_IDS)) {
			this.neighbourID = new long[attNameValue.size()];
			int i = 0;
			for(String s : attNameValue.values()){
				neighbourID[i++] = stol(s);
			}
		}else if (nodeName.equals(RATIONUC2CP)) {
			this.ratioNuc2Cp = stod(attNameValue.get("value"));
		} else if (nodeName.equals(NUCDENSITY)) {
			this.nucDensity = stod(attNameValue.get("value"));
		} else if (nodeName.equals(MEANINT)) {
			this.meanInt = stod(attNameValue.get("value"));
		} else if (nodeName.equals(MEDIANINT)) {
			this.medianInt = stod(attNameValue.get("value"));
		} else if (nodeName.equals(QUANT75INT)) {
			this.quant75Int = stod(attNameValue.get("value"));
		} else if (nodeName.equals(CELLINTR)) {
			this.cellIntR = stod(attNameValue.get("value"));
		} else if (nodeName.equals(CELLINTG)) {
			this.cellIntG = stod(attNameValue.get("value"));
		} else if (nodeName.equals(CELLINTB)) {
			this.cellIntB = stod(attNameValue.get("value"));
		} else if (nodeName.equals(MEMINTR)) {
			this.memIntR = stod(attNameValue.get("value"));
		} else if (nodeName.equals(MEMINTG)) {
			this.memIntG = stod(attNameValue.get("value"));
		} else if (nodeName.equals(MEMINTB)) {
			this.memIntB = stod(attNameValue.get("value"));
		}else if (nodeName.equals(NUCINTR)) {
			this.nucIntR = stod(attNameValue.get("value"));
		} else if (nodeName.equals(NUCINTG)) {
			this.nucIntG = stod(attNameValue.get("value"));
		} else if (nodeName.equals(NUCINTB)) {
			this.nucIntB = stod(attNameValue.get("value"));
		}else if (nodeName.equals(CYTINTR)) {
			this.cytIntR = stod(attNameValue.get("value"));
		} else if (nodeName.equals(CYTINTG)) {
			this.cytIntG = stod(attNameValue.get("value"));
		} else if (nodeName.equals(CYTINTB)) {
			this.cytIntB= stod(attNameValue.get("value"));
		}else
			return false;
		return true;
		}

	public double getRatioAxis() {
		return ratioAxis;
	}

	public double getEccentricity() {
		return eccentricity;
	}

	public double getSolidity() {
		return solidity;
	}

	public double getnNuclei() {
		return nNuclei;
	}

	public long[] getNucleiID() {
		return nucleiID;
	}

	public double getnNeighbour() {
		return nNeighbour;
	}

	public double getRatioNuc2Cp() {
		return ratioNuc2Cp;
	}

	public double getNucDensity() {
		return nucDensity;
	}

	public double getMeanInt() {
		return meanInt;
	}

	public double getMedianInt() {
		return medianInt;
	}

	public double getQuant75Int() {
		return quant75Int;
	}

	public double getCellIntR() {
		return cellIntR;
	}

	public double getCellIntG() {
		return cellIntG;
	}

	public double getCellIntB() {
		return cellIntB;
	}

	public double getMemIntR() {
		return memIntR;
	}

	public double getMemIntG() {
		return memIntG;
	}

	public double getMemIntB() {
		return memIntB;
	}

	public double getNucIntR() {
		return nucIntR;
	}

	public double getNucIntG() {
		return nucIntG;
	}

	public double getNucIntB() {
		return nucIntB;
	}

	public double getCytIntR() {
		return cytIntR;
	}

	public double getCytIntG() {
		return cytIntG;
	}

	public double getCytIntB() {
		return cytIntB;
	}
	
	public long[] getNeighbourID() {
		return neighbourID;
	}	
}

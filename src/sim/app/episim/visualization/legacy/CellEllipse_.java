package sim.app.episim.visualization.legacy;

import java.awt.Color;
import java.util.ArrayList;

import sim.app.episim.tissueimport.evaluation.tabledata.CellColumn;
import sim.app.episim.tissueimport.xmlread.ImportedCellData;

public class CellEllipse_ extends AbstractCellEllipse_ {

	public enum CellMember implements CellColumn{

		ID("ID", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getId();
			}
		}), DIST_TO_BL_NORM("Distance to BL", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getDist2BlNorm();
			}
		}), DIST_TO_BL_ABS("Distance to BL Abs", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getDist2BlAbs();
			}
		}), AREA("Area", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getArea();
			}
		}), PERIMETER("Perimeter", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getPerimeter();
			}
		}), ORIENTATION_X("Orientation X", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getOrientationX();
			}
		}), ORIENTATION_BL("Orientation BL", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getOrientationBL();
			}
		}) ,CENTER_X("Center X", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getX();
			}
		}), CENTER_Y("Center Y", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getY();
			}
		}), MAJOR_AXIS("Major Axis", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getMajorAxis();
			}
		}), MINOR_AXIS("Minor Axis", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getMinorAxis();
			}
		}), HEIGHT("Height", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getHeight();
			}
		}), WIDTH("Width", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getWidth();
			}
		}), RATIO_N_LEN("Ratio n Len", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getRationLen();
			}
		}), ROUNDNESS("Roundness", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getRoundness();
			}
		}), RATIO_AXIS("Ratio Axis", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getRatioAxis();
			}
		}), ECCENTRICITY("Eccentricity", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getEccentricity();
			}
		}), SOLIDITY("Solodity", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getSolidity();
			}
		}), N_NUCLEI("n Nuclei", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getnNuclei();
			}
		}), N_NEIGHBOURS("n Neighbours", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getnNeighbour();
			}
		}), RATIO_NUC_TO_CP("Ratio Nuc to CP", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getRatioNuc2Cp();
			}
		}), NUC_DENSITY("Nuc Density", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getNucDensity();
			}
		}), MEAN_INT("Mean Int", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getMeanInt();
			}
		}), MEDIAN_INT("Median Int", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getMedianInt();
			}
		}), QUANT_75_INT("75Quantil Int", new MethodCallback() {
			public double execute(CellEllipse_ cell) {
				return cell.getQuant75Int();
			}
		});

		private String name;
		private MethodCallback callback;

		private CellMember(String name, MethodCallback callback) {
			this.name = name;
			this.callback = callback;
		}

		public String toString() {
			return name;
		}

		
		public String getColumnName() {
			return name;
		}

		
		public double getColumnValue(CellEllipse_ cell) {
			return callback.execute(cell);
		}
	}
	
	private double ratioAxis;
	private double eccentricity;
	private double solidity;
	private double nNuclei;
	private ArrayList<NucleusEllipse_> nuclei;
	private double nNeighbour;
	private double ratioNuc2Cp;
	private double nucDensity;
	private double meanInt;
	private double medianInt;
	private double quant75Int;
	private Color cellInt;
	private Color memInt;
	private Color nucInt;
	private Color cytInt;

	public CellEllipse_(ImportedCellData cellData, double micrometerPerPixel, ArrayList<NucleusEllipse_> nucleiData) {
		super(cellData, micrometerPerPixel);
		this.ratioAxis = cellData.getRatioAxis();
		this.eccentricity = cellData.getEccentricity();
		this.solidity = cellData.getSolidity();
		this.nNuclei = cellData.getnNuclei();
		this.nNeighbour = cellData.getnNeighbour();
		this.ratioNuc2Cp = cellData.getRatioNuc2Cp();
		this.nucDensity = cellData.getNucDensity();
		this.meanInt = cellData.getMeanInt();
		this.medianInt = cellData.getMedianInt();
		this.quant75Int = cellData.getQuant75Int();
		this.cellInt = new Color((float)cellData.getCellIntR()/255, (float)cellData.getCellIntG()/255, (float)cellData.getCellIntB()/255);
		this.memInt = new Color((float)cellData.getMemIntR()/255, (float)cellData.getMemIntG()/255, (float)cellData.getMemIntB()/255);
		this.nucInt = new Color((float)cellData.getNucIntR()/255, (float)cellData.getNucIntG()/255, (float)cellData.getNucIntB()/255);
		this.cytInt = new Color((float)cellData.getCytIntR()/255, (float)cellData.getCytIntG()/255, (float)cellData.getCytIntB()/255);
		
		this.nuclei = new ArrayList<NucleusEllipse_>();
		
		for(long id : cellData.getNucleiID()){
			for(NucleusEllipse_ n : nucleiData)
				if(n.getId() == id) this.nuclei.add(n);
		}
	}

	public double getRatioAxis() {
		return ratioAxis;
	}

	public void setRatioAxis(double ratioAxis) {
		this.ratioAxis = ratioAxis;
	}

	public double getEccentricity() {
		return eccentricity;
	}

	public void setEccentricity(double eccentricity) {
		this.eccentricity = eccentricity;
	}

	public double getSolidity() {
		return solidity;
	}

	public void setSolidity(double solidity) {
		this.solidity = solidity;
	}

	public double getnNuclei() {
		return nNuclei;
	}

	public void setnNuclei(double nNuclei) {
		this.nNuclei = nNuclei;
	}

	public ArrayList<NucleusEllipse_> getNucleiID() {
		return nuclei;
	}


	public double getnNeighbour() {
		return nNeighbour;
	}

	public void setnNeighbour(double nNeighbour) {
		this.nNeighbour = nNeighbour;
	}

	public double getRatioNuc2Cp() {
		return ratioNuc2Cp;
	}

	public void setRatioNuc2Cp(double ratioNuc2Cp) {
		this.ratioNuc2Cp = ratioNuc2Cp;
	}

	public double getNucDensity() {
		return nucDensity;
	}

	public void setNucDensity(double nucDensity) {
		this.nucDensity = nucDensity;
	}

	public double getMeanInt() {
		return meanInt;
	}

	public void setMeanInt(double meanInt) {
		this.meanInt = meanInt;
	}

	public double getMedianInt() {
		return medianInt;
	}

	public void setMedianInt(double medianInt) {
		this.medianInt = medianInt;
	}

	public double getQuant75Int() {
		return quant75Int;
	}

	public void setQuant75Int(double quant75Int) {
		this.quant75Int = quant75Int;
	}

	public Color getCellInt() {
		return cellInt;
	}

	public void setCellInt(Color cellInt) {
		this.cellInt = cellInt;
	}

	public Color getMemInt() {
		return memInt;
	}

	public void setMemInt(Color memInt) {
		this.memInt = memInt;
	}

	public Color getNucInt() {
		return nucInt;
	}

	public void setNucInt(Color nucInt) {
		this.nucInt = nucInt;
	}

	public Color getCytInt() {
		return cytInt;
	}

	public void setCytInt(Color cytInt) {
		this.cytInt = cytInt;
	}

	// public void setXY(int x, int y) { 
	// if (this.nucleus != null) {
	// int deltaX = x - this.getX();
	// int deltaY = y - this.getY();
	//
	// int newX = this.getNucleus().getX() + deltaX;
	// int newY = this.getNucleus().getY() + deltaY;
	// this.getNucleus().setXY(newX, newY);
	// super.setXY(x, y);
	// } else
	// super.setXY(x, y);
	// }
	
	private interface MethodCallback {
		double execute(CellEllipse_ cell);
	}

}



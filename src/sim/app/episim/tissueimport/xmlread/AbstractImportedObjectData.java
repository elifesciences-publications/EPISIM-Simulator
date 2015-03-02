package sim.app.episim.tissueimport.xmlread;

import java.util.HashMap;

public abstract class AbstractImportedObjectData {
	

	private static final String HEIGHT = "Height";
	private static final String WIDTH = "Width";
	private static final String DIST2BLABS = "Dist2BlAbs";
	private static final String DIST2BLNORM = "Dist2BlNorm";
	private static final String AREA = "Area";
	private static final String PERIMETER = "Perimeter";
	private static final String CENTER = "Center";
	private static final String MAJORAXISLENGTH = "MajorAxisLength";
	private static final String RATIOLEN = "RatioLen";
	private static final String ROUNDNESS = "Roundness";
	private static final String ORIENTATIONBL = "OrientationBL";
	private static final String MINORAXISLENGTH = "MinorAxisLength";
	private static final String ORIENTATION_X = "OrientationX";

	

	private long id;
	private double dist2BlNorm;
	private double dist2BlAbs;
	private int area;
	private double perimeter;
	private int orientationX;
	private double orientationBL;
	private int centerX;
	private int centerY;
	private double majorAxisLength;
	private double minorAxisLength;
	private double height;
	private double width;
	private double rationLen;
	private double roundness;
	
	public AbstractImportedObjectData(long id) {
		this.id = id;
	}
	
	public boolean setAttribute(String nodeName, HashMap<String, String> attNameValue) throws NumberFormatException{
		if(nodeName.equals(DIST2BLNORM)){
			this.dist2BlNorm = stod(attNameValue.get("value"));
		} else if(nodeName.equals(DIST2BLABS)){
			this.dist2BlAbs = stod(attNameValue.get("value"));
		}else if(nodeName.equals(AREA)){
			this.area = stoi(attNameValue.get("value"));
		}else if(nodeName.equals(PERIMETER)){
			this.perimeter = stod(attNameValue.get("value"));
		}else if(nodeName.equals(ORIENTATION_X)){
			this.orientationX = stoi(attNameValue.get("value"));
		}else if(nodeName.equals(ORIENTATIONBL)){
			this.orientationBL = stod(attNameValue.get("value"));
		}else if(nodeName.equals(CENTER)){
			this.centerX = stoi(attNameValue.get("value1"));
			this.centerY = stoi(attNameValue.get("value2"));
		}else if(nodeName.equals(MAJORAXISLENGTH)){
			this.majorAxisLength = stod(attNameValue.get("value"));
		}else if(nodeName.equals(MINORAXISLENGTH)){
			this.minorAxisLength = stod(attNameValue.get("value"));
		}else if(nodeName.equals(HEIGHT)){
			this.height = stod(attNameValue.get("value"));
		}else if(nodeName.equals(WIDTH)){
			this.width = stod(attNameValue.get("value"));
		}else if(nodeName.equals(RATIOLEN)){
			this.rationLen = stod(attNameValue.get("value"));
		}else if(nodeName.equals(ROUNDNESS)){
			this.roundness = stod(attNameValue.get("value"));
		} else return false;
		return true;
	}
	
	/*
	 * String to Integer
	 */
	protected int stoi(String s) throws NumberFormatException {
		if (s != null && !s.equals(""))
			return Integer.parseInt(s);
		else
			throw new NumberFormatException();
	}

	/*
	 * String to Double
	 */
	protected double stod(String s) throws NumberFormatException {
		if (s != null && !s.equals(""))
			return Double.parseDouble(s);
		else
			throw new NumberFormatException();
	}

	/*
	 * String to long
	 */
	protected long stol(String s) throws NumberFormatException {
		if (s != null && !s.equals(""))
			return Long.parseLong(s);
		else
			throw new NumberFormatException();
	}

	public long getId() {
		return id;
	}

	public double getDist2BlNorm() {
		return dist2BlNorm;
	}

	public double getDist2BlAbs() {
		return dist2BlAbs;
	}

	public int getArea() {
		return area;
	}

	public double getPerimeter() {
		return perimeter;
	}

	public int getOrientationX() {
		return orientationX;
	}

	public double getOrientationBL() {
		return orientationBL;
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public double getMajorAxisLength() {
		return majorAxisLength;
	}

	public double getMinorAxisLength() {
		return minorAxisLength;
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public double getRationLen() {
		return rationLen;
	}

	public double getRoundness() {
		return roundness;
	}
	

}

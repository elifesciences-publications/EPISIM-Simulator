package sim.app.episim.visualization;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import sim.app.episim.tissue.xmlread.AbstractImportedObjectData;

public abstract class AbstractCellEllipse_ {
	private transient Area clippedEllipse;
	private transient Area ellipseAsArea;
	private long id;
	private double dist2BlNorm;
	private double dist2BlAbs;
	private double area;
	private double perimeter;
	private double orientationX;
	private double orientationBL;
	private double centerX;
	private double centerY;
	private double majorAxisLength;
	private double minorAxisLength;

	private double height;
	private double width;
	private double rationLen;
	private double roundness;

	private double orientationInRadians;
	private double scaleFactorWidth = 1;
	private double scaleFactorHeight = 1;

	public static final char SEPARATORCHAR = ';';

	public AbstractCellEllipse_(AbstractImportedObjectData objectData,
			double micrometerPerPixel) {
		this.id = objectData.getId();
		this.area = objectData.getArea() * micrometerPerPixel * micrometerPerPixel;
		this.centerX = objectData.getCenterX() * micrometerPerPixel;
		this.centerY = objectData.getCenterY() * micrometerPerPixel;
		this.dist2BlAbs = objectData.getDist2BlAbs() * micrometerPerPixel;
		this.dist2BlNorm = objectData.getDist2BlNorm() * micrometerPerPixel;
		this.height = objectData.getHeight() * micrometerPerPixel;
		this.majorAxisLength = objectData.getMajorAxisLength() * micrometerPerPixel;
		this.minorAxisLength = objectData.getMinorAxisLength() * micrometerPerPixel;
		this.orientationBL = objectData.getOrientationBL();
		this.orientationX = objectData.getOrientationX();
		this.perimeter = objectData.getPerimeter();
		this.rationLen = objectData.getRationLen();
		this.roundness = objectData.getRoundness();
		this.width = objectData.getWidth() * micrometerPerPixel;
	}

	public void rotateCellEllipseInDegrees(double degrees) {

		rotateCellEllipseInRadians(Math.toRadians(degrees));
	}

	public void rotateCellEllipseInRadians(double radians) {
		this.orientationInRadians = radians % (2 * Math.PI);
		AffineTransform trans = new AffineTransform();
		trans.rotate(radians, centerX, centerY);
		ellipseAsArea = new Area(trans.createTransformedShape(ellipseAsArea));
		if (this.clippedEllipse != null)
			this.clippedEllipse = new Area(
					trans.createTransformedShape(clippedEllipse));
	}

	private void testMajorMinorAxisSwap() {
		if (minorAxisLength > majorAxisLength) {
			double tmp = minorAxisLength;
			minorAxisLength = majorAxisLength;
			majorAxisLength = tmp;

			double t = scaleFactorHeight;
			this.scaleFactorHeight = scaleFactorWidth;
			scaleFactorWidth = t;

		}
	}

	public void setMinorAxisLength(double minorAxis) {

		this.minorAxisLength = minorAxis;

		testMajorMinorAxisSwap();
	}

	private String getIDString(long idOtherEllipse) {
		return "" + this.id + SEPARATORCHAR + idOtherEllipse;
	}

	public double getX() {

		return centerX;

	}

	public void setXY(int x, int y) {
		this.centerX = x;
		this.centerY = y;

	}

	public double getY() {
		return centerY;
	}

	public double getMajorAxis() {
		return majorAxisLength;
	}

	public double getMinorAxis() {
		return minorAxisLength;
	}
	
	public long getId() {
		return id;
	}

	public Area getClippedEllipse() {
		return clippedEllipse;
	}

	public Area getEllipseAsArea() {
		return ellipseAsArea;
	}

	public double getDist2BlNorm() {
		return dist2BlNorm;
	}

	public double getDist2BlAbs() {
		return dist2BlAbs;
	}

	public double getArea() {
		return area;
	}

	public double getPerimeter() {
		return perimeter;
	}

	public double getOrientationX() {
		return orientationX;
	}

	public double getOrientationBL() {
		return orientationBL;
	}

	public double getCenterX() {
		return centerX;
	}

	public double getCenterY() {
		return centerY;
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

	public double getOrientationInRadians() {
		return orientationInRadians;
	}

	public double getScaleFactorWidth() {
		return scaleFactorWidth;
	}

	public double getScaleFactorHeight() {
		return scaleFactorHeight;
	}

}

package sim.portrayal.simple;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;


public class HexagonalPortrayal2DHack extends HexagonalPortrayal2D {
	
	
	
	 public HexagonalPortrayal2DHack() { this(Color.gray,1.0,true); }
    public HexagonalPortrayal2DHack(Paint paint)  { this(paint,1.0,true); }
    public HexagonalPortrayal2DHack(double scale) { this(Color.gray,scale,true); }
    public HexagonalPortrayal2DHack(boolean filled) { this(Color.gray,1.0,filled); }
    public HexagonalPortrayal2DHack(Paint paint, double scale)  { this(paint,scale,true); }
    public HexagonalPortrayal2DHack(Paint paint, boolean filled)  { this(paint,1.0,filled); }
    public HexagonalPortrayal2DHack(double scale, boolean filled)  { this(Color.gray,scale,filled); }
    public HexagonalPortrayal2DHack(Paint paint, double scale, boolean filled){
   	 super(paint, scale, filled);
    }
	
   protected AffineTransform getTransform() {
   
   	return transform;
   }
	
   protected void setTransform(AffineTransform transform) {
   
   	this.transform = transform;
   }
	
   protected double[] getxPoints() {
   
   	return xPoints;
   }
	
   protected void setxPoints(double[] xPoints) {
   
   	this.xPoints = xPoints;
   }
	
   protected double[] getyPoints() {
   
   	return yPoints;
   }
	
   protected void setyPoints(double[] yPoints) {
   
   	this.yPoints = yPoints;
   }
	
   protected double[] getScaledXPoints() {
   
   	return scaledXPoints;
   }
	
   protected void setScaledXPoints(double[] scaledXPoints) {
   
   	this.scaledXPoints = scaledXPoints;
   }
	
   protected double[] getScaledYPoints() {
   
   	return scaledYPoints;
   }
	
   protected void setScaledYPoints(double[] scaledYPoints) {
   
   	this.scaledYPoints = scaledYPoints;
   }
	
   protected int[] getTranslatedXPoints() {
   
   	return translatedXPoints;
   }
	
   protected void setTranslatedXPoints(int[] translatedXPoints) {
   
   	this.translatedXPoints = translatedXPoints;
   }
	
   protected int[] getTranslatedYPoints() {
   
   	return translatedYPoints;
   }
	
   protected void setTranslatedYPoints(int[] translatedYPoints) {
   
   	this.translatedYPoints = translatedYPoints;
   }
	
   protected double getScaling() {
   
   	return scaling;
   }
	
   protected void setScaling(double scaling) {
   
   	this.scaling = scaling;
   }
	
   protected double getBufferedWidth() {
   
   	return bufferedWidth;
   }
	
   protected void setBufferedWidth(double bufferedWidth) {
   
   	this.bufferedWidth = bufferedWidth;
   }
	
   protected double getBufferedHeight() {
   
   	return bufferedHeight;
   }
	
   protected void setBufferedHeight(double bufferedHeight) {
   
   	this.bufferedHeight = bufferedHeight;
   }
	
   protected Shape getBufferedShape() {
   
   	return bufferedShape;
   }
	
   protected void setBufferedShape(Shape bufferedShape) {
   
   	this.bufferedShape = bufferedShape;
   }

}

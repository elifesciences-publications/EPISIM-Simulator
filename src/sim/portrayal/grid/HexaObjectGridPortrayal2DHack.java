package sim.portrayal.grid;


public class HexaObjectGridPortrayal2DHack extends HexaObjectGridPortrayal2D {
	 public HexaObjectGridPortrayal2DHack(){
		 super();
	 }
	 
	 protected static void getxyCHack(final int x, final int y, final double xScale, final double yScale, final double tx, final double ty, final double[] xyC){
		 HexaObjectGridPortrayal2D.getxyC(x, y, xScale, yScale, tx, ty, xyC);
	 }
	 
    int[] xPoints = new int[6];
    int[] yPoints = new int[6];

    double[] xyC = new double[2];
    double[] xyC_ul = new double[2];
    double[] xyC_up = new double[2];
    double[] xyC_ur = new double[2];
    
    protected int[] getXPoints(){ return xPoints;}
    protected int[] getYPoints(){ return yPoints;}
    
    protected double[] getXyC(){ return xyC; }
    protected double[] getXyC_ul(){ return xyC_ul; }
    protected double[] getXyC_up(){ return xyC_up; }
    protected double[] getXyC_ur(){ return xyC_ur; }
}

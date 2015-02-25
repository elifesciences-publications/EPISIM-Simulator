package sim.app.episim.visualization;

import java.awt.Color;

import sim.util.gui.ColorMap;


public class TissueCrossSectionColorMap implements ColorMap {
	
	private int alpha =255;
	
	public TissueCrossSectionColorMap(int alpha){
		this.alpha = alpha;	
	}
	
	public Color getColor(double level) {		
		return new Color((int) level);		
	}

	
	public int getRGB(double level) {
		Color c = getColor(level);
		return c != null ? c.getRGB() : 0;
	}
	public int getAlpha(double level) {
		return ((int)level)==Color.BLACK.getRGB()? 0 : alpha;
	}

	
	public boolean validLevel(double level) {
		return true;
	}

	
	public double defaultValue() {
		return 0;
	}

}

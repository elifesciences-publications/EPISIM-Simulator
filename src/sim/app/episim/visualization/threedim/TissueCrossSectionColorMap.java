package sim.app.episim.visualization.threedim;

import java.awt.Color;

import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
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
		boolean enableAlpha = true;
		if(MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D){
			enableAlpha = !(((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getDrawOpaqueTissueCrosssection());
		}
		return ((int)level)==Color.BLACK.getRGB() && enableAlpha ? 0 : alpha;
	}
	
	public boolean validLevel(double level) {
		return true;
	}
	
	public double defaultValue() {
		return 0;
	}

}

package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import episiminterfaces.EpisimPortrayal;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.DiffusionColorGradient;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;


public interface ExtraCellularDiffusionPortrayal extends EpisimPortrayal{
	public static final Color[] COLOR_STEPS = new Color[]{new Color(0,0,136), Color.blue, Color.cyan, Color.green, Color.yellow, Color.orange, Color.red, new Color(218,0,0)};


	ExtraCellularDiffusionField getExtraCellularDiffusionField();
	void setExtraCellularDiffusionField(ExtraCellularDiffusionField diffusionField);
}
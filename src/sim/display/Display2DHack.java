package sim.display;

import java.awt.Component;

import sim.app.episim.gui.EpidermisGUIState;
import sim.util.gui.NumberTextField;


public class Display2DHack extends Display2D {
	private EpidermisGUIState epiSimulation = null;
	
	public Display2DHack(double width, double height, GUIState simulation, long interval) {

		super(width, height, simulation, interval);
		
		if(simulation instanceof EpidermisGUIState) epiSimulation = (EpidermisGUIState) simulation;
		
		//no unnecessary Entry: Show Console in the popup
		if(popup != null && popup.getComponentCount()>1){
			popup.remove(0);
			popup.remove(0);
		}
		
		for(Component comp :header.getComponents()){
			if(comp instanceof NumberTextField) header.remove(comp);
		} 
	    // add the scale field
      NumberTextField scaleField = new NumberTextField("  Scale: ", 1.0, true)
          {
          public double newValue(double newValue)
              {
              if (newValue <= 0.0) newValue = currentValue;
              epiSimulation.workaroundConsolePause();
              setScale(newValue);
              port.setView(insideDisplay);
              //optionPane.xOffsetField.add *= (newValue / currentValue);
              optionPane.xOffsetField.setValue(insideDisplay.xOffset * newValue);
              //optionPane.yOffsetField.add *= (newValue / currentValue);
              optionPane.yOffsetField.setValue(insideDisplay.yOffset * newValue);
              epiSimulation.workaroundConsolePlay();
              return newValue;
              }
          };
      scaleField.setToolTipText("Zoom in and out");
      header.add(scaleField);
      
      // add the interval (skip) field
      NumberTextField skipField = new NumberTextField("  Skip: ", 1, false)
          {
          public double newValue(double newValue)
              {
              int val = (int) newValue;
              if (val < 1) val = (int)currentValue;
                      
              // reset with a new interval
              setInterval(val);
              reset();
                      
              return val;
              }
          };
      skipField.setToolTipText("Specify the number of steps between screen updates");
      header.add(skipField);
		
	}
	
	
	
	public void setPortrayalVisible(String name, boolean visible){
		FieldPortrayal2DHolder holder =getPortrayalHolder(name);
		if(holder != null){
			holder.visible = visible;
			insideDisplay.repaint();
		}
		
	}
	
	public boolean isPortrayalVisible(String name){
		FieldPortrayal2DHolder holder =getPortrayalHolder(name);
		if(holder != null) return holder.visible;
		else return false;
	}
	
	private FieldPortrayal2DHolder getPortrayalHolder(String name){
		FieldPortrayal2DHolder holder;
		for(Object obj :portrayals){
			if(obj instanceof FieldPortrayal2DHolder){
				if((holder =(FieldPortrayal2DHolder)obj).name.equals(name)) return holder;
			}
		}
		return null;
	}
	

}

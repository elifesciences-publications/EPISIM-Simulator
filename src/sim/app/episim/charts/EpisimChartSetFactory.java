package sim.app.episim.charts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import episimexceptions.MissingObjectsException;
import episimfactories.AbstractChartSetFactory;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.engine.Steppable;



public class EpisimChartSetFactory extends AbstractChartSetFactory{
		
	
	
	
	
	

	
	public List<ChartPanel> getChartPanels() {

		
		return null;
	}

	public List<Steppable> getSteppablesOfCharts() {

		
		return null;
	}




	
   public void registerNecessaryObjects(Object[] objects) throws MissingObjectsException {

	   // TODO Auto-generated method stub
	   
   }

	

}

package sim.app.episim.charts;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.JPanel;

import episimfactories.AbstractChartSetFactory;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.Names;
import sim.engine.Steppable;



public class EpisimChartSetFactory extends AbstractChartSetFactory{
		
	private EpisimChartSet loadedChartSet;
	
	public EpisimChartSetFactory(){
		try{
			ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(new File(Names.EPISIMCHARTSETFILENAME)));
			Object result = objIn.readObject();
			if(result instanceof EpisimChartSet){
				loadedChartSet = (EpisimChartSet) result;
			}
			
		}
		catch(Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
	}
	
	public EpisimChartSet getEpisimChartSet() {

		
		return loadedChartSet;
	}

	
	public JPanel getPanelForChart(EpisimChart chart) {

		
		return null;
	}

	public Steppable getSteppableForChart(EpisimChart chart) {

		
		return null;
	}

}

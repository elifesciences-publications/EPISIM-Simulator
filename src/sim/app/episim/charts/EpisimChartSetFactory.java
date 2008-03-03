package sim.app.episim.charts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.swing.JPanel;

import episimfactories.AbstractChartSetFactory;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.engine.Steppable;



public class EpisimChartSetFactory extends AbstractChartSetFactory{
		
	private EpisimChartSet loadedChartSet;
	
	public EpisimChartSetFactory(){
		try{
			
		}
		catch(Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
	}
	
	

	
	public JPanel getPanelForChart(EpisimChart chart) {

		
		return null;
	}

	public Steppable getSteppableForChart(EpisimChart chart) {

		
		return null;
	}

	public EpisimChartSet getEpisimChartSet(InputStream stream) {
		try{
			ObjectInputStream objIn =ObjectStreamFactory.getObjectInputStreamForInputStream(stream);
			Object result =objIn.readObject();
			if(result instanceof EpisimChartSet) return (EpisimChartSet) result;
		}
		catch(Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
		return null;
	}

	
	public String getEpisimChartSetBinaryName() {

		
		return Names.EPISIMCHARTSETFILENAME;
	}

}

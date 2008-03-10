package episimfactories;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.engine.Steppable;


public abstract class AbstractChartSetFactory {
	/**
	 * Override this Method if necessary 
	 * 
	 */
	public EpisimChartSet getEpisimChartSet(InputStream stream) throws ModelCompatibilityException{
		
			ObjectInputStream objIn =ObjectStreamFactory.getObjectInputStreamForInputStream(stream);
			Object result = null;
			try{
				result = objIn.readObject();
			}
			catch (IOException e){
				
				if(e instanceof InvalidClassException) throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
				else{
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
			catch (ClassNotFoundException e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
			}
			if(result != null && result instanceof EpisimChartSet) return (EpisimChartSet) result;
		
		
		return null;
	}

	/**
	 * Override this Method if necessary 
	 * 
	 */
	public String getEpisimChartSetBinaryName() {

		
		return Names.EPISIMCHARTSETFILENAME;
	}
	public abstract List<ChartPanel> getChartPanels();
   public abstract List<Steppable> getSteppablesOfCharts();
   
   public abstract void registerNecessaryObjects(Object[] objects) throws MissingObjectsException;
}

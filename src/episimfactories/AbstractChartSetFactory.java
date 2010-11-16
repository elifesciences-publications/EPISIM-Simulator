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
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;


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
				stream.close();
				objIn.close();
				
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

		
		return Names.EPISIM_CHARTSET_FILENAME;
	}
	public abstract List<ChartPanel> getChartPanels();
   public abstract List<EnhancedSteppable> getSteppablesOfCharts();
   
   public abstract void registerNecessaryObjects(GenericBag<AbstractCell> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException;
}

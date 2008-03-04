package episimfactories;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.JPanel;

import episimexceptions.IncompatibleObjectsException;
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

	/**
	 * Override this Method if necessary 
	 * 
	 */
	public String getEpisimChartSetBinaryName() {

		
		return Names.EPISIMCHARTSETFILENAME;
	}
	public abstract List<JPanel> getPanelsOfCharts();
   public abstract List<Steppable> getSteppablesOfCharts();
   
   public abstract void registerNecessaryObjects(Object[] objects) throws IncompatibleObjectsException;
}

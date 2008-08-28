package episimfactories;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.List;

import org.jfree.chart.ChartPanel;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.field.continuous.Continuous2D;
import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;

import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.EpisimDataExportDefinitionSet;
import episiminterfaces.GeneratedDataExport;


public abstract class AbstractDataExportFactory {
	
	/**
	 * Override this Method if necessary 
	 * 
	 */
	public EpisimDataExportDefinitionSet getEpisimDataExportDefinitionSet(InputStream stream) throws ModelCompatibilityException{
		
			ObjectInputStream objIn =ObjectStreamFactory.getObjectInputStreamForInputStream(stream);
			Object result = null;
			try{
				result = objIn.readObject();
				stream.close();
				objIn.close();
				
			}
			catch (IOException e){
				
				if(e instanceof InvalidClassException) throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data-Export-Definiton-Set!");
				else{
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
			catch (ClassNotFoundException e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton-Set!");
			}
			if(result != null && result instanceof EpisimDataExportDefinitionSet) return (EpisimDataExportDefinitionSet) result;
		
		
		return null;
	}

	/**
	 * Override this Method if necessary 
	 * 
	 */
	public String getEpisimDataExportDefinitionSetBinaryName() {
		
		return Names.EPISIMDATAEXPORTFILENAME;
	}
   public abstract List<EnhancedSteppable> getSteppablesOfDataExports();
   
   public abstract List<GeneratedDataExport> getDataExports();
   
   public abstract void registerNecessaryObjects(GenericBag<CellType> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException;
}

package episimfactories;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.List;

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


public abstract class AbstractDataExportFactory {
	
	/**
	 * Override this Method if necessary 
	 * 
	 */
	public EpisimDataExportDefinition getEpisimDataExportDefinition(InputStream stream) throws ModelCompatibilityException{
		
			ObjectInputStream objIn =ObjectStreamFactory.getObjectInputStreamForInputStream(stream);
			Object result = null;
			try{
				result = objIn.readObject();
				stream.close();
				objIn.close();
				
			}
			catch (IOException e){
				
				if(e instanceof InvalidClassException) throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data-Export-Definiton!");
				else{
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
			catch (ClassNotFoundException e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton!");
			}
			if(result != null && result instanceof EpisimDataExportDefinition) return (EpisimDataExportDefinition) result;
		
		
		return null;
	}

	/**
	 * Override this Method if necessary 
	 * 
	 */
	public String getEpisimDataExportDefinitionBinaryName() {
		
		return Names.EPISIMDATAEXPORTFILENAME;
	}
   public abstract EnhancedSteppable getSteppableOfDataExport();
   
   public abstract void registerNecessaryObjects(GenericBag<CellType> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException;
}

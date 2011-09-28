package episimfactories;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.dataexport.EpisimDataExportColumnImpl;
import sim.app.episim.datamonitoring.dataexport.EpisimDataExportImpl;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.field.continuous.Continuous2D;
import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;

import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;
import episiminterfaces.monitoring.GeneratedDataExport;


public abstract class AbstractDataExportFactory {
	

	public static EpisimDataExportDefinitionSet getEpisimDataExportDefinitionSet(InputStream stream) throws ModelCompatibilityException{
		
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
			catch(ClassNotFoundException e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton-Set!");
			}
			catch(NoClassDefFoundError e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton-Set!");
			}
			if(result != null && result instanceof EpisimDataExportDefinitionSet){
				try{
					EpisimDataExportDefinitionSet definitionSet = (EpisimDataExportDefinitionSet) result;
					for(EpisimDataExportDefinition def: definitionSet.getEpisimDataExportDefinitions()){
						addRequiredClassesToDataExportDefinition(def);
					}
					return definitionSet;
				}
				catch(ClassNotFoundException e){
					throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton-Set!");
				}
			}
		
		
		return null;
	}

	private static void addRequiredClassesToDataExportDefinition(EpisimDataExportDefinition definition) throws ClassNotFoundException{
		if(definition instanceof EpisimDataExportImpl){
			EpisimDataExportImpl definitionImpl = (EpisimDataExportImpl) definition;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			for(String actClassName :definitionImpl.getAllRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());
	            if(actClass!= null){
	            	requiredClasses.put(actClassName, actClass);
	            }
	            
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass());
            		definitionImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
            		definitionImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels()[0].getClass());
            		definitionImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes()[0].getClass());
            		definitionImpl.setIsDirty(true);
            	}
            	else{
            		throw e;
            	}
            }
			}
			HashSet<Class<?>> requiredClassesSet = new HashSet<Class<?>>();
			for(EpisimDataExportColumn column : definition.getEpisimDataExportColumns()){
				if(column instanceof EpisimDataExportColumnImpl){
					EpisimDataExportColumnImpl columnImpl = (EpisimDataExportColumnImpl)column;
					requiredClassesSet = new HashSet<Class<?>>();
					for(String actClass : columnImpl.getRequiredClassesNameSet()){
						if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
					}
					columnImpl.setRequiredClasses(requiredClassesSet);
				}
			}
		}
	}
	
	
	public static String getEpisimDataExportDefinitionSetBinaryName() {
		
		return Names.EPISIM_DATAEXPORT_FILENAME;
	}
   public abstract List<EnhancedSteppable> getSteppablesOfDataExports();
   
   public abstract List<GeneratedDataExport> getDataExports();
   
   public abstract void registerNecessaryObjects(GenericBag<AbstractCell> allCells, Object[] objects) throws MissingObjectsException;
}

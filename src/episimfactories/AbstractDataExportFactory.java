package episimfactories;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;



import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.datamonitoring.dataexport.EpisimDataExportColumnImpl;
import sim.app.episim.datamonitoring.dataexport.EpisimDataExportImpl;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectStreamFactory;
import sim.field.continuous.Continuous2D;
import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;
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
				if(e instanceof InvalidClassException) throw new ModelCompatibilityException("Actually Data Export Version is not Compatible with Data-Export-Definiton-Set!");
				else{
					EpisimExceptionHandler.getInstance().displayException(e);
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
					def.setIsDirty(true);
				}
				return definitionSet;
			}
			catch(ClassNotFoundException e){
				throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton-Set!");
			}
		}		
		return null;
	}
	
	public static EpisimDataExportDefinitionSet getEpisimDataExportDefinitionSetBasedOnXML(InputStream stream) throws ModelCompatibilityException{
		
		JAXBContext jc = null;
		Unmarshaller u = null;
		EpisimDataExportDefinitionSet defSet = null;
      try{
	      jc = JAXBContext.newInstance(sim.app.episim.datamonitoring.dataexport.EpisimDataExportDefinitionSetImpl.class);
	      u = jc.createUnmarshaller();
	      Object o = u.unmarshal(stream);
	      if(o instanceof EpisimDataExportDefinitionSet){
	      	defSet = (EpisimDataExportDefinitionSet) o;
	      }
      }
      catch (JAXBException e){
	      EpisimExceptionHandler.getInstance().displayException(e);
      }
      if(defSet != null){
      	for(EpisimDataExportDefinition export : defSet.getEpisimDataExportDefinitions()){ 
				try{
	            checkDataExportDirtyStatus(export);
            }
            catch (ClassNotFoundException e){
            	throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Data Export-Definiton-Set!");
            }
			}
      }
		return defSet;		
		
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
	private static void checkDataExportDirtyStatus(EpisimDataExportDefinition definition) throws ClassNotFoundException{
		if(definition instanceof EpisimDataExportImpl){
			EpisimDataExportImpl definitionImpl = (EpisimDataExportImpl) definition;
			for(String actClassName :definitionImpl.getAllRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());	            
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		definitionImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
            		definitionImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
            		definitionImpl.setIsDirty(true);
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
            		definitionImpl.setIsDirty(true);
            	}
            	else{
            		throw e;
            	}
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

package sim.app.episim.datamonitoring.dataexport.io.xml;

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import sim.app.episim.datamonitoring.dataexport.EpisimDataExportColumnImpl;
import sim.app.episim.datamonitoring.dataexport.io.EDEFileReader;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.GlobalClassLoader;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimDataExportColumn;

public class EpisimDataExportColumnAdapter extends XmlAdapter<AdaptedEpisimDataExportColumn, EpisimDataExportColumn> implements java.io.Serializable{

	public EpisimDataExportColumn unmarshal(AdaptedEpisimDataExportColumn v) throws Exception {
		EpisimDataExportColumnImpl column = new EpisimDataExportColumnImpl(v.getId());
	   column.setCalculationAlgorithmConfigurator(v.getCalculationAlgorithmConfigurator());
	   column.setName(v.getName());
	   try{
			addRequiredClassesToDataExportColumn(v, column);
		}
		catch(ClassNotFoundException e){
			throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
		}
	   return column;
   }
	
	private void addRequiredClassesToDataExportColumn(AdaptedEpisimDataExportColumn adaptedColumn, EpisimDataExportColumn column) throws ClassNotFoundException{
		if(column instanceof EpisimDataExportColumnImpl){
			EpisimDataExportColumnImpl columnImpl = (EpisimDataExportColumnImpl) column;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			for(String actClassName :adaptedColumn.getRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());
	            if(actClass!= null){
	            	requiredClasses.put(actClassName, actClass);
	            }
	            
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass());
            		EDEFileReader.foundDirtyDataExportColumnDuringImport = true;
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
            		EDEFileReader.foundDirtyDataExportColumnDuringImport = true;
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels()[0].getClass());
            		EDEFileReader.foundDirtyDataExportColumnDuringImport = true;
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes()[0].getClass());
            		EDEFileReader.foundDirtyDataExportColumnDuringImport = true;
            	}
            	else{
            		throw e;
            	}
            }
			}
			HashSet<Class<?>> requiredClassesSet = new HashSet<Class<?>>();
			for(String actClass : adaptedColumn.getRequiredClassesNameSet()){
				if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
			}
			columnImpl.setRequiredClasses(requiredClassesSet);
		}
	}
	
	
   public AdaptedEpisimDataExportColumn marshal(EpisimDataExportColumn v) throws Exception {
   	
   	AdaptedEpisimDataExportColumn column = new AdaptedEpisimDataExportColumn();
   	column.setCalculationAlgorithmConfigurator(v.getCalculationAlgorithmConfigurator());
   	column.setId(v.getId());
   	column.setName(v.getName());
   	column.setRequiredClassesNameSet(((EpisimDataExportColumnImpl)v).getRequiredClassesNameSet());
   	
	   return column;
   }

}

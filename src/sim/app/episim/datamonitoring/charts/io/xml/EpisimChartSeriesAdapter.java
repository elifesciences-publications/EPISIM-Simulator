package sim.app.episim.datamonitoring.charts.io.xml;

import java.util.HashMap;
import java.util.HashSet;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import sim.app.episim.datamonitoring.charts.EpisimChartSeriesImpl;
import sim.app.episim.datamonitoring.charts.io.ECSFileReader;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.GlobalClassLoader;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimChartSeries;


public class EpisimChartSeriesAdapter extends XmlAdapter<AdaptedEpisimChartSeries, EpisimChartSeries> implements java.io.Serializable{

	public EpisimChartSeries unmarshal(AdaptedEpisimChartSeries v) throws Exception {

		EpisimChartSeriesImpl series = new EpisimChartSeriesImpl(v.getId());
		series.setCalculationAlgorithmConfigurator(v.getCalculationAlgorithmConfigurator());
		series.setColor(v.getColor());
		series.setDash(v.getDash());
		series.setName(v.getName());
		series.setStretch(v.getStretch());
		
		try{
			addRequiredClassesToChartSeries(v, series);
		}
		catch(ClassNotFoundException e){
			throw new ModelCompatibilityException("Actually Loaded Model is not Compatible with Chart-Set!");
		}
		series.setThickness(v.getThickness());		
		
	   return series;
   }
	
	private void addRequiredClassesToChartSeries(AdaptedEpisimChartSeries adaptedChartSeries, EpisimChartSeries chartSeries) throws ClassNotFoundException{
		
		if(chartSeries instanceof EpisimChartSeriesImpl){
			EpisimChartSeriesImpl chartSeriesImpl = (EpisimChartSeriesImpl) chartSeries;
			HashMap<String, Class<?>> requiredClasses = new HashMap<String, Class<?>>();
			for(String actClassName :adaptedChartSeries.getRequiredClassesNameSet()){
				try{
	            Class<?> actClass = Class.forName(actClassName, true, GlobalClassLoader.getInstance());
	            if(actClass!= null){
	            	requiredClasses.put(actClassName, actClass);
	            }
	            
            }
            catch (ClassNotFoundException e){
            	if(actClassName.contains(".Cell_")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getNewEpisimCellBehavioralModelObject().getClass());
            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
            	}
            	else if(actClassName.contains(".Parameters_") && !(actClassName.endsWith("DiffLevel") || actClassName.endsWith("CellType"))){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass());
            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("DiffLevel")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels()[0].getClass());
            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
            	}
            	else if(actClassName.contains(".Parameters_") && actClassName.endsWith("CellType")){
            		requiredClasses.put(actClassName, ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes()[0].getClass());
            		ECSFileReader.foundDirtyChartSeriesDuringImport = true;
            	}
            	else{
            		throw e;
            	}
            }
			}
			HashSet<Class<?>> requiredClassesSet = new HashSet<Class<?>>();
			for(String actClass: adaptedChartSeries.getRequiredClassesNameSet()){
				if(requiredClasses.containsKey(actClass))requiredClassesSet.add(requiredClasses.get(actClass));
			}
			chartSeriesImpl.setRequiredClasses(requiredClassesSet);			
		}
	}	

   public AdaptedEpisimChartSeries marshal(EpisimChartSeries v) throws Exception {
   	
   	AdaptedEpisimChartSeries series = new AdaptedEpisimChartSeries();
   	series.setId(v.getId());
   	series.setCalculationAlgorithmConfigurator(v.getCalculationAlgorithmConfigurator());
   	series.setColor(v.getColor());
   	series.setDash(v.getDash());
   	series.setName(v.getName());
   	series.setRequiredClassesNameSet(((EpisimChartSeriesImpl)v).getRequiredClassesNameSet());
	   series.setStretch(v.getStretch());
	   series.setThickness(v.getThickness());
   	
	   return series;
   }

}

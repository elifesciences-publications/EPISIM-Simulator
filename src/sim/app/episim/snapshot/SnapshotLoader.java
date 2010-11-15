package sim.app.episim.snapshot;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.AbstractCellType;

import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.Epidermis;
import sim.engine.SimStateHack.TimeSteps;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;


public class SnapshotLoader {
	
	private List<Double2D> woundRegionCoordinates = null;
	private List<AbstractCellType> loadedCells;
	
	private java.awt.geom.Rectangle2D.Double[] deltaInfo = null;
	private EpisimCellBehavioralModelGlobalParameters behavioralModelGlobalParameters = null;
	private EpisimMechanicalModelGlobalParameters mechModelGlobalParameters = null;
	
	private TimeSteps timeSteps;
	private Continuous2D cellContinuous= null;
	
	public SnapshotLoader(File snapshotFile, File jarFile) throws IllegalArgumentException{
		
		if(snapshotFile != null && jarFile != null){
			
			List<SnapshotObject> snapshotobjects = SnapshotReader.getInstance().loadSnapshot(snapshotFile, jarFile);
			loadedCells = new LinkedList<AbstractCellType>();
			for(SnapshotObject sObj : snapshotobjects){
				if(sObj.getIdentifier().equals(SnapshotObject.CELL)){
					loadedCells.add((AbstractCellType) sObj.getSnapshotObject());
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.CELLCONTINUOUS)){
					this.cellContinuous = (Continuous2D) sObj.getSnapshotObject();
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.TIMESTEPS)){
					this.timeSteps = (TimeSteps) sObj.getSnapshotObject();
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.CELLBEHAVIORALMODELGLOBALPARAMETERS)){
					behavioralModelGlobalParameters = (EpisimCellBehavioralModelGlobalParameters) sObj.getSnapshotObject();
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.MECHANICALMODELGLOBALPARAMETERS)){
					mechModelGlobalParameters = (EpisimMechanicalModelGlobalParameters) sObj.getSnapshotObject();
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.MISCALLENEOUSGLOBALPARAMETERS)){
					MiscalleneousGlobalParameters.instance().reloadMiscalleneousGlobalParametersObject((MiscalleneousGlobalParameters) sObj.getSnapshotObject());
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.WOUND)){
					Object obj= null;
					if((obj=sObj.getSnapshotObject())instanceof List)
					                        woundRegionCoordinates = (List<Double2D>) obj;
					else deltaInfo = (java.awt.geom.Rectangle2D.Double[])sObj.getSnapshotObject();
					
				}
				
			}
		
			SnapshotWriter.getInstance().addSnapshotListener(MiscalleneousGlobalParameters.instance());
	}
		else throw new IllegalArgumentException("Snapshot-Path and/or Model-File-Path is null");	
}


	
   public List<Double2D> getWoundRegionCoordinates() {
   
   	return woundRegionCoordinates;
   }


	
   public List<AbstractCellType> getLoadedCells() {
   
   	return loadedCells;
   }


	
   
	
   public java.awt.geom.Rectangle2D.Double[] getDeltaInfo() {
   
   	return deltaInfo;
   }



	
   public EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters() {
   
   	return behavioralModelGlobalParameters;
   }
   public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
      
   	return mechModelGlobalParameters;
   }
   
   public Continuous2D getCellContinous2D(){
   	return this.cellContinuous;
   }
   public TimeSteps getTimeSteps(){
   	return this.timeSteps;
   }
}
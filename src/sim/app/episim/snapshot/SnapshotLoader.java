package sim.app.episim.snapshot;

import java.io.File;
import java.util.List;

import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.Epidermis;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.model.ModelController;
import sim.util.Double2D;


public class SnapshotLoader {
	
	private List<Double2D> woundRegionCoordinates = null;
	private Epidermis epidermis = null;
	private DefaultCharts charts = null;
	private java.awt.geom.Rectangle2D.Double[] deltaInfo = null;
	private EpisimCellDiffModelGlobalParameters diffModelGlobalParameters = null;
	private EpisimMechanicalModelGlobalParameters mechModelGlobalParameters = null;
	
	public SnapshotLoader(File snapshotFile, File jarFile) throws IllegalArgumentException{
		
		if(snapshotFile != null && jarFile != null){
			
			List<SnapshotObject> snapshotobjects = SnapshotReader.getInstance().loadSnapshot(snapshotFile, jarFile);
			for(SnapshotObject sObj : snapshotobjects){
				if(sObj.getIdentifier().equals(SnapshotObject.EPIDERMIS)){
					epidermis = (Epidermis) sObj.getSnapshotObject();
					epidermis.setReloadedSnapshot(true);
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.CHARTS)){
					charts = (DefaultCharts) sObj.getSnapshotObject();
				
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.CELLDIFFMODELGLOBALPARAMETERS)){
					diffModelGlobalParameters = (EpisimCellDiffModelGlobalParameters) sObj.getSnapshotObject();
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.MECHANICALMODELGLOBALPARAMETERS)){
					mechModelGlobalParameters = (EpisimMechanicalModelGlobalParameters) sObj.getSnapshotObject();
					
				}
				else if(sObj.getIdentifier().equals(SnapshotObject.WOUND)){
					Object obj= null;
					if((obj=sObj.getSnapshotObject())instanceof List)
					                        woundRegionCoordinates = (List<Double2D>) obj;
					else deltaInfo = (java.awt.geom.Rectangle2D.Double[])sObj.getSnapshotObject();
					
				}
				
			}
			if(charts != null) SnapshotWriter.getInstance().addSnapshotListener(charts);
			if(epidermis != null) SnapshotWriter.getInstance().addSnapshotListener(epidermis);
	}
		else throw new IllegalArgumentException("Snapshot-Path and/or Model-File-Path is null");	
}


	
   public List<Double2D> getWoundRegionCoordinates() {
   
   	return woundRegionCoordinates;
   }


	
   public Epidermis getEpidermis() {
   
   	return epidermis;
   }


	
   public DefaultCharts getCharts() {
   
   	return charts;
   }


	
   public java.awt.geom.Rectangle2D.Double[] getDeltaInfo() {
   
   	return deltaInfo;
   }



	
   public EpisimCellDiffModelGlobalParameters getEpisimCellDiffModelGlobalParameters() {
   
   	return diffModelGlobalParameters;
   }
   public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
      
   	return mechModelGlobalParameters;
   }
}
package sim.app.episim.model.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ObjectManipulations;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class MiscalleneousGlobalParameters implements java.io.Serializable, ClassLoaderChangeListener{
	
	
	
	private static MiscalleneousGlobalParameters instance;
	
	private static MiscalleneousGlobalParameters resetinstance;
	
	private int diffusionFieldOpacity=255;
	private boolean showDiffusionFieldLegend = true;
	
	private boolean highlightTrackedCells = true;
	
	private double coloringThreshold=0.0;
	private static Semaphore sem = new Semaphore(1);
	private MiscalleneousGlobalParameters(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	
	public boolean getHighlightTrackedCells(){ return this.highlightTrackedCells;}
	
	public void setHighlightTrackedCells(boolean val){ this.highlightTrackedCells = val; }	
	
	@NoUserModification
	public static MiscalleneousGlobalParameters getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
					instance = new MiscalleneousGlobalParameters();
					resetinstance = new MiscalleneousGlobalParameters();
				}
				else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
					instance = new MiscalleneousGlobalParameters3D();
					resetinstance = new MiscalleneousGlobalParameters3D();
				}				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		
		return instance;
	}
	
	public void reloadMiscalleneousGlobalParametersObject(MiscalleneousGlobalParameters parametersObject){
		if(parametersObject != null){
			resetinstance = parametersObject;
			ObjectManipulations.resetInitialGlobalValues(instance, resetinstance);
		}
	}
	
	public void resetInitialGlobalValues(){
		ObjectManipulations.resetInitialGlobalValues(instance, resetinstance);
	}
	
	
	
   public boolean getShowDiffusionFieldLegend() {
	   return showDiffusionFieldLegend;
   }
   
   public void setShowDiffusionFieldLegend(boolean showDiffusionFieldLegend) {
	   this.showDiffusionFieldLegend = showDiffusionFieldLegend;
   }
	
   public int getDiffusionFieldOpacity() {
	   return diffusionFieldOpacity;
   }   
   
   public void setDiffusionFieldOpacity(int diffusionFieldOpacity) {
   	if(diffusionFieldOpacity > 255) diffusionFieldOpacity =255;
   	if(diffusionFieldOpacity < 0) diffusionFieldOpacity =0;
   	this.diffusionFieldOpacity = diffusionFieldOpacity;
   }
   
   public double getDiffusionFieldColoringMinThreshold(){ return this.coloringThreshold; }
   
   public void setDiffusionFieldColoringMinThreshold(double coloringThreshold) {
   	if(coloringThreshold > 1) coloringThreshold =1;
   	if(coloringThreshold < 0) coloringThreshold =0;
   	this.coloringThreshold = coloringThreshold;
   }
   
   public static class MiscalleneousGlobalParameters3D extends MiscalleneousGlobalParameters{
   	
   	
   	private boolean standardMembrane_2_Dim_Gauss = false;
   	private boolean optimizedGraphics = false;
   	private MiscalleneousGlobalParameters3D(){}
   	
   	public boolean getStandardMembrane_2_Dim_Gauss(){
      
      	return standardMembrane_2_Dim_Gauss;
      }
		
      public void setStandardMembrane_2_Dim_Gauss(boolean standardMembrane_2_Dim_Gauss) {      
      	this.standardMembrane_2_Dim_Gauss = standardMembrane_2_Dim_Gauss;
      }  
      
   	@NoUserModification
      public boolean getOptimizedGraphics(){ return optimizedGraphics; }
   	@NoUserModification
      public void setOptimizedGraphics(boolean val){ this.optimizedGraphics = val; }
   }
	
   public void classLoaderHasChanged() {
	   instance = null;
   }
   
   
}

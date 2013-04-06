package sim.app.episim.model.misc;

import java.util.ArrayList;
import java.util.List;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
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
	
	private int typeColor = 1;
	
	private double coloringThreshold=0.0;
	
	public String[] typeString = { "Unused", "Color by cell type", "Cell type and outer cells", "Color by age", "Episim-Modeller Custom Coloring"};
	
	
	private MiscalleneousGlobalParameters(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	
	public boolean getHighlightTrackedCells(){ return this.highlightTrackedCells;}
	
	public void setHighlightTrackedCells(boolean val){ this.highlightTrackedCells = val; }
   
	
	
	public String getTypeColorName(){
		if((typeColor < 1) || (typeColor >= typeString.length))
			typeColor = 1;
		return typeString[typeColor];
	}
	@NoUserModification
	public static MiscalleneousGlobalParameters getInstance(){
		if(instance == null){
			if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
				instance = new MiscalleneousGlobalParameters();
				resetinstance = new MiscalleneousGlobalParameters();
			}
			else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
				instance = new MiscalleneousGlobalParameters3D();
				resetinstance = new MiscalleneousGlobalParameters3D();
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
	
	public int getTypeColor() {

		return typeColor;
	}

	public void setTypeColor(int val) {

		if((val < 1) || (val >= typeString.length)) this.typeColor = 1;
		else this.typeColor = val;
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
   	
   	private MiscalleneousGlobalParameters3D(){}
   	
   	public boolean getStandardMembrane_2_Dim_Gauss(){
      
      	return standardMembrane_2_Dim_Gauss;
      }
		
      public void setStandardMembrane_2_Dim_Gauss(boolean standardMembrane_2_Dim_Gauss) {      
      	this.standardMembrane_2_Dim_Gauss = standardMembrane_2_Dim_Gauss;
      }   	
   }
	
   public void classLoaderHasChanged() {
	   instance = null;
   }
   
   
}

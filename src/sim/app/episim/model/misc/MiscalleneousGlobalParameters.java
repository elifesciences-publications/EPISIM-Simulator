package sim.app.episim.model.misc;

import java.util.ArrayList;
import java.util.List;

import sim.app.episim.util.ObjectManipulations;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class MiscalleneousGlobalParameters implements java.io.Serializable{
	private static MiscalleneousGlobalParameters instance;
	
	private static MiscalleneousGlobalParameters resetinstance;
	
	private int diffusionFieldOpacity=255;
	private boolean showDiffusionFieldLegend = true;
	
	
	
	private int typeColor = 1;
	
	public String[] typeString = { "Unused", "Color by cell type", "Cell type and outer cells", "Color by age", "Episim-Modeller Custom Coloring"};
	
	private MiscalleneousGlobalParameters(){
		
	}
	
	
	public String getTypeColorName(){
		if((typeColor < 1) || (typeColor >= typeString.length))
			typeColor = 1;
		return typeString[typeColor];
	}
	
	public static synchronized MiscalleneousGlobalParameters instance(){
		if(instance == null){ 
			instance = new MiscalleneousGlobalParameters();
			resetinstance = new MiscalleneousGlobalParameters();
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
	
   public boolean isShowDiffusionFieldLegend() {
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
}

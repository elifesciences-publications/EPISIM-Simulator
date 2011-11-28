package sim.app.episim.model.misc;

import java.util.ArrayList;
import java.util.List;

import sim.app.episim.util.ObjectManipulations;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class MiscalleneousGlobalParameters implements java.io.Serializable{
	private static MiscalleneousGlobalParameters instance;
	
	private static MiscalleneousGlobalParameters resetinstance;
	
	private int typeColor = 1;
	
	public String[] typeString = { "Unused", "Color by cell type", "Cell type and outer cells", "Color by age",
	      "Color by calcium", "Color by lamella", "Enough lipids for barrier", "Episim-Modeller Custom Coloring", "Ellipse Morphology(center based model only)"};
	
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
	
}

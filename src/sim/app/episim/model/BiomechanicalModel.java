package sim.app.episim.model;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.Epidermis;
import sim.app.episim.KCyte;




public class BiomechanicalModel implements java.io.Serializable{
	
	

	
	
	private MechanicalModelGlobalParameters actParametersObject;
	private MechanicalModelGlobalParameters resetParametersObject;
	
	
	
	public BiomechanicalModel(){
		
		actParametersObject = new MechanicalModelGlobalParameters();
		resetParametersObject = new MechanicalModelGlobalParameters();
	}
	
	
	
	
	
	
	public EpisimMechanicalModel getEpisimMechanicalModel() {
		return null;
	}
	
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
		return actParametersObject;
	}
	
	
   
   
}
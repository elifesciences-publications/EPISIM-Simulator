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
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;




public class BiomechanicalModel implements java.io.Serializable, SnapshotListener{
	
	

	
	
	private MechanicalModelGlobalParameters actParametersObject;
	private EpisimMechanicalModelGlobalParameters resetParametersObject;
	
	
	
	public BiomechanicalModel(){
		
		actParametersObject = new MechanicalModelGlobalParameters();
		resetParametersObject = new MechanicalModelGlobalParameters();
		SnapshotWriter.getInstance().addSnapshotListener(this);
		
	}
	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimMechanicalModelGlobalParameters parametersObject){
		this.resetParametersObject = parametersObject;
		resetInitialGlobalValues();
	}
	
	
	
	public EpisimMechanicalModel getEpisimMechanicalModel() {
		return null;
	}
	
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
		return actParametersObject;
	}
	
	public void resetInitialGlobalValues(){
	   try{	
	   	List<Method> getMethods = getMethods("get", this.resetParametersObject);
	   	List<Method> setMethods = getMethods("set", this.actParametersObject);
	   	
	   	Iterator<Method> iterSet = setMethods.iterator();
	   	Method setM = null;
	   	Method getM = null;
	   	while(iterSet.hasNext()){
	   		setM = iterSet.next();
	   		Iterator<Method> iterGet = getMethods.iterator();
	      	while(iterGet.hasNext()){
	      		getM = iterGet.next();
	      		if(setM.getName().endsWith(getM.getName().substring(3))){
	      			setM.invoke(this.actParametersObject, new Object[]{(getM.invoke(this.resetParametersObject, (new Object[]{})))});
	      		}
	      	}
	   	}
	   	}
	   	catch(Exception e){
	   		e.printStackTrace();
	   	}
	}

	private List<Method> getMethods(String prefix, Object object) throws SecurityException, NoSuchMethodException{
		  List<Method> methods = new ArrayList<Method>();
	   	
	   	for(Method m :object.getClass().getMethods()){
	   		if(m.getName().startsWith(prefix)) methods.add(m);
	   	}
	   	
	   	return methods;
	   
	}


	public List<SnapshotObject> getSnapshotObjects() {
		List<SnapshotObject> list = new ArrayList<SnapshotObject>();
		list.add(new SnapshotObject(SnapshotObject.MECHANICALMODELGLOBALPARAMETERS, this.actParametersObject));
		return list;
	}
   
   
}
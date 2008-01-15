package sim.app.episim.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.KCyte;


public class BioChemicalModelController implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2406025736169916469L;
	private static BioChemicalModelController instance;
	private BiochemicalModel biochemicalModel;
	
	private ConcurrentHashMap<String, Object> cache;
	private boolean caching = true;
	
	private double[][] adhesionValues = null;
	private double[][] resetAdhesionValues = null;
	private ConcurrentHashMap<String, Object> resetCache;
	private BioChemicalModelController(){
		cache = new ConcurrentHashMap<String, Object>();
		
	}
	public synchronized static BioChemicalModelController getInstance(){
		if(instance == null) instance = new BioChemicalModelController();
		return instance;
	}
	public int getGlobalIntConstant(String name){
		try{
			
			return ((Integer)cache.get(name)).intValue();
			
		}
			
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		return -1;
	}
	
	
	
	public boolean loadModelFile(File modelFile){
		try{
			JarClassLoader jarLoader = new JarClassLoader(modelFile.toURI().toURL());
			biochemicalModel = new BiochemicalModel(jarLoader.getMainModelClass());
			cache.clear();
			loadCache(cache);
			return true;
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return false;
		}
		
	}
	
	public EpisimStateModel getNewEpisimStateModelObject(){
		
			try{
				////////////////////////////////////////////////////////
				//TODO: Anpassen
				//////////////////////////////////////////////////////////
				return biochemicalModel.getEpisimStateModel();
			}
			catch (Exception e){
				
				ExceptionDisplayer.getInstance().displayException(e);
				return null;
			}
		
		
	}
	
	public EpisimStateModelGlobalParameters getEpisimStateModelGlobalParameters(){
		
		try{
			return biochemicalModel.getEpisimStateModelGlobalParameters();
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	
	
}
	
	public void resetInitialGlobalValues(){
		try{
			biochemicalModel.resetInitialGlobalValues();
			cache.clear();
			loadCache(cache);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
	}
	
	public int getIntField(String name){
	
		
		try{
			
			return ((Integer)cache.get(name)).intValue();
   			
   			
   		
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return -1;
		}
		
	}
	
   public double getDoubleField(String name){
   	
   	try{
   		return((Double)cache.get(name)).doubleValue();
   			
   			
   		
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return -1;
		}
	}
   public float getFloatField(String name){
   	
		
   	try{
   		return ((Float)cache.get(name)).floatValue();
   			
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return -1;
		}
	}
   
   public boolean getBooleanField(String name){
   	
   	try{
   		return ((Boolean)cache.get(name)).booleanValue();
   			
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return false;
		}
   }
   
   public void setIntField(String name, int value){
   	name = name.trim().toLowerCase();
   	try{
		   biochemicalModel.setIntField(name, value);
		  cache.put(name.trim(), value);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			
		}
		
	}
	
   public void setDoubleField(String name, double value){
   	try{
		   biochemicalModel.setDoubleField(name, value);
		   cache.put(name.trim(), value);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			
		}
		
	}
   public void setFloatField(String name, float value){
   	try{
		   biochemicalModel.setFloatField(name, value);
		   cache.put(name.trim(), value);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			
		}
		
		
	}
   
   public void setBooleanField(String name, boolean value){
   	try{
		   biochemicalModel.setBooleanField(name, value);
		   cache.put(name.trim(), value);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			
		}
   	
   }
   
   public double get2DDoubleArrayValue(String name, int pos1, int pos2){
   	try{
   		
			return adhesionValues[pos1][pos2];
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
			return -1;
		}
		
   }
  /* public void set2DDoubleArrayValue(String name, int pos1, int pos2, double val){
   	try{
			 biochemicalModel.set2DDoubleArrayValue(name, pos1, pos2, val);
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
			
		}
		
   }*/
   
   public void initModel(){
   	try{
			biochemicalModel.initModel();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			
		}
		
   }
   
   public void reloadValue(String name){
   	try{
   	if(name.startsWith("Ad_")){
   		
   		adhesionValues = biochemicalModel.getAdhesionArray();
   	}
   	else{
   	String name2 = name.trim().substring(0, 1).toLowerCase()+ name.trim().substring(1, name.length());
   	if(cache.containsKey(name2)){
   	
   		
   		
   		
   		Object obj = cache.get(name2);
   		if(obj !=null){
   			
   			if(obj instanceof Integer){
   				
   				
   				cache.put(name2, biochemicalModel.getIntField(name));
   			}
   			else if(obj instanceof Double){
   				
   				
   				cache.put(name2, biochemicalModel.getDoubleField(name));
   			}
   			else if(obj instanceof Float){
   				cache.put(name2, biochemicalModel.getFloatField(name));
   			}
   			else if(obj instanceof Boolean){
   				cache.put(name2, biochemicalModel.getBooleanField(name));
   			}
   			}
   	}
   	}
   	}
   			catch(Exception e){
   				ExceptionDisplayer.getInstance().displayException(e);
   			}
   	
   	
   	
   }
	
	public void differentiate(KCyte  kCyte, Epidermis theEpidermis, boolean pBarrierMember){
		try{
			biochemicalModel.differentiate(kCyte, theEpidermis, pBarrierMember);
		}
		catch(Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
	}
	
	public void loadCache(Map<String, Object> cache){
		try{
			biochemicalModel.loadCacheValues(cache);
			
			adhesionValues = biochemicalModel.getAdhesionArray();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
	}
	
	
	
}

package sim.app.episim.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimSimulator;


public class GlobalClassLoader extends URLClassLoader{
	
	public static final String IGNOREDATAEXPORTMODE = "ignoredataexportmode";
	public static final String IGNORECHARTSETMODE = "ignorechartsetmode";
	public static final String NOMODE = "nomode";
	
	private static GlobalClassLoader instance;
	
	private String mode = NOMODE;
	
	private Set<String> urlRegistry;
	
	boolean destroyed = false;
	
	public Set<ClassLoaderChangeListener> changeListener;
	
	private int orderCounter = 1;
	private HashSet<String> urlsToBeDeleted = new HashSet<String>();
	
	private GlobalClassLoader() {

	  super(new URL[]{});
	   	 urlRegistry = new HashSet<String>(); 
	   	 changeListener = new HashSet<ClassLoaderChangeListener>();
   }
	
	public static GlobalClassLoader getInstance(){
		if(instance == null) instance = new GlobalClassLoader();
		
		return instance;
		
	}
	
	private void refresh(){
		Set<ClassLoaderChangeListener> setCopy = this.changeListener;
		Set<String> registryCopy = this.urlRegistry;
		URL[] urls = this.getURLs();
		
		HashSet<String> deleteUrlSet = this.urlsToBeDeleted;
		
		String modeCopy = this.mode;
		
		destroyClassLoader(false);
		instance = new GlobalClassLoader();
		for(ClassLoaderChangeListener listener : setCopy){
			instance.addClassLoaderChangeListener(listener);
		}
		for(URL url: urls){
			if(!deleteUrlSet.contains(url.getPath())){
				instance.addURL(url);
			}
		}
		
		instance.mode =modeCopy;
		
		instance.notifyAllListeners();
		
		for(String str : registryCopy){
		//	System.out.println(str);
			if(!deleteUrlSet.contains(str)){
				instance.urlRegistry.add(str);
			}
		}
		
		
	}
	
	public void destroyClassLoader(boolean notifyListeners){
		destroyed = true;
		
		if(notifyListeners){
			instance.notifyAllListeners();
		}
		instance=null;
	}
	
	public void registerURL(URL url){
		if(urlRegistry.contains(url.getPath()) || containsFormerDataExportOrChartSet(url)){
			if(!urlRegistry.contains(url.getPath())){
				urlRegistry.add(url.getPath());
				super.addURL(url);
			}
			if(url.getPath().endsWith(Names.DATAEXPORT_FILETYPE)){
				
				if(ChartController.getInstance().isAlreadyChartSetLoaded() && mode.equals(NOMODE)){
					this.mode = IGNORECHARTSETMODE;
					refresh();
				}
				else if(!mode.equals(IGNOREDATAEXPORTMODE)){
					refresh();
					
				}
				
			}
			else if(url.getPath().endsWith(Names.CHARTSET_FILETYPE)){
				if(DataExportController.getInstance().isAlreadyDataExportSetLoaded() && mode.equals(NOMODE)){
					this.mode = IGNOREDATAEXPORTMODE;
					refresh();
				}
				else if(!mode.equals(IGNORECHARTSETMODE)){
					refresh();
				}
				
			}
			else if(url.getPath().endsWith(Names.MODEL_FILETYPE)){
				//System.out.println( (orderCounter++) + ".) Neues Modell wird geladen ");
			}
			
			
		}
		else{
			urlRegistry.add(url.getPath());
			super.addURL(url);
		}
	}
	private boolean containsFormerDataExportOrChartSet(URL url){
		URL[] registeredUrls = this.getURLs();
		this.urlsToBeDeleted = new HashSet<String>();
		boolean result = false;
		if(registeredUrls != null){
			if(url.getPath().endsWith(Names.DATAEXPORT_FILETYPE)){
				for(URL actUrl : registeredUrls){
					if(actUrl.getPath().endsWith(Names.DATAEXPORT_FILETYPE)){
						this.urlsToBeDeleted.add(actUrl.getPath());
						result =  true;
					}
				}
			}
			else if(url.getPath().endsWith(Names.CHARTSET_FILETYPE)){
				for(URL actUrl : registeredUrls){
					if(actUrl.getPath().endsWith(Names.CHARTSET_FILETYPE)){
						this.urlsToBeDeleted.add(actUrl.getPath());
						result = true;
					}
				}
			}
		}
		return result;
	}
	
	
	public void addClassLoaderChangeListener(ClassLoaderChangeListener listener){
		cleanListeners(listener.getClass().getName());
	   this.changeListener.add(listener);
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if(destroyed) throw new ClassNotFoundException("GlobalClassLoader: ClassLoader was destroyed. Please reload Global ClassLoader!");
		return super.loadClass(name);
	}
	
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(destroyed) throw new ClassNotFoundException("GlobalClassLoader: ClassLoader was destroyed. Please reload Global ClassLoader!");
		return super.loadClass(name, resolve);
	}
	
	
	public void notifyAllListeners(){
	   Set<ClassLoaderChangeListener> listenerCopy = new HashSet<ClassLoaderChangeListener>();
	   listenerCopy.addAll(this.changeListener);
	   EpisimSimulator simulator = null;
		for(ClassLoaderChangeListener listener: listenerCopy){
			if(listener instanceof EpisimSimulator) simulator = (EpisimSimulator)listener;
			else listener.classLoaderHasChanged();
		}
		if(simulator != null) simulator.classLoaderHasChanged();
	}
	
	private void cleanListeners(String className){
	
		for(ClassLoaderChangeListener actListener: changeListener){
			if(actListener.getClass().getName().equals(className)){
				
				changeListener.remove(actListener);
				return;
			}
		}
		
	}
	public void resetMode(){
		mode = NOMODE;
	}
	
	public String getMode(){
		return mode;
	}
}

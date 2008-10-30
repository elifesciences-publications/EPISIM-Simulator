package sim.app.episim.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;


public class GlobalClassLoader extends URLClassLoader{
	
	private static GlobalClassLoader instance;
	
	private Set<String> urlRegistry;
	
	boolean destroyed = false;
	
	public Set<ClassLoaderChangeListener> changeListener;
	
	private int orderCounter = 1;
	
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
		destroyClassLoader();
		instance = new GlobalClassLoader();
		for(ClassLoaderChangeListener listener : setCopy){
			instance.addClassLoaderChangeListener(listener);
		}
		for(URL url: urls) instance.addURL(url);
		
		instance.notifyAllListeners();
		for(String str : registryCopy){
			System.out.println(str);
			instance.urlRegistry.add(str);	
		}
		
		
	}
	
	public void destroyClassLoader(){
		destroyed = true;
		instance=null;
	}
	
	public void registerURL(URL url){
		if(urlRegistry.contains(url.getPath())){
			
			if(url.getPath().endsWith(Names.DATAEXPORTFILETYPE)){
				
				if(ChartController.getInstance().isAlreadyChartSetLoaded()){
					ChartController.getInstance();
				}
				else{
					System.out.println( (orderCounter++) + ".) Neuer Dataexport wird geladen ");
				}
				
			}
			else if(url.getPath().endsWith(Names.CHARTSETFILETYPE)){
				if(ChartController.getInstance().isAlreadyChartSetLoaded()){
					System.out.println("Entering Ingore-Refresh-Caused-by-DataExport-Mode");
				}
				else{
					System.out.println( (orderCounter++) + ".) Neue ChartSet wird geladen ");
				}
				
			}
			else if(url.getPath().endsWith(Names.MODELFILETYPE)){
				System.out.println( (orderCounter++) + ".) Neues Modell wird geladen ");
			}
			
			refresh();
		}
		else{
			urlRegistry.add(url.getPath());
			super.addURL(url);
		}
	}
	
	
	public void addClassLoaderChangeListener(ClassLoaderChangeListener listener){
		cleanListeners(listener.getClass().getName());
	   this.changeListener.add(listener);
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if(destroyed) throw new ClassNotFoundException("GlobalClassLoader: ClassLoader was destroyed. Please reload Global ClassLoader!");
		return super.loadClass(name);
	}
	
	public void notifyAllListeners(){
		for(ClassLoaderChangeListener listener: changeListener) listener.classLoaderHasChanged();
	}
	
	private void cleanListeners(String className){
		
		for(ClassLoaderChangeListener actListener: changeListener){
			if(actListener.getClass().getName().equals(className)){
				
				changeListener.remove(actListener);
				return;
			}
		}
		
	}
}

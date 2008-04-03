package sim.app.episim.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class GlobalClassLoader extends URLClassLoader{
	
	private static GlobalClassLoader instance;
	
	private Set<String> urlRegistry;
	
	boolean destroyed = false;
	
	public List<ClassLoaderChangeListener> changeListener;
	
	private GlobalClassLoader() {

	  super(new URL[]{});
	   	 urlRegistry = new HashSet<String>(); 
	   	 changeListener = new LinkedList<ClassLoaderChangeListener>();
   }
	
	public static GlobalClassLoader getInstance(){
		if(instance == null) instance = new GlobalClassLoader();
		
		return instance;
		
	}
	
	private void refresh(){
		List<ClassLoaderChangeListener> listCopy = changeListener;
		Set<String> registryCopy = urlRegistry;
		URL[] urls = this.getURLs();
		destroyClassLoader();
		instance = new GlobalClassLoader();
		instance.changeListener = listCopy;
		for(URL url: urls) instance.addURL(url);
		notifyAllListeners();
	}
	
	public void destroyClassLoader(){
		destroyed = true;
		instance=null;
	}
	
	public void registerURL(URL url){
		if(urlRegistry.contains(url.getPath())) refresh();
		else{
			urlRegistry.add(url.getPath());
			super.addURL(url);
		}
	}
	public void addClassLoaderChangeListener(ClassLoaderChangeListener listener){
	   this.changeListener.add(listener);
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if(destroyed) throw new ClassNotFoundException("GlobalClassLoader: ClassLoader was destroyed. Please reload Global ClassLoader!");
		return super.loadClass(name);
	}
	
	public void notifyAllListeners(){
		for(ClassLoaderChangeListener listener: changeListener) listener.classLoaderHasChanged();
	}
}

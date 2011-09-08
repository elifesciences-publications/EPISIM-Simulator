package sim.app.episim.datamonitoring.charts.io;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;

import org.jfree.chart.ChartPanel;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.ModelCompatibilityException;
import episimfactories.AbstractChartSetFactory;
import episimfactories.AbstractEpisimCellBehavioralModelFactory;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.monitoring.EpisimChartSet;


public class ECSFileReader{
	    private URL url;

	   
	    private Class factoryClass;
	    private AbstractChartSetFactory factory;
	    
	    /**
	     * Creates a new JarClassLoader for the specified url.
	     *
	     * @param url the url of the jar file
	     */
	    public ECSFileReader(URL url){	       
	        this.url = url;      
	    }
	    
	  public AbstractChartSetFactory getChartSetFactory(){
		  if(this.factory == null) loadFactory();
		  return this.factory;
	  }
	  
	  private void loadFactory(){
		  try{
      	  GlobalClassLoader.getInstance().registerURL(url);
	      this.factoryClass = GlobalClassLoader.getInstance().loadClass(getClassName(new Attributes.Name("Factory-Class")));
	     
	      if(factoryClass != null && AbstractChartSetFactory.class.isAssignableFrom(this.factoryClass)){
	      	factory = (AbstractChartSetFactory) factoryClass.newInstance();
	      	
	      }
	      else throw new Exception("No compatible EpisimChartSetFactory found!");
        }
        catch (Exception e){
      	  ExceptionDisplayer.getInstance().displayException(e);
        }
	  }
	    
	 public EpisimChartSet getEpisimChartSet() throws ModelCompatibilityException {

		URL u = null;

		try{
			u = new URL("jar", "", url + "!/" + AbstractChartSetFactory.getEpisimChartSetBinaryName());
		}
		catch (MalformedURLException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}

		JarURLConnection uc = null;
		try{
			uc = (JarURLConnection) u.openConnection();
			uc.setDefaultUseCaches(false);
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}

		try{
			return AbstractChartSetFactory.getEpisimChartSet(uc.getInputStream());
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}

		return null;
	}
	    
	public List<ChartPanel> getChartPanels(){
		if(this.factory == null) loadFactory();
		return this.factory.getChartPanels();
	}
	
	public List<EnhancedSteppable> getChartSteppables(){
		if(this.factory == null) loadFactory();
		return this.factory.getSteppablesOfCharts();
	}
	   
	    
	    
	    private String getClassName(Attributes.Name attrName)throws IOException{
	   	 URL u = new URL("jar", "", url + "!/");
	       JarURLConnection uc = (JarURLConnection)u.openConnection();
	       Attributes attr = uc.getMainAttributes();
	       
	       return attr != null ? attr.getValue(attrName) : null;
	    }
	     
	

}

package sim.app.episim.datamonitoring.charts.io;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.charts.DiffusionChartFactory;
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
	    private AbstractChartSetFactory chartSetFactory;
	    private DiffusionChartFactory diffusionChartFactory;
	    
	    /**
	     * Creates a new JarClassLoader for the specified url.
	     *
	     * @param url the url of the jar file
	     */
	    public ECSFileReader(URL url){	       
	        this.url = url;      
	    }
	    
	  public AbstractChartSetFactory getChartSetFactory(){
		  if(this.chartSetFactory == null) loadChartSetFactory();
		  return this.chartSetFactory;
	  }
	  
	  private void loadChartSetFactory(){
		  try{
      	
			GlobalClassLoader.getInstance().registerURL(url);
	      this.factoryClass = GlobalClassLoader.getInstance().loadClass(getClassName(new Attributes.Name("Factory-Class")));
	     
	      if(factoryClass != null && AbstractChartSetFactory.class.isAssignableFrom(this.factoryClass)){
	      	chartSetFactory = (AbstractChartSetFactory) factoryClass.newInstance();	      	
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
		if(this.chartSetFactory == null) loadChartSetFactory();
		return this.chartSetFactory.getChartPanels();
	}
	
	public List<JPanel> getDiffusionChartPanels() throws ModelCompatibilityException{
		if(this.diffusionChartFactory == null){
			EpisimChartSet chartSet = getEpisimChartSet();
			this.diffusionChartFactory = new DiffusionChartFactory(chartSet.getEpisimDiffFieldCharts());
		}
		return diffusionChartFactory.getDiffusionChartPanels();
	}
	
	public List<EnhancedSteppable> getChartSteppables() throws ModelCompatibilityException{
		if(this.chartSetFactory == null) loadChartSetFactory();
		if(this.diffusionChartFactory == null){
			EpisimChartSet chartSet = getEpisimChartSet();
			this.diffusionChartFactory = new DiffusionChartFactory(chartSet.getEpisimDiffFieldCharts());
		}
		List<EnhancedSteppable> steppables = new ArrayList<EnhancedSteppable>();
		steppables.addAll(this.chartSetFactory.getSteppablesOfCharts());
		steppables.addAll(this.diffusionChartFactory.getDiffusionChartSteppables());
		return steppables;
	}
	    
	private String getClassName(Attributes.Name attrName)throws IOException{
	   URL u = new URL("jar", "", url + "!/");
	   JarURLConnection uc = (JarURLConnection)u.openConnection();
	   Attributes attr = uc.getMainAttributes();	       
	   return attr != null ? attr.getValue(attrName) : null;
	}
}

package sim.app.episim.charts.io;

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
import episimexceptions.ModelCompatibilityException;
import episimfactories.AbstractChartSetFactory;
import episimfactories.AbstractEpisimCellDiffModelFactory;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChartSet;


public class ECSFileReader extends URLClassLoader {
	    private URL url;

	   
	    private Class factoryClass;
	    private AbstractChartSetFactory factory;
	    
	    /**
	     * Creates a new JarClassLoader for the specified url.
	     *
	     * @param url the url of the jar file
	     */
	    public ECSFileReader(URL url) {
	        super(new URL[] { url });
	        this.url = url;
	        
	        try{
		      this.factoryClass = loadClass(getClassName(new Attributes.Name("Factory-Class")));
		     
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
			u = new URL("jar", "", url + "!/" + factory.getEpisimChartSetBinaryName());
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
			return factory.getEpisimChartSet(uc.getInputStream());
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}

		return null;
	}
	    
	public List<ChartPanel> getChartPanels(){
		return this.factory.getChartPanels();
	}
	   
	    
	    
	    private String getClassName(Attributes.Name attrName)throws IOException{
	   	 URL u = new URL("jar", "", url + "!/");
	       JarURLConnection uc = (JarURLConnection)u.openConnection();
	       Attributes attr = uc.getMainAttributes();
	       
	       return attr != null ? attr.getValue(attrName) : null;
	    }
	     
	

}

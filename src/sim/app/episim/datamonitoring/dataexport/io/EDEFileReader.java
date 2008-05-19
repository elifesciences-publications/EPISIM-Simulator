package sim.app.episim.datamonitoring.dataexport.io;

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
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.ModelCompatibilityException;
import episimfactories.AbstractChartSetFactory;
import episimfactories.AbstractDataExportFactory;
import episimfactories.AbstractEpisimCellDiffModelFactory;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChartSet;
import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.EpisimDataExportDefinitionSet;

public class EDEFileReader{

	private URL url;

	private Class<?> factoryClass;

	private AbstractDataExportFactory factory;

	/**
	 * Creates a new JarClassLoader for the specified url.
	 * 
	 * @param url
	 *           the url of the jar file
	 */
	public EDEFileReader(URL url) {

		
		this.url = url;

		try{
			 GlobalClassLoader.getInstance().registerURL(url);
		      this.factoryClass = GlobalClassLoader.getInstance().loadClass(getClassName(new Attributes.Name("Factory-Class")));
			

			if(factoryClass != null && AbstractDataExportFactory.class.isAssignableFrom(this.factoryClass)){
				factory = (AbstractDataExportFactory) factoryClass.newInstance();

			}
			else
				throw new Exception("No compatible EpisimDataExportFactory found!");
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}

	}

	public EpisimDataExportDefinitionSet getEpisimDataExportDefinitionSet() throws ModelCompatibilityException {

		URL u = null;

		try{
			u = new URL("jar", "", url + "!/" + factory.getEpisimDataExportDefinitionSetBinaryName());
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
			return factory.getEpisimDataExportDefinitionSet(uc.getInputStream());
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}

		return null;
	}

	private String getClassName(Attributes.Name attrName) throws IOException {

		URL u = new URL("jar", "", url + "!/");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		Attributes attr = uc.getMainAttributes();

		return attr != null ? attr.getValue(attrName) : null;
	}
		     
		

	}




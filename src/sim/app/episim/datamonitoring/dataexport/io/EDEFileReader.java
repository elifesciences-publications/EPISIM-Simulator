package sim.app.episim.datamonitoring.dataexport.io;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.ModelCompatibilityException;
import episimfactories.AbstractDataExportFactory;
import episiminterfaces.EpisimDataExportDefinitionSet;
import episiminterfaces.GeneratedDataExport;

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
	
	public AbstractDataExportFactory getDataExportFactory(){ return this.factory;}
	
	public List<GeneratedDataExport> getDataExports(){
		return this.factory.getDataExports();
	}
	
	public List<EnhancedSteppable> getDataExportSteppables(){
		return this.factory.getSteppablesOfDataExports();
	}

	private String getClassName(Attributes.Name attrName) throws IOException {

		URL u = new URL("jar", "", url + "!/");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		Attributes attr = uc.getMainAttributes();

		return attr != null ? attr.getValue(attrName) : null;
	}
		     
		

	}




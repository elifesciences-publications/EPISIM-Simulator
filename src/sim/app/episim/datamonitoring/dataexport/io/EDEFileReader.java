package sim.app.episim.datamonitoring.dataexport.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.dataexport.DiffusionFieldDataExport;
import sim.app.episim.datamonitoring.dataexport.DiffusionFieldDataExportFactory;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.Names;
import episimexceptions.ModelCompatibilityException;
import episimfactories.AbstractDataExportFactory;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;
import episiminterfaces.monitoring.GeneratedDataExport;

public class EDEFileReader{

	private URL url;

	private Class<?> factoryClass;
	private AbstractDataExportFactory dataExportDefinitionFactory;
	private DiffusionFieldDataExportFactory diffusionFieldDataExportFactory;

	public static boolean foundDirtyDataExportColumnDuringImport = false;
	
	/**
	 * Creates a new JarClassLoader for the specified url.
	 * 
	 * @param url
	 *           the url of the jar file
	 */
	public EDEFileReader(URL url) {		
		this.url = url;
	}
	private void loadFactory(){
		try{
			 GlobalClassLoader.getInstance().registerURL(url);
		    this.factoryClass = GlobalClassLoader.getInstance().loadClass(getClassName(new Attributes.Name("Factory-Class")));
			

			if(factoryClass != null && AbstractDataExportFactory.class.isAssignableFrom(this.factoryClass)){
				dataExportDefinitionFactory = (AbstractDataExportFactory) factoryClass.newInstance();

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
			u = new URL("jar", "", url + "!/" + Names.EPISIM_DATAEXPORT_XML_FILENAME);
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
			return AbstractDataExportFactory.getEpisimDataExportDefinitionSetBasedOnXML(uc.getInputStream());
		}catch(FileNotFoundException e){
			System.out.println("DataExport XML File not found, falling back to serialized version!");
			try{
				u = new URL("jar", "", url + "!/" + Names.EPISIM_DATAEXPORT_FILENAME);
			}
			catch (MalformedURLException ex){
				ExceptionDisplayer.getInstance().displayException(e);
			}
			uc = null;
			try{
				uc = (JarURLConnection) u.openConnection();
				uc.setDefaultUseCaches(false);
				return AbstractDataExportFactory.getEpisimDataExportDefinitionSet(uc.getInputStream());
			}
			catch (IOException ex){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		return null;
	}
	
	public AbstractDataExportFactory getDataExportFactory(){
		if(this.dataExportDefinitionFactory == null) loadFactory();
		return this.dataExportDefinitionFactory;
	}
	
	public List<GeneratedDataExport> getDataExports(){
		if(this.dataExportDefinitionFactory == null) loadFactory();
		return this.dataExportDefinitionFactory.getDataExports();
	}
	
	public List<DiffusionFieldDataExport> getDiffusionFieldDataExports() throws ModelCompatibilityException{
		if(this.diffusionFieldDataExportFactory == null){
			EpisimDataExportDefinitionSet dataExportDefinitionSet = getEpisimDataExportDefinitionSet();
			this.diffusionFieldDataExportFactory = new DiffusionFieldDataExportFactory(dataExportDefinitionSet.getEpisimDiffFieldDataExportDefinitions());
		}
		return diffusionFieldDataExportFactory.getDiffFieldDataExports();
	}
	
	public List<EnhancedSteppable> getDataExportSteppables() throws ModelCompatibilityException{
		if(this.dataExportDefinitionFactory == null) loadFactory();
		if(this.diffusionFieldDataExportFactory == null){
			EpisimDataExportDefinitionSet dataExportDefinitionSet = getEpisimDataExportDefinitionSet();
			this.diffusionFieldDataExportFactory = new DiffusionFieldDataExportFactory(dataExportDefinitionSet.getEpisimDiffFieldDataExportDefinitions());
		}
		List<EnhancedSteppable> steppables = new ArrayList<EnhancedSteppable>();
		steppables.addAll(this.dataExportDefinitionFactory.getSteppablesOfDataExports());
		steppables.addAll(this.diffusionFieldDataExportFactory.getDiffFieldDataExportSteppables());
		return steppables;
	}

	private String getClassName(Attributes.Name attrName) throws IOException {
		URL u = new URL("jar", "", url + "!/");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		Attributes attr = uc.getMainAttributes();
		return attr != null ? attr.getValue(attrName) : null;
	}
}




package binloc;

import java.io.File;
import java.net.URISyntaxException;


public class ProjectLocator {
	
	
	public static File getBinPath() throws URISyntaxException{
		
		//return new File(ProjectLocator.class.getResource("../").toURI());
	//return new File(Thread.currentThread().getContextClassLoader().getResource("./").toURI())
		File f = new File("");
		String path = f.getAbsolutePath();
		path = path.endsWith(System.getProperty("file.separator")) ? path.substring(0, path.length()-1) : path;
		return new File(path.endsWith("bin") ? path : path +System.getProperty("file.separator")+"bin");
	}
	
	
	
	public static File getPathOf(String directoryName)throws URISyntaxException{
		
	/*	String path = ((getBinPath()).getAbsolutePath());
		
		if(path.endsWith(System.getProperty("file.separator"))) path = path.substring(0, path.length()-1);
		int i = path.length()-1;
		for(;path.charAt(i)!= System.getProperty("file.separator").charAt(0); i--);*/
		File f = new File("");
		String path = f.getAbsolutePath();
		path = path.endsWith(System.getProperty("file.separator")) ? path.substring(0, path.length()-1) : path;
		path = path.endsWith("bin") ? path.substring(0, path.length()-(3+System.getProperty("file.separator").length())):path;		
		return (new File(path+System.getProperty("file.separator")+directoryName));
	}

}

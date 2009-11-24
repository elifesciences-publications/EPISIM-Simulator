package binloc;

import java.io.File;
import java.net.URISyntaxException;


public class ProjectLocator {
	
	
	public static File getBinPath() throws URISyntaxException{
		return new File(ProjectLocator.class.getResource("../").toURI());
	}
	
	
	
	public static File getPathOf(String directoryName)throws URISyntaxException{
		String path = ((getBinPath()).getAbsolutePath());
		
		if(path.endsWith(System.getProperty("file.separator"))) path = path.substring(0, path.length()-1);
		int i = path.length()-1;
		for(;path.charAt(i)!= System.getProperty("file.separator").charAt(0); i--);
		
		return (new File((path.substring(0, i)+System.getProperty("file.separator")+directoryName)));
	}

}

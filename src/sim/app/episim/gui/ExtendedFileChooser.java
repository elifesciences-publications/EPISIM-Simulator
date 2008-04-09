package sim.app.episim.gui;





import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class ExtendedFileChooser extends JFileChooser {
	private String fileExtension = "";
	public ExtendedFileChooser(String _fileExtension){
		super();
		if(_fileExtension == null || _fileExtension.equals("")) throw new IllegalArgumentException("File-Extension must not be null or empty!");
		if(!_fileExtension.startsWith(".")) _fileExtension = ".".concat(_fileExtension);
		this.fileExtension = _fileExtension;
		this.setFileFilter(new FileFilter() {
         public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(fileExtension) || f.isDirectory();
        }
        public String getDescription() {
            return fileExtension.substring(1)+"-Files";
        }
    });
		
		
	}
	
	
	public File getSelectedFile(){
		File file = super.getSelectedFile();
		if(file !=null && !file.isDirectory()&& !file.getAbsolutePath().toLowerCase().trim().endsWith(fileExtension)){ 
			file= new File(file.getAbsolutePath() +fileExtension);
		}
		return file;
	}

	
}

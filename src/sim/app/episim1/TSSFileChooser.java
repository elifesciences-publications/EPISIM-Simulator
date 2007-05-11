package sim.app.episim1;





import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class TSSFileChooser extends JFileChooser {
	
	public TSSFileChooser(){
		super();
		this.setDialogTitle("Open EpiSim Model");
		this.setFileFilter(new FileFilter() {
         public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".tss") || f.isDirectory();
        }
        public String getDescription() {
            return "tss-Files";
        }
    });
		
		
	}
	
	public File getSelectedFile(){
		File file = super.getSelectedFile();
		if(file !=null && !file.getAbsolutePath().toLowerCase().trim().endsWith(".tss")){ 
			file= new File(file.getAbsolutePath() +".tss");
		}
		return file;
	}

}

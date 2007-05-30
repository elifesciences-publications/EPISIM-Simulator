package sim.app.episim;





import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class TSSFileChooser extends JFileChooser {
	
	public TSSFileChooser(){
		super();
		
		this.setFileFilter(new FileFilter() {
         public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".tss") || f.isDirectory();
        }
        public String getDescription() {
            return "tss-Files";
        }
    });
		
		
	}
	public int showOpenDialog(Component comp){
		this.setDialogTitle("Open Snapshot");
		return super.showOpenDialog(comp);
	}
	public int showSaveDialog(Component comp){
		this.setDialogTitle("Select Snapshot Path");
		return super.showOpenDialog(comp);
	}
	
	public File getSelectedFile(){
		File file = super.getSelectedFile();
		if(file !=null && !file.getAbsolutePath().toLowerCase().trim().endsWith(".tss")){ 
			file= new File(file.getAbsolutePath() +".tss");
		}
		return file;
	}

}

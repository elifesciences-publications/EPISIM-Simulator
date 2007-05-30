package sim.app.episim.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class JarFileChooser extends JFileChooser {
	
	public JarFileChooser(){
		super();
		this.setDialogTitle("Open EpiSim Model");
		this.setFileFilter(new FileFilter() {
         public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".jar") || f.isDirectory();
        }
        public String getDescription() {
            return "Jar-Files";
        }
    });
		
		
	}

	
	public int showOpenDialog(Component parent) throws HeadlessException {
		
		int result = super.showOpenDialog(parent);
		
		
		
		return result;
	}

}

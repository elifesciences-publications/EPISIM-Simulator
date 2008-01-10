package sim.app.episim;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.*;



import java.io.*;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import sim.app.episim.gui.JarFileChooser;



public class CompileWizard {

	
	private JFileChooser fileChoose;
	private JarFileChooser jarChooser;
	private JWindow progressWindow;
	private JLabel progressLabel;
	private JProgressBar progressBar;
	private Frame owner;
	
	public CompileWizard(Frame owner)throws IllegalStateException{
		if(owner == null) throw new IllegalStateException("Owner mustn't be null");
		else{
			
			progressWindow = new JWindow(owner);
			
			progressWindow.getContentPane().setLayout(new BorderLayout(5, 5));
			if(progressWindow.getContentPane() instanceof JPanel)
				((JPanel)progressWindow.getContentPane()).setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(10,10, 10, 10)));
			progressBar = new JProgressBar();
			progressLabel = new JLabel("");
			progressWindow.getContentPane().add(progressLabel, BorderLayout.NORTH);
			progressWindow.getContentPane().add(progressBar, BorderLayout.CENTER);
			
			progressWindow.setSize(400, 65);
			
			progressWindow.setLocation(owner.getLocation().x + (owner.getWidth()/2) - (progressWindow.getWidth()/2), 
					owner.getLocation().y + (owner.getHeight()/2) - (progressWindow.getHeight()/2));
			
			fileChoose = new JFileChooser();
			
			fileChoose.setFileFilter(new FileFilter() {
	
				public boolean accept(File f) {
	
					return f.getName().toLowerCase().endsWith(".java") || f.isDirectory();
				}
	
				public String getDescription() {
	
					return "Java-Files";
				}
			});
			
			jarChooser = new JarFileChooser();
			jarChooser.setDialogTitle("Select name for Episim-Model-Archive");
		}
	}
	
	
	public void showSelectFilesDialogs(){
		boolean mainModelApproved = false;
		boolean parametersApproved = false;
		boolean modelFilesApproved = false;
		boolean jarFileApproved = false;
		File [] modelFiles = null;
		File mainModelFile = null;
		File parametersFile = null;
		File jarFile = null;
		
		fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChoose.setMultiSelectionEnabled(true);
		fileChoose.setDialogTitle("Select Episim-Model Java-Files");
		
		modelFilesApproved = JFileChooser.APPROVE_OPTION == fileChoose.showDialog(null, "Select");
		modelFiles = fileChoose.getSelectedFiles();
		if(modelFilesApproved){
			fileChoose.setMultiSelectionEnabled(false);
			fileChoose.setDialogTitle("Select Episim-Main-Model Java-File");
			mainModelApproved = JFileChooser.APPROVE_OPTION == fileChoose.showDialog(null, "Select");
			mainModelFile = fileChoose.getSelectedFile();
		}
		if(mainModelApproved && modelFilesApproved){
			fileChoose.setDialogTitle("Select Episim-Parameters Java-File");
			parametersApproved = JFileChooser.APPROVE_OPTION == fileChoose.showDialog(null, "Select");
			parametersFile = fileChoose.getSelectedFile();
		}
		
		if(mainModelApproved && parametersApproved && modelFilesApproved){
			jarChooser.setCurrentDirectory(fileChoose.getCurrentDirectory());
			jarFileApproved = JFileChooser.APPROVE_OPTION == jarChooser.showSaveDialog(null);
		   jarFile = jarChooser.getSelectedFile();
		}
			if(mainModelApproved && parametersApproved && modelFilesApproved && jarFileApproved){
				try{
	            createModelArchive(modelFiles, mainModelFile, parametersFile, jarFile);
            }
            catch (IOException e){
            	ExceptionDisplayer.getInstance()
					.displayException(e);
            }
            catch (URISyntaxException e){
            	ExceptionDisplayer.getInstance()
					.displayException(e);
            }
			}
		
	}
	
	private void createModelArchive(File[] modelFiles, File mainModelFile, File parametersFile, File jarFile) throws IOException, URISyntaxException {
		final List<File> files = Arrays.asList(modelFiles);
		final File mainModelFileFinal = mainModelFile;
		final File parametersFileFinal = parametersFile;
		final File jarFileFinal = jarFile;
		
		Runnable runnable = new Runnable() {
			public void run() {
				
				JarOutputStream jarOut=null;
				Manifest manifest;
				
				progressWindow.setVisible(true);
						
					try {
						
						// Preparing Compiler
						progressLabel.setText("Compiling Episim-Model Java-Files");
						List<File> classFiles = compileModelFiles(files);

						int ticksize = 50 / (classFiles.size() + 1);
						
						progressBar.setValue(50);
							
							

							// Adding MANIFEST:MF

							StringBuffer sBuffer = new StringBuffer();

							sBuffer.append("Manifest-Version: 1.0\n");
							sBuffer.append("Created-By: 1.1 (Episim - Uni Heidelberg)\n");
							sBuffer.append("Model-Class: " + mainModelFileFinal.getName().substring(0, mainModelFileFinal.getName().length()-5)+"\n");
							sBuffer.append("Parameters-Class: " + parametersFileFinal.getName().substring(0, parametersFileFinal.getName().length()-5)+"\n");

		
							ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));

							manifest = new Manifest(byteIn);

							progressLabel.setText("Wrinting MANIFEST.MF into Episim-Model-Archive");
							progressBar.setValue(progressBar.getValue()+ ticksize);

							jarOut = new JarOutputStream(new FileOutputStream(jarFileFinal), manifest);

							
							File[] fileList = new File[classFiles.size()];
							classFiles.toArray(fileList);
							for (File f : fileList) {

								if (f.isDirectory())
									continue;
								String name = f.getName();
								
								if (name.endsWith(".class")) {

									progressLabel.setText("Wrinting " + "into Episim-Model-Archive");
									progressBar.setValue(progressBar.getValue()+ ticksize);
									Thread.sleep(500);
									jarOut.putNextEntry(new JarEntry(f.getName()));
								} else
									continue;

								FileInputStream fileInput = new FileInputStream(f);

								byte[] bytes = new byte[1024];
								int available;
								while ((available = fileInput.read(bytes)) > 0) {
									jarOut.write(bytes, 0, available);
								}
								jarOut.flush();
								fileInput.close();

							}
							jarOut.flush();
							jarOut.finish();
							jarOut.close();
							
							
							progressLabel.setText("Episim-Model-Archive complete");
							progressBar.setValue(100);
							progressWindow.repaint();
							Thread.sleep(1000);
							progressWindow.setVisible(false);
						} catch (Exception e) {
							ExceptionDisplayer.getInstance()
									.displayException(e);

						}
					}
		 };
		Thread thread = new Thread(runnable);
	    thread.start();
	    
	    
	}
	
	private List<File> compileModelFiles(List<File> files) throws URISyntaxException{
		
		List<File> classFiles = new ArrayList<File>();
		// Wo liegen die Binaries der Simulationsumgebung
		File binPath = new File(this.getClass().getResource("../../../").toURI());
		JavaCompiler compiler;
		StandardJavaFileManager fileManager;
		Iterable<? extends JavaFileObject> compilationUnits;
		Iterable<String> options;

		// Preparing Class-File-Objects
		for (File src : files) {
			File tmp = new File(src.getAbsolutePath()
					.substring(0,
							src.getAbsolutePath().length() - 4)
					+ "class");
			classFiles.add(tmp);
		}
		
		//Preparing Compiler
				compiler = ToolProvider.getSystemJavaCompiler();
				fileManager = compiler.getStandardFileManager(null,	null, null);
				compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
				options = Arrays.asList(new String[] { "-cp", binPath.getAbsolutePath() });

				

				try {
					compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
					
					fileManager.close();
					
					
				} catch (Exception e) {
					ExceptionDisplayer.getInstance()
							.displayException(e);

				}
			return classFiles;
		
	}

}


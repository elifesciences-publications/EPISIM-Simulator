package sim.app.episim;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.tools.*;

import sim.app.episim.gui.JarFileChooser;



public class CompileTest {
	private static String path;
	public static void main(String[] args) throws IOException {

		JFileChooser fileChoose = new JFileChooser();
		fileChoose.setDialogTitle("Select Java Files");
		fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChoose.setMultiSelectionEnabled(true);
		fileChoose.setFileFilter(new FileFilter() {

			public boolean accept(File f) {

				return f.getName().toLowerCase().endsWith(".java") || f.isDirectory();
			}

			public String getDescription() {

				return "Java-Files";
			}
		});
		List<File> classFiles = new ArrayList<File>();
		if(JFileChooser.APPROVE_OPTION == fileChoose.showDialog(null, "Select")){

			File[] files = fileChoose.getSelectedFiles();
			for(File src : files){
				System.out.println("compiling the file: " + src.getAbsolutePath());

				File tmp = new File(src.getAbsolutePath().substring(0, src.getAbsolutePath().length() - 4) + "class");
				classFiles.add(tmp);

			}
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays
					.asList(files));

			fileChoose.setDialogTitle("Select Binary Directory");
			fileChoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChoose.setMultiSelectionEnabled(false);
			if(JFileChooser.APPROVE_OPTION == fileChoose.showOpenDialog(null)){
				Iterable<String> options = Arrays.asList(new String[] { "-cp",
						fileChoose.getSelectedFile().getAbsolutePath() });
				compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
				fileManager.close();

				JarFileChooser jarChooser = new JarFileChooser();
				jarChooser.setDialogTitle("Select Path");
				if(JFileChooser.APPROVE_OPTION == jarChooser.showSaveDialog(null)){

					File jarFile = jarChooser.getSelectedFile();

					//Adding MANIFEST:MF

					StringBuffer sBuffer = new StringBuffer();

					sBuffer.append("Manifest-Version: 1.0\n");
					sBuffer.append("Created-By: 1.1 (Episim - Uni Heidelberg)\n");
					sBuffer.append("Main-Model-Class: EpisimModel\n");
					
					byte[] buffer = new byte[sBuffer.toString().getBytes("UTF-8").length];
					ByteArrayInputStream byteIn = new ByteArrayInputStream(sBuffer.toString().getBytes("UTF-8"));
					

					

					Manifest manifest = new Manifest(byteIn);

					

					System.out.println("putting entry  MANIFEST.MF");

					JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);

					// compressing the class files in the '<packageName>' directory
					File[] fileList = new File[classFiles.size()];
					classFiles.toArray(fileList);
					for(File f : fileList){

						try{
							if(f.isDirectory())
								continue;
							String name = f.getName();
							System.out.println("Trying to put " + name + " to the jar File " + jarFile.getAbsolutePath());

							/* if (name.endsWith(".java") && doJarSources) {
							 String entry = getJarFilePath("src"+File.separator+getRelativePackagePath(f.getAbsolutePath()));
							 System.out.println("putting entry "+entry);
							 jarOut.putNextEntry(new JarEntry(entry));
							 }*/
							if(name.endsWith(".class")){

								System.out.println("putting entry " + f.getName());
								jarOut.putNextEntry(new JarEntry(f.getName()));
							}
							else
								continue;

							FileInputStream fileInput = new FileInputStream(f);

							byte[] bytes = new byte[1024];
							int available;
							while ((available = fileInput.read(bytes)) > 0){
								jarOut.write(bytes, 0, available);
							}
							jarOut.flush();
							fileInput.close();

						}
						catch (Exception e){
							e.printStackTrace();

						}
					}

					jarOut.flush();
					jarOut.finish();
					jarOut.close();

				}
			}
		}
	}

}

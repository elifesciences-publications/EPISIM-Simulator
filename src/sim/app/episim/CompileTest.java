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



public class CompileTest {
	
	public static void main(String[] args) throws IOException{
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      StandardJavaFileManager fileManager = compiler.getStandardFileManager(null,null,null);
      File src = new File("d:/BeispielModels/EpisimModel.java");
      System.out.println("compiling the file: "+src.getAbsolutePath());
      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{src}));
      
     
      Iterable<String> options = Arrays.asList(new String[] {"-cp", "D:/JOpenGL/EpiSim/bin"} );
      compiler.getTask(null, fileManager, null,options,null, compilationUnits).call();
      fileManager.close(); 
      File jarFile = new File("d:/BeispielModels/EpisimModelTest.jar");
      	
      	
      	//Adding MANIFEST:MF
      	
      	StringBuffer sBuffer= new StringBuffer();
          
         sBuffer.append("Manifest-Version: 1.0\n");
         sBuffer.append("Created-By: 1.1 (Episim - Uni Heidelberg)\n");
         sBuffer.append("Main-Model-Class: EpisimModel\n");
         File tempManifest =new File(System.getProperty("java.io.tmpdir") +File.pathSeparatorChar+ "manifest.txt");
         FileOutputStream fileOut = new FileOutputStream(tempManifest);
        
         fileOut.write(sBuffer.toString().getBytes("UTF-8"));
         
         fileOut.flush();
         fileOut.close();
        
         FileInputStream fileIn = new FileInputStream(tempManifest);
         
        
         
         
        
         Manifest manifest = new Manifest(fileIn);
         
         
         
         
         FileOutputStream fileOut2 = new FileOutputStream(new File("d:/test.mf"));
         manifest.write(fileOut2);
         
            System.out.println("putting entry  MANIFEST.MF");
            
            
            
            JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        
         // compressing the class files in the '<packageName>' directory
         
      	File[] fileList = new File[]{new File("d:/BeispielModels/EpisimModel.class")};
         for (File f : fileList) {
             
                 
             try {
                 if (f.isDirectory() )
                     continue;
                 String name = f.getName();
                 System.out.println("Trying to put "+name+" to the jar File "+jarFile.getAbsolutePath());
                
                /* if (name.endsWith(".java") && doJarSources) {
                     String entry = getJarFilePath("src"+File.separator+getRelativePackagePath(f.getAbsolutePath()));
                     System.out.println("putting entry "+entry);
                     jarOut.putNextEntry(new JarEntry(entry));
                 }*/
                 if (name.endsWith(".class")) {
                     
                     System.out.println("putting entry "+ f.getName());
                     jarOut.putNextEntry(new JarEntry(f.getName()));
                 }
                 else
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
             catch(Exception e) {
                 e.printStackTrace();
                 
             }
         }
         
         jarOut.flush();
         jarOut.finish();
         jarOut.close();
         
         

    }
	


}

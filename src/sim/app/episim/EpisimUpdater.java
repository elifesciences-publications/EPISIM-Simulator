package sim.app.episim;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.net.ssl.HttpsURLConnection;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import binloc.ProjectLocator;
import sim.app.episim.gui.EpisimSimulator;


public class EpisimUpdater {

  private static int BUFFER_SIZE = 8192;
  
  public enum EpisimUpdateState{ NOTAVAILABLE, NOTPOSSIBLE, POSSIBLE};
    
  private DbxClientV2 dbxClient;
  
  private static final String ROOT_DIR = "/episim_simulator";
  
  private static final String UPDATE_META_DATA_FILE = "update.properties";

  private static final String CURRENT_VERSION ="currentversion";
  private static final String UPDATEFILE ="updatefile";
  private static final String MIN_OLD_VERSION ="minoldversion";
  
  
  private String mostCurrentVersion = "";
  private String minOldVersion = "";
  private String updateFile = "";
 
  private long currentFileSize = 0;
  
  private File currentUpdateFile;
  
  public EpisimUpdater() {}
  
  private  void connect(){	 
	      EpisimLogger.getInstance().logInfo("Connecting to EPISIM update Server ");    

	      DbxRequestConfig config = new DbxRequestConfig("EPISIM/5.2", Locale.getDefault().toString());
	      dbxClient = new DbxClientV2(config, "6KOB1DFCefAAAAAAAAABMzE7I7Nmbyd4pFbkatroI3ZywC_l0fOElo_VqR-vLpdo");	       
	      EpisimLogger.getInstance().logInfo("Connection Successful");	   
  }
  
  public void downloadUpdate(EpisimUpdateCallback cb, boolean log) throws IOException, DbxException{
	  connect();
	  if (dbxClient != null && cb != null) {
		  byte[] buffer = new byte[BUFFER_SIZE];  
		  
		  if(log)EpisimLogger.getInstance().logInfo("Read EPISIM update metadata.");
		  readUpdateMetadata();
		  
		  long size = 0;
		  FileMetadata fileMetaData = (FileMetadata)dbxClient.files().getMetadata(ROOT_DIR+"/"+updateFile);
		  size = fileMetaData.getSize();
		  			  
	     if (size > 0) {
	   	  currentFileSize=size;
	   	  GetTemporaryLinkResult dbxUrl = dbxClient.files().getTemporaryLink(ROOT_DIR+"/"+updateFile);
	   	  
	      URL url= new URL(dbxUrl.getLink());
	   	  HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
	   	 
	   	  if(log)EpisimLogger.getInstance().logInfo("EPISIM-Update-File " + updateFile + ": " + size + " bytes");
	        cb.sizeOfUpdate((int)size);   
	     
		     InputStream in = con.getInputStream();
			  
		     String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
			  if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));		     
			  currentUpdateFile = new File(userTmpDir+"EPISIM_Update.zip");
			  FileOutputStream fileOut = new FileOutputStream(currentUpdateFile);
			  if(log)EpisimLogger.getInstance().logInfo("Downloading EPISIM-Update-File");
			  int bytes =0;
			  while ((bytes = in.read(buffer)) != -1) {	       
		        fileOut.write(buffer, 0, bytes);
		        cb.progressOfUpdate(bytes);
		     }
		     fileOut.close();
		     in.close();			     
		     if(log) EpisimLogger.getInstance().logInfo("Successfully Downloaded EPISIM-Update-File");
	     }
	     else throw new IOException("Cannot Download Update File");
	     disconnect();
		  cb.updateHasFinished();
	  }
  }
  
  public void downloadEXEPatch(EpisimUpdateCallback cb, boolean log) throws IOException, DbxException{
	  final String patchFile = "./exe_patch.zip";
	  connect();
	  if (dbxClient != null && cb != null) {
		  
		  byte[] buffer = new byte[BUFFER_SIZE];
		  
		  
		  long size = 0;
		  FileMetadata fileMetaData = (FileMetadata)dbxClient.files().getMetadata(ROOT_DIR+"/"+patchFile);
		  size = fileMetaData.getSize();
		  		  
	     if (size > 0) {
	   	  currentFileSize=size;
	   	  GetTemporaryLinkResult dbxUrl = dbxClient.files().getTemporaryLink(ROOT_DIR+"/"+updateFile); 
	      URL url= new URL(dbxUrl.getLink());
	   	  HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
	   	  if(log)EpisimLogger.getInstance().logInfo("EPISIM-EXE-Patch-File " + patchFile + ": " + size + " bytes");
	        cb.sizeOfUpdate((int)size);   
	     
		     InputStream in =con.getInputStream();
			  
		     String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
			  if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));		     
			  currentUpdateFile = new File(userTmpDir+"EPISIM_Update.zip");
			  FileOutputStream fileOut = new FileOutputStream(currentUpdateFile);
			  if(log)EpisimLogger.getInstance().logInfo("Downloading EPISIM-EXE-Patch-File");
			  int bytes =0;
			  while ((bytes = in.read(buffer)) != -1) {	       
		        fileOut.write(buffer, 0, bytes);
		        cb.progressOfUpdate(bytes);
		     }
		     fileOut.close();
		     in.close();
		     
		     if(log) EpisimLogger.getInstance().logInfo("Successfully Downloaded EPISIM-EXE-Patch-File");
	     }
	     else throw new IOException("Cannot Download EPISIM-EXE-Patch-File");
	     disconnect();
		  cb.updateHasFinished();
	  }
  }
  
  public String getMostCurrentVersion(){ return this.mostCurrentVersion;}
  
  public void installEXEPatch(EpisimUpdateCallback cb, boolean log) throws IOException, URISyntaxException{
	  if (cb != null) {
		
		  if(log)EpisimLogger.getInstance().logInfo("Installing EPISIM EXE-Patch ");
		  long zipFileSize = currentUpdateFile.length();
		  if(zipFileSize > 0){
			  cb.sizeOfUpdate((int) zipFileSize);
			  
			  ZipFile updateZip = new ZipFile(currentUpdateFile);
			  Enumeration<? extends ZipEntry> entries = updateZip.entries();
			  
			  String installationPath = ProjectLocator.getBinPath().getParentFile().getAbsolutePath();
			  if(!installationPath.endsWith(System.getProperty("file.separator"))) installationPath = installationPath.concat(System.getProperty("file.separator"));
			 
			  //add this when developing inside Eclipse
		     //installationPath = installationPath.concat("update"+System.getProperty("file.separator"));
		     //deleteFolderContent(new File(installationPath+"bin"+System.getProperty("file.separator")));
			  while(entries.hasMoreElements()){
				  ZipEntry entry = (ZipEntry)entries.nextElement();
				  if(entry.isDirectory()) {
					  if(!(new File(installationPath+entry.getName())).exists()){
						  (new File(installationPath+entry.getName())).mkdirs();
					  }
				  }
				  else{
					  if(!(new File(installationPath+entry.getName())).getParentFile().exists()){
						  (new File(installationPath+entry.getName())).getParentFile().mkdirs();
					  }
					  byte[] buffer = new byte[1024];
					  int len;
					  InputStream in = updateZip.getInputStream(entry);
					  BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(installationPath+entry.getName()));
					  while((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
					  in.close();
					  out.close();					 
				  }
				  cb.progressOfUpdate((int)entry.getCompressedSize());
			  }
			  updateZip.close();
			  currentUpdateFile.delete();
			  cb.updateHasFinished();			  
		  }	  
	  }
  }
  
  public void installUpdate(EpisimUpdateCallback cb, boolean log) throws IOException, URISyntaxException{
	  if (cb != null) {
		
		  if(log)EpisimLogger.getInstance().logInfo("Installing EPISIM update ");
		  long zipFileSize = currentUpdateFile.length();
		  if(zipFileSize > 0){
			  cb.sizeOfUpdate((int) zipFileSize);
			  
			  ZipFile updateZip = new ZipFile(currentUpdateFile);
			  Enumeration<? extends ZipEntry> entries = updateZip.entries();
			  
			  String installationPath = ProjectLocator.getBinPath().getParentFile().getAbsolutePath();
			  if(!installationPath.endsWith(System.getProperty("file.separator"))) installationPath = installationPath.concat(System.getProperty("file.separator"));
			 
			  //add this when developing inside Eclipse
		     //installationPath = installationPath.concat("update"+System.getProperty("file.separator"));
		     deleteFolderContent(new File(installationPath+"bin"+System.getProperty("file.separator")));
			  while(entries.hasMoreElements()){
				  ZipEntry entry = (ZipEntry)entries.nextElement();
				  if(entry.isDirectory()) {
					  if(!(new File(installationPath+entry.getName())).exists()){
						  (new File(installationPath+entry.getName())).mkdirs();
					  }
				  }
				  else{
					  if(!(new File(installationPath+entry.getName())).getParentFile().exists()){
						  (new File(installationPath+entry.getName())).getParentFile().mkdirs();
					  }
					  byte[] buffer = new byte[1024];
					  int len;
					  InputStream in = updateZip.getInputStream(entry);
					  BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(installationPath+entry.getName()));
					  while((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
					  in.close();
					  out.close();					 
				  }
				  cb.progressOfUpdate((int)entry.getCompressedSize());
			  }
			  updateZip.close();
			  currentUpdateFile.delete();
			  cb.updateHasFinished();			  
		  }	  
	  }
  }
  
  
  public EpisimUpdateState checkForUpdates() throws IOException, DbxException{
	  connect();
	  readUpdateMetadata();
	  int newVersion = Integer.parseInt(mostCurrentVersion.trim().replace(".", ""));
	  int currentVersion = Integer.parseInt(EpisimSimulator.versionID.trim().replace(".", ""));
	  int minOldVersion = Integer.parseInt(this.minOldVersion.trim().replace(".", ""));
	  disconnect();
	  if(newVersion > currentVersion){
		  if(currentVersion >= minOldVersion){
			  return EpisimUpdateState.POSSIBLE;
		  }
		  else{
			  return EpisimUpdateState.NOTPOSSIBLE;
		  }
	  }
	  return EpisimUpdateState.NOTAVAILABLE;
  }
  

  private void disconnect() throws IOException {
    if (dbxClient != null) {
   	 dbxClient = null;   	 
    }
  }
  
  public String readableFileSize() {
      if(currentFileSize <= 0) return "0";
      
      final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
      int digitGroups = (int) (Math.log10(currentFileSize)/Math.log10(1024));
      return new DecimalFormat("#,##0.#").format(currentFileSize/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
  
  private void readUpdateMetadata() throws DbxException, IOException{ 
	  	
	 	ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	 	//dbxClient.getFile(ROOT_DIR+"/"+UPDATE_META_DATA_FILE, null, byteOut);
	 	
	 	Metadata fileMetaData = dbxClient.files().getMetadata(ROOT_DIR+"/"+UPDATE_META_DATA_FILE);
	 	
	 	// Create Dropbox Downloader
	 	DbxDownloader<FileMetadata> dl = dbxClient.files().download(fileMetaData.getPathLower());	
	 	dl.download(byteOut);

    if (byteOut.size() > 0) {  
	      InputStream in = new ByteArrayInputStream(byteOut.toByteArray());
	      Properties updateProp = new Properties();
	      updateProp.load(in);
	      mostCurrentVersion = updateProp.getProperty(CURRENT_VERSION);
	      minOldVersion = updateProp.getProperty(MIN_OLD_VERSION);
	      updateFile = updateProp.getProperty(UPDATEFILE);
	      in.close();
    } 
  }
  
  public void restartApplication() throws IOException, URISyntaxException{
	  String episimPath = ProjectLocator.getBinPath().getParentFile().getAbsolutePath();
	  if(!episimPath.endsWith(System.getProperty("file.separator"))) episimPath = episimPath.concat(System.getProperty("file.separator"));  
     List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();   
     int counter = 2;
     for (String jvmArg : jvmArgs) {
        if(!jvmArg.contains("Simulator.jar")){      	 
      	 counter++;
        }
     }
     counter++;
     String[] cmd = new String[counter];
     if (System.getProperty("os.name").toLowerCase().contains("mac")){
   	  cmd[0]= "java";
	  }
     else cmd[0]= episimPath + "jre" + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
     cmd[1] ="-jar";
     counter = 2;
     for (String jvmArg : jvmArgs) {
        if(!jvmArg.contains("Simulator.jar")){
      	 cmd[counter]= jvmArg;
      	 counter++;
        }
     }    
     cmd[counter]=episimPath + "bin" + System.getProperty("file.separator") + "Simulator.jar";
    
 
    Runtime.getRuntime().exec(cmd);  
    System.exit(0);
  }

  private void deleteFolderContent(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    
  }
  private void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
  
  public interface EpisimUpdateCallback {
		
		/**
		 * This method is called to execute the task the progress window is responsible for
		 */
		void sizeOfUpdate(int size);
		
		void progressOfUpdate(int progress);
		/**
		 * This method is called after the task has finished
		 */
		void updateHasFinished();
		
	}
}

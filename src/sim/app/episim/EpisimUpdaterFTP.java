package sim.app.episim;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import binloc.ProjectLocator;

import sim.app.episim.gui.EpisimSimulator;


public class EpisimUpdaterFTP {
  private static int BUFFER_SIZE = 10240;
  
  public enum EpisimUpdateState{ NOTAVAILABLE, NOTPOSSIBLE, POSSIBLE};
  
  
  
  private FTPClient ftpClient;

  // set the values for your server
  
  private static final String HOST = "filetiga.bioquant.uni-heidelberg.de";
  

  private static final String USER = "episimupdate";

  private static final String PASSWORD = ".update!my!stuff.";

  private static final String ROOT_DIR = "./episim_update/episim_simulator";
  
  private static final String UPDATE_META_DATA_FILE = "update.properties";

  private static final String CURRENT_VERSION ="currentversion";
  private static final String UPDATEFILE ="updatefile";
  private static final String MIN_OLD_VERSION ="minoldversion";
  
  
  private String mostCurrentVersion = "";
  private String minOldVersion = "";
  private String updateFile = "";
 
  private long currentFileSize = 0;
  
  private File currentUpdateFile;
  
  public EpisimUpdaterFTP() {}
  
  private  void connectFTP() throws  IOException{
	 
	      EpisimLogger.getInstance().logInfo("Connecting to EPISIM update server " + HOST);
	      ftpClient = new FTPClient();
	      FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
	      ftpClient.configure(conf);
	      ftpClient.connect(HOST);	      
	      ftpClient.login(USER, PASSWORD);
	      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	      ftpClient.enterLocalPassiveMode();
	      ftpClient.setConnectTimeout(20000);
	      ftpClient.setControlKeepAliveTimeout(2000);
         ftpClient.setDataTimeout(20000);
	      EpisimLogger.getInstance().logInfo("User " + USER + " login OK");
	      ftpClient.changeWorkingDirectory(ROOT_DIR);	      
	      EpisimLogger.getInstance().logInfo("Connection Successful");	   
  }
  
  public void downloadUpdate(EpisimUpdateCallback cb, boolean log) throws IOException{
	  connectFTP();
	  if (ftpClient != null && cb != null) {
		  
		  byte[] buffer = new byte[BUFFER_SIZE];
		  if(log)EpisimLogger.getInstance().logInfo("Read EPISIM update metadata.");
		  readUpdateMetadata();
		  
		  long size = 0;
		  	  
		  FTPFile[] file =ftpClient.listFiles(updateFile);
		
		  if(file!=null && file.length >0 && file[0] != null){
			  size = file[0].getSize();
		  }
		  
	     if (size > 0) {
	   	  currentFileSize=size;
	   	  if(log)EpisimLogger.getInstance().logInfo("EPISIM-Update-File " + updateFile + ": " + size + " bytes");
	        cb.sizeOfUpdate((int)size);   
	     
		     InputStream in = ftpClient.retrieveFileStream(updateFile);
			  
		     String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
			  if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));		     
			  currentUpdateFile = new File(userTmpDir+"EPISIM_Update.zip");
			  FileOutputStream fileOut = new FileOutputStream(currentUpdateFile);
			  if(log)EpisimLogger.getInstance().logInfo("Downloading EPISIM-Update-File");
		     while (true) {	       
		        int bytes = in.read(buffer);
		        if (bytes < 0) break;
		        fileOut.write(buffer, 0, bytes);
		        cb.progressOfUpdate(bytes);
		     }
		     fileOut.close();
		     in.close();
		     if(!ftpClient.completePendingCommand()) throw new IOException("Cannot complete Download of Update File");
		     if(log) EpisimLogger.getInstance().logInfo("Successfully Downloaded EPISIM-Update-File");
	     }
	     else throw new IOException("Cannot Download Update File");
	     disconnect();
		  cb.updateHasFinished();
	  }
  }
  
  public void downloadEXEPatch(EpisimUpdateCallback cb, boolean log) throws IOException{
	  final String patchFile = "./exe_patch.zip";
	  connectFTP();
	  if (ftpClient != null && cb != null) {
		  
		  byte[] buffer = new byte[BUFFER_SIZE];
		  
		  
		  long size = 0;
		  	  
		  FTPFile[] file =ftpClient.listFiles(patchFile);
		
		  if(file!=null && file.length >0 && file[0] != null){
			  size = file[0].getSize();
		  }
		  
	     if (size > 0) {
	   	  currentFileSize=size;
	   	  if(log)EpisimLogger.getInstance().logInfo("EPISIM-EXE-Patch-File " + patchFile + ": " + size + " bytes");
	        cb.sizeOfUpdate((int)size);   
	     
		     InputStream in = ftpClient.retrieveFileStream(patchFile);
			  
		     String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
			  if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));		     
			  currentUpdateFile = new File(userTmpDir+"EPISIM_Update.zip");
			  FileOutputStream fileOut = new FileOutputStream(currentUpdateFile);
			  if(log)EpisimLogger.getInstance().logInfo("Downloading EPISIM-EXE-Patch-File");
		     while (true) {	       
		        int bytes = in.read(buffer);
		        if (bytes < 0) break;
		        fileOut.write(buffer, 0, bytes);
		        cb.progressOfUpdate(bytes);
		     }
		     fileOut.close();
		     in.close();
		     if(!ftpClient.completePendingCommand()) throw new IOException("Cannot complete Download of EPISIM-EXE-Patch-File");
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
  
  
  public EpisimUpdateState checkForUpdates() throws IOException{
	  connectFTP();
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
    if (ftpClient != null) {     
      ftpClient.logout();
      ftpClient.disconnect();
      ftpClient = null;
    }
  }
  
  public String readableFileSize() {
      if(currentFileSize <= 0) return "0";
      
      final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
      int digitGroups = (int) (Math.log10(currentFileSize)/Math.log10(1024));
      return new DecimalFormat("#,##0.#").format(currentFileSize/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
  
 /* protected long getFileSize(String filename) throws FtpProtocolException, IOException{
	  
	   Iterator<FtpDirEntry> fileIter = ftpClient.listFiles("./");
	   System.out.println("\n available files: ");
	   while(fileIter.hasNext()){
	   	FtpDirEntry entry = fileIter.next();
	   
	   	if(filename.endsWith(entry.getName())) return entry.getSize();
	   }
	   return -1;  
  }*/

  

  private void readUpdateMetadata() throws IOException { 
   	 
   	 	long size = 0;		
		  FTPFile[] file = ftpClient.listFiles("./"+UPDATE_META_DATA_FILE);
		  if(file!=null && file.length >0 && file[0] != null){
			  size = file[0].getSize();
		  }	      
	      		
	      if (size > 0) {  
		      InputStream in = ftpClient.retrieveFileStream("./"+UPDATE_META_DATA_FILE);
		      Properties updateProp = new Properties();
		      updateProp.load(in);
		      mostCurrentVersion= updateProp.getProperty(CURRENT_VERSION);
		      minOldVersion= updateProp.getProperty(MIN_OLD_VERSION);
		      updateFile = updateProp.getProperty(UPDATEFILE);
		      in.close();
		      if(!ftpClient.completePendingCommand()) throw new IOException("Cannot complete Download of Update Metadata File");
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

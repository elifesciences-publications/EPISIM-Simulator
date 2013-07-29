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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import binloc.ProjectLocator;

import sim.app.episim.gui.EpisimSimulator;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpDirEntry;
import sun.net.ftp.FtpProtocolException;
import sun.tools.jar.Main;

public class EpisimUpdater {
  private static int BUFFER_SIZE = 10240;
  
  public enum EpisimUpdateState{ NOTAVAILABLE, NOTPOSSIBLE, POSSIBLE};
  
  
  
  private FtpClient ftpClient;

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
  
  public EpisimUpdater() {}
  
  public  void connect() throws FtpProtocolException, IOException{
	 
	      EpisimLogger.getInstance().logInfo("Connecting to EPISIM update server " + HOST);
	      ftpClient = FtpClient.create(HOST);
	      ftpClient.login(USER, PASSWORD.toCharArray());
	      EpisimLogger.getInstance().logInfo("User " + USER + " login OK");
	      EpisimLogger.getInstance().logInfo(ftpClient.getWelcomeMsg());
	      ftpClient.changeDirectory(ROOT_DIR);
	      ftpClient.setBinaryType();
	      EpisimLogger.getInstance().logInfo("Connection Successful");	   
  }
  
  public void downloadUpdate(EpisimUpdateCallback cb) throws FtpProtocolException, IOException{
	  if (ftpClient != null && cb != null) {
		  byte[] buffer = new byte[BUFFER_SIZE];
		  EpisimLogger.getInstance().logInfo("Read EPISIM update metadata.");
		  readUpdateMetadata();
		  
		  
		  long size = ftpClient.getSize(updateFile); 		
	     if (size > 0) {
	   	  currentFileSize=size;
	   	  EpisimLogger.getInstance().logInfo("EPISIM-Update-File " + updateFile + ": " + size + " bytes");
	        cb.sizeOfUpdate((int)size);   
	     
		     InputStream in =  ftpClient.getFileStream(updateFile);
			  
		     String userTmpDir = System.getProperty("java.io.tmpdir", "temp");
			  if(!userTmpDir.endsWith(System.getProperty("file.separator"))) userTmpDir = userTmpDir.concat(System.getProperty("file.separator"));		     
			  currentUpdateFile = new File(userTmpDir+"EPISIM_Update.zip");
			  FileOutputStream fileOut = new FileOutputStream(currentUpdateFile);
			  EpisimLogger.getInstance().logInfo("Downloading EPISIM-Update-File");
		     while (true) {	       
		        int bytes = in.read(buffer);
		        if (bytes < 0) break;
		        fileOut.write(buffer, 0, bytes);
		        cb.progressOfUpdate(bytes);
		     }
		     fileOut.close();
		     in.close();
	     }
	     EpisimLogger.getInstance().logInfo("Successfully Downloaded EPISIM-Update-File");
		  cb.updateHasFinished();
	  }
  }
  
  public String getMostCurrentVersion(){ return this.mostCurrentVersion;}
  
  public void installUpdate(EpisimUpdateCallback cb) throws IOException, URISyntaxException{
	  if (cb != null) {
		
		  EpisimLogger.getInstance().logInfo("Installing EPISIM update ");
		  long zipFileSize = currentUpdateFile.length();
		  if(zipFileSize > 0){
			  cb.sizeOfUpdate((int) zipFileSize);
			  
			  ZipFile updateZip = new ZipFile(currentUpdateFile);
			  Enumeration<? extends ZipEntry> entries = updateZip.entries();
			  
			  String installationPath = ProjectLocator.getBinPath().getParentFile().getAbsolutePath();
			  if(!installationPath.endsWith(System.getProperty("file.separator"))) installationPath = installationPath.concat(System.getProperty("file.separator"));
			 
			  //add this when developing inside Eclipse
			  // installationPath = installationPath.concat("update"+System.getProperty("file.separator"));
			  while(entries.hasMoreElements()){
				  ZipEntry entry = (ZipEntry)entries.nextElement();
				  if(entry.isDirectory()) {
					  if(!(new File(installationPath+entry.getName())).exists()){
						  Files.createDirectories(Paths.get((installationPath+entry.getName())));
					  }
				  }
				  else{
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
			  cb.updateHasFinished();
			  System.out.println("Delete update file: " +currentUpdateFile.delete());
		  }	  
	  }
  }
  
  public EpisimUpdateState checkForUpdates(){
	  readUpdateMetadata();
	  int newVersion = Integer.parseInt(mostCurrentVersion.trim().replace(".", ""));
	  int currentVersion = Integer.parseInt(EpisimSimulator.versionID.trim().replace(".", ""));
	  int minOldVersion = Integer.parseInt(this.minOldVersion.trim().replace(".", ""));
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
  

  public void disconnect() throws IOException {
    if (ftpClient != null) {     
      ftpClient.close();      
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

  

  private void readUpdateMetadata() {   
    try {
	      long size =  ftpClient.getSize("./"+UPDATE_META_DATA_FILE);
	      		
	      if (size > 0) {  
		      InputStream in = ftpClient.getFileStream("./"+UPDATE_META_DATA_FILE);
		      Properties updateProp = new Properties();
		      updateProp.load(in);
		      mostCurrentVersion= updateProp.getProperty(CURRENT_VERSION);
		      minOldVersion= updateProp.getProperty(MIN_OLD_VERSION);
		      updateFile = updateProp.getProperty(UPDATEFILE);   
	      }
    } catch (Exception ex) {
	      ExceptionDisplayer.getInstance().displayException(ex);
	 }
  }  
  public void restartApplication() throws IOException, URISyntaxException{
	  String episimPath = ProjectLocator.getBinPath().getParentFile().getAbsolutePath();
	  if(!episimPath.endsWith(System.getProperty("file.separator"))) episimPath = episimPath.concat(System.getProperty("file.separator"));	  
	  
	  StringBuilder cmd = new StringBuilder();
     cmd.append(episimPath + "jre" + File.separator + "bin" + File.separator + "java -jar ");
     for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
        if(!jvmArg.contains("Simulator.jar")) cmd.append(jvmArg + " ");
     }
    // cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
     cmd.append(episimPath + "bin" + File.separator + "Simulator.jar");
     System.out.println(cmd.toString());
     Runtime.getRuntime().exec(cmd.toString());     
     System.exit(0);
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

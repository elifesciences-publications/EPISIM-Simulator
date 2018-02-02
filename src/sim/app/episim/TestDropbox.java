package sim.app.episim;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v1.DbxUrlWithExpiration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Properties;

import java.io.File;
import java.io.FileOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.net.ftp.FTPFile;

import sim.app.episim.EpisimUpdater.EpisimUpdateCallback;

public class TestDropbox {
	private DbxClientV2 dbxClient;
	
	private static int BUFFER_SIZE = 8192;
	public enum EpisimUpdateState{ NOTAVAILABLE, NOTPOSSIBLE, POSSIBLE};
	  
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
	
	public void start() throws DbxException, IOException{
		connect();
		readUpdateMetadata();
		downloadUpdate(new EpisimUpdateCallback() {
			int sum = 0;
			@Override
			public void updateHasFinished() {
			
				System.out.println("Sum: "+sum);
				
			}
			
			@Override
			public void sizeOfUpdate(int size) {
				
				System.out.println("Update size: "+size);
				
			}
			
			
			public void progressOfUpdate(int progress) {
				sum +=progress;
				System.out.println("Progress: "+progress);
			}
		}, false);
	}
	
	private  void connect(){	 
      EpisimLogger.getInstance().logInfo("Connecting to EPISIM update Server ");    

      DbxRequestConfig config = new DbxRequestConfig("EPISIM/5.2", Locale.getDefault().toString());
      dbxClient = new DbxClientV2(config, "6KOB1DFCefAAAAAAAAABMzE7I7Nmbyd4pFbkatroI3ZywC_l0fOElo_VqR-vLpdo");     
      EpisimLogger.getInstance().logInfo("Connection Successful");	   
     
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
	
	public void downloadUpdate(EpisimUpdateCallback cb, boolean log) throws IOException, DbxException{
		  connect();
		  if (dbxClient != null && cb != null) {
			  byte[] buffer = new byte[BUFFER_SIZE];  
			  
			  if(log)EpisimLogger.getInstance().logInfo("Read EPISIM update metadata.");
			  readUpdateMetadata();
			  
			  long size = 0;
			  FileMetadata fileMetaData = (FileMetadata)dbxClient.files().getMetadata(ROOT_DIR+"/"+UPDATE_META_DATA_FILE);
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
	
	private void disconnect(){}
	public static void main(String[] args)throws IOException, DbxException{
		new TestDropbox().start();
		 /*
	   System.out.println("Linked account: " + client.getAccountInfo().displayName);
	   DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
      System.out.println("Files in the root path:");
      for (DbxEntry child : listing.children) {
          System.out.println("	" + child.name + ": " + child.toString());
      }*/
	}
}

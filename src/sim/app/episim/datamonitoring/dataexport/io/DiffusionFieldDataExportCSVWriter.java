package sim.app.episim.datamonitoring.dataexport.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import episiminterfaces.EpisimDiffusionFieldConfiguration;

import sim.SimStateServer;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;

public class DiffusionFieldDataExportCSVWriter implements SimulationStateChangeListener{
	
	private BufferedWriter csvWriter;	
	private File csvFile;
		
	private boolean firstTime = true;
	
	private String diffusionFieldName="";
	
	public DiffusionFieldDataExportCSVWriter(File csvFile, String diffusionFieldName){
		this.csvFile = csvFile;
		this.diffusionFieldName = diffusionFieldName;
	}
	
	public void writeDiffusionFieldToDisk(){
		ExtraCellularDiffusionField2D diffusionField = ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(this.diffusionFieldName);
		if(diffusionField != null){
			if(firstTime){
				writeHeader();
				firstTime = false;
			}
			try{
				
				csvWriter.write("\n\nSimulation Step No. "+ SimStateServer.getInstance().getSimStepNumber()+";\n");
				EpisimDiffusionFieldConfiguration diffFieldConfig = diffusionField.getFieldConfiguration();
				int width = diffusionField.getExtraCellularField().getWidth();
				int height = diffusionField.getExtraCellularField().getHeight();
				double latticeSideLength = diffFieldConfig.getLatticeSiteSizeInMikron();
				csvWriter.write("Y (µm) / X (µm);");
				for(double i = 0; i< width; i++) csvWriter.write(""+(i*latticeSideLength)+";");
				csvWriter.write("\n");
				for(double y = 0; y < height; y++){
					for(double x = 0; x < width; x++){
						if(x==0d) csvWriter.write(""+(y*latticeSideLength)+";");
						csvWriter.write(""+diffusionField.getExtraCellularField().get((int)x, (int)y)+";");
					}
					csvWriter.write("\n");
				}
			}
			catch (IOException e){
		     ExceptionDisplayer.getInstance().displayException(e);
		   }
		}
	}
	
	
	
	private void writeHeader(){
		try{			
			if(csvWriter != null){				
				csvWriter.write("Episim Simulation Run on " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(new Date())+";\n\n");
				csvWriter.flush();
         }	
		}
	   catch (IOException e){
	     ExceptionDisplayer.getInstance().displayException(e);
	   }		
	}
	public void simulationWasStopped(){		  
	     if(csvWriter != null){
	   	  Thread t = null;
	   	  Runnable r = new Runnable(){

				public void run() {
					try{
						Thread.sleep(500);
	               csvWriter.close();
             }
             catch (IOException e){
             	ExceptionDisplayer.getInstance().displayException(e);
             }
             catch (InterruptedException e){
             	ExceptionDisplayer.getInstance().displayException(e);
             }	            
          }};
          t = new Thread(r);
          t.start();
	     }  
	}
	
	public void simulationWasStarted() {
		try{
			csvWriter = new BufferedWriter(new FileWriter(csvFile, true));
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		firstTime = true;	   
   }
	
	public void simulationWasPaused() {}

}

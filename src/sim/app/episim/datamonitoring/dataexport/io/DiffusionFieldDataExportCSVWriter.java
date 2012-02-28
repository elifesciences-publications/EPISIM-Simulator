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
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;

public class DiffusionFieldDataExportCSVWriter implements SimulationStateChangeListener{
	
	private BufferedWriter csvWriter;	
	private File csvFile;
		
	private boolean firstTime = true;
	
	private String diffusionFieldName="";
	
	private String name = "";
	private String description = "";
	
	public DiffusionFieldDataExportCSVWriter(File csvFile, String diffusionFieldName){
		this.csvFile = csvFile;
		this.diffusionFieldName = diffusionFieldName;
	}
	
	public DiffusionFieldDataExportCSVWriter(File csvFile, String diffusionFieldName, String name, String description){
		this.csvFile = csvFile;
		this.diffusionFieldName = diffusionFieldName;
		this.name = name;
		this.description = description;
	}
	
	public void writeDiffusionFieldToDisk(){
		ExtraCellularDiffusionField diffusionField = ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(this.diffusionFieldName);
		if(diffusionField != null){
			if(firstTime){
				writeHeader();
				firstTime = false;
			}
			if(diffusionField instanceof ExtraCellularDiffusionField2D)writeDiffusionField2D((ExtraCellularDiffusionField2D) diffusionField);
			if(diffusionField instanceof ExtraCellularDiffusionField3D)writeDiffusionField3D((ExtraCellularDiffusionField3D) diffusionField);
		}			
	}
	
	private void writeDiffusionField2D(ExtraCellularDiffusionField2D diffusionField){
		try{			
			csvWriter.write("\n\nSimulation Step No. "+ SimStateServer.getInstance().getSimStepNumber()+";\n");
			EpisimDiffusionFieldConfiguration diffFieldConfig = diffusionField.getFieldConfiguration();
			int width = diffusionField.getExtraCellularField().getWidth();
			int height = diffusionField.getExtraCellularField().getHeight();
			double latticeSideLength = diffFieldConfig.getLatticeSiteSizeInMikron();
			csvWriter.write("Y (�m) / X (�m);");
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
	
	private void writeDiffusionField3D(ExtraCellularDiffusionField3D diffusionField){
		try{			
			csvWriter.write("\n\nSimulation Step No. "+ SimStateServer.getInstance().getSimStepNumber()+";\n");
			EpisimDiffusionFieldConfiguration diffFieldConfig = diffusionField.getFieldConfiguration();
			int width = diffusionField.getExtraCellularField().getWidth();
			int height = diffusionField.getExtraCellularField().getHeight();
			int length = diffusionField.getExtraCellularField().getHeight();
			double latticeSideLength = diffFieldConfig.getLatticeSiteSizeInMikron();
			for(double z = 0; z < length; z++){
				csvWriter.write("\nZ (�m):;"+(z*latticeSideLength)+"\n");
				csvWriter.write("Y (�m) / X (�m);");
				for(double i = 0; i< width; i++) csvWriter.write(""+(i*latticeSideLength)+";");
				csvWriter.write("\n");
				for(double y = 0; y < height; y++){
					for(double x = 0; x < width; x++){
						if(x==0d) csvWriter.write(""+(y*latticeSideLength)+";");
						csvWriter.write(""+diffusionField.getExtraCellularField().get((int)x, (int)y, (int)z)+";");
					}
					csvWriter.write("\n");
				}
			}
		}
		catch (IOException e){
	     ExceptionDisplayer.getInstance().displayException(e);
	   }		
	}
	
	
	
	private void writeHeader(){
		try{			
			if(csvWriter != null){				
				csvWriter.write("Episim Simulation Run on " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(new Date())+";\n\n");
				csvWriter.write("Data-Export-Name:;\n" + name +";\n");
				csvWriter.write("Data-Export-Description:;\n" + description +";\n");
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

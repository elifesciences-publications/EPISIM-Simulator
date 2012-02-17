package sim.app.episim.datamonitoring.dataexport.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.datamonitoring.dataexport.ValueMapListener;
import sim.app.episim.util.ObservedDataCollection;
import sim.app.episim.util.ObservedDataCollection.ObservedDataCollectionType;
import episiminterfaces.*;
import episiminterfaces.calc.EntityChangeEvent;
import episiminterfaces.calc.EntityChangeEvent.EntityChangeEventType;

public class DataExportCSVWriter implements SimulationStateChangeListener{
	

	
	private BufferedWriter csvWriter;
	
	private HashMap<Long, Integer> indexLookUp = new HashMap<Long, Integer>();
	
	private Set<Long> alreadyCalculatedColumnsIds = new HashSet<Long>();
	
	private Double[][] values;
	
	private Vector<Double>[] columnValues;
	
	private boolean firstTime = true;
	
	private int actIndex = 0;
	
	private String columnNames;
	
	private File csvFile;
	
	private boolean aCellHasBeenChanged = false;
	
	private long simStepCounter = 0;
	
	private long lastSimStepCounterWritten = -1;
	
	
	
	public DataExportCSVWriter(File csvFile, String columnNames) {		
		this.csvFile = csvFile;		
		this.columnNames = columnNames;
	}
	
	
	
	public void registerObservedDataCollection(final long columnId, ObservedDataCollection<Double> map){
		indexLookUp.put(columnId, actIndex);
		actIndex++;
		map.addValueMapListener(new ValueMapListener<Double>(){
			public void valueAdded(Double key, Double value) {				
		         values[indexLookUp.get(columnId)][0] = key;
		         values[indexLookUp.get(columnId)][1] = value;
		         alreadyCalculatedColumnsIds.add(columnId);
		         checkIfDataWriteToDisk();
		   }

			public void valueAdded(Double value) {				
		         values[indexLookUp.get(columnId)][0] = Double.NEGATIVE_INFINITY;
		         values[indexLookUp.get(columnId)][1] = value;
		         alreadyCalculatedColumnsIds.add(columnId);
		         checkIfDataWriteToDisk();
         }
			
			public void observedDataSourceChanged(EntityChangeEvent event){
				if(event.getEventType() == EntityChangeEventType.CELLCHANGE) aCellHasBeenChanged = true;
				//if(event.getEventType() == EntityChangeEventType.SIMULATIONSTEPCHANGE) simStepCounter++;
			}
			public void simStepChanged(long simStep){
				if(simStep > simStepCounter){
					simStepCounter = simStep;
				}
			}
			public void valueAdded(Vector<Double> value) {
				columnValues[indexLookUp.get(columnId)] = value;
				alreadyCalculatedColumnsIds.add(columnId);
				checkIfDataWriteToDisk();
			}
		});
		if(map.getType() == ObservedDataCollectionType.MULTIDIMTYPE) columnValues = new Vector[actIndex];
		else values = new Double[actIndex][2];
		
	}
	
	private void checkIfDataWriteToDisk(){
				
		if(firstTime){
			writeHeader();
			writeColumnNames();
			firstTime = false;
		}
		if((values != null && this.alreadyCalculatedColumnsIds.size() == values.length)
				|| (columnValues != null && this.alreadyCalculatedColumnsIds.size() == columnValues.length)){
			try{
				if(simStepCounter > lastSimStepCounterWritten){
					csvWriter.write(simStepCounter+";");
					lastSimStepCounterWritten = simStepCounter;
				}
				else csvWriter.write(";");
				if(values != null){
					for(int i = 0; i < values.length; i++){					
							if(values[i][0] != Double.NEGATIVE_INFINITY) csvWriter.write(getFormattedValue(values[i][0]) + ";");											 
							csvWriter.write(getFormattedValue(values[i][1]) + ";");													
					}
				}
				if(columnValues != null){
					int maxVectorSize = getLargestVectorSize(columnValues);
					for(int n = 0; n < maxVectorSize; n++){
						if(n>0)csvWriter.write(";");
						for(int i = 0; i < columnValues.length; i++){
							csvWriter.write((columnValues[i]!= null && n < columnValues[i].size())? (getFormattedValue(columnValues[i].get(n)) + ";"):";");
						}
						csvWriter.write("\n");
					}					
				}
				if(aCellHasBeenChanged){
					csvWriter.write("(A Cell was changed);");
					aCellHasBeenChanged = false;
				}
				csvWriter.write("\n");
				csvWriter.flush();
				alreadyCalculatedColumnsIds.clear();
			}
			catch(IOException ex){
				ExceptionDisplayer.getInstance().displayException(ex);
			}
		}
	}
	
	private int getLargestVectorSize(Vector[] vectors){
		int maxVectorSize = Integer.MIN_VALUE;
		for(int i = 0; i < vectors.length; i++){
			if(vectors[i]!= null && vectors[i].size() > maxVectorSize) maxVectorSize = vectors[i].size();
		}
		return maxVectorSize;
	}
	
	private void writeColumnNames(){
		try{			
		  if(csvWriter != null){					
				csvWriter.write("sim step no;");
				csvWriter.write(columnNames);
				csvWriter.flush();
	     }	
		}
      catch (IOException e){
        ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	private void writeHeader(){
		try{			
			if(csvWriter != null){				
				csvWriter.write("Episim Simulation Run on " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(new Date())+";\n");
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
	     aCellHasBeenChanged = false;
	     simStepCounter = 0;
	    lastSimStepCounterWritten=-1;
      
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
	
	private String getFormattedValue(double value){
		//localisation leads to unnecessary errors
	//	if(Locale.getDefault() == Locale.GERMAN || Locale.getDefault() == Locale.GERMANY) return (new Double (value)).toString().replace('.', ',');
	//	else return ""+ value;
		
		return "" +value;
	}



	public void simulationWasPaused() {}

}

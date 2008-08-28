package sim.app.episim.datamonitoring.dataexport.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.dataexport.ObservedHashMap;
import sim.app.episim.datamonitoring.dataexport.ValueMapListener;
import episiminterfaces.*;

public class DataExportCSVWriter{
	

	
	private BufferedWriter csvWriter;
	
	private HashMap<Long, Integer> indexLookUp = new HashMap<Long, Integer>();
	
	private Set<Long> alreadyCalculatedColumnsIds = new HashSet<Long>();
	
	private Double[][] values;
	
	private boolean firstTime = true;
	
	private int actIndex = 0;
	
	private String columnNames;
	
	public DataExportCSVWriter(File csvFile, String columnNames) {

		try{
			csvWriter = new BufferedWriter(new FileWriter(csvFile, true));
		}
		catch (IOException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
		this.columnNames = columnNames;
	}
	
	public void simulationWasStopped(){
		// csvWriter
	}
	
	public void registerObservedHashMap(final long columnId, ObservedHashMap<Double, Double> map){
		indexLookUp.put(columnId, actIndex);
		actIndex++;
		map.addValueMapListener(new ValueMapListener<Double, Double>(){
			public void valueAdded(Double key, Double value) {
	         values[indexLookUp.get(columnId)][0] = key;
	         values[indexLookUp.get(columnId)][1] = value;
	         alreadyCalculatedColumnsIds.add(columnId);
	         checkIfDataWriteToDisk();
         }
		});
		values = new Double[actIndex][2];
	}
	
	private void checkIfDataWriteToDisk(){
		if(firstTime){
			writeColumnNames();
			firstTime = false;
		}
		if(this.alreadyCalculatedColumnsIds.size() == values.length){
			try{
				for(int i = 0; i < values.length; i++){
					if(values[i][0] != Double.NEGATIVE_INFINITY) csvWriter.write(NumberFormat.getInstance(Locale.getDefault()).format(values[i][0]) + ";");
					csvWriter.write(NumberFormat.getInstance(Locale.getDefault()).format(values[i][1]) + ";");
										
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
	
	private void writeColumnNames(){
		try{
			
				if(csvWriter != null){
					
					csvWriter.write(columnNames);
					csvWriter.flush();
	         }
		
			
		}
      catch (IOException e){
        ExceptionDisplayer.getInstance().displayException(e);
      }
	}
		

}

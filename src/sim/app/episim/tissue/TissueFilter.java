package sim.app.episim.tissue;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.text.DateFormatter;
import javax.swing.text.NumberFormatter;

import sim.app.episim.model.visualization.CellEllipse;


public class TissueFilter {
	private File outputPath;
	private StringBuffer resultsBuffer;
	private int filteredCellsCounter = 0;
	public TissueFilter(File outputPath){
		this.outputPath = outputPath;
		resultsBuffer = new StringBuffer();
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.GERMANY);
		DateFormatter dformat = new DateFormatter(df);
		try{
			long time = System.currentTimeMillis();
	      resultsBuffer.append("----- Tissue Filtering Results ("+dformat.valueToString(time)+") ------\n\n");
      }
      catch (ParseException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
	}
	
	public void filterTissue(ImportedTissue importedTissue){
		filteredCellsCounter = 0;
		resultsBuffer.append("Filtering tissue with tissue image id: "+importedTissue.getTissueImageID()+"\n");
		filterCellCellContainments(importedTissue);
		resultsBuffer.append("\nTotal number of filtered Cells: " +filteredCellsCounter+"\n\n");
		try{
	      writeFilterResults(resultsBuffer.toString());
      }
      catch (IOException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
	}
	
	
	private void filterCellCellContainments(ImportedTissue importedTissue){
		
		resultsBuffer.append("Filtered Cell-Cell-Containments: \n");
		
		CellEllipse[] cellEllipses = importedTissue.getCells().toArray(new CellEllipse[importedTissue.getCells().size()]);
		for(int i = 0; i < cellEllipses.length; i++){
			CellEllipse ellipse1 = cellEllipses[i];
			for(int n=0; n < cellEllipses.length && ellipse1!=null ; n++){
				CellEllipse ellipse2 = cellEllipses[n];
				if(ellipse1 != null && ellipse1.getId()!=ellipse2.getId()){
					
					if(ellipse2.getEllipse().contains(ellipse1.getEllipse().getBounds2D())){
					/*	Rectangle2D boundsEllipse1 = ellipse1.getEllipse().getBounds2D();
						Rectangle2D boundsEllipse2 = ellipse2.getEllipse().getBounds2D();*/
						CellEllipse filteredCell = null;
					/*	if((boundsEllipse1.getWidth()*boundsEllipse1.getHeight())> (boundsEllipse2.getWidth()*boundsEllipse2.getHeight())){
							filteredCell = ellipse2;
						}
						else{
							filteredCell = ellipse1;
							ellipse1=null;
						}*/
						filteredCell = ellipse1;
						if(filteredCell != null && importedTissue.getCells().remove(filteredCell)){
							resultsBuffer.append("Cell-Id: "+filteredCell.getId()+"\n");
							filteredCellsCounter++;
						}
					}
				}
			}
		}
		
	}
	
	private void writeFilterResults(String results) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, true));
		writer.write(results);
		writer.flush();
		writer.close();
		
	}

}

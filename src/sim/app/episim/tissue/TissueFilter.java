package sim.app.episim.tissue;

import java.awt.Graphics2D;
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

import sim.app.episim.model.visualization.AbstractCellEllipse;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.IntersectionPoints;


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
		resultsBuffer.append("Filtering tissue with tissue id: "+importedTissue.getTissueID()+"\n");
		filterCellCellContainments(importedTissue);
		filterCentroidCellCellContainments(importedTissue);
		filterVerySmallCellsAfterClippingContainments(importedTissue);
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
		resultsBuffer.append("\nFiltered full Cell-Cell-Containments: \n");
		
		CellEllipse[] cellEllipses = importedTissue.getCells().toArray(new CellEllipse[importedTissue.getCells().size()]);
		for(int i = 0; i < cellEllipses.length; i++){
			CellEllipse ellipse1 = cellEllipses[i];
			for(int n=0; n < cellEllipses.length && ellipse1!=null ; n++){
				CellEllipse ellipse2 = cellEllipses[n];
				if(ellipse1 != null && ellipse1.getId()!=ellipse2.getId()){
					
					if(ellipse2.getEllipse().contains(ellipse1.getEllipse().getBounds2D())){
						handleFilteredCell(ellipse1, importedTissue);
						ellipse1=null;						
					}
				}
			}
		}		
	}
	private void filterCentroidCellCellContainments(ImportedTissue importedTissue){		
		resultsBuffer.append("\nFiltered Centroid-Cell-Cell-Containments after clipping: \n");
		CellEllipseIntersectionCalculationRegistry.getInstance().reset();
		for(CellEllipse ell : importedTissue.getCells()){
			ell.resetClippedEllipse();
		}
		calculateIntersectionPointsForCellEllipses(importedTissue);
		CellEllipse[] cellEllipses = importedTissue.getCells().toArray(new CellEllipse[importedTissue.getCells().size()]);
		for(int i = 0; i < cellEllipses.length; i++){
			CellEllipse ellipse1 = cellEllipses[i];
			for(int n=0; n < cellEllipses.length && ellipse1!=null ; n++){
				CellEllipse ellipse2 = cellEllipses[n];
				if(ellipse1 != null && ellipse1.getId()!=ellipse2.getId()){
					
					if(ellipse2.getClippedEllipse().contains(ellipse1.getX(), ellipse1.getY())){
						Rectangle2D boundsEllipse1 = ellipse1.getClippedEllipse().getBounds2D();
						Rectangle2D boundsEllipse2 = ellipse2.getClippedEllipse().getBounds2D();
					
						if((boundsEllipse1.getWidth()*boundsEllipse1.getHeight())< (boundsEllipse2.getWidth()*boundsEllipse2.getHeight())){
							handleFilteredCell(ellipse1, importedTissue);
							ellipse1=null;
						}				
					}
				}
			}
		}
		CellEllipseIntersectionCalculationRegistry.getInstance().reset();
	}
	
	private void filterVerySmallCellsAfterClippingContainments(ImportedTissue importedTissue){		
		resultsBuffer.append("\nFiltered very small cells after clipping: \n");
		CellEllipseIntersectionCalculationRegistry.getInstance().reset();
		final double percentage = 0.3;
		for(CellEllipse ell : importedTissue.getCells()){
			ell.resetClippedEllipse();
		}
		calculateIntersectionPointsForCellEllipses(importedTissue);
		CellEllipse[] cellEllipses = importedTissue.getCells().toArray(new CellEllipse[importedTissue.getCells().size()]);
		for(int i = 0; i < cellEllipses.length; i++){
			CellEllipse ellipse = cellEllipses[i];
			Rectangle2D ellipseBounds = ellipse.getEllipse().getBounds2D();
			Rectangle2D clippedEllipseBounds = ellipse.getClippedEllipse().getBounds2D();
			if((clippedEllipseBounds.getWidth()*clippedEllipseBounds.getHeight()) <((ellipseBounds.getWidth()*ellipseBounds.getHeight())*percentage)) 
				handleFilteredCell(ellipse, importedTissue);
		}
		CellEllipseIntersectionCalculationRegistry.getInstance().reset();
	}
	
	
	private void handleFilteredCell(AbstractCellEllipse filteredCell, ImportedTissue importedTissue){
		if(filteredCell != null && importedTissue.getCells().remove(filteredCell)){
			resultsBuffer.append("Cell-Id: "+filteredCell.getId()+"\n");
			filteredCellsCounter++;
		}
	}
	
	private void writeFilterResults(String results) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, true));
		writer.write(results);
		writer.flush();
		writer.close();
		
	}
	
	private void calculateIntersectionPointsForCellEllipses(ImportedTissue tissue){
		int numberOfCells = tissue.getCells().size();
		
		for(int n = 0; n < numberOfCells; n++){
			CellEllipse actEll = tissue.getCells().get(n);			
			for(int m = 0; m < numberOfCells; m++){
				if(n == m) continue;
				else{
					CellEllipse otherEll = tissue.getCells().get(m);
						
					if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(actEll.getId(), otherEll.getId(), 1)){
				   	CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(actEll.getId(), otherEll.getId());					   				
							EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(null ,actEll, otherEll);
						
					}					
				}
			}				
		}
	}

}

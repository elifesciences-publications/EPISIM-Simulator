package sim.app.episim.datamonitoring.build;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;

import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateServer;
import sim.app.episim.datamonitoring.charts.build.ChartSourceBuilder;
import sim.app.episim.datamonitoring.charts.io.PNGPrinter;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.util.Names;
import sim.app.episim.util.ProjectionPlane;
import sim.engine.SimState;
import episimexceptions.CellNotValidException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;


public abstract class SteppableCodeFactory {
	
	private static StringBuffer steppableCode;
	
	
	public enum SteppableType{CHART, DATAEXPORT}
	//returns something like new EnhancedSteppable(){...}
	public synchronized static String getEnhancedSteppableSourceCode(String nameOfCallBackList, double updatingFrequency, SteppableType type){
				
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		if(type == SteppableType.CHART){
			steppableCode.append("     	  SwingUtilities.invokeLater(new Runnable() {\n");
			steppableCode.append("     	  public void run() {\n");
		}		
		steppableCode.append("for(CalculationCallBack callBack: "+ nameOfCallBackList + ") callBack.calculate(SimStateServer.getInstance().getSimStepNumber());");		
		if(type == SteppableType.CHART){
			steppableCode.append("     	    }\n");
			steppableCode.append("     	  });\n");
		}
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");

		if(type == SteppableType.CHART){
			steppableCode.append("return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ)== null"+ 
					"|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ)) <= 0 ?" +updatingFrequency +":" +
					"Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ));\n");
		}
		else if(type == SteppableType.DATAEXPORT){
			steppableCode.append("return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTUPDATEFREQ)== null"+ 
					"|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTUPDATEFREQ)) <= 0 ? " +updatingFrequency +" :" +
					"Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTUPDATEFREQ));\n");
		}
		
	
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
	}
	
	public synchronized static String getEnhancedSteppableSourceCode(EpisimCellVisualizationChart episimChart){
		
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		steppableCode.append("   final XYPlot xyPlot = (XYPlot) "+ChartSourceBuilder.CHARTDATAFIELDNAME+".getXYPlot();\n");
		steppableCode.append("     	  SwingUtilities.invokeLater(new Runnable() {\n");
		steppableCode.append("     	  public void run() {\n");
		steppableCode.append("           xyPlot.clearAnnotations();\n");		
	//	steppableCode.append("           long start = System.currentTimeMillis();\n");
		steppableCode.append("           for(AbstractCell actCell : allCells){\n");
		steppableCode.append("   	       final EpisimBiomechanicalModel bmModel = actCell.getEpisimBioMechanicalModelObject();\n");
		
		steppableCode.append("      	  final CellBoundaries cb = bmModel.getCellBoundariesInMikron(0);\n");
		steppableCode.append("     	  final double width = cb.getMaxXInMikron()-cb.getMinXInMikron();\n");
		steppableCode.append("      	  final double height = cb.getMaxYInMikron()-cb.getMinYInMikron();\n");
		steppableCode.append("      	  final double length = cb.getMaxZInMikron()-cb.getMinZInMikron();\n");
		if(episimChart.getMinXMikron() > Double.NEGATIVE_INFINITY
			|| episimChart.getMinYMikron() > Double.NEGATIVE_INFINITY
			|| episimChart.getMinZMikron() > Double.NEGATIVE_INFINITY
			|| episimChart.getMaxXMikron() < Double.POSITIVE_INFINITY
			|| episimChart.getMaxYMikron() < Double.POSITIVE_INFINITY
			|| episimChart.getMaxZMikron() < Double.POSITIVE_INFINITY){
			steppableCode.append("   	  if(");
			boolean alreadyConditionDefined = false;
			if(episimChart.getMinXMikron() > Double.NEGATIVE_INFINITY){
				steppableCode.append("(bmModel.getX()+(width/2))>= "+episimChart.getMinXMikron());
				alreadyConditionDefined=true;
			}
			if(episimChart.getMaxXMikron() < Double.POSITIVE_INFINITY){
				if(alreadyConditionDefined) steppableCode.append(" && ");
				steppableCode.append("(bmModel.getX()-(width/2))<= "+episimChart.getMaxXMikron());
				alreadyConditionDefined=true;
			}
			if(episimChart.getMinYMikron() > Double.NEGATIVE_INFINITY){
				if(alreadyConditionDefined) steppableCode.append(" && ");
				steppableCode.append("(bmModel.getY()+(height/2))>= "+episimChart.getMinYMikron());
				alreadyConditionDefined=true;
			}
			if(episimChart.getMaxYMikron() < Double.POSITIVE_INFINITY){
				if(alreadyConditionDefined) steppableCode.append(" && ");
				steppableCode.append("(bmModel.getY()-(height/2))<= " + episimChart.getMaxYMikron());
				alreadyConditionDefined=true;
			}
			if(episimChart.getMinZMikron() > Double.NEGATIVE_INFINITY){
				if(alreadyConditionDefined) steppableCode.append(" && ");
				steppableCode.append("(bmModel.getZ()+(length/2))>= "+episimChart.getMinZMikron());
				alreadyConditionDefined=true;
			}
			if(episimChart.getMaxZMikron() < Double.POSITIVE_INFINITY){
				if(alreadyConditionDefined) steppableCode.append(" && ");
				steppableCode.append("(bmModel.getZ()-(length/2))<= "+episimChart.getMaxZMikron());
				alreadyConditionDefined=true;
			}
			steppableCode.append("){\n");	
		}
		
		steppableCode.append("      	  final AbstractCell theCell = actCell;\n");
		steppableCode.append("            try{\n");
		
		if(episimChart.getCellProjectionPlane() == ProjectionPlane.XY_PLANE){
			steppableCode.append("     	  xyPlot.addAnnotation(new XYShapeAnnotation(new Ellipse2D.Double(bmModel.getX()-(width/2), bmModel.getY()-(height/2), width, height), new BasicStroke(1), Color.black, getCellColoring(theCell)));\n");
		}
		else if(episimChart.getCellProjectionPlane() == ProjectionPlane.XZ_PLANE){
			steppableCode.append("     	  xyPlot.addAnnotation(new XYShapeAnnotation(new Ellipse2D.Double(bmModel.getX()-(width/2), bmModel.getZ()-(length/2), width, length), new BasicStroke(1), Color.black, getCellColoring(theCell)));\n");
		}
		else if(episimChart.getCellProjectionPlane() == ProjectionPlane.YZ_PLANE){
			steppableCode.append("     	  xyPlot.addAnnotation(new XYShapeAnnotation(new Ellipse2D.Double(bmModel.getZ()-(length/2), bmModel.getY()-(height/2), length, height), new BasicStroke(1), Color.black, getCellColoring(theCell)));\n");
		}	  
		steppableCode.append("           }\n");
		steppableCode.append("           catch (CellNotValidException e){\n");
		steppableCode.append("              ExceptionDisplayer.getInstance().displayException(e);\n");
		steppableCode.append("           }\n");
		
		if(episimChart.getMinXMikron() > Double.NEGATIVE_INFINITY
				|| episimChart.getMinYMikron() > Double.NEGATIVE_INFINITY
				|| episimChart.getMinZMikron() > Double.NEGATIVE_INFINITY
				|| episimChart.getMaxXMikron() < Double.POSITIVE_INFINITY
				|| episimChart.getMaxYMikron() < Double.POSITIVE_INFINITY
				|| episimChart.getMaxZMikron() < Double.POSITIVE_INFINITY){
		steppableCode.append("   }\n");
		}	
		steppableCode.append("}\n");	
		//steppableCode.append("           long end = System.currentTimeMillis();\n");
		//steppableCode.append("          System.out.println(\"Dauer: \"+(end-start));\n");
		steppableCode.append("     	    }\n");
		steppableCode.append("     	  });\n");
		
		
		
		steppableCode.append("}\n");
		steppableCode.append("public double getInterval(){\n");
		steppableCode.append("   return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ)== null"+ 
					"|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ)) <= 0 ?" +episimChart.getChartUpdatingFrequency() +":" +
					"Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTUPDATEFREQ));\n");		
	
		steppableCode.append("}\n");
		steppableCode.append("}\n");
		
		return steppableCode.toString();
	}
	
	
	public synchronized static String getEnhancedSteppableForPNGPrinting(EpisimChart chart){
		if(chart.isPNGPrintingEnabled()) return getEnhancedSteppableForPNGPrinting(chart.getId(), chart.getTitle(), chart.getPNGPrintingPath(), chart.getPNGPrintingFrequency(), Double.POSITIVE_INFINITY);		
		return getEnhancedSteppableForPNGPrinting(chart.getId(), Names.cleanString(chart.getTitle()), null, chart.getPNGPrintingFrequency(), Double.POSITIVE_INFINITY);
	}
	
	public synchronized static String getEnhancedSteppableForPNGPrinting(EpisimCellVisualizationChart chart, double widthToHeightScale){		
		if(chart.isPNGPrintingEnabled()) return getEnhancedSteppableForPNGPrinting(chart.getId(), chart.getTitle(), chart.getPNGPrintingPath(), chart.getPNGPrintingFrequency(), widthToHeightScale);		
		return getEnhancedSteppableForPNGPrinting(chart.getId(), Names.cleanString(chart.getTitle()), null, chart.getPNGPrintingFrequency(), widthToHeightScale);		
	}
			
	private synchronized static String getEnhancedSteppableForPNGPrinting(long id, String title, File pngFilePath, int printFrequency, double widthToHeightScale){
		
		steppableCode = new StringBuffer();
		steppableCode.append("new EnhancedSteppable(){\n");
		
		steppableCode.append("public void step(SimState state){\n");
		steppableCode.append("    final SimState _state = state;\n");
		steppableCode.append("  SwingUtilities.invokeLater(new Runnable(){\n");
		steppableCode.append("    public void run() {\n");
		
					
            
			
			steppableCode.append("if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null){");
			steppableCode.append("  PNGPrinter.getInstance().printChartAsPng("+ id+"l, "+
					                  "null, "+
					                  "\""+ (title == null || title.length()==0 ? "EpisimChartPNG":title) +"\", "+ChartSourceBuilder.CHARTDATAFIELDNAME+", _state);\n");
			steppableCode.append("}\n");
			steppableCode.append("else{");
			if(pngFilePath!=null){
				if(widthToHeightScale < Double.POSITIVE_INFINITY){
					steppableCode.append("  PNGPrinter.getInstance().printChartAsPng("+ id +"l, "+
		               "new File(\""+ pngFilePath.getAbsolutePath().replace(File.separatorChar, '/')+"\"), "+
		               "\""+ title +"\", "+ChartSourceBuilder.CHARTDATAFIELDNAME+", _state,"+widthToHeightScale+");\n");
				}
				else{
					steppableCode.append("  PNGPrinter.getInstance().printChartAsPng("+ id +"l, "+
		               "new File(\""+ pngFilePath.getAbsolutePath().replace(File.separatorChar, '/')+"\"), "+
		               "\""+ title +"\", "+ChartSourceBuilder.CHARTDATAFIELDNAME+", _state);\n");
				}
			}
			steppableCode.append("}\n");
			steppableCode.append("        }});\n");
			steppableCode.append("}\n");
			steppableCode.append("public double getInterval(){\n");
			steppableCode.append("if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null){");
			steppableCode.append("return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ)== null"+ 
					"|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ)) <= 0 ? 100 :" +
					"Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ));\n");
			steppableCode.append("}\n");
			steppableCode.append("else{");
			if(pngFilePath!=null)steppableCode.append("return " + printFrequency + ";\n");
			else steppableCode.append("return 1000;\n");
			steppableCode.append("}\n");
			steppableCode.append("}\n");			
		
		steppableCode.append("}\n");
		
		return steppableCode.toString();
		
	}
	
	
		
	
	

}

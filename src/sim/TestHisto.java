package sim;

import org.jfree.chart.*;

import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

import org.jfree.chart.axis.*;

import org.jfree.data.statistics.*;
import episiminterfaces.calc.*;
import episiminterfaces.monitoring.*;
import episiminterfaces.*;
import episimexceptions.*;
import episimfactories.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;

import sim.app.episim.util.*;
import sim.engine.Steppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.datamonitoring.calc.*;
import sim.app.episim.datamonitoring.charts.io.*;
import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.engine.SimState;

public class TestHisto implements GeneratedChart{
  private EnhancedSteppable steppable;
  private ArrayList<CalculationCallBack> calculationCallbacks = new ArrayList<CalculationCallBack>();
  private EnhancedSteppable pngSteppable = null;
  private JFreeChart chart;
  private GenericBag<AbstractCell> allCells;
  private ChartPanel chartPanel;
  private SimpleHistogramDataset noofneighbours1317375085810 = new SimpleHistogramDataset("noofneighbours");
 
public TestHisto(){
  chart = ChartFactory.createHistogram("Number of Neighbours","no of neighbours","no of cells", null,PlotOrientation.VERTICAL, false, true, false);
  chart.setBackgroundPaint(Color.white);
  chart.setAntiAlias(true);
  chartPanel = new ChartPanel(chart, true);
  chartPanel.setPreferredSize(new java.awt.Dimension(640,480));
  chartPanel.setMinimumDrawHeight(10);
  chartPanel.setMaximumDrawHeight(2000);
  chartPanel.setMinimumDrawWidth(20);
  chartPanel.setMaximumDrawWidth(2000);

  XYPlot plot = chart.getXYPlot();
  
 
  XYBarRenderer renderer = null;
  plot.setDataset(0, noofneighbours1317375085810);
  renderer = new XYBarRenderer();
 
  renderer.setSeriesPaint(0, new Color(204, 0, 0));
  for(SimpleHistogramBin bin: buildBins(0.0, 6.0,7)) noofneighbours1317375085810.addBin(bin);
noofneighbours1317375085810.setAdjustForBinSize(false);  plot.setRenderer(0, renderer);
calculationCallbacks.add(CalculationController.getInstance().registerAtCalculationAlgorithm(new CalculationHandler(){
  private Map<String, Object> params;
  {
params = new HashMap<String, Object>();
params.put("number of bins", 7);
params.put("max value", 6.0);
params.put("min value", 0.0);
  }
  public long getID(){ return 1317375831738l; }
  public long getCorrespondingBaselineCalculationHandlerID(){ return -9223372036854775808l; }
  public int getCalculationAlgorithmID(){ return -1272873399; }
  public Map<String, Object> getParameters(){ return params; }
  public boolean isBaselineValue(){ return false; }
  public Class<? extends AbstractCell> getRequiredCellType(){
    return null;
}
  private boolean isValidCell(AbstractCell cellType){
    return true;
  }
  public double calculate(AbstractCell cellTypeLocal) throws CellNotValidException{
   return 0;


  }
  public boolean conditionFulfilled(AbstractCell cellTypeLocal) throws CellNotValidException{
return true;  }
}
, noofneighbours1317375085810, false, false));
steppable = new EnhancedSteppable(){
public void step(SimState state){
for(CalculationCallBack callBack: calculationCallbacks) callBack.calculate(state.schedule.getSteps());}
public double getInterval(){
return 1.0;
}
}
;
pngSteppable = new EnhancedSteppable(){
public void step(SimState state){
if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON)&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null){  PNGPrinter.getInstance().printChartAsPng(1317375083532l, null, "Number of Neighbours", chart, state);
}
else{}
}
public double getInterval(){
if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON)&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null){return EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ)== null|| Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ)) <= 0 ? 100 :Integer.parseInt(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTFREQ));
}
else{return 1000;
}
}
}
;
}
public EnhancedSteppable getSteppable(){return steppable;}
public ChartPanel getChartPanel(){ return this.chartPanel;}
public EnhancedSteppable getPNGSteppable(){ return this.pngSteppable;}
  public void registerRequiredObjects(GenericBag<AbstractCell> allCells){
    this.allCells = allCells;
 }
  public void clearAllSeries(){
    noofneighbours1317375085810.clearObservations();
  }
private SimpleHistogramBin[] buildBins(double minValue, double maxValue, int numberOfBins){
  if(minValue > maxValue){
    double tmp = minValue;
    minValue = maxValue;
    maxValue = tmp;
  }
  if(minValue == maxValue)maxValue = (minValue + 1);
  if(numberOfBins < 0)numberOfBins = Math.abs(numberOfBins);
  if(numberOfBins == 0)numberOfBins = 1;
  double binSize = (Math.abs(maxValue - minValue)+1) / ((double)numberOfBins);
  SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins];
  for(int i = 0; i < numberOfBins; i ++){
    bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, false);
  }
  return bins;
}

public static void main(String[] args){
	TestHisto test = new TestHisto();
	JFrame frame = new JFrame();
	frame.setSize(new Dimension(300, 300));
	frame.getContentPane().add(test.getChartPanel(), BorderLayout.CENTER);
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}



}

package sim.app.episim;

//MASON
import sim.app.episim.charts.EpiSimCharts;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

//Charts
import org.jfree.chart.JFreeChart;

//PDF Writer + ELSE
import java.awt.*; 
import java.awt.geom.*; 

//PDF Writer
import java.io.*;       
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lowagie.text.*;  
import com.lowagie.text.pdf.*;  

public class Epidermis extends SimStateHack implements SnapshotListener
{

//	---------------------------------------------------------------------------------------------------------------------------------------------------
// CONSTANTS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
		
	public final int InitialKeratinoSize=5;
	public final int NextToOuterCell=7;
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
// VARIABLES
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	private transient BioChemicalModelController modelController;

	//get charts from Chart-Factory
	private  EpiSimCharts epiSimCharts = EpiSimCharts.getInstance();

	private boolean reloadedSnapshot = false;

	private  String graphicsDirectory="pdf_png_simres/";

	private Continuous2D cellContinous2D;
	private Continuous2D basementContinous2D;
   
	private Bag allCells=new Bag(3000); //all cells will be stored in this bag
	private int allocatedKCytes=0;   // allocated memory
	private int actualStem=0;        // Stem cells
	private int actualKCytes=0;      // num of kcytes that are not in nirvana
	private int actualSpi=0;         // Spinosum
	private int actualTA=0;          // TA Cells
	private int actualLateSpi=0;     // Late Spinosum
	private int actualGranu=0;       // num of Granulosum KCytes
	private int actualCorneum=0;       // num of Granulosum KCytes
	private int actualNoNucleus=0;   // Cells after lifetime but not shed from the surface
	private int actualBasalStatisticsCells=0;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	
	private  int PNG_ChartWidth=400;
	private  int PNG_ChartHeight=300;
	private  int PNG_ChartWidth_Large=600;
	private  int PNG_ChartHeight_Large=400;
	private  int PDF_ChartWidth_Large=600;
	private  int PDF_ChartHeight_Large=400;
	 
	private int gCorneumY=20;    // gCorneum would start at this ..
	 
	private double height = 150;
		
	private int individualColor=1;
	 
	private double gTimefactor=0.5;   // conversion from timeticks to h for all diagrams: 2 time ticks mean 1 hour
	 
   private double consistency = 0.0;
   private double minDist=0.1;    
	private boolean developGranulosum=true;
	
	private int basalY=80;          // y coordinate at which undulations start, the base line    
	
	private double gStatistics_KCytes_MeanAge=0;
	private double gStatistics_Barrier_ExtCalcium=0;
	private double gStatistics_Barrier_IntCalcium=0;
	private double gStatistics_Barrier_Lamella=0;
	private double gStatistics_Barrier_Lipids=0;
	private double gStatistics_Apoptosis_Basal=0;    // apoptosis events during 10 ticks, is calculated from  ..Counter   
	private int    gStatistics_Apoptosis_BasalCounter=0;    // Counter is reset every 100 ticks
	private double gStatistics_Apoptosis_EarlySpi;
	private int    gStatistics_Apoptosis_EarlySpiCounter=0;    // Counter is reset every 100 ticks
	private double gStatistics_Apoptosis_LateSpi;
	private int    gStatistics_Apoptosis_LateSpiCounter=0;    // Counter is reset every 100 ticks
	private double gStatistics_Apoptosis_Granu;
	private int    gStatistics_Apoptosis_GranuCounter=0;    // Counter is reset every 100 ticks
	private int    gStatistics_GrowthFraction=0;             // Percentage
	private double gStatistics_TurnoverTime=0;             // Percentage
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public Epidermis(long seed)
 {
     super(new ec.util.MersenneTwisterFast(seed), new Schedule(1));
     
     modelController = BioChemicalModelController.getInstance();
     
     SnapshotWriter.getInstance().addSnapshotListener(this);
 }

 

 public final double depthFrac(double y) // wie tief ist in prozent die uebergebene y-position relativ zu retezapfen tiefe
 {
     return (y-basalY)/modelController.getIntField("basalAmplitude_µm");                
 }

 
 
 void printChartToPDF( JFreeChart chart, int width, int height, String fileName )
 {
     // call: printChartToPDF( EpidermisClass.createChart(), 500, 500, "test.pdf" );
 try
     {
     Document document = new Document(new com.lowagie.text.Rectangle(width,height));
     PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
     document.addAuthor("Thomas Sütterlin, Niels Grabe");
     document.open();
     PdfContentByte cb = writer.getDirectContent();
     PdfTemplate tp = cb.createTemplate(width, height); 
     Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
     Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height); 
     chart.draw(g2, rectangle2D);
     g2.dispose();
     cb.addTemplate(tp, 0, 0);
     document.close();
     }
 catch( Exception e )
     {
     e.printStackTrace();
     }
 }
 
    
 
 public void start()
     {
	 
   	  super.start(reloadedSnapshot);
   	  
     
     
     modelController.initModel();
   if(!reloadedSnapshot){
     allCells.clear();
     // set up the C2dHerd field.  It looks like a discretization
     // of about neighborhood / 1.5 is close to optimal for us.  Hmph,
     // that's 16 hash lookups! I would have guessed that 
     // neighborhood * 2 (which is about 4 lookups on average)
     // would be optimal.  Go figure.
     cellContinous2D = new Continuous2D(modelController.getDoubleField("neighborhood_µm")/1.5,modelController.getDoubleField("width"),height);
     basementContinous2D = new Continuous2D(modelController.getDoubleField("width"),modelController.getDoubleField("width"),height);
    
    basementContinous2D.setObjectLocation("DummyObjektForDrawingTheBasementMembrane", new Double2D(50, 50));
     double x=0;
     // seeding the stem cells
     allocatedKCytes=0;   // allocated memory
     actualKCytes=0;      // num of kcytes that are not in nirvana
     actualSpi=0;         // Spinosum
     actualTA=0;          // TA Cells
     actualLateSpi=0;     // Late Spinosum
     actualGranu=0;
     actualNoNucleus=0;
     actualStem=0;
     
     Double2D lastloc=new Double2D(2, BasementMembrane.lowerBound(2));        
     for(x=2; x<=modelController.getDoubleField("width")-2; x+=2)
     {           
         Double2D newloc=new Double2D(x,BasementMembrane.lowerBound(x));
         double distance=newloc.distance(lastloc);            
         
         if ((depthFrac(newloc.y)>modelController.getDoubleField("seedMinDepth_frac") && (!modelController.getBooleanField("seedReverse"))) || (depthFrac(newloc.y)<modelController.getDoubleField("seedMinDepth_frac") && modelController.getBooleanField("seedReverse")))
             if (distance>modelController.getIntField("basalDensity_µm"))
             {                
                 KCyte stemCell= new KCyte(this);                 
                 stemCell.setKeratinoType(modelController.getGlobalIntConstant("KTYPE_STEM"));
                 stemCell.setOwnColor(10);
                 stemCell.setKeratinoAge(random.nextInt(modelController.getIntField("stemCycle_t")));     // somewhere on the stemCycle
                 cellContinous2D.setObjectLocation(stemCell, newloc);
                 lastloc=newloc;
                 Stoppable stoppable = schedule.scheduleRepeating(stemCell);
                 stemCell.setStoppable(stoppable);
                 // x+=basalDensity; // in any case jump a step to the right to avoid overlay of stem cells
                 actualStem++;
                 actualKCytes++;
             }
     }
     
     //BackImageClass backImage=new BackImageClass(this);        
     //schedule.scheduleOnce(backImage);
     
     
     gStatistics_KCytes_MeanAge=0;
     gStatistics_Barrier_ExtCalcium=0;
     gStatistics_Barrier_IntCalcium=0;
     gStatistics_Barrier_Lamella=0;
     gStatistics_Barrier_Lipids=0;
     
     epiSimCharts.getXYSeries("ChartSeries_Kinetics_MeanCycleTime").clear();  // remove previous (X,Y) pairs from the chart
     epiSimCharts.getXYSeries("ChartSeries_Kinetics_GrowthFraction").clear();  // remove previous (X,Y) pairs from the chart
     epiSimCharts.getXYSeries("ChartSeries_Kinetics_Turnover").clear();  // remove previous (X,Y) pairs from the chart
     epiSimCharts.getXYSeries("ChartSeries_KCyte_All").clear();  // remove previous (X,Y) pairs from the chart
     epiSimCharts.getXYSeries("ChartSeries_KCyte_Spi").clear();
     epiSimCharts.getXYSeries("ChartSeries_KCyte_TA").clear();
     epiSimCharts.getXYSeries("ChartSeries_KCyte_LateSpi").clear();
     epiSimCharts.getXYSeries("ChartSeries_KCyte_Granu").clear();
     epiSimCharts.getXYSeries("ChartSeries_KCyte_NoNuc").clear();
     epiSimCharts.getXYSeries("ChartSeries_KCyte_MeanAgeDate").clear();
     epiSimCharts.getXYSeries("ChartSeries_Barrier_Calcium").clear();  // remove previous (X,Y) pairs from the chart
     epiSimCharts.getXYSeries("ChartSeries_Barrier_Lamella").clear();
     epiSimCharts.getXYSeries("ChartSeries_Barrier_Lipids").clear();
     epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Basal").clear();  // remove previous (X,Y) pairs from the chart
     epiSimCharts.getXYSeries("ChartSeries_Apoptosis_EarlySpi").clear();
     epiSimCharts.getXYSeries("ChartSeries_Apoptosis_LateSpi").clear();        
     epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Granu").clear();
     epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Basal").clear(); 
   }
  /* else{
   	
   	Iterator iter = allCells.iterator();
   		
   		while(iter.hasNext()){
   		  Object obj = iter.next();
   		  if (obj instanceof KCyteClass){
   			  KCyteClass kcyte =(KCyteClass) obj;
   			  
   			  schedule.scheduleRepeating(kcyte);
   			  schedule.getSteps();
   				  
   			  }
   		  }
   }*/
     /////////////////////////////////
     // charts
     /////////////////////////////////
     
 /*
    Steppable chartPrinter = new Steppable()
    {
         public void step(SimState state)
         {        
            long t=(long) state.schedule.time();
            File dir = new File(graphicsDirectory);
            if(!dir.exists()){ 
            	dir.mkdir();
            	System.out.println("Directory " + dir.getAbsolutePath() + " created!");
            }
           
             String fnumcells=graphicsDirectory+"episim_chart_numcells_"+t+".png";
             String fnumcells_Large=graphicsDirectory+"episim_chart_numcells_large_"+t+".png";
             String pdfnumcells=graphicsDirectory+"episim_chart_numcells_"+t+".pdf";
             
             String fbarrier=graphicsDirectory+"episim_chart_barrier_"+t+".png";
             String fbarrier_Large=graphicsDirectory+"episim_chart_barrier_large_"+t+".png";
             String pdfbarrier=graphicsDirectory+"episim_chart_barrier_"+t+".pdf";
             
             String fcelltypes=graphicsDirectory+"episim_chart_celltypes_"+t+".png";                
             String fcelltypes_Large=graphicsDirectory+"episim_chart_celltypes_large_"+t+".png";
             String pdfcelltypes=graphicsDirectory+"episim_chart_celltypes_"+t+".pdf";
             
             String fpartdist=graphicsDirectory+"episim_chart_particlegradients_"+t+".png";
             String fpartdist_Large=graphicsDirectory+"episim_chart_particlegradients_large_"+t+".png";
             String pdfpartdist=graphicsDirectory+"episim_chart_particlegradients_"+t+".pdf";
             
             String fagedist=graphicsDirectory+"episim_chart_agegradient_"+t+".png";
             String fagedist_Large=graphicsDirectory+"episim_chart_agegradient_large_"+t+".png";
             String pdfagedist=graphicsDirectory+"episim_chart_agegradient_"+t+".pdf";
             
             String fkinetics=graphicsDirectory+"episim_chart_kinetics_"+t+".png";                
             String fkinetics_Large=graphicsDirectory+"episim_chart_kinetics_large_"+t+".png";
             String pdfkinetics=graphicsDirectory+"episim_chart_kinetics_"+t+".pdf";
             
             String fapoptosis=graphicsDirectory+"episim_chart_apoptosis_"+t+".png";
             String fapoptosis_Large=graphicsDirectory+"episim_chart_apoptosis_large_"+t+".png";
             String pdfapoptosis=graphicsDirectory+"episim_chart_apoptosis_"+t+".pdf";

             try {
                     ChartUtilities.saveChartAsPNG(new File(fnumcells),  epiSimCharts.getNumCellsChart(),    PNG_ChartWidth, PNG_ChartHeight);
                     ChartUtilities.saveChartAsPNG(new File(fnumcells_Large),  epiSimCharts.getNumCellsChart(),    PNG_ChartWidth_Large, PNG_ChartHeight_Large);
                     ChartUtilities.saveChartAsPNG(new File(fbarrier),   epiSimCharts.getBarrierChart(), PNG_ChartWidth, PNG_ChartHeight);
                     ChartUtilities.saveChartAsPNG(new File(fcelltypes), epiSimCharts.getParticleCellTypeChart(),   PNG_ChartWidth, PNG_ChartHeight);
                     ChartUtilities.saveChartAsPNG(new File(fpartdist),  epiSimCharts.getParticleDistribution(),    PNG_ChartWidth, PNG_ChartHeight);
                     ChartUtilities.saveChartAsPNG(new File(fpartdist_Large),  epiSimCharts.getParticleDistribution(),    PNG_ChartWidth_Large, PNG_ChartHeight_Large);
                     ChartUtilities.saveChartAsPNG(new File(fagedist),   epiSimCharts.getAgeDistribution(),     PNG_ChartWidth, PNG_ChartHeight);
                     ChartUtilities.saveChartAsPNG(new File(fkinetics),  epiSimCharts.getKineticsChart(),    PNG_ChartWidth, PNG_ChartHeight);
                     ChartUtilities.saveChartAsPNG(new File(fkinetics_Large),  epiSimCharts.getKineticsChart(),    PNG_ChartWidth_Large, PNG_ChartHeight_Large);
                     ChartUtilities.saveChartAsPNG(new File(fapoptosis), epiSimCharts.getApoptosisChart(),   PNG_ChartWidth, PNG_ChartHeight);
	} catch(Exception ex){
		System.out.println("File writing for charts didn't work.");
             }
                     // alternatice for pdf creation:
             /*
                     printChartToPDF(chartNumCells, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfnumcells);
                     printChartToPDF(chartBarrierDist, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfbarrier);
                     printChartToPDF(chartCellTypes, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfcelltypes);
                     printChartToPDF(chartPartDist, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfpartdist);
                     printChartToPDF(chartAgeDist, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfagedist);
                     printChartToPDF(chartKinetics, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfkinetics);
                     printChartToPDF(chartApoptosis, PDF_ChartWidth_Large, PDF_ChartHeight_Large, pdfapoptosis);
              */
     /*         }          
};
    // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartPrinter, 1000);
     schedule.scheduleOnce(300,chartPrinter);
   */  
     //////////////////////////////////////        
     // CHART Updating Kinetics Chart
     //////////////////////////////////////
     // clear is necessary for restart of simulation
     

     Steppable chartUpdaterKinetics= new Steppable()
    {
         public void step(SimState state)
         {            	
         	// add a new (X,Y) point on the graph, with X = the time step and Y = the number of live cells
         	//ChartSeries_KCyte_All.add((double)(state.schedule.time()), actualKCytes);    
             double meanCycleTime=0;
             double turnover=0;
             //double growthFraction=0; // instead globally defined
             if (actualKCytes>0)
             {
                 meanCycleTime=(actualStem*modelController.getIntField("stemCycle_t")+actualTA*modelController.getIntField("tACycle_t"))/(actualStem+actualTA);
                 epiSimCharts.getXYSeries("ChartSeries_Kinetics_MeanCycleTime").add((double)(state.schedule.time()*gTimefactor), meanCycleTime*gTimefactor);
                 if (actualBasalStatisticsCells>0)
                     gStatistics_GrowthFraction=100*(actualTA+actualStem)/actualBasalStatisticsCells;
                 if (gStatistics_GrowthFraction>100) gStatistics_GrowthFraction=100;
                 //ChartSeries_Kinetics_GrowthCells.add((double)(state.schedule.time()), growthFraction);                
                 epiSimCharts.getXYSeries("ChartSeries_Kinetics_GrowthFraction").add((double)(state.schedule.time()*gTimefactor), gStatistics_GrowthFraction);                
                 if (meanCycleTime>0) 
                     gStatistics_TurnoverTime=(actualKCytes)*meanCycleTime/(actualTA+actualStem); // Number of cells producing X mean production per time
                 else
                     gStatistics_TurnoverTime=0;
                 epiSimCharts.getXYSeries("ChartSeries_Kinetics_Turnover").add((double)(state.schedule.time()*gTimefactor), gStatistics_TurnoverTime*gTimefactor);
             }
         }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartUpdaterKinetics, 100);
     
     //////////////////////////////////////        
     // CHART Updating Num Cell Chart
     //////////////////////////////////////
     
     
     Steppable chartUpdaterNumCells = new Steppable()
    {
         public void step(SimState state)
         {            	
         	// add a new (X,Y) point on the graph, with X = the time step and Y = the number of live cells
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_All").add((double)(state.schedule.time()*gTimefactor), actualKCytes);
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_TA").add((double)(state.schedule.time()*gTimefactor), actualTA);
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_Spi").add((double)(state.schedule.time()*gTimefactor), actualSpi);
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_LateSpi").add((double)(state.schedule.time()*gTimefactor), actualLateSpi);
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_Granu").add((double)(state.schedule.time()*gTimefactor), actualGranu);
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_NoNuc").add((double)(state.schedule.time()*gTimefactor), actualNoNucleus);
         	 epiSimCharts.getXYSeries("ChartSeries_KCyte_MeanAgeDate").add((double)(state.schedule.time()*gTimefactor), gStatistics_KCytes_MeanAge*gTimefactor);
         }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartUpdaterNumCells, 100);

     //////////////////////////////////////        
     // CHART Updating Barrier Chart
     //////////////////////////////////////
    
     Steppable chartUpdaterBarrier = new Steppable()
    {
         public void step(SimState state)
         {          
         	epiSimCharts.getXYSeries("ChartSeries_Barrier_Calcium").add((double)(state.schedule.time()*gTimefactor), gStatistics_Barrier_ExtCalcium);
         	epiSimCharts.getXYSeries("ChartSeries_Barrier_Lamella").add((double)(state.schedule.time()*gTimefactor), gStatistics_Barrier_Lamella);
         	epiSimCharts.getXYSeries("ChartSeries_Barrier_Lipids").add((double)(state.schedule.time()*gTimefactor), gStatistics_Barrier_Lipids);
         }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartUpdaterBarrier, 100);

     //////////////////////////////////////        
     // CHART Updating Logfile
     //////////////////////////////////////
      
               
     Steppable logfileUpdater = new Steppable()
    {
         public void step(SimState state)
         {   
             int timestamp=(int) (state.schedule.time()*gTimefactor+0.5);
             if (timestamp>=100)
                    {
                    try {
                     BufferedWriter out = new BufferedWriter(new FileWriter("C:\\simresults.csv", true));
                     out.write((int) (state.schedule.time()) + ";");
                     out.write(timestamp + ";");
                     out.write(actualStem+ ";" +actualTA + ";" + actualSpi+";" +actualLateSpi+";"+actualGranu+";"+(actualKCytes-actualNoNucleus)+";");
                     out.write((int)0.5+100*(actualStem+actualTA) + ";");
                     out.write((int)0.5+100*(actualLateSpi+actualSpi+actualGranu)+ ";");
                                 out.write((int)0.5+100*(actualStem+actualTA)/(actualKCytes-actualNoNucleus) + ";");
                     out.write((int)0.5+100*(actualLateSpi+actualSpi+actualGranu)/(actualKCytes-actualNoNucleus)+ ";");
                     //System.out.print(gStatistics_Apoptosis_EarlySpi + ";" + gStatistics_Apoptosis_LateSpi + ";" + gStatistics_Apoptosis_Granu+";");
                     out.write((int) (gStatistics_TurnoverTime*gTimefactor+0.5) + ";" + (int)(0.5+gStatistics_GrowthFraction) + ";");
                     out.write("\n");
                     out.close();
                      } catch (IOException e) {}         
                    }
             else { 
                 }
        }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(logfileUpdater, 200);
     try {
         BufferedWriter out = new BufferedWriter(new FileWriter("C:\\simresults.csv", false));
         out.write("SimTicks; (h)Time; #Stem; #TA; #EarlySpi; #LateSpi; #Granu; #TotalNum; #NumProl; #NumDiff; %PropProl; %PropDiff; Turnover; %GrowthFrac;\n");
         //logfilestat.write ("SimTicks; (h)SimTime; Stem; TA; EarlySpi; LateSpi; Granu; TotalNum; PropProl; PropDiff; Turnover; Growth;");
         out.close();
         } catch (IOException e) {}  
     
     //////////////////////////////////////        
     // CHART Updating Apoptosis Chart
     //////////////////////////////////////
       
     Steppable chartUpdaterApoptosis = new Steppable()
    {
         public void step(SimState state)
         {   
             if (state.schedule.time()>2000)
             {
            	 epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Basal").add((double)(state.schedule.time()*gTimefactor), gStatistics_Apoptosis_Basal);
            	 epiSimCharts.getXYSeries("ChartSeries_Apoptosis_EarlySpi").add((double)(state.schedule.time()*gTimefactor), gStatistics_Apoptosis_EarlySpi);
            	 epiSimCharts.getXYSeries("ChartSeries_Apoptosis_LateSpi").add((double)(state.schedule.time()*gTimefactor), gStatistics_Apoptosis_LateSpi);
            	 epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Granu").add((double)(state.schedule.time()*gTimefactor), gStatistics_Apoptosis_Granu);
             }
         }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartUpdaterApoptosis, 100);
     
     //////////////////////////////////////
     // CHART Updating Performance Chart
     //////////////////////////////////////
        
     Steppable chartUpdaterPerformance = new Steppable()
    {
        private long previousTime = 0;
        private long previousSteps = 0;
   	  
   	  public void step(SimState state)
         {   
         	
   		   if(state.schedule.getSteps() > 400){
   		   	long actTime = System.currentTimeMillis()/1000;
         	long actSteps = state.schedule.getSteps();
   		   long deltaTime = actTime - previousTime;
   		   long deltaSteps = actSteps - previousSteps;
   		   
   		   previousTime = actTime;
   		   previousSteps = actSteps;
   		   if(deltaTime > 0){
   		   double stepsPerTime = deltaSteps/deltaTime;
         	epiSimCharts.getXYSeries("Steps_Time").add(state.schedule.getSteps(), stepsPerTime);
   		   epiSimCharts.getXYSeries("Num_Cells_Steps").add(state.schedule.getSteps(), actualKCytes);
   		   }
   		   }	
   		   
             
         }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartUpdaterPerformance, 100);

     
     //////////////////////////////////////        
     // CHART Updating Particle Distributions
     //////////////////////////////////////        
    
     
     Steppable chartUpdaterParticleDistributions = new Steppable()
    {
             public void step(SimState state)
             {
            	  epiSimCharts.getXYSeries("ExtCalConcAvg").clear();
            	  epiSimCharts.getXYSeries("LamellaConcAvg").clear(); 
            	  epiSimCharts.getXYSeries("LipidsConcAvg").clear();
            	  epiSimCharts.getXYSeries("AgeAvg").clear();
            	  epiSimCharts.getXYSeries("Num").clear();
                 
            	  int MAX_YBINS=30; // for every 10 y coordinates one bin
                 int[] HistoExtCalConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
                 int[] HistoIntCalConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
                 int[] HistoLamellaConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
                 int[] HistoLipidsConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
                 int[] HistoAgeAvg=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
                 int[] HistoNum=new int[MAX_YBINS];  // Number of Cells in this bin
                 double ExtCal_TA=0;                    
                 double ExtCal_Spi=0;
                 double ExtCal_LateSpi=0;
                 double ExtCal_Granu=0; 
                 double ExtCal_NoNuc=0;
                 double IntCal_TA=0;                    
                 double IntCal_Spi=0;
                 double IntCal_LateSpi=0;
                 double IntCal_Granu=0; 
                 double IntCal_NoNuc=0;
                 double Lam_TA=0;                    
                 double Lam_Spi=0;
                 double Lam_LateSpi=0;
                 double Lam_Granu=0;                    
                 double Lam_NoNuc=0;
                 double Lip_TA=0;                    
                 double Lip_Spi=0;
                 double Lip_LateSpi=0;
                 double Lip_Granu=0;
                 double Lip_NoNuc=0;
                 
                 for (int k=0; k<MAX_YBINS; k++)
                 {
                     HistoExtCalConc[k]=0;
                     HistoIntCalConc[k]=0;
                     HistoNum[k]=0;
                 }
                 
                 for (int i=0; i<allCells.size(); i++)
                 {
                     // iterate through all cells
                     KCyte act=(KCyte)allCells.get(i);
                     if (act.isInNirvana()) continue;
                     // is a living cell..
                     Double2D loc=cellContinous2D.getObjectLocation(act);
                     int histobin=(int)loc.y/7;
                     HistoNum[histobin]++;
                     HistoExtCalConc[histobin]+=act.getOwnSigExternalCalcium();
                     HistoIntCalConc[histobin]+=act.getOwnSigInternalCalcium();
                     HistoLamellaConc[histobin]+=act.getOwnSigLamella();
                     HistoLipidsConc[histobin]+=act.getOwnSigLipids();
                     if (act.getKeratinoType()!= modelController.getGlobalIntConstant("KTYPE_STEM"))
                         HistoAgeAvg[histobin]+=act.getKeratinoAge();
                     
                     if(act.getKeratinoType() == modelController.getGlobalIntConstant("KTYPE_TA")) 
                     { IntCal_TA+=act.getOwnSigInternalCalcium(); 
                       ExtCal_TA+=act.getOwnSigExternalCalcium(); 
                       Lam_TA+=act.getOwnSigLamella(); 
                       Lip_TA+=act.getOwnSigLipids();  
                      }
                     else if(act.getKeratinoType() ==modelController.getGlobalIntConstant("KTYPE_SPINOSUM")){ 
                     	IntCal_Spi+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_Spi+=act.getOwnSigExternalCalcium(); 
                     	Lam_Spi+=act.getOwnSigLamella(); 
                     	Lip_Spi+=act.getOwnSigLipids(); 
                     }
                     else if(act.getKeratinoType() == modelController.getGlobalIntConstant("KTYPE_LATESPINOSUM")) { 
                     	IntCal_LateSpi+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_LateSpi+=act.getOwnSigExternalCalcium(); 
                     	Lam_LateSpi+=act.getOwnSigLamella(); 
                     	Lip_LateSpi+=act.getOwnSigLipids(); 
                     }
                     else if(act.getKeratinoType() == modelController.getGlobalIntConstant("KTYPE_GRANULOSUM")){ 
                     	IntCal_Granu+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_Granu+=act.getOwnSigExternalCalcium(); 
                     	Lam_Granu+=act.getOwnSigLamella(); 
                     	Lip_Granu+=act.getOwnSigLipids(); 
                     }
                     else if(act.getKeratinoType() == modelController.getGlobalIntConstant("KTYPE_NONUCLEUS")){ 
                     	IntCal_NoNuc+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_NoNuc+=act.getOwnSigExternalCalcium(); 
                     	Lam_NoNuc+=act.getOwnSigLamella(); 
                     	Lip_NoNuc+=act.getOwnSigLipids(); 
                     }
                     
                 }
                 
                 ///////////////////////////////////////////
                 // Cell Type Statistics
                 ///////////////////////////////////////////
                 
                 if (actualTA>3) ExtCal_TA/=actualTA;
                 if (actualSpi>3) ExtCal_Spi/=actualSpi;
                 if (actualLateSpi>3) ExtCal_LateSpi/=actualLateSpi;
                 if (actualGranu>3) ExtCal_Granu/=actualGranu;                    
                 if (actualNoNucleus>3) ExtCal_NoNuc/=actualNoNucleus;                    

                 if (actualTA>3) IntCal_TA/=actualTA;
                 if (actualSpi>3) IntCal_Spi/=actualSpi;
                 if (actualLateSpi>3) IntCal_LateSpi/=actualLateSpi;
                 if (actualGranu>3) IntCal_Granu/=actualGranu;                    
                 if (actualNoNucleus>3) IntCal_NoNuc/=actualNoNucleus;                    

                 if (actualTA>3) Lam_TA/=actualTA;
                 if (actualSpi>3) Lam_Spi/=actualSpi;
                 if (actualLateSpi>3) Lam_LateSpi/=actualLateSpi;
                 if (actualGranu>3) Lam_Granu/=actualGranu;
                 if (actualNoNucleus>3) Lam_NoNuc/=actualNoNucleus;                    
                 
                 if (actualTA>3) Lip_TA/=actualTA;
                 if (actualSpi>3) Lip_Spi/=actualSpi;
                 if (actualLateSpi>3) Lip_LateSpi/=actualLateSpi;
                 if (actualGranu>3) Lip_Granu/=actualGranu;
                 if (actualNoNucleus>3) Lip_NoNuc/=actualNoNucleus;                    
                 
                 // row keys...
                 String sExtCal = "Ext Cal (mg/kg)";
                 String sIntCal = "Int Cal (mg/kg)";
                 String sLam= "Lamella";
                 String sLip = "Lipids";

                 // column keys...
                 String cTA = "TA";
                 String cSpi = "EarlySpinosum";
                 String cLateSpi = "LateSpinosum";
                 String cGranu = "Granulosum";
                 String cNoNuc = "NoNucleus";
                 
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").clear();
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_TA, sExtCal, cTA);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_Spi, sExtCal, cSpi);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_LateSpi, sExtCal, cLateSpi);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_Granu, sExtCal, cGranu);                    
             
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_TA, sLam, cTA);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_Spi, sLam, cSpi);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_LateSpi, sLam, cLateSpi);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_Granu, sLam, cGranu);                    
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_TA, sLip, cTA);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_Spi, sLip, cSpi);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_LateSpi, sLip, cLateSpi);
                 epiSimCharts.getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_Granu, sLip, cGranu);                              
                 
                 // Make Chartdata from Histo
                 double concExtCal=0;   // averaged concentrations
                 double concIntCal=0;   // averaged concentrations
                 double concLamella=0;
                 double concLipids=0;
                 double avgAge=0;
                 for (int j=0; j<MAX_YBINS; j++)
                 {                        
                     if (HistoNum[j]>=10)
                     {
                         concExtCal=HistoExtCalConc[j]/ HistoNum[j];
                         concIntCal=HistoIntCalConc[j]/ HistoNum[j];
                         concLamella=HistoLamellaConc[j] /HistoNum[j];
                         concLipids=HistoLipidsConc[j]/ HistoNum[j];
                         avgAge=HistoAgeAvg[j]/ HistoNum[j];
                         
                     }
                     else
                     {
                         concExtCal=0;
                         concIntCal=0;
                         concLamella=0;
                         concLipids=0;
                         avgAge=0;
                     }
                     if (HistoNum[j]>=10)
                     {
                     	 epiSimCharts.getXYSeries("ExtCalConcAvg").add(j*7-gCorneumY, concExtCal);
                     	 epiSimCharts.getXYSeries("LamellaConcAvg").add(j*7-gCorneumY, concLamella);
                     	 epiSimCharts.getXYSeries("LipidsConcAvg").add(j*7-gCorneumY, concLipids);
                     	 epiSimCharts.getXYSeries("AgeAvg").add(j*7-gCorneumY, avgAge*gTimefactor);
                     	 epiSimCharts.getXYSeries("Num").add(j*7-gCorneumY, HistoNum[j]);
                     }
                 }
                        
          }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(chartUpdaterParticleDistributions, 50);


             
 //////////////////////////////////////        
 // CELL STATISTICS & Updating OUTER SURFACE CELLS
 //////////////////////////////////////  

 // the agent that updates the isOuterSurface Flag for the surface exposed cells
     Steppable airSurface = new Steppable()
    {
             public void step(SimState state)
             {
                 int MAX_XBINS=300; // for every 3 x coordinates one bin
                 KCyte[] XLookUp=new KCyte[MAX_XBINS];                                         
                 double [] YLookUp=new double[MAX_XBINS]; // Concentrations *10 = 0 to 200
                 boolean [] LookUpUsed=new boolean[MAX_XBINS]; 
                 for (int k=0; k< MAX_XBINS; k++)
                 {
                     YLookUp[k]=9999.9; // deepest value, all coming are above
                     XLookUp[k]=null;
                 }
                 gStatistics_KCytes_MeanAge=0;
                 gStatistics_Barrier_ExtCalcium=0;
                 gStatistics_Barrier_Lipids=0;
                 gStatistics_Barrier_Lamella=0;
                 int OldNumOuterCells=0;                    
                 actualBasalStatisticsCells=0;                    
                 
                 for (int i=0; i<allCells.size(); i++)
                 {
                     // iterate through all cells and determine the KCyte with lowest Y at bin
                     KCyte act=(KCyte)allCells.get(i);
                     if (act.isInNirvana()) continue;
                     // is a living cell..
                     
                     if (act.isOuterCell()) // statistics from last time evaluation (so we are always lacking behind one calling period !)
                     {
                         gStatistics_Barrier_ExtCalcium+=act.getOwnSigExternalCalcium();
                         gStatistics_Barrier_Lamella+=act.getOwnSigLamella();
                         gStatistics_Barrier_Lipids+=act.getOwnSigLipids();                            
                         OldNumOuterCells++;
                     }
                     
                     if (act.isBasalStatisticsCell()) actualBasalStatisticsCells++;
                     
                     //act.isOuterCell=false; // set new default 
                     Double2D loc=cellContinous2D.getObjectLocation(act);
                     int xbin=(int)loc.x/InitialKeratinoSize;
                     if (XLookUp[xbin]==null) 
                     {
                         XLookUp[xbin]=act;                            
                         YLookUp[xbin]=loc.y;
                     }
                     else
                         if (loc.y<YLookUp[xbin]) 
                         {
                             XLookUp[xbin]=act;
                             YLookUp[xbin]=loc.y;
                         }
                     // other statistics
                     if ((act.getKeratinoType()!=modelController.getGlobalIntConstant("KTYPE_STEM")) && (act.getKeratinoType()!=modelController.getGlobalIntConstant("KTYPE_NONUCLEUS")))
                     {
                         gStatistics_KCytes_MeanAge+=act.getKeratinoAge();  
                         if (act.getKeratinoAge()>modelController.getIntField("maxCellAge_t"))
                             {
                                 System.out.println("Age Error");
                             }
                     }
                 }            

                 for (int k=0; k< MAX_XBINS; k++)
                 {
                     if ((XLookUp[k]==null) || (XLookUp[k].getKeratinoType()==modelController.getGlobalIntConstant("KTYPE_STEM"))) continue; // stem cells cannot be outer cells (Assumption)                        
                     XLookUp[k].setOuterCell(true);
                 }
                 // other statistics
                 gStatistics_KCytes_MeanAge/=actualKCytes-actualNoNucleus;
                 gStatistics_Barrier_ExtCalcium/=OldNumOuterCells;
                 gStatistics_Barrier_Lipids/=OldNumOuterCells;
                 gStatistics_Barrier_Lamella/=OldNumOuterCells;
                 
                 gStatistics_Apoptosis_Basal=gStatistics_Apoptosis_BasalCounter;
                 gStatistics_Apoptosis_Basal=gStatistics_Apoptosis_Basal/10/actualBasalStatisticsCells*100;    // /10: per 10 timeticks, then:percentage of Apopotosis
                 gStatistics_Apoptosis_EarlySpi=gStatistics_Apoptosis_EarlySpiCounter;
                 gStatistics_Apoptosis_EarlySpi=gStatistics_Apoptosis_EarlySpi/10/actualSpi*100;    // /10: per 10 timeticks, then:percentage of Apopotosis
                 gStatistics_Apoptosis_LateSpi=gStatistics_Apoptosis_LateSpiCounter;
                 gStatistics_Apoptosis_LateSpi=gStatistics_Apoptosis_LateSpi/10/actualLateSpi*100;    // /10: per 10 timeticks, then:percentage of Apopotosis
                 gStatistics_Apoptosis_Granu=gStatistics_Apoptosis_GranuCounter;
                 gStatistics_Apoptosis_Granu=gStatistics_Apoptosis_Granu/10/actualGranu*100;    // /10: per 10 timeticks, then:percentage of Apopotosis

                 gStatistics_Apoptosis_BasalCounter=0;    // Cells removed from simulation during last time tick    
                 gStatistics_Apoptosis_EarlySpiCounter=0;
                 gStatistics_Apoptosis_LateSpiCounter=0;
                 gStatistics_Apoptosis_GranuCounter=0;

             }
     };
     // Schedule the agent to update is Outer Flag
     
     schedule.scheduleRepeating(airSurface, 100);
     }





	public void removeCells(GeneralPath path){
	Iterator iter = allCells.iterator();
		
		while(iter.hasNext()){
		  Object obj = iter.next();
		  if (obj instanceof KCyte){
			  KCyte kcyte =(KCyte) obj;
			  if(path.contains(kcyte.getLastDrawInfoX(), kcyte.getLastDrawInfoY())){ 
				  System.out.println("Zelle gelöscht");
				  //iter.remove();
				  kcyte.killCell();
			  }
		  }
		}
	}



 
//---------------------------------------------------------------------------------------------------------------------------------------------------
//INKREMENT-DEKREMENT-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 	public void inkrementAllocatedKCytes(){allocatedKCytes +=1;}
	public void dekrementAllocatedKCytes(){allocatedKCytes -=1;}
	
	public void inkrementActualStem(){actualStem +=1;}
	public void dekrementActualStem(){actualStem -=1;}

	public void inkrementActualKCytes(){actualKCytes +=1;}
	public void dekrementActualKCytes(){actualKCytes -=1;}

	public void inkrementActualSpi(){actualSpi +=1;}
	public void dekrementActualSpi(){actualSpi -=1;}

	public void inkrementActualTA(){actualTA +=1;}
	public void dekrementActualTA(){actualTA -=1;}

	public void inkrementActualLateSpi(){actualLateSpi +=1;}
	public void dekrementActualLateSpi(){actualLateSpi -=1;}


	public void inkrementActualGranu(){actualGranu +=1;}
	public void dekrementActualGranu(){actualGranu -=1;}

	public void inkrementActualCorneum(){actualCorneum +=1;}
	public void dekrementActualCorneum(){actualCorneum -=1;}

	public void inkrementActualNoNucleus(){actualNoNucleus +=1;}
	public void dekrementActualNoNucleus(){actualNoNucleus -=1;}

	public void inkrementActualBasalStatisticsCells(){actualBasalStatisticsCells +=1;}
	public void dekrementActualBasalStatisticsCells(){actualBasalStatisticsCells -=1;}
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//GETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 

	public int getActualKCytes() { return actualKCytes; }
	public Bag getAllCells() {	return allCells; }
	public int getAllocatedKCytes() { return allocatedKCytes; }
	
	public Continuous2D getBasementContinous2D() { return basementContinous2D; }
	
	public Continuous2D getCellContinous2D() { return cellContinous2D; }
	public double getConsistency() { return consistency; }
	
	public int getGCorneumY() { return gCorneumY; }
	public String getGraphicsDirectory() {	return graphicsDirectory; }
	public double getGStatistics_Apoptosis_Basal() { return gStatistics_Apoptosis_Basal; }
	public int getGStatistics_Apoptosis_BasalCounter() { return gStatistics_Apoptosis_BasalCounter; }
	public double getGStatistics_Apoptosis_EarlySpi() { return gStatistics_Apoptosis_EarlySpi; }
	public int getGStatistics_Apoptosis_EarlySpiCounter() { return gStatistics_Apoptosis_EarlySpiCounter; }
	public double getGStatistics_Apoptosis_Granu() { return gStatistics_Apoptosis_Granu; }
	public int getGStatistics_Apoptosis_GranuCounter() { return gStatistics_Apoptosis_GranuCounter; }
	public double getGStatistics_Apoptosis_LateSpi() {	return gStatistics_Apoptosis_LateSpi; }
	public int getGStatistics_Apoptosis_LateSpiCounter() { return gStatistics_Apoptosis_LateSpiCounter; }
	public double getGStatistics_Barrier_ExtCalcium() { return gStatistics_Barrier_ExtCalcium; }
	public double getGStatistics_Barrier_IntCalcium() { return gStatistics_Barrier_IntCalcium; }
	public double getGStatistics_Barrier_Lamella() { return gStatistics_Barrier_Lamella; }
	public double getGStatistics_Barrier_Lipids() {	return gStatistics_Barrier_Lipids; }
	public int getGStatistics_GrowthFraction() {	return gStatistics_GrowthFraction; }
	public double getGStatistics_KCytes_MeanAge() { return gStatistics_KCytes_MeanAge; }
	public double getGStatistics_TurnoverTime() { return gStatistics_TurnoverTime; }
	
	public int getIndividualColor() { return individualColor; }
	
	public double getMinDist() { return minDist; }
	
	public boolean isDevelopGranulosum() {	return developGranulosum; }
	
	//complex-Methods------------------------------------------------------------------------------------------------------------------
	
	public List<SnapshotObject> getSnapshotObjects() {
		
		List<SnapshotObject> list = new LinkedList<SnapshotObject>();
		/*Iterator iter = allCells.iterator();
		
		while(iter.hasNext()){
			list.add(new SnapshotObject(SnapshotObject.KCYTE, iter.next()));
		}*/
		list.add(new SnapshotObject(SnapshotObject.EPIDERMIS, this));

		return list;
	}  
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//SETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 
	public void setActualKCytes(int actualKCytes) {	this.actualKCytes = actualKCytes; }
	public void setAllCells(Bag allCells) { this.allCells = allCells; }
	public void setAllocatedKCytes(int allocatedKCytes) {	this.allocatedKCytes = allocatedKCytes; }
	
	public void setBasementContinous2D(Continuous2D basementContinous2D) { this.basementContinous2D = basementContinous2D; }
	
	public void setCellContinous2D(Continuous2D cellContinous2D) { this.cellContinous2D = cellContinous2D; }
	public void setConsistency(double consistency) { this.consistency = consistency; }
	
	public void setDevelopGranulosum(boolean developGranulosum) { this.developGranulosum = developGranulosum; }
	
	public void setEpiSimCharts(EpiSimCharts epiSimCharts) {	this.epiSimCharts = epiSimCharts; }
	
	public void setGCorneumY(int corneumY) { gCorneumY = corneumY; }
	public void setGraphicsDirectory(String graphicsDirectory) { this.graphicsDirectory = graphicsDirectory; }
	public void setGStatistics_Apoptosis_Basal(double statistics_Apoptosis_Basal) { gStatistics_Apoptosis_Basal = statistics_Apoptosis_Basal; }
	public void setGStatistics_Apoptosis_BasalCounter(int statistics_Apoptosis_BasalCounter) { gStatistics_Apoptosis_BasalCounter = statistics_Apoptosis_BasalCounter; }
	public void setGStatistics_Apoptosis_EarlySpi(double statistics_Apoptosis_EarlySpi) { gStatistics_Apoptosis_EarlySpi = statistics_Apoptosis_EarlySpi; }
	public void setGStatistics_Apoptosis_EarlySpiCounter(int statistics_Apoptosis_EarlySpiCounter) { gStatistics_Apoptosis_EarlySpiCounter = statistics_Apoptosis_EarlySpiCounter; }
	public void setGStatistics_Apoptosis_Granu(double statistics_Apoptosis_Granu) { gStatistics_Apoptosis_Granu = statistics_Apoptosis_Granu; }
	public void setGStatistics_Apoptosis_GranuCounter(int statistics_Apoptosis_GranuCounter) { gStatistics_Apoptosis_GranuCounter = statistics_Apoptosis_GranuCounter; }
	public void setGStatistics_Apoptosis_LateSpi(double statistics_Apoptosis_LateSpi) { gStatistics_Apoptosis_LateSpi = statistics_Apoptosis_LateSpi; }
	public void setGStatistics_Apoptosis_LateSpiCounter(int statistics_Apoptosis_LateSpiCounter) { gStatistics_Apoptosis_LateSpiCounter = statistics_Apoptosis_LateSpiCounter; }
	public void setGStatistics_Barrier_ExtCalcium(double statistics_Barrier_ExtCalcium) { gStatistics_Barrier_ExtCalcium = statistics_Barrier_ExtCalcium; }
	public void setGStatistics_Barrier_IntCalcium(double statistics_Barrier_IntCalcium) { gStatistics_Barrier_IntCalcium = statistics_Barrier_IntCalcium; }
	public void setGStatistics_Barrier_Lamella(double statistics_Barrier_Lamella) { gStatistics_Barrier_Lamella = statistics_Barrier_Lamella; }
	public void setGStatistics_Barrier_Lipids(double statistics_Barrier_Lipids) { gStatistics_Barrier_Lipids = statistics_Barrier_Lipids; }
	public void setGStatistics_GrowthFraction(int statistics_GrowthFraction) { gStatistics_GrowthFraction = statistics_GrowthFraction; }
	public void setGStatistics_KCytes_MeanAge(double statistics_KCytes_MeanAge) { gStatistics_KCytes_MeanAge = statistics_KCytes_MeanAge; }
	public void setGStatistics_TurnoverTime(double statistics_TurnoverTime) { gStatistics_TurnoverTime = statistics_TurnoverTime; }
	
	public void setIndividualColor(int individualColor) {	this.individualColor = individualColor; }
	
	public void setMinDist(double minDist) { this.minDist = minDist; }
	
	public void setReloadedSnapshot(boolean reloadedSnapshot) {	this.reloadedSnapshot = reloadedSnapshot; }
	
	
	//	complex-Methods------------------------------------------------------------------------------------------------------------------
	
	
	public void setModelController(BioChemicalModelController modelController) {

		this.modelController = modelController;
	   Iterator iter = allCells.iterator();
		
		while(iter.hasNext()){
		  Object obj = iter.next();
		  if (obj instanceof KCyte){
			  KCyte kcyte =(KCyte) obj;
			  
				  kcyte.setModelController(modelController);
				  
			  }
		  }
		
	}

//	---------------------------------------------------------------------------------------------------------------------------------------------------
//	--------------------------------------------------------------------------------------------------------------------------------------------------- 

 }





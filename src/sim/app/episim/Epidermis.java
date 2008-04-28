
package sim.app.episim;


import sim.Dummy;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;

import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.BioMechanicalModelController;
import sim.app.episim.model.ModelController;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lowagie.text.*;  
import com.lowagie.text.pdf.*;  

import episimexceptions.MissingObjectsException;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;

public class Epidermis extends TissueType implements SnapshotListener, CellDeathListener
{

//	---------------------------------------------------------------------------------------------------------------------------------------------------
// CONSTANTS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
		
	public final int InitialKeratinoSize=5;
	public final int NextToOuterCell=7;
	public final String NAME ="Epidermis";
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
// VARIABLES
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	private transient ModelController modelController;
	private transient BioMechanicalModelController biomechModelContr;
	private transient BioChemicalModelController biochemModelContr;

	
	
	public boolean alreadyfollow = false;
	
	
	

	private boolean reloadedSnapshot = false;

	private  String graphicsDirectory="pdf_png_simres/";

	private Continuous2D cellContinous2D;
	private Continuous2D basementContinous2D;
	private Continuous2D rulerContinous2D;
	private Continuous2D gridContinous2D;
   
	private GenericBag<CellType> allCells=new GenericBag<CellType>(3000); //all cells will be stored in this bag
	private int allocatedKCytes=0;   // allocated memory
	
	
	private  int PDF_ChartWidth_Large=600;
	private  int PDF_ChartHeight_Large=400;
	 
	private int gCorneumY=20;    // gCorneum would start at this ..
	 
	private long timeInSimulationSteps = 0;
		
	private int individualColor=1;
	 
	
	 
   private double consistency = 0.0;
   private double minDist=0.1;    
	private boolean developGranulosum=true;
	
	    
	
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
	
	private List<EnhancedSteppable> chartSteppables = null;
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public Epidermis(long seed)
 {
     super(seed);
     
     modelController = ModelController.getInstance();
     biomechModelContr = modelController.getBioMechanicalModelController();
     biochemModelContr =  modelController.getBioChemicalModelController();
     SnapshotWriter.getInstance().addSnapshotListener(this);
     this.registerCellType(KCyte.class);
     this.registerCellType(Dummy.class);
     
     ChartController.getInstance().setChartMonitoredTissue(this);
     DataExportController.getInstance().setDataExportMonitoredTissue(this);
     ChartController.getInstance().registerChartSetChangeListener(this);
  	// set up the C2dHerd field. It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us. Hmph,
		// that's 16 hash lookups! I would have guessed that
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal. Go figure.
		
		//TODO: plus 2 Korrektur überprüfen
		cellContinous2D = new Continuous2D(biomechModelContr.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm() / 1.5, 
				TissueBorder.getInstance().getWidth() + 2, 
				TissueBorder.getInstance().getHeight());
		basementContinous2D = new Continuous2D(TissueBorder.getInstance().getWidth() + 2, 
				TissueBorder.getInstance().getWidth() + 2, 
				TissueBorder.getInstance().getHeight());
		rulerContinous2D = new Continuous2D(TissueBorder.getInstance().getWidth()+2,
	   			TissueBorder.getInstance().getWidth()+2,
	   			TissueBorder.getInstance().getHeight());
	   gridContinous2D = new Continuous2D(TissueBorder.getInstance().getWidth()+2,
	  			TissueBorder.getInstance().getWidth()+2,
	  			TissueBorder.getInstance().getHeight());
 }

 

 public final double depthFrac(double y) // wie tief ist in prozent die uebergebene y-position relativ zu retezapfen tiefe
 {
     return (y-TissueBorder.getInstance().getUndulationBaseLine())/modelController.getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getBasalAmplitude_µm();                
 }

 
 
 public void checkMemory(){
	 // Memory Management
    if (getNumberOfKCytes()>getAllCells().size()-2) // for safety -2
        getAllCells().resize(getAllCells().size()+500); // alloc 500 in advance
 }
 
 
 void printChartToPDF( JFreeChart chart, int width, int height, String fileName )
 {
     // call: printChartToPDF( EpidermisClass.createChart(), 500, 500, "test.pdf" );
	 try
	     {
	     Document document = new Document(new com.lowagie.text.Rectangle(width,height));
	     PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
	     document.addAuthor("Thomas Sütterlin");
	     document.open();
	     PdfContentByte cb = writer.getDirectContent();
	     PdfTemplate tp = cb.createTemplate(width, height); 
	     Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
	     Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height); 
	     chart.draw(g2, rectangle2D);
	     g2.dispose();
	     cb.addTemplate(tp, 0, 0);
	     document.close();
	     writer.close();
	     }
	 catch( Exception e )
	     {
	     e.printStackTrace();
	     }
 }
 
 
private void seedStemCells(){
	Double2D lastloc = new Double2D(2, TissueBorder.getInstance().lowerBound(2));
	for(double x = 2; x <= TissueBorder.getInstance().getWidth(); x += 2){
		Double2D newloc = new Double2D(x, TissueBorder.getInstance().lowerBound(x));
		double distance = newloc.distance(lastloc);

		if((depthFrac(newloc.y) > biomechModelContr.getEpisimMechanicalModelGlobalParameters()
				.getSeedMinDepth_frac() && (!biomechModelContr.getEpisimMechanicalModelGlobalParameters()
				.getSeedReverse()))
				|| (depthFrac(newloc.y) < biomechModelContr.getEpisimMechanicalModelGlobalParameters()
						.getSeedMinDepth_frac() && biomechModelContr.getEpisimMechanicalModelGlobalParameters()
						.getSeedReverse()))
			if(distance > biomechModelContr.getEpisimMechanicalModelGlobalParameters().getBasalDensity_µm()){

				// TODO: Check creation of Stem Cells
				KCyte stemCell = new KCyte(CellType.getNextCellId(),-1,this, biochemModelContr.getNewEpisimCellDiffModelObject());
				// stemCell.setKeratinoType(modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_STEM"));
				stemCell.setOwnColor(10);
				stemCell.getEpisimCellDiffModelObject().setAge(
						random.nextInt(biochemModelContr.getEpisimCellDiffModelGlobalParameters().getCellCycleStem()));// somewhere
																																						// on
																																						// the
																																						// stemCycle
				stemCell.getEpisimCellDiffModelObject().setDifferentiation(
						EpisimCellDiffModelGlobalParameters.STEMCELL);
				stemCell.getEpisimCellDiffModelObject().setSpecies(EpisimCellDiffModelGlobalParameters.KERATINOCYTE);
				stemCell.getEpisimCellDiffModelObject().setIsAlive(true);
	

				cellContinous2D.setObjectLocation(stemCell, newloc);

				lastloc = newloc;
				Stoppable stoppable = schedule.scheduleRepeating(stemCell);
				stemCell.setStoppable(stoppable);
				// x+=basalDensity; // in any case jump a step to the right to
				// avoid overlay of stem cells
				GlobalStatistics.getInstance().inkrementActualNumberStemCells();
				GlobalStatistics.getInstance().inkrementActualNumberKCytes();
			}
	}

}
 
 
 
 public void start() {

		super.start(reloadedSnapshot);

		if(!reloadedSnapshot){
			allCells.clear();
			// set up the C2dHerd field. It looks like a discretization
			// of about neighborhood / 1.5 is close to optimal for us. Hmph,
			// that's 16 hash lookups! I would have guessed that
			// neighborhood * 2 (which is about 4 lookups on average)
			// would be optimal. Go figure.
			
			//TODO: plus 2 Korrektur überprüfen
			cellContinous2D.clear();
			basementContinous2D.clear();
			rulerContinous2D.clear();
		   gridContinous2D.clear();
						
	     basementContinous2D.setObjectLocation("DummyObjektForDrawingTheBasementMembrane", new Double2D(50, 50));
	     rulerContinous2D.setObjectLocation("DummyObjektForDrawingTheRuler", new Double2D(50, 50));
	     gridContinous2D.setObjectLocation("DummyObjektForDrawingTheGrid", new Double2D(50, 50));
			
			
			
			
			allocatedKCytes = 0; // allocated memory
			GlobalStatistics.getInstance().reset();
	
			// seeding the stem cells
			seedStemCells();
			
			
			
			if(this.chartSteppables != null){
				for(EnhancedSteppable steppable: this.chartSteppables){
			   	schedule.scheduleRepeating(steppable, steppable.getInterval());
			   }
			}
			
			GlobalStatistics.getInstance().reset();
			EnhancedSteppable globalStatisticsSteppable = GlobalStatistics.getInstance().getUpdateSteppable(this.allCells);
			schedule.scheduleRepeating(globalStatisticsSteppable, globalStatisticsSteppable.getInterval());
			
			
			
			// BackImageClass backImage=new BackImageClass(this);
			// schedule.scheduleOnce(backImage);

			gStatistics_KCytes_MeanAge = 0;
			gStatistics_Barrier_ExtCalcium = 0;
			gStatistics_Barrier_IntCalcium = 0;
			gStatistics_Barrier_Lamella = 0;
			gStatistics_Barrier_Lipids = 0;
			
			ChartController.getInstance().clearAllSeries();
/*
			epiSimCharts.getXYSeries("ChartSeries_Kinetics_MeanCycleTime").clear(); // remove
																											// previous
																											// (X,Y)
																											// pairs
																											// from
																											// the
																											// chart
			epiSimCharts.getXYSeries("ChartSeries_Kinetics_GrowthFraction").clear(); // remove
																												// previous
																												// (X,Y)
																												// pairs
																												// from
																												// the
																												// chart
			epiSimCharts.getXYSeries("ChartSeries_Kinetics_Turnover").clear(); // remove
																										// previous
																										// (X,Y)
																										// pairs
																										// from
																										// the
																										// chart
			epiSimCharts.getXYSeries("ChartSeries_KCyte_All").clear(); // remove
																							// previous
																							// (X,Y)
																							// pairs
																							// from the
																							// chart
			epiSimCharts.getXYSeries("ChartSeries_KCyte_Spi").clear();
			epiSimCharts.getXYSeries("ChartSeries_KCyte_TA").clear();
			epiSimCharts.getXYSeries("ChartSeries_KCyte_LateSpi").clear();
			epiSimCharts.getXYSeries("ChartSeries_KCyte_Granu").clear();
			epiSimCharts.getXYSeries("ChartSeries_KCyte_NoNuc").clear();
			epiSimCharts.getXYSeries("ChartSeries_KCyte_MeanAgeDate").clear();
			epiSimCharts.getXYSeries("ChartSeries_Barrier_Calcium").clear(); // remove
																									// previous
																									// (X,Y)
																									// pairs
																									// from
																									// the
																									// chart
			epiSimCharts.getXYSeries("ChartSeries_Barrier_Lamella").clear();
			epiSimCharts.getXYSeries("ChartSeries_Barrier_Lipids").clear();
			epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Basal").clear(); // remove
																									// previous
																									// (X,Y)
																									// pairs
																									// from
																									// the
																									// chart
			epiSimCharts.getXYSeries("ChartSeries_Apoptosis_EarlySpi").clear();
			epiSimCharts.getXYSeries("ChartSeries_Apoptosis_LateSpi").clear();
			epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Granu").clear();
			epiSimCharts.getXYSeries("ChartSeries_Apoptosis_Basal").clear();*/
		}
		else{

			Iterator iter = allCells.iterator();

			while (iter.hasNext()){
				Object obj = iter.next();
				if(obj instanceof KCyte){
					KCyte kcyte = (KCyte) obj;

					kcyte.reloadControllers();

				}
			}
		}
     // ///////////////////////////////
     // charts
     // ///////////////////////////////
     
 
     //////////////////////////////////////        
     // CHART Updating Kinetics Chart
     //////////////////////////////////////
     // clear is necessary for restart of simulation
   

    
    
    /* 
     
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
    */ 
     
/*
     
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
                     if (act.getKeratinoType()!= modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_STEM"))
                         HistoAgeAvg[histobin]+=act.getKeratinoAge();
                     
                     if(act.getKeratinoType() == modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_TA")) 
                     { IntCal_TA+=act.getOwnSigInternalCalcium(); 
                       ExtCal_TA+=act.getOwnSigExternalCalcium(); 
                       Lam_TA+=act.getOwnSigLamella(); 
                       Lip_TA+=act.getOwnSigLipids();  
                      }
                     else if(act.getKeratinoType() ==modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_SPINOSUM")){ 
                     	IntCal_Spi+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_Spi+=act.getOwnSigExternalCalcium(); 
                     	Lam_Spi+=act.getOwnSigLamella(); 
                     	Lip_Spi+=act.getOwnSigLipids(); 
                     }
                     else if(act.getKeratinoType() == modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_LATESPINOSUM")) { 
                     	IntCal_LateSpi+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_LateSpi+=act.getOwnSigExternalCalcium(); 
                     	Lam_LateSpi+=act.getOwnSigLamella(); 
                     	Lip_LateSpi+=act.getOwnSigLipids(); 
                     }
                     else if(act.getKeratinoType() == modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_GRANULOSUM")){ 
                     	IntCal_Granu+=act.getOwnSigInternalCalcium(); 
                     	ExtCal_Granu+=act.getOwnSigExternalCalcium(); 
                     	Lam_Granu+=act.getOwnSigLamella(); 
                     	Lip_Granu+=act.getOwnSigLipids(); 
                     }
                     else if(act.getKeratinoType() == modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_NONUCLEUS")){ 
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


  */           
 //////////////////////////////////////        
 // CELL STATISTICS & Updating OUTER SURFACE CELLS
 //////////////////////////////////////  

 // the agent that updates the isOuterSurface Flag for the surface exposed cells
     Steppable airSurface = new Steppable()
    {
             public void step(SimState state)
             {
                 int MAX_XBINS=300; // for every 3 x coordinates one bin
                 CellType[] xLookUp=new CellType[MAX_XBINS];                                         
                 double [] yLookUp=new double[MAX_XBINS]; // Concentrations *10 = 0 to 200
                 boolean [] LookUpUsed=new boolean[MAX_XBINS]; 
                 for (int k=0; k< MAX_XBINS; k++)
                 {
                     yLookUp[k]=9999.9; // deepest value, all coming are above
                     xLookUp[k]=null;
                 }
                 gStatistics_KCytes_MeanAge=0;
                 gStatistics_Barrier_ExtCalcium=0;
                 gStatistics_Barrier_Lipids=0;
                 gStatistics_Barrier_Lamella=0;
                 int OldNumOuterCells=0;                    
                                 
                 
                 for (int i=0; i<allCells.size(); i++)
                 {
                     // iterate through all cells and determine the KCyte with lowest Y at bin
                     CellType act=(CellType)allCells.get(i);
                     if (act.isInNirvana()) continue;
                     // is a living cell..
            /*         
                     if (act.isOuterCell()) // statistics from last time evaluation (so we are always lacking behind one calling period !)
                     {
                         gStatistics_Barrier_ExtCalcium+=act.getOwnSigExternalCalcium();
                         gStatistics_Barrier_Lamella+=act.getOwnSigLamella();
                         gStatistics_Barrier_Lipids+=act.getOwnSigLipids();                            
                         OldNumOuterCells++;
                     }
                  */   
                   //  if (act.isBasalStatisticsCell()) actualBasalStatisticsCells++;
                     
                     //act.isOuterCell=false; // set new default 
                     Double2D loc=cellContinous2D.getObjectLocation(act);
                     int xbin=(int)loc.x/InitialKeratinoSize;
                     if (xLookUp[xbin]==null) 
                     {
                         xLookUp[xbin]=act;                            
                         yLookUp[xbin]=loc.y;
                     }
                     else
                         if (loc.y<yLookUp[xbin]) 
                         {
                             xLookUp[xbin]=act;
                             yLookUp[xbin]=loc.y;
                         }
                     /*
                     // other statistics
                     if ((act.getKeratinoType()!=modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_STEM")) 
                     		  && (act.getKeratinoType()!=modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_NONUCLEUS")))
                     {
                         gStatistics_KCytes_MeanAge+=act.getKeratinoAge();  
                         if (act.getKeratinoAge()>modelController.getBioChemicalModelController().getIntField("maxCellAge_t"))
                             {
                                 System.out.println("Age Error");
                             }
                     }*/
                 }            

                 for (int k=0; k< MAX_XBINS; k++)
                 {
                     if ((xLookUp[k]==null) || (xLookUp[k].getEpisimCellDiffModelObject().getDifferentiation()==EpisimCellDiffModelGlobalParameters.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
                     xLookUp[k].setIsOuterCell(true);
                 }
                 // other statistics
                 /*
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
*/
             }
     };
     // Schedule the agent to update is Outer Flag
     
    // schedule.scheduleRepeating(airSurface, 1);
     


	 	//////////////////////////////////////
	 	// Time Updater
	 	//////////////////////////////////////
	  	Steppable timeUpdater= new Steppable()
	  	{
	     public void step(SimState state)
	     {            	
	   	  setTimeInSimulationSteps(state.schedule.getSteps());
	     }
	  	};
	  	schedule.scheduleRepeating(timeUpdater, 1);
 	}

	public void removeCells(GeneralPath path){
	Iterator<CellType> iter = allCells.iterator();
	Map<Long, Double2D> map = new HashMap<Long, Double2D>();
	List<CellType> livingCells = new LinkedList<CellType>();
		int i = 0;
		while(iter.hasNext()){
			CellType cell = iter.next();
	
			if(path.contains(cell.getLastDrawInfoX(), cell.getLastDrawInfoY())&&
					cell.getEpisimCellDiffModelObject().getDifferentiation() != EpisimCellDiffModelGlobalParameters.STEMCELL){  
				cell.killCell();
				 
				  i++;
			}
			else{
				 livingCells.add(cell);
				 map.put(cell.getIdentity(), this.cellContinous2D.getObjectLocation(cell));
			}
		}
		
	this.allCells.clear();
	this.cellContinous2D.clear();
		for(CellType cell: livingCells){
			this.allCells.add(cell);
			this.cellContinous2D.setObjectLocation(cell, map.get(cell.getIdentity()));
		}
		
	}




 
//---------------------------------------------------------------------------------------------------------------------------------------------------
//INKREMENT-DEKREMENT-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 	public void inkrementNumberOfKCytes(){allocatedKCytes +=1;}
	public void dekrementAllocatedKCytes(){allocatedKCytes -=1;}
	
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//GETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 

	
	public GenericBag<CellType> getAllCells() {	return allCells; }
	public int getNumberOfKCytes() { return allocatedKCytes; }
	public static List <Class<? extends CellType>> getAvailableCellTypes; 
	
	public Continuous2D getBasementContinous2D() { return basementContinous2D; }
	
	public Continuous2D getCellContinous2D() { return cellContinous2D; }
	public double getConsistency() { return consistency; }
	
	public int getGCorneumY() { return gCorneumY; }
	public Continuous2D getGridContinous2D() { return gridContinous2D; }
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
	
	public Continuous2D getRulerContinous2D() { return rulerContinous2D; }
	
	public long getTimeInSimulationSteps(){ return this.timeInSimulationSteps;}

	public String getTissueName() {return NAME;}
	
	public boolean isDevelopGranulosum() {	return developGranulosum; }
	
	//complex-Methods------------------------------------------------------------------------------------------------------------------
	
	public List<SnapshotObject> collectSnapshotObjects() {
		
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
	 
 
	
	public void setAllCells(GenericBag<CellType> allCells) { this.allCells = allCells; }
	public void setAllocatedKCytes(int allocatedKCytes) {	this.allocatedKCytes = allocatedKCytes; }
	
	public void setBasementContinous2D(Continuous2D basementContinous2D) { this.basementContinous2D = basementContinous2D; }
	
	public void setCellContinous2D(Continuous2D cellContinous2D) { this.cellContinous2D = cellContinous2D; }
	public void setConsistency(double consistency) { this.consistency = consistency; }
	
	public void setDevelopGranulosum(boolean developGranulosum) { this.developGranulosum = developGranulosum; }
	
	
	
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
	
	public void setTimeInSimulationSteps(long time){ if(time >= 0) this.timeInSimulationSteps = time;}
	
	
	//	complex-Methods------------------------------------------------------------------------------------------------------------------
	
	
	public void setModelController(ModelController modelController) {

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



	


	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		 methods.addAll(Arrays.asList(this.biochemModelContr.getEpisimCellDiffModelGlobalParameters().getClass().getMethods()));
	    methods.addAll(Arrays.asList(this.getClass().getMethods()));
	   
		return methods;
	}



	public void chartSetHasChanged() {

		try{
	      this.chartSteppables = ChartController.getInstance().getChartSteppablesOfActLoadedChartSet(allCells, this.cellContinous2D, new Object[]{
	      	this.biochemModelContr.getEpisimCellDiffModelGlobalParameters(), 
	      	this.biomechModelContr.getEpisimMechanicalModelGlobalParameters(), 
	      	this.biomechModelContr.getEpisimMechanicalModel(),
	      	this});
      }
      catch (MissingObjectsException e){
	     ExceptionDisplayer.getInstance().displayException(e);
      }
		}



	public void cellIsDead(CellType cell) {
		this.allCells.remove(cell);
		this.cellContinous2D.remove(cell);
		
		
	}
	   
   



	
	

//	---------------------------------------------------------------------------------------------------------------------------------------------------
//	--------------------------------------------------------------------------------------------------------------------------------------------------- 

 }





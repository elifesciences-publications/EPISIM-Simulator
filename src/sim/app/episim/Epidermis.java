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
import sim.app.episim.tissue.TissueController;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
	 
	
	 
   private double consistency=0.0;
   private double minDist=0.1;    
	private boolean developGranulosum=true;
	
	    
	
	private double gStatistics_KCytes_MeanAge=0;
	
	
	
	
	
	private int    gStatistics_GrowthFraction=0;             // Percentage
	private double gStatistics_TurnoverTime=0;             // Percentage
	
	private List<EnhancedSteppable> chartSteppables = null;
	
	private List<EnhancedSteppable> dataExportSteppables = null;
	
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
     DataExportController.getInstance().registerDataExportChangeListener(this);
  	// set up the C2dHerd field. It looks like a discretization
		// of about neighborhood / 1.5 is close to optimal for us. Hmph,
		// that's 16 hash lookups! I would have guessed that
		// neighborhood * 2 (which is about 4 lookups on average)
		// would be optimal. Go figure.
		
		//TODO: plus 2 Korrektur überprüfen
		cellContinous2D = new Continuous2D(biomechModelContr.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm() / 1.5, 
				TissueController.getInstance().getTissueBorder().getWidth() + 2, 
				TissueController.getInstance().getTissueBorder().getHeight());
		basementContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth() + 2, 
				TissueController.getInstance().getTissueBorder().getWidth() + 2, 
				TissueController.getInstance().getTissueBorder().getHeight());
		rulerContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth()+2,
				TissueController.getInstance().getTissueBorder().getWidth()+2,
				TissueController.getInstance().getTissueBorder().getHeight());
	   gridContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth()+2,
	   		TissueController.getInstance().getTissueBorder().getWidth()+2,
	   		TissueController.getInstance().getTissueBorder().getHeight());
 }

 

 public final double depthFrac(double y) // wie tief ist in prozent die uebergebene y-position relativ zu retezapfen tiefe
 {
     return (y-TissueController.getInstance().getTissueBorder().getUndulationBaseLine())/modelController.getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getBasalAmplitude_µm();                
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
	Double2D lastloc = new Double2D(2, TissueController.getInstance().getTissueBorder().lowerBound(2));
	for(double x = 2; x <= TissueController.getInstance().getTissueBorder().getWidth(); x += 2){
		Double2D newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBound(x));
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
				Stoppable stoppable = schedule.scheduleRepeating(stemCell, 1, 1);
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
			ChartController.getInstance().clearAllSeries();
			DataExportController.getInstance().newSimulationRun();
			
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
			GlobalStatistics.getInstance().reset(true);
	
			// seeding the stem cells
			seedStemCells();
			
			
			
			if(this.chartSteppables != null){
				for(EnhancedSteppable steppable: this.chartSteppables){
			   	schedule.scheduleRepeating(steppable, 4, steppable.getInterval());
			   }
			}
			

			if(this.dataExportSteppables != null){
				for(EnhancedSteppable steppable: this.dataExportSteppables){
			   	schedule.scheduleRepeating(steppable, 4, steppable.getInterval());
			   }
			}
			
			
			EnhancedSteppable globalStatisticsSteppable = GlobalStatistics.getInstance().getUpdateSteppable(this.allCells);
			schedule.scheduleRepeating(globalStatisticsSteppable, 3, globalStatisticsSteppable.getInterval());
			
			
			
			// BackImageClass backImage=new BackImageClass(this);
			// schedule.scheduleOnce(backImage);

			gStatistics_KCytes_MeanAge = 0;
			
			
			
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
     
    
    /* 
     
     
    

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
                
                                     
                                 
                 
                 for (int i=0; i<allCells.size(); i++)
                 {
                     // iterate through all cells and determine the KCyte with lowest Y at bin
                     CellType act=(CellType)allCells.get(i);
                     if (act.isInNirvana()) continue;
                     // is a living cell..
                     
                     
                     
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
                 
               //  gStatistics_KCytes_MeanAge/=actualKCytes-actualNoNucleus;
                 

             }
     };
     // Schedule the agent to update is Outer Flag     
     schedule.scheduleRepeating(airSurface,2,1);
     
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
		
	public int getIndividualColor() { return individualColor; }
	
	public double getMinDist() { return minDist; }
	
	public Continuous2D getRulerContinous2D() { return rulerContinous2D; }
	
	public long getTimeInSimulationSteps(){ return schedule.getSteps();}

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
			if(allCells != null && this.cellContinous2D != null && this.biochemModelContr.getEpisimCellDiffModelGlobalParameters() != null
					&& this.biomechModelContr.getEpisimMechanicalModelGlobalParameters() != null
					&& this.biomechModelContr.getEpisimMechanicalModel() != null){
		      this.chartSteppables = ChartController.getInstance().getChartSteppablesOfActLoadedChartSet(allCells, this.cellContinous2D, new Object[]{
		      	this.biochemModelContr.getEpisimCellDiffModelGlobalParameters(), 
		      	this.biomechModelContr.getEpisimMechanicalModelGlobalParameters(), 
		      	this.biomechModelContr.getEpisimMechanicalModel(),
		      	this});
		   }
      }
      catch (MissingObjectsException e){
	     ExceptionDisplayer.getInstance().displayException(e);
      }
		}



	public void cellIsDead(CellType cell) {
		this.allCells.remove(cell);
		this.cellContinous2D.remove(cell);
		
		
	}



	public void dataExportHasChanged() {

	   try{
	      this.dataExportSteppables = DataExportController.getInstance().getDataExportSteppablesOfActLoadedChartSet(getAllCells(), getBasementContinous2D(), new Object[]{
	         	this.biochemModelContr.getEpisimCellDiffModelGlobalParameters(), 
	         	this.biomechModelContr.getEpisimMechanicalModelGlobalParameters(), 
	         	this.biomechModelContr.getEpisimMechanicalModel(),
	         	this});
      }
      catch (MissingObjectsException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      }
	   
   }



	
	   
   



	
	

//	---------------------------------------------------------------------------------------------------------------------------------------------------
//	--------------------------------------------------------------------------------------------------------------------------------------------------- 

 }





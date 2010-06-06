package sim.app.episim;


import sim.DummyCellType;
import sim.app.episim.biomechanics.Calculators;
import sim.app.episim.biomechanics.CellPolygon;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;

import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.BioMechanicalModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.TysonRungeCuttaCalculator;
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
import java.lang.reflect.Field;
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
		
	public final String NAME ="Epidermis";
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
// VARIABLES
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	private boolean reloadedSnapshot = false;

	

	private Continuous2D cellContinous2D;
	private Continuous2D basementContinous2D;
	private Continuous2D rulerContinous2D;
	private Continuous2D gridContinous2D;
   
	private GenericBag<CellType> allCells=new GenericBag<CellType>(3000); //all cells will be stored in this bag
	
	// Percentage
	
	private transient List<EnhancedSteppable> chartSteppables = null;
	
	private transient List<EnhancedSteppable> dataExportSteppables = null;
	
	private TimeSteps timeStepsAfterSnapshotReload = null;
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public Epidermis(long seed)
 {
     super(seed);
     
     
     SnapshotWriter.getInstance().addSnapshotListener(this);
     this.registerCellType(KCyte.class);
     this.registerCellType(DummyCell.class);
     
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
		cellContinous2D = new Continuous2D(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm() / 1.5, 
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

 

 public final double depthFrac(double y) // wie tief ist in prozent die uebergebene y-position relativ zu rete tiefe
 {
     return (y-TissueController.getInstance().getTissueBorder().getUndulationBaseLine())/ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getBasalAmplitude_µm();                
 }

 
 
 public void checkMemory(){
	 // Memory Management
    if (GlobalStatistics.getInstance().getActualNumberKCytes()>getAllCells().size()-50) // for safety -50
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

		if((depthFrac(newloc.y) > ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters()
				.getSeedMinDepth_frac() && (!ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters()
				.getSeedReverse()))
				|| (depthFrac(newloc.y) < ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters()
						.getSeedMinDepth_frac() && ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters()
						.getSeedReverse()))
			if(distance > ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getBasalDensity_µm()){

				// TODO: Check creation of Stem Cells
				KCyte stemCell = new KCyte(CellType.getNextCellId(),-1, ModelController.getInstance().getBioChemicalModelController().getNewEpisimCellDiffModelObject());
				// stemCell.setKeratinoType(modelController.getBioChemicalModelController().getGlobalIntConstant("KTYPE_STEM"));
				stemCell.setOwnColor(10);
				int cellCyclePos = random.nextInt(ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getCellCycleStem());
				
				//assign random age
				stemCell.getEpisimCellDiffModelObject().setAge((double)(cellCyclePos));// somewhere in the stemcellcycle
				TysonRungeCuttaCalculator.assignRandomCellcyleState(stemCell.getEpisimCellDiffModelObject(), cellCyclePos);																																		// on
																																						
				stemCell.getEpisimCellDiffModelObject().setDifferentiation(EpisimCellDiffModelGlobalParameters.STEMCELL);
				stemCell.getEpisimCellDiffModelObject().setSpecies(EpisimCellDiffModelGlobalParameters.KERATINOCYTE);
				stemCell.getEpisimCellDiffModelObject().setIsAlive(true);
	
				stemCell.getCellEllipseObject().setXY(((int)newloc.x), ((int)newloc.y));
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

		super.start(timeStepsAfterSnapshotReload);
		ChartController.getInstance().newSimulationRun();
		DataExportController.getInstance().newSimulationRun();
		
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
		GlobalStatistics.getInstance().reset(true);
		EnhancedSteppable globalStatisticsSteppable = GlobalStatistics.getInstance().getUpdateSteppable(this.allCells);
		schedule.scheduleRepeating(globalStatisticsSteppable, 3, globalStatisticsSteppable.getInterval());
		
			
		schedule.scheduleRepeating(new Steppable(){

			public void step(SimState state) {
				
				if(MiscalleneousGlobalParameters.instance().getTypeColor() == 10){
					
					Calculators.globallyCleanAllPolygonsEstimatedVertices(CellEllipseIntersectionCalculationRegistry.getInstance().getAllCellPolygons());
					
					
					
				/*	int[] neighbourHistogram = new int[9];
					for(CellType cell: allCells){
						CellPolygon pol = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(cell.getCellEllipseObject().getId());
						if(pol != null){
							int neighbours = pol.getNumberOfNeighbourPolygons();
							if(neighbours < 1) neighbours = 1;
							if(neighbours > 9) neighbours = 9;
							neighbours -= 1;
							neighbourHistogram[neighbours]++;
						}
					}
					 try {
			           BufferedWriter out = new BufferedWriter(new FileWriter("d:\\cellNeighbourEvaluation.csv", true));			       
			           out.write("Zellanzahl;1 Nachbar;2 Nachbarn;3 Nachbarn;4 Nachbarn;5 Nachbarn;6 Nachbarn;7 Nachbarn;8 Nachbarn;9 Nachbarn;\n");
			           out.write(""+allCells.size()+";");
			           for(int i= 0; i< neighbourHistogram.length; i++){
			         	  out.write(""+ neighbourHistogram[i]+";");
			           }
			                   
			           out.write("\n");
			           out.flush();
			           out.close();
			            } catch (IOException e) {e.printStackTrace();}
			            */
					
				}
				
			}}, 5, 1);	
			
			
			basementContinous2D.clear();
			rulerContinous2D.clear();
		   gridContinous2D.clear();
						
	     basementContinous2D.setObjectLocation("DummyObjektForDrawingTheBasementMembrane", new Double2D(50, 50));
	     rulerContinous2D.setObjectLocation("DummyObjektForDrawingTheRuler", new Double2D(50, 50));
	     gridContinous2D.setObjectLocation("DummyObjektForDrawingTheGrid", new Double2D(50, 50));
			
     
   

	     
	     
	     if(!reloadedSnapshot){
	   	  cellContinous2D.clear();
	   	  allCells.clear();
	   	  seedStemCells();
	     }
		  else{
							 
			     for(CellType cell: this.allCells){		   	  
			   		
			   		schedule.scheduleRepeating(cell, 1, 1);
			   		if(cell instanceof KCyte){
			   			KCyte kcyte = (KCyte) cell;
			   			
			   			kcyte.removeCellDeathListener();
			   			kcyte.addCellDeathListener(this);
			   			kcyte.addCellDeathListener(GlobalStatistics.getInstance());
			   		}
			   		
			     }
			}
	     
	     
	     
        
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
                 
                
                                     
                                 
                 
                 for (int i=0; i<allCells.size(); i++)
                 {
                     // iterate through all cells and determine the KCyte with lowest Y at bin
                     CellType act=(CellType)allCells.get(i);
                     if (act.isInNirvana()) continue;
                     // is a living cell..
                     
                     
                     
                   //  if (act.isBasalStatisticsCell()) actualBasalStatisticsCells++;
                     
                     //act.isOuterCell=false; // set new default 
                     Double2D loc=cellContinous2D.getObjectLocation(act);
                     
                     int xbin=(int)loc.x/ KCyte.GINITIALKERATINOWIDTH;
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
	
			if(path.contains(cell.getCellEllipseObject().getLastDrawInfo2D().draw.x, cell.getCellEllipseObject().getLastDrawInfo2D().draw.y)&&
					cell.getEpisimCellDiffModelObject().getDifferentiation() != EpisimCellDiffModelGlobalParameters.STEMCELL){  
				cell.killCell();
				 
				  i++;
			}
			else{
				 livingCells.add(cell);
				 map.put(cell.getID(), this.cellContinous2D.getObjectLocation(cell));
			}
		}
		
	this.allCells.clear();
	this.cellContinous2D.clear();
		for(CellType cell: livingCells){
			this.allCells.add(cell);
			this.cellContinous2D.setObjectLocation(cell, map.get(cell.getID()));
		}
		
	}




 
//---------------------------------------------------------------------------------------------------------------------------------------------------
//INKREMENT-DEKREMENT-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	 
 	
	
	
	
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//GETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 

	
	public GenericBag<CellType> getAllCells() {	return allCells; }
	
	public static List <Class<? extends CellType>> getAvailableCellTypes; 
	
	public Continuous2D getBasementContinous2D() { return basementContinous2D; }
	
	public Continuous2D getCellContinous2D() { return cellContinous2D; }
	
	
	
	public Continuous2D getGridContinous2D() { return gridContinous2D; }
	
		
	
	
	
	
	public Continuous2D getRulerContinous2D() { return rulerContinous2D; }
	
	

	public String getTissueName() {return NAME;}
	
	
	
	//complex-Methods------------------------------------------------------------------------------------------------------------------
	
	public List<SnapshotObject> collectSnapshotObjects() {
		
		List<SnapshotObject> list = new LinkedList<SnapshotObject>();
		Iterator<CellType> iter = allCells.iterator();
		
		while(iter.hasNext()){
			list.add(new SnapshotObject(SnapshotObject.CELL, iter.next()));
		}
		
		list.add(new SnapshotObject(SnapshotObject.CELLCONTINUOUS, this.cellContinous2D));
		list.add(new SnapshotObject(SnapshotObject.TIMESTEPS, new TimeSteps(schedule.getTime(), schedule.getSteps())));
		return list;
	}  
	
	public void addSnapshotLoadedCells(List<CellType> cells) { this.allCells.addAll(cells); }
	
//---------------------------------------------------------------------------------------------------------------------------------------------------
//SETTER-METHODS
//--------------------------------------------------------------------------------------------------------------------------------------------------- 
	
	
	
	
	public void setBasementContinous2D(Continuous2D basementContinous2D) { this.basementContinous2D = basementContinous2D; }
	
	public void setCellContinous2D(Continuous2D cellContinous2D) { this.cellContinous2D = cellContinous2D; }
	
	
	

	
	

	
	
	
	
	public void setReloadedSnapshot(boolean reloadedSnapshot) {	this.reloadedSnapshot = reloadedSnapshot; }
	
	
	
	
	//	complex-Methods------------------------------------------------------------------------------------------------------------------
	
	
	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		 methods.addAll(Arrays.asList(ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getClass().getMethods()));
	    methods.addAll(Arrays.asList(this.getClass().getMethods()));
	   
		return methods;
	}
	
	public List<Field> getContants() {	
		List<Field> fields = new ArrayList<Field>();
		for(Field field : ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getClass().getFields()){
	   		if(!field.getDeclaringClass().isInterface()) fields.add(field);
		}
		
		return fields;
	}



	public void chartSetHasChanged() {

		try{
			if(allCells != null && this.cellContinous2D != null && ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters() != null
					&& ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters() != null
					&& ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel() != null){
		      this.chartSteppables = ChartController.getInstance().getChartSteppablesOfActLoadedChartSet(allCells, this.cellContinous2D, new Object[]{
		      		ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters(), 
		      		ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters(), 
		      		ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel(),
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
	
	public void setSnapshotTimeSteps(TimeSteps timeSteps){
		this.timeStepsAfterSnapshotReload = timeSteps;
	}


	public void dataExportHasChanged() {

	   try{
	      this.dataExportSteppables = DataExportController.getInstance().getDataExportSteppablesOfActLoadedChartSet(getAllCells(), getBasementContinous2D(), new Object[]{
	      	ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters(), 
	      	ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters(), 
	      	ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel(),
	         	this});
      }
      catch (MissingObjectsException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      }
	   
   }



	
	   
   



	
	

//	---------------------------------------------------------------------------------------------------------------------------------------------------
//	--------------------------------------------------------------------------------------------------------------------------------------------------- 

	
	
	
 }





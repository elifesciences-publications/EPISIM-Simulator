package sim.app.episim.visualization.twodim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.DiffusionColorGradient;
import sim.app.episim.visualization.ExtraCellularDiffusionPortrayal;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;
import episiminterfaces.EpisimPortrayal;


public class ExtraCellularDiffusionPortrayal2D extends FastValueGridPortrayal2D implements ExtraCellularDiffusionPortrayal{
	
	
	
	private String name;
	private ExtraCellularDiffusionField2D extraCellularDiffusionField;
	
	
	private Color[] legendLookUpTable;
	
	private double minValue=0;
	private double maxValue=0;
	
	public ExtraCellularDiffusionPortrayal2D(ExtraCellularDiffusionField diffusionField){
		super(diffusionField.getName(), false);
		this.name = diffusionField.getName();
		if(diffusionField instanceof ExtraCellularDiffusionField2D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField2D)diffusionField;
		else throw new IllegalArgumentException("diffusionField must be of type ExtraCellularDiffusionField2D");
		this.setField(this.extraCellularDiffusionField .getExtraCellularField());
		this.setMap(buildColorMap());
		
	}
	
	
	 public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
		 minValue = extraCellularDiffusionField.getFieldConfiguration().getMinimumConcentration();
	    double newMaxValue = extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
	   							? extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration()
	   							: extraCellularDiffusionField.getExtraCellularField().max();		 
		 if(newMaxValue > maxValue) this.maxValue = newMaxValue;
		 this.setMap(buildColorMap());		 
		 super.draw(object, graphics, info);
		 if(MiscalleneousGlobalParameters.getInstance().getShowDiffusionFieldLegend()){
			 drawLegend(graphics, info);
		 }
	 }
	
	
	
   public ExtraCellularDiffusionField2D getExtraCellularDiffusionField() {
	   return extraCellularDiffusionField;
   }   
   
   public void setExtraCellularDiffusionField(ExtraCellularDiffusionField extraCellularDiffusionField) {
   	 if(extraCellularDiffusionField == null) this.extraCellularDiffusionField = null;
       else if(extraCellularDiffusionField instanceof ExtraCellularDiffusionField2D)this.extraCellularDiffusionField = (ExtraCellularDiffusionField2D)extraCellularDiffusionField;
	   if(extraCellularDiffusionField != null){
	   	this.name = extraCellularDiffusionField.getName();
	   	this.setField(this.extraCellularDiffusionField.getExtraCellularField());
	   }
   }
	
	
	public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP, guiState.getEpiDisplayWidth(), guiState.getEpiDisplayHeight());
 	   else return new Rectangle2D.Double(0,0,0, 0);
   }

   public String getPortrayalName() {	   
   	return this.name;
   }
   
   
   
   private SimpleColorMap buildColorMap(){   	
   	Color[] colorTable = DiffusionColorGradient.createMultiGradient(COLOR_STEPS, (int)(maxValue-minValue+1));
      legendLookUpTable = new Color[colorTable.length];
   	for(int i = 0; i < colorTable.length; i++){
   		legendLookUpTable[i]= colorTable[i];
   		if(i< (colorTable.length*MiscalleneousGlobalParameters.getInstance().getDiffusionFieldColoringMinThreshold()))colorTable[i] = new Color(colorTable[i].getRed(), colorTable[i].getGreen(), colorTable[i].getBlue(),0);
   		else colorTable[i] = new Color(colorTable[i].getRed(), colorTable[i].getGreen(), colorTable[i].getBlue(),MiscalleneousGlobalParameters.getInstance().getDiffusionFieldOpacity()); 
      }
   	for(int i = 0; i < (colorTable.length*0.1); i++){
   		
   	}
   	return new SimpleColorMap(colorTable);
   }
   
   private void drawLegend(Graphics2D graphics, DrawInfo2D info){
   	final double height = info.draw.height;   	
   	
   	final double legendBorderY=30;
   	final double legendBorderX=10;
   	final double legendWidth=10;
   	
   	final double legendX = info.draw.x + info.draw.width+legendBorderX;
   	final double legendY = info.draw.y+legendBorderY;
   	
   	
   	
   	graphics.setColor(new Color(192, 192, 192, 150));
   	graphics.drawRect((int)legendX, (int)legendY, (int)legendWidth, (int)(height - (2*legendBorderY)));
       for (int y = 0; y < height - (2*legendBorderY+1); y++)
       {           
           int yStart = (int)(legendY + height - (2*legendBorderY+1) - y);
           graphics.setColor(legendLookUpTable[(int) ((y / (double) (height - (2*legendBorderY))) * (legendLookUpTable.length * 1.0))]);
           graphics.fillRect((int)(legendX +1), yStart, (int)(legendWidth-1), 1);
       }
       graphics.setColor(Color.white);
       
       double intervalSizeY = (height - (2*legendBorderY))/ (COLOR_STEPS.length-1);
       double intervalSizeConcentration = (maxValue-minValue)/ (COLOR_STEPS.length-1);
       String text ="";
       graphics.setFont(new Font("Arial", Font.PLAIN, 10));
       NumberFormat format = new DecimalFormat("0.0E0");
      // format.setMaximumIntegerDigits(3);
       for(double y = 0; y < COLOR_STEPS.length; y++){
      	 double currentConcentration = maxValue-(y*intervalSizeConcentration);
      	
      	 text = maxValue>=1000?format.format(currentConcentration): ""+((int) currentConcentration);
      	 Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
			 graphics.drawString(text, (float)(legendX+ legendWidth+5), (float)(legendY+ (y*intervalSizeY)+(stringBounds.getHeight()/3)));
       }
   }
   
   public Inspector getInspector(LocationWrapper wrapper, GUIState state){ return null; }
   
   
   
}

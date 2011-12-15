package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import episiminterfaces.EpisimPortrayal;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.DiffusionColorGradient;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.SimpleColorMap;


public class ExtraCellularDiffusionPortrayal extends FastValueGridPortrayal2D implements EpisimPortrayal{
	
	private static final Color[] COLOR_STEPS = new Color[]{Color.black, Color.blue, Color.cyan, Color.green, Color.yellow, Color.orange, Color.red};
	
	private String name;
	private ExtraCellularDiffusionField extraCellularDiffusionField;
	
	private double previousMaxValue;
	private Color[] legendLookUpTable;
	
	private double minValue=0;
	private double maxValue=0;
	
	public ExtraCellularDiffusionPortrayal(ExtraCellularDiffusionField diffusionField){
		super(diffusionField.getName(), false);
		this.name = diffusionField.getName();
		this.extraCellularDiffusionField = diffusionField;
		this.setField(diffusionField.getExtraCellularField());
		this.setMap(buildColorMap());
		
	}
	
	
	 public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
		 minValue = extraCellularDiffusionField.getFieldConfiguration().getMinimumConcentration();
	    maxValue = extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration() < Double.POSITIVE_INFINITY 
	   							? extraCellularDiffusionField.getFieldConfiguration().getMaximumConcentration()
	   							: extraCellularDiffusionField.getExtraCellularField().max();
		 
		 
		 
		 this.setMap(buildColorMap());		 
		 super.draw(object, graphics, info);
		 if(MiscalleneousGlobalParameters.instance().isShowDiffusionFieldLegend()){
			 drawLegend(graphics, info);
		 }
	 }
	
	
	
   public ExtraCellularDiffusionField getExtraCellularDiffusionField() {
	   return extraCellularDiffusionField;
   }   
   
   public void setExtraCellularDiffusionField(ExtraCellularDiffusionField extraCellularDiffusionField) {
	   this.extraCellularDiffusionField = extraCellularDiffusionField;
	   if(extraCellularDiffusionField != null){
	   	this.name = extraCellularDiffusionField.getName();
	   	this.setField(extraCellularDiffusionField.getExtraCellularField());
	   }
   }
	
	
	public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
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
      	colorTable[i] = new Color(colorTable[i].getRed(), colorTable[i].getGreen(), colorTable[i].getBlue(),MiscalleneousGlobalParameters.instance().getDiffusionFieldOpacity()); 
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
       for(double y = 0; y <= COLOR_STEPS.length; y++){
      	 double currentConcentration = maxValue-(y*intervalSizeConcentration);
      	
      	 text = maxValue>=1000?format.format(currentConcentration): ""+((int) currentConcentration);
      	 Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
			 graphics.drawString(text, (float)(legendX+ legendWidth+5), (float)(legendY+ (y*intervalSizeY)+(stringBounds.getHeight()/3)));
       }
   }
   
   
   
}

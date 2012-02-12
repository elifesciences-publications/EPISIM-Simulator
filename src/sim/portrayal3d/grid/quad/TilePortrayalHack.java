package sim.portrayal3d.grid.quad;

import java.awt.Color;

import javax.vecmath.Color4f;

import sim.util.gui.ColorMap;


public class TilePortrayalHack extends TilePortrayal {
	
	private float scaleX= 1;
	private float scaleY= 1;

	 public TilePortrayalHack(ColorMap colorDispenser, float scaleX, float scaleY)
    {
	    super(colorDispenser, 0);
	    this.scaleX = scaleX;
	    this.scaleY = scaleY;
    }
	 
	 public void setData(ValueGridCellInfo gridCell, float[] coordinates, Color4f[] colors, int quadIndex, int gridWidth, int gridHeight){
	        int x = gridCell.x;
	        int y = gridCell.y;
	        float value = (float)gridCell.value();
	        Color c = colorDispenser.getColor(value);
	        Color4f c4f = new Color4f(c);
	        c4f.setW((((float)colorDispenser.getAlpha(value))/ 255.0f));
	        
	       
	        value*=zScale;
	        
	        for(int i=0;i <4;i++) 
	           colors[(quadIndex*4+i)] = c4f;  // 3 color values -- alpha transparency doesn't work here :-(

	        int offset = quadIndex*12;
	        float translationConstantX = scaleX/2;
	        float translationConstantY = scaleY/2;
	        float factorX = 0.5f*scaleX;
	        float factorY = 0.5f*scaleY;
	        coordinates[offset+0] = scaleX*x - factorX + translationConstantX;
	        coordinates[offset+1] = scaleY*y- factorY + translationConstantY;
	        coordinates[offset+2] = value;
	        coordinates[offset+3] = scaleX*x + factorX + translationConstantX;
	        coordinates[offset+4] = scaleY*y - factorY + translationConstantY;
	        coordinates[offset+5] = value;
	        coordinates[offset+6] = scaleX*x + factorX + translationConstantX;
	        coordinates[offset+7] = scaleY*y + factorY + translationConstantY;
	        coordinates[offset+8] = value;
	        coordinates[offset+9] = scaleX*x - factorX + translationConstantX;
	        coordinates[offset+10]= scaleY*y + factorY + translationConstantY;
	        coordinates[offset+11]= value;
	  }
}

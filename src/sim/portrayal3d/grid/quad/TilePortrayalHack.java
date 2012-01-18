package sim.portrayal3d.grid.quad;

import sim.util.gui.ColorMap;


public class TilePortrayalHack extends TilePortrayal {
	
	private float scale= 1;

	 public TilePortrayalHack(ColorMap colorDispenser, float scale)
    {
	    super(colorDispenser, 0);
	    this.scale = scale;
    }
	 
	 public void setData(ValueGridCellInfo gridCell, float[] coordinates, float[] colors, int quadIndex, int gridWidth, int gridHeight){
	        int x = gridCell.x;
	        int y = gridCell.y;
	        float value = (float)gridCell.value();
	        colorDispenser.getColor(value).getComponents(tmpColor);
	        value*=zScale;
	        
	        for(int i=0;i <4;i++) 
	            System.arraycopy(tmpColor, 0, colors, (quadIndex*4+i)*3, 3);  // 3 color values -- alpha transparency doesn't work here :-(

	        int offset = quadIndex*12;
	        float translationConstant = scale/2;
	        float factor = 0.5f*scale;
	        coordinates[offset+0] = scale*x - factor + translationConstant;
	        coordinates[offset+1] = scale*y- factor + translationConstant;
	        coordinates[offset+2] = value;
	        coordinates[offset+3] = scale*x + factor + translationConstant;
	        coordinates[offset+4] = scale*y - factor + translationConstant;
	        coordinates[offset+5] = value;
	        coordinates[offset+6] = scale*x + factor + translationConstant;
	        coordinates[offset+7] = scale*y + factor + translationConstant;
	        coordinates[offset+8] = value;
	        coordinates[offset+9] = scale*x - factor + translationConstant;
	        coordinates[offset+10]= scale*y + factor + translationConstant;
	        coordinates[offset+11]= value;
	  }
}

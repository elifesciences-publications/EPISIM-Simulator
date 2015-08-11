package sim.portrayal3d.grid;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Color4f;
import javax.vecmath.Vector4f;

import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;

import sim.field.grid.DoubleGrid2D;
import sim.field.grid.Grid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.grid.quad.QuadPortrayal;
import sim.portrayal3d.grid.quad.TilePortrayal;
import sim.portrayal3d.grid.quad.TilePortrayalHack;
import sim.portrayal3d.grid.quad.ValueGridCellInfo;
import sim.util.Int2D;
import sim.util.MutableDouble;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;


public class ValueGrid2DPortrayal3DHack extends ValueGrid2DPortrayal3D {
	
	 private ValueGridCellInfo tmpGCI;
	 private Color4f[] colors4f = null;
	 public ValueGrid2DPortrayal3DHack(float scaleX, float scaleY)
    {
		 this("Value", scaleX, scaleY);
    }
	 public ValueGrid2DPortrayal3DHack(String valueName, float scaleX, float scaleY)
    {
		 this(valueName,1.0f, scaleX, scaleY);
    }
	 
	 public ValueGrid2DPortrayal3DHack(String valueName, double transparency, float scaleX, float scaleY)
    {
	    this.valueName = valueName;
	    // we make a default portrayal that goes from blue to red when going from 0 to 1,
	    // no change in height
	    sim.util.gui.SimpleColorMap cm = new sim.util.gui.SimpleColorMap();
	    cm.setLevels(0.0,1.0,java.awt.Color.blue, java.awt.Color.red);
	    defaultPortrayal = new TilePortrayalHack(cm, scaleX, scaleY);
	    this.transparency = transparency;
	
	    mPolyAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
	    mPolyAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
	    mPolyAttributes.clearCapabilityIsFrequent(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
	    mPolyAttributes.clearCapabilityIsFrequent(PolygonAttributes.ALLOW_MODE_WRITE);
    }
	 
	 public void setMap(ColorMap map){
	 		this.defaultPortrayal.setMap(map);
	 }
	 
	 protected void renewTilePortrayal(float scaleX, float scaleY){
		    
	    defaultPortrayal = new TilePortrayalHack(defaultPortrayal.getMap(), scaleX, scaleY);
	 }	 
	 
	 public void setField(Object grid)
    {	   
		 Grid2D localField = (Grid2D)grid;
		 this.field = localField;
	    tmpGCI = new ValueGridCellInfo(this, localField);
	    coords = new float[localField.getWidth()* localField.getHeight()*4*3];    // 3 coordinates: x, y, z
	    colors4f = new Color4f[localField.getWidth()* localField.getHeight()*4];    // 3 color values -- alpha transparency doesn't work here :-(
	    resetField = true;
    }
	 
	 public TransformGroup createModel()
    {
	    TransformGroup globalTG = new TransformGroup(); 
	    globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
	    globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
	    globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
	    
	    Grid2D field = (Grid2D)getField();
	    
	    if (field == null) return globalTG;
	    
	    TilePortrayalHack quadPortrayal = (TilePortrayalHack)getPortrayalForObject(tmpGCI);
	    
	   
	            
	    GeometryArray ga;
	    if(!useTriangles)
	        ga = new QuadArray(4*field.getWidth()*field.getHeight(), 
	      		  GeometryArray.COORDINATES | GeometryArray.COLOR_4 | // 3 color values -- alpha transparency doesn't work here :-(
	            (image != null ? QuadArray.TEXTURE_COORDINATE_2 : 0));
	    else
	        {
	        int[] lengths = new int[field.getWidth()*field.getHeight()];                       
	        for(int i=0; i<lengths.length;i++)
	            lengths[i]=4;
	        ga = new TriangleFanArray(      4*lengths.length, 
	            TriangleFanArray.COORDINATES | TriangleFanArray.COLOR_4 | // 3 color values -- alpha transparency doesn't work here :-(
	            (image != null ? QuadArray.TEXTURE_COORDINATE_2 : 0),
	            lengths);
	        }
	
	    ga.setCapability(QuadArray.ALLOW_COLOR_WRITE);
	    ga.setCapability(QuadArray.ALLOW_COLOR_READ);
	    ga.setCapability(QuadArray.ALLOW_COORDINATE_WRITE);
	    SimplePortrayal3D.setPickableFlags(ga);
	            
	    tmpVect.z = 0;
	    int quadIndex = 0;
	    final int width = field.getWidth();
	    final int height = field.getHeight();
	    
	    for(int i=0; i<width;i++)
	        {           
	        tmpGCI.x = i;
	                    
	        // cell<i,j> is i units to far on x and j unit too far on y.
	        //
	        tmpVect.x = i;
	        for(int j=0; j<height;j++)
	            {
	            tmpGCI.y = j;
	            tmpVect.y = j;
	            //                              quadPortrayal.setQuad(tmpGCI, qa,quadIndex);
	            quadPortrayal.setData(tmpGCI, coords, colors4f, quadIndex, width, height);
	            quadIndex++;
	            }
	        }
	    ga.setCoordinates(0, coords);
	    if(colors4f != null)ga.setColors(0,colors4f);
	            
	    Shape3D shape = new Shape3D(ga);
	    shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
	
	    Appearance appearance;
	    if (image!=null)
	        {
	        appearance = SimplePortrayal3D.appearanceForImage(image,true);
	        TexCoordGeneration tex = new TexCoordGeneration();
	        Vector4f s = new Vector4f(1f/width,0,0,0);
	        tex.setPlaneS(s);
	        Vector4f t = new Vector4f(0,1f/height,0,0);
	        tex.setPlaneT(t);
	        appearance.setTexCoordGeneration(tex);
	        }
	    else 
	    {
	        appearance = new Appearance();
	        if (transparency < 1.0f )
	        {
	            appearance.setTransparencyAttributes(
	                new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - (float)transparency));  // duh, alpha's backwards  
	        }
	        else{
	      	  TransparencyAttributes trans = new TransparencyAttributes();
	      	  trans.setTransparencyMode(TransparencyAttributes.BLENDED);
	      	  appearance.setTransparencyAttributes(trans);
	 	    }
	    }
	   
	    
	    appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
	    appearance.setPolygonAttributes(mPolyAttributes);
	    appearance.setColoringAttributes( new ColoringAttributes(1.0f,1.0f,1.0f,ColoringAttributes.SHADE_GOURAUD));
	
	    shape.setAppearance(appearance);
	
	    LocationWrapper pi = new LocationWrapper(null, null, this);
	    shape.setUserData(pi);
	    
	    BranchGroup bg = new BranchGroup();
	    bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
	    bg.setCapability(BranchGroup.ALLOW_DETACH);
	    bg.addChild(shape);
	    globalTG.addChild(bg);
	    return globalTG;
    }

	 public void updateModel(TransformGroup modelTG)
    {
	    if (resetField || modelTG.numChildren()==0)  // won't even be considered if we're immutable though
	        {
	        // need to rebuild the model entirely :-(
	        TransformGroup g = createModel();
	        if (g.numChildren() > 0)
	            {
	            Node model = g.getChild(0);
	            g.removeChild(0);
	            // I've noticed on MacOS X an occasional spurious index error in the
	            // following method.  It checks to see how many kids there
	            // are, then removes them, but in-between the kids disappear
	            // and the underlying ArrayList generates an index out of bounds
	            // error. Might be an internal race condition in Apple's code.
	            // I'll keep an eye on it -- Sean
	            modelTG.removeAllChildren();
	            modelTG.addChild(model);
	            }
	        	 resetField = false;
	        }
	    else
	        {
		        Grid2D field = (Grid2D)getField();
		
		        TilePortrayalHack quadPortrayal = (TilePortrayalHack)getPortrayalForObject(tmpGCI);         
		        BranchGroup bg = (BranchGroup)modelTG.getChild(0);  
		        Shape3D shape = (Shape3D)bg.getChild(0);
		        GeometryArray ga = (GeometryArray)shape.getGeometry();
		        int quadIndex = 0;
		        final int width = field.getWidth();
		        final int height = field.getHeight();
		        
		        for(int i=0; i< width;i++)
		            {               
		            tmpGCI.x = i;
		            for(int j=0; j< height;j++)
		                {
		                tmpGCI.y = j;
		                //                          quadPortrayal.setQuad(tmpGCI, qa,quadIndex);
		                quadPortrayal.setData(tmpGCI, coords, colors4f, quadIndex, width, height);
		                quadIndex++;
		                }
		            }
		        ga.setCoordinates(0, coords);
		        ga.setColors(0,colors4f);
	        }
    }
    
	/** This method is called by the default inspector to filter new values set by the user.
	    You should return the "corrected" value if the given value is invalid. The default version
	    of this method bases values on the values passed into the setLevels() and setColorTable() methods. */
	public double newValue(int x, int y, double value)
    {
	    
	   
	    tmpGCI.x = x;
	    tmpGCI.y = y;
	    QuadPortrayal quadPortrayal = (QuadPortrayal)getPortrayalForObject(tmpGCI);
	    if(quadPortrayal.getMap().validLevel(value)) return value;
	    Grid2D field = (Grid2D)getField();
	    // at this point we need to reset to current value
	    if (field != null)
	        {
	        if (field instanceof DoubleGrid2D)
	            return ((DoubleGrid2D)field).field[x][y];
	        else if (field instanceof ObjectGrid2D)
	            return doubleValue(((ObjectGrid2D)field).field[x][y]);
	        else return ((IntGrid2D)field).field[x][y];
	        }
	    else return quadPortrayal.getMap().defaultValue(); // return *something*
    }

	public LocationWrapper completedWrapper(LocationWrapper w, PickIntersection pi, PickResult pr)
    {
    Grid2D field = (Grid2D)(this.field);

    return new LocationWrapper(new ValueGridCellInfo(ValueGrid2DPortrayal3DHack.this, field), 
        ((QuadPortrayal)getPortrayalForObject(tmpGCI)).getCellForIntersection(pi,field),
        this ) 
        {
        // we keep this around so we don't keep allocating MutableDoubles
        // every time getObject is called -- that's wasteful, but more importantly,
        // it causes the inspector to load its property inspector entirely again,
        // which will cause some flashing...
        MutableDouble val = null;  
                    
        public Object getObject()
            {
            if (val == null) val = new MutableDouble(0);  // create the very first time only
            val.val = ((ValueGridCellInfo)object).value();
            return val;
            }
        public String getLocationName()
            {
            if (location!=null) return ((Int2D)location).toCoordinates();
            return null;
            }
        };
    }
}

package sim.portrayal3d.simple;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import sim.display.GUIState;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;
import sim.field.grid.Grid3D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.IntGrid3D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimpleInspector;
import sim.portrayal3d.grid.ValueGridPortrayal3D;

import sim.util.Int3D;


public class ValuePortrayal3DHack extends Shape3DPortrayal3D
   {
   public static final int SHAPE_CUBE = 0;
   public static final int SHAPE_SQUARE = 1;
   
  static final float[] verts_cube = {
       // front face
       0.5f, -0.5f,  0.5f,                             0.5f,  0.5f,  0.5f,
       -0.5f,  0.5f,  0.5f,                            -0.5f, -0.5f,  0.5f,
       // back face
       -0.5f, -0.5f, -0.5f,                            -0.5f,  0.5f, -0.5f,
       0.5f,  0.5f, -0.5f,                             0.5f, -0.5f, -0.5f,
       // right face
       0.5f, -0.5f, -0.5f,                             0.5f,  0.5f, -0.5f,
       0.5f,  0.5f,  0.5f,                             0.5f, -0.5f,  0.5f,
       // left face
       -0.5f, -0.5f,  0.5f,                            -0.5f,  0.5f,  0.5f,
       -0.5f,  0.5f, -0.5f,                            -0.5f, -0.5f, -0.5f,
       // top face
       0.5f,  0.5f,  0.5f,                             0.5f,  0.5f, -0.5f,
       -0.5f,  0.5f, -0.5f,                            -0.5f,  0.5f,  0.5f,
       // bottom face
       -0.5f, -0.5f,  0.5f,                            -0.5f, -0.5f, -0.5f,
       0.5f, -0.5f, -0.5f,                             0.5f, -0.5f,  0.5f,
       };
   
  
   static final float[] verts_square = {
       0.5f, -0.5f,  0.0f,                             0.5f,  0.5f,  0.0f,
       -0.5f,  0.5f,  0.0f,                            -0.5f, -0.5f,  0.0f
       };

   /** Creates a ValuePortrayal3D with a cube shape. */
   public ValuePortrayal3DHack(float scale)
       {
       this(SHAPE_CUBE, scale);
       }

   /** Returns false and does not set the transform (there's nothing to set). */
   public boolean setTransform(TransformGroup j3dModel, Transform3D transform)
       {
       return false;  // there's no transform for this class
       }
       
   protected Shape3D getShape(TransformGroup j3dModel, int shapeNumber)
       {
       Shape3D p = null;
       if (j3dModel.getChild(0) instanceof TransformGroup)
           {
           TransformGroup g = (TransformGroup)(j3dModel.getChild(0));
           p = (Shape3D)(g.getChild(0));
           }
       else
           {
           p = (Shape3D)(j3dModel.getChild(0));
           }
       return p;
       }

   // must be static or else we can't call super() below
   static GeometryArray processArray(int shape, float scale) 
       {
       float[] verts = (shape == SHAPE_CUBE ? verts_cube : verts_square);
       float[] scaledVerts = new float[verts.length];
       double x= 0, y= 0, z=0;
       for (int i = 0; i < verts.length/3; i++)
       {
	       scaledVerts[3*i  ] = verts[3*i  ] * (float)(scale-x) + (float)x;
	       scaledVerts[3*i+1] = verts[3*i+1] * (float)(scale-y) + (float)y;
	       scaledVerts[3*i+2] = verts[3*i+2] * (float)(scale-z) + (float)z;
       }   
       
       GeometryArray ga = new QuadArray(scaledVerts.length/3, QuadArray.COORDINATES);
       ga.setCoordinates(0, scaledVerts);
       return ga;
       }
               
   PolygonAttributes mPolyAttributes;
   static final java.awt.Color SEMITRANSPARENT = new java.awt.Color(64, 64, 64, 64);


   /** Creates a ValuePortrayal3D with a cube (SHAPE_CUBE) or square (SHAPE_SQUARE) shape. */
   private ValuePortrayal3DHack(int shape, float scale)
       {
       // Java's requirements for super() to be first are irritating and stupid
       super(processArray(shape, scale), SEMITRANSPARENT);  // we provide a semitransparent color to force transparency attributes to be created in appearanceForColor  
                       
       mPolyAttributes = new PolygonAttributes();
       mPolyAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
       mPolyAttributes.clearCapabilityIsFrequent(PolygonAttributes.ALLOW_MODE_WRITE);
       if (shape == SHAPE_SQUARE) 
           {
           mPolyAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
           mPolyAttributes.clearCapabilityIsFrequent(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
           }
       else 
           mPolyAttributes.setCullFace(PolygonAttributes.CULL_BACK);
       }
       
   public PolygonAttributes polygonAttributes()
       {
       return mPolyAttributes;
       }
       
       
   /* Builds a model, but obj is expected to be a ValuePortrayal3D.ValueWrapper. */
   public TransformGroup getModel(Object obj, TransformGroup j3dModel) 
       {
       //float[] c = ((ValueGridPortrayal3D)getCurrentFieldPortrayal()).getMap().getColor(((ValueWrapper)obj).lastVal).getRGBComponents(null);
       Color color = ((ValueGridPortrayal3D)getCurrentFieldPortrayal()).getColorFor(obj);
               
       // make sure the polygon attributes are set
       if(j3dModel==null) 
           {
           j3dModel = super.getModel(obj, j3dModel);
                       
           /*
           // [This may break things, so we don't do it.  Dunno about the speed really anyway.  Memory is our problem here, not speed.]
           // We dispense of our TransformGroup: it makes us about 20% faster.
                       
           Shape3D s = getShape(j3dModel, 0);
           ((TransformGroup)(j3dModel.getChild(0))).removeChild(0);
           j3dModel = new TransformGroup();
           j3dModel.setCapability(Group.ALLOW_CHILDREN_READ);
           j3dModel.addChild(s);
           */

           Appearance app = appearanceForColor(color);
           app.setPolygonAttributes(polygonAttributes());

/*
//[not really 40%, maybe about 15%]
//Rather than clone the existing appearance, we create a simpler appearance with
//fewer attributes set.  This makes us about 40% faster.

Appearance app = new Appearance();
app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);  
app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ); 
app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
app.setPolygonAttributes(polygonAttributes());
ColoringAttributes ca = new ColoringAttributes(c[0], c[1], c[2], ColoringAttributes.SHADE_FLAT);
ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
app.setColoringAttributes(ca);
TransparencyAttributes ta = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f - c[3]);  // duh, alpha's backwards
ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
app.setTransparencyAttributes(ta);
*/

           setAppearance(j3dModel, app);

           // obj is our ValueWrapper, which is a LocationWrapper already
           getShape(j3dModel, 0).setUserData(obj);  // we only have one Shape, #0
           }
       else
           {
           j3dModel = super.getModel(obj, j3dModel);

           // extract color to use
           Appearance appearance = getAppearance(j3dModel);        
           float[] c = color.getRGBComponents(null);
           appearance.getColoringAttributes().setColor(c[0],c[1],c[2]);
           	if(appearance.getTransparencyAttributes() != null) appearance.getTransparencyAttributes().setTransparency(1.0f - c[3]);  // duh, alpha's backwards
           }
       return j3dModel;
       }


   // Filter is a simple abstract class which holds the location (x/y/z) associated with
   // the value being inspected.  There are two concrete subclasses: DoubleFilter and
   // IntFilter, which have getValue() and setValue() methods and so are inspectable.
   // We use these Filter objects as the "objects" being inspected by various inspectors. 

   // The only reason for these two subclasses is that they differ in the data
   // type of their property (double vs int).  This allows us to guarantee that
   // ints are displayed or set as opposed to doubles in the Inspector.  No
   // big whoop -- it's more a formatting thing than anything else.
   
   public static abstract class Filter  // must be public so it can be accessed by SimpleInspector
       {
       int x;
       int y;
       int z; 
           
       ValueGridPortrayal3D fieldPortrayal;
       Grid3D grid; 
       String name;
               
       public Filter(LocationWrapper wrapper)
           {
           fieldPortrayal = (ValueGridPortrayal3D)(wrapper.getFieldPortrayal());
           grid = (Grid3D)fieldPortrayal.getField(); 
           Int3D loc = (Int3D)(wrapper.getLocation());
           x = loc.x;
           y = loc.y;
           z = loc.z; 
           name = fieldPortrayal.getValueName() + " at " + wrapper.getLocationName();
           }
                       
       public String toString() { return name; }
       }

   public static class DoubleFilter extends Filter  // must be public so it can be accessed by SimpleInspector
       {
       public DoubleFilter(LocationWrapper wrapper) { super(wrapper); }
       
       public double getValue() { 
           if (grid instanceof DoubleGrid3D)
               return ((DoubleGrid3D)grid).field[x][y][z];
           else // if (field instanceof DoubleGrid2D)
               return ((DoubleGrid2D)grid).field[x][y];
           }

       public void setValue(double val) { 
           if (grid instanceof DoubleGrid3D)
               ((DoubleGrid3D)grid).field[x][y][z] = fieldPortrayal.newValue(x,y,z,val);
           else //if (field instanceof DoubleGrid2D)
               ((DoubleGrid2D)grid).field[x][y] = fieldPortrayal.newValue(x,y,z,val);
           }
       // static inner classes don't need serialVersionUIDs
       }
       

   public static class IntFilter extends Filter  // must be public so it can be accessed by SimpleInspector
       {
       public IntFilter(LocationWrapper wrapper) { super(wrapper); }
       
       public int getValue() 
           { 
           if (grid instanceof IntGrid3D)
               return ((IntGrid3D)grid).field[x][y][z];
           else //if (field instanceof IntGrid2D)
               return ((IntGrid2D)grid).field[x][y];
           }

       public void setValue(int val) 
           { 
           if (grid instanceof IntGrid3D)
               ((IntGrid3D)grid).field[x][y][z] = (int)fieldPortrayal.newValue(x,y,z,val);
           else //if (field instanceof IntGrid2D)
               ((IntGrid2D)grid).field[x][y] = (int)fieldPortrayal.newValue(x,y,z,val);
           }
       }


   public Inspector getInspector(LocationWrapper wrapper, GUIState state)
       {
       Object field = ((ValueGridPortrayal3D)(wrapper.getFieldPortrayal())).getField();
               
       if (field instanceof DoubleGrid3D || field instanceof DoubleGrid2D)
           return new SimpleInspector(new DoubleFilter(wrapper), state, "Properties");
       else
           return new SimpleInspector(new IntFilter(wrapper) ,state, "Properties");
       // static inner classes don't need serialVersionUIDs
       }
   
       
   public String getName(LocationWrapper wrapper)
       {
       ValueGridPortrayal3D portrayal = (ValueGridPortrayal3D)(wrapper.getFieldPortrayal());
       return portrayal.getValueName() + " at " + wrapper.getLocationName();
       }
}

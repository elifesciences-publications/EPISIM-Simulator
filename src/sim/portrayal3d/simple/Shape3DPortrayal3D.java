package sim.portrayal3d.simple;

import sim.portrayal3d.*;
import javax.media.j3d.*;
import sim.portrayal.*;

/**
   A simple portrayal for displaying Shape3D objects.  You can find Shape3D objects,
   or CompressedGeometry objects (which you can make into a Shape3D in its constructor)
   all over the web.  

   <p> Note that this is <i>not</i>
   the superclass of ConePortrayal, SpherePortrayal, etc.  Those display, in Java3D-speak,
   "Primitives": bundles of shapes.  No, we don't understand why either.
*/

public class Shape3DPortrayal3D extends SimplePortrayal3D
    {
    public Shape3D shape;
    public Appearance appearance;
    
    /** Constructs a Shape3DPortrayal3D with the given shape and a default (flat opaque white) appearance. */
    public Shape3DPortrayal3D(Shape3D shape)
        {
        this(shape,java.awt.Color.white);
        }

    /** Constructs a Shape3DPortrayal3D  with the given shape and a flat opaque appearance of the given color. */
    public Shape3DPortrayal3D(Shape3D shape, java.awt.Color color)
        {
        this(shape,appearanceForColor(color));
        }

    /** Constructs a Shape3DPortrayal3D with the given shape and (opaque) image. */
    public Shape3DPortrayal3D(Shape3D shape, java.awt.Image image)
        {
        this(shape,appearanceForImage(image,true));
        }

    /** Constructs a Shape3DPortrayal3D with the given shape and appearance. */
    public Shape3DPortrayal3D(Shape3D shape, Appearance appearance)
        {
        this.appearance = appearance;  this.shape = shape;
        }

    public TransformGroup getModel(Object obj, TransformGroup j3dModel)
        {
        if(j3dModel==null)
            {
            j3dModel = new TransformGroup();
            
            // make a shape
            Shape3D s = (Shape3D)(shape.cloneNode(false));  // can I share geometries?
            s.setAppearance(appearanceForColor(java.awt.Color.red));
//                      s.setAppearance(appearance);

            // make it pickable
            setPickableFlags(s);
            
            // build a LocationWrapper for the object
            LocationWrapper pickI = new LocationWrapper(obj, null, parentPortrayal);

            // Store the LocationWrapper in the user data
            s.setUserData(pickI);

            j3dModel.addChild(s);
            }
        return j3dModel;
        }
    }

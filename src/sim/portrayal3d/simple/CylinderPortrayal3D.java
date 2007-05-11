package sim.portrayal3d.simple;

import com.sun.j3d.utils.geometry.*;
import sim.portrayal3d.*;
import sim.portrayal.*;
import javax.media.j3d.*;

/**
 * Portrays objects as a cylinder of the specified color or appearance (flat opaque white by default)
 * which fills the region from (-0.5*scale,-0.5*scale,-0.5*scale) to (0.5*scale,0.5*scale,0.5*scale).
 * The axis of the cylinder runs along the Y axis. Objects portrayed by this portrayal are selectable.
 */
public class CylinderPortrayal3D extends SimplePortrayal3D
    {
    public float scale = 1f;
    public Appearance appearance;
    public boolean generateNormals;
    public boolean generateTextureCoordinates;

    /** Constructs a CylinderPortrayal3D with a default (flat opaque white) appearance and a scale of 1.0. */
    public CylinderPortrayal3D()
        {
        this(1f);
        }
        
    /** Constructs a CylinderPortrayal3D with a default (flat opaque white) appearance and the given scale. */
    public CylinderPortrayal3D(float scale)
        {
        this(java.awt.Color.white,scale);
        }
        
    /** Constructs a CylinderPortrayal3D with a flat opaque appearance of the given color and a scale of 1.0. */
    public CylinderPortrayal3D(java.awt.Color color)
        {
        this(color,1f);
        }
        
    /** Constructs a CylinderPortrayal3D with a flat opaque appearance of the given color and the given scale. */
    public CylinderPortrayal3D(java.awt.Color color, float scale)
        {
        this(appearanceForColor(color),false,false,scale);
        }

    /** Constructs a CylinderPortrayal3D with the given (opaque) image and a scale of 1.0. */
    public CylinderPortrayal3D(java.awt.Image image)
        {
        this(image,1f);
        }

    /** Constructs a CylinderPortrayal3D with the given (opaque) image and scale. */
    public CylinderPortrayal3D(java.awt.Image image, float scale)
        {
        this(appearanceForImage(image,true),false,true,scale);
        }


    /** Constructs a CylinderPortrayal3D with the given appearance and scale, plus whether or not to generate normals or texture coordinates.  Without texture coordiantes, a texture will not be displayed. */
    public CylinderPortrayal3D(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates, float scale)
        {
        this.generateNormals = generateNormals;
        this.generateTextureCoordinates = generateTextureCoordinates;
        this.appearance = appearance;  this.scale = scale;
        }


    public TransformGroup getModel(Object obj, TransformGroup j3dModel)
        {
        if(j3dModel==null)
            {
            j3dModel = new TransformGroup();
            
            // make a cylinder
            Cylinder cylinder = new Cylinder(scale/2,scale,
                                             Primitive.GEOMETRY_NOT_SHARED | 
                                             (generateNormals ? Primitive.GENERATE_NORMALS : 0) | 
                                             (generateTextureCoordinates ? Primitive.GENERATE_TEXTURE_COORDS : 0),appearance);
            
            // make all of its shapes pickable
            setPickableFlags(cylinder.getShape(Cylinder.BODY));
            setPickableFlags(cylinder.getShape(Cylinder.TOP));
            setPickableFlags(cylinder.getShape(Cylinder.BOTTOM));
            
            // build a LocationWrapper for the object
            LocationWrapper pickI = new LocationWrapper(obj, null, parentPortrayal);
            
            // Store the LocationWrapper in the user data of each shape
            cylinder.getShape(Cylinder.BODY).setUserData(pickI);
            cylinder.getShape(Cylinder.TOP).setUserData(pickI);
            cylinder.getShape(Cylinder.BOTTOM).setUserData(pickI);

            j3dModel.addChild(cylinder);
            }
        return j3dModel;
        }
    }

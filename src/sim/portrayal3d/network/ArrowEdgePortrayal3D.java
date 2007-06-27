/*
 * Created on Nov 18, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package sim.portrayal3d.network;

import java.awt.Color;


import javax.media.j3d.*;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.*;
import sim.portrayal3d.simple.*;
/**
 * @author Gabriel Balan
 *
 */
public class ArrowEdgePortrayal3D extends GenericEdgePortrayal3D
    {
    static class PickableArrow extends Arrow
        {
        public PickableArrow(){this(1f);}
        public PickableArrow(Appearance ap){this(1f,ap);}
        public PickableArrow(float radius)
            {
            this(radius, null);
            }
                
        public PickableArrow(float arrowTailRadius, Appearance appearance)
            {
            super(arrowTailRadius, 
                  new Vector3f(0f,-1f,0f),
                  new Vector3f(0f,1f,0f),
                  null, null, appearance);
            }

        public void setUserData(java.lang.Object userData)
            {
            super.setUserData(userData);
            setup(arrowHead.getShape(Cylinder.BODY), userData);
            setup(arrowHead.getShape(Cylinder.TOP), userData);
                        
            setup(arrowTail.getShape(Cylinder.TOP), userData);
            setup(arrowTail.getShape(Cylinder.BOTTOM), userData);
            setup(arrowTail.getShape(Cylinder.BODY), userData);
            }
                
        private void setup(Shape3D shape,java.lang.Object userData)
            {
            shape.setUserData(userData);
            setPickableFlags(shape);
            }
                
        }

    public ArrowEdgePortrayal3D()
        {
        super(new PickableArrow());
        }
        
    public ArrowEdgePortrayal3D(float radius)
        {
        super(new PickableArrow(radius));
        }
        
    public ArrowEdgePortrayal3D(float radius, Appearance ap)
        {
        super(new PickableArrow(radius, ap));
        }       


    public ArrowEdgePortrayal3D(Color labelColor)
        {
        super(new PickableArrow(), labelColor);
        }

    public ArrowEdgePortrayal3D(
        Appearance edgeAppearance,
        Color labelColor)
        {
        super(new PickableArrow(edgeAppearance), labelColor);
        }


    }

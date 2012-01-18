package sim.portrayal3d.grid;

import javax.media.j3d.Group;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import sim.field.grid.AbstractGrid2D;
import sim.field.grid.AbstractGrid3D;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.IntGrid3D;
import sim.portrayal.Portrayal;
import sim.portrayal3d.SimplePortrayal3D;
import sim.portrayal3d.grid.ValueGridPortrayal3D.ValueWrapper;
import sim.portrayal3d.simple.TransformedPortrayal3D;
import sim.portrayal3d.simple.ValuePortrayal3DHack;
import sim.util.Int3D;


public class ValueGridPortrayal3DHack extends ValueGridPortrayal3D{
	
	
	 private ValuePortrayal3DHack defaultPortrayalCustom;
	 
	 private float gridScale = 1;
	
	 public ValueGridPortrayal3DHack(float gridScale) 
    { 
		 this("Value", 1,gridScale); 
    }

	 public ValueGridPortrayal3DHack(String valueName,float gridScale) 
    { 
		 this(valueName, 1,gridScale); 
    }

	 public ValueGridPortrayal3DHack(double s,float gridScale) 
    { 
    this("Value", s,gridScale); 
    }

	 public ValueGridPortrayal3DHack(String valueName, double scale,float gridScale) 
    { 
		 super(valueName, scale);
		 this.gridScale = gridScale;
		 defaultPortrayalCustom = new ValuePortrayal3DHack(gridScale);
    }
	  public Portrayal getDefaultPortrayal() 
     { 
		  //TransformedPortrayal3D trans = new TransformedPortrayal3D(defaultPortrayal);
		  //trans.scale(10);
		  return defaultPortrayalCustom; 
     }
	  
	  public TransformGroup createModel()
     {
     TransformGroup globalTG = new TransformGroup(); 
     globalTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
     
     if (field == null) return globalTG;
             
     dirtyScale = false;  // we'll be revising the scale entirely
     
     Switch localSwitch = new Switch(Switch.CHILD_MASK); 
     localSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
     localSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
     localSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
             
     globalTG.addChild(localSwitch); 

     extractDimensions(); // set the width, height, and length based on the underlying grid
     
     java.util.BitSet childMask = new java.util.BitSet(width*height*length); 

     Transform3D trans = new Transform3D(); 
     
     Portrayal p = getPortrayalForObject(new ValueWrapper(0.0, new Int3D(), this));
     if (!(p instanceof SimplePortrayal3D))
         throw new RuntimeException("Unexpected Portrayal " + p + "for object " +
             valueToPass + " -- expected a SimplePortrayal3D");
     
     SimplePortrayal3D portrayal = (SimplePortrayal3D) p;
     portrayal.setCurrentFieldPortrayal(this);

     int i = 0;
     int width = this.width;
     int height = this.height;
     int length = this.length;
     for (int x=0;x<width;x++) 
         for (int y=0;y<height;y++) 
             for (int z=0;z<length;z++) 
                 {
                 double value = gridValue(x,y,z); 
                 TransformGroup tg = portrayal.getModel(new ValueWrapper(0.0, new Int3D(x,y,z), this), null);
                 tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
                 tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                 tg.setCapability(Group.ALLOW_CHILDREN_READ);
                 float translationConstant = gridScale/2;
                 trans.setTranslation(new Vector3f(translationConstant+(x*gridScale),translationConstant+(y*gridScale),translationConstant+(z*gridScale))); 
                 trans.setScale(scale); 
                 tg.setTransform(trans); 
                 //tg.setUserData(wrapper);  // already done when the object was created
                 localSwitch.addChild(tg);

                 if (map.getAlpha(value) > 2) // nontransparent
                     childMask.set(i);
                 else 
                     childMask.clear(i);

                 i++;
                 }

     localSwitch.setChildMask(childMask); 
     return globalTG;
     }
     
     

 public void updateModel(TransformGroup modelTG)
     {
     if (field == null) return; 

     extractDimensions(); 
     Switch localSwitch = (Switch) modelTG.getChild(0); 
     java.util.BitSet childMask = localSwitch.getChildMask(); 
     
     Portrayal p = getPortrayalForObject(valueToPass);
     if (!(p instanceof SimplePortrayal3D))
         throw new RuntimeException("Unexpected Portrayal " + p + "for object " +
             valueToPass + " -- expected a SimplePortrayal3D");
     
     SimplePortrayal3D portrayal = (SimplePortrayal3D) p;
     portrayal.setCurrentFieldPortrayal(this);
             
     if (dirtyScale || isDirtyField())
         reviseScale(localSwitch);               // sizes may have changed
                                             
     int i = 0;
     int width = this.width;
     int height = this.height;
     int length = this.length;
     for (int x=0;x<width;x++) 
         for (int y=0;y<height;y++) 
             for (int z=0;z<length;z++) 
                 { 
                 TransformGroup tg = (TransformGroup)localSwitch.getChild(i);
                                     
                 // ValuePortrayal3D dispenses with its TransformGroup in order to achieve some
                 // additional speed.  We recognize that fact here.
                 // TransformGroup g = (TransformGroup)(g.getChild(0));
                 // Shape3D shape = (Shape3D)(g.getChild(0));
                                     
                 Shape3D shape = (Shape3D)(tg.getChild(0));

                 ValueWrapper wrapper = (ValueWrapper)(shape.getUserData());
                 double value = gridValue(x,y,z); 
                 double oldValue = wrapper.lastValue;
                                     
                 if (value != oldValue) // change to new value
                     if (map.getAlpha(value) > 2)  // nontransparent
                         { 
                         childMask.set(i);
                         wrapper.lastValue = value;
                         portrayal.getModel(wrapper, tg); 
                         }
                     else childMask.clear(i);
                 i++;  // next index
                 }
     localSwitch.setChildMask(childMask); 
     }
}

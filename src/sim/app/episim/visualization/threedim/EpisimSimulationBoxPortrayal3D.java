package sim.app.episim.visualization.threedim;

import java.awt.Color;
import java.awt.Font;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Text2D;






import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.display3d.Display3DHack;
import sim.portrayal3d.simple.WireFrameBoxPortrayal3D;



public class EpisimSimulationBoxPortrayal3D extends WireFrameBoxPortrayal3D {
	
	private float x; 
	private float y; 
	private float z;
	private float x2; 
	private float y2; 
	private float z2;
	
	private static final float OFFSET = 0;
	
	
// A larger font size makes the label bigger but also uses much more memory
   static final int FONT_SIZE = 14;
   // A smaller scaling factor reduces the label size
   static final double SCALING_MODIFIER = 1.0 / 5.0; 
       
   double labelScale = 1.0;
	private Font font;
	private Font3D font3D;
	
	
	 /** Draws a white wireframe box from (-0.5,-0.5,-0.5) to (0.5,0.5,0.5) */
   public EpisimSimulationBoxPortrayal3D() { this(-0.5,-0.5,-0.5,0.5,0.5,0.5); }

   /** Draws a white wireframe box from (x,y,z) to (x2,y2,z2) */
   public EpisimSimulationBoxPortrayal3D(double x, double y, double z, double x2, double y2, double z2)
   {
   	this(x,y,z,x2,y2,z2,Color.white);
   }

   /** Draws a wireframe box from (x,y,z) to (x2,y2,z2) in the specified color. */
   public EpisimSimulationBoxPortrayal3D(double x, double y, double z, double x2, double y2, double z2, Color color)
   {
       super(x,y,z,x2,y2,z2,appearanceForColor(color));
       this.x = (float)x;
       this.y = (float)y;
       this.z = (float)z;
       this.x2 = (float)x2;
       this.y2 = (float)y2;
       this.z2 = (float)z2;
     
   }
   
   public TransformGroup getModel(Object obj, TransformGroup tg){
   	boolean setChildrenAddCapability = (tg ==null);
   	TransformGroup modelTG = super.getModel(obj, tg);
   	
	   if(setChildrenAddCapability)modelTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
	   BranchGroup branchGroup = new BranchGroup();
	   branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
	   branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
	   branchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
	   
	   branchGroup.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
	   branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
	   
	
	   if (font == null) font = new Font("SansSerif",Font.PLAIN, FONT_SIZE);
      
      font3D = new Font3D(font, new FontExtrusion());
	   
	   
      if(getCurrentDisplay() != null){
	   	double dispScale = ((Display3DHack) getCurrentDisplay()).getInitialDisplayScale();
	   	labelScale = 0.0035/dispScale;
      }
	   
      Color labelColor = Color.WHITE;
      MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();
		if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){			
			labelColor = Optimized3DVisualization.simulationBoxColor;
		} 
	  
	   String label = "(0,0,0) µm";
	   
	   addLabel(label, branchGroup, new Vector3f(OFFSET,0f,0f), font,labelColor);   	
	   	
	   
	   Vector3f stPt = new Vector3f(x, y, z);
	   Vector3f endPt = new Vector3f(x2+OFFSET, y, z);
	   Vector3f v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "("+((int)(x2-x))+",0,0) µm";
	   addLabel(label, branchGroup, v, font,labelColor);
	     
	   stPt = new Vector3f(x, y, z);
	   endPt = new Vector3f(x+OFFSET, y2, z);
	   v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "(0,"+((int)(y2-y))+",0) µm";
	   addLabel(label, branchGroup, v, font,labelColor);
	      
	   stPt = new Vector3f(x, y, z);
	   endPt = new Vector3f(x+OFFSET, y, z2);
	   v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "(0,0,"+((int)(z2-z))+") µm";
	   addLabel(label, branchGroup, v, font,labelColor);    
	    
	   stPt = new Vector3f(x, y, z);
	   endPt = new Vector3f(x2+OFFSET, y2, z);
	   v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "("+((int)(x2-x))+","+((int)(y2-y))+",0) µm";
	   addLabel(label, branchGroup, v, font,labelColor);
	    
	   stPt = new Vector3f(x, y, z);
	   endPt = new Vector3f(x2+OFFSET, y, z2);
	   v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "("+((int)(x2-x))+",0,"+((int)(z2-z))+") µm";
	   addLabel(label, branchGroup, v, font,labelColor);
	      
	   stPt = new Vector3f(x, y, z);
	   endPt = new Vector3f(x+OFFSET, y2, z2);
	   v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "(0,"+((int)(y2-y))+","+((int)(z2-z))+") µm";
	   addLabel(label, branchGroup, v, font,labelColor);
	      
	   stPt = new Vector3f(x, y, z);
	   endPt = new Vector3f(x2+OFFSET, y2, z2);
	   v = new Vector3f(stPt);
	   v.negate();
	   v.add(new Vector3f(endPt));
	   label = "("+((int)(x2-x))+","+((int)(y2-y))+","+((int)(z2-z))+") µm";
	   addLabel(label, branchGroup, v, font,labelColor);
	      
	      
	   	modelTG.addChild(branchGroup);
   	
      return modelTG;
   }
   
   private void addLabel(String label, BranchGroup branchGroup, Vector3f position, Font font, Color color){
   	 
   
      
   	
   	 
   	Text2D text = new Text2D(label, new Color3f(color), font.getFamily(), font.getSize(), font.getStyle());
   	text.setRectangleScaleFactor((float)(labelScale * SCALING_MODIFIER));
   
      
      OrientedShape3D o3d = new OrientedShape3D(text.getGeometry(), text.getAppearance(),
          OrientedShape3D.ROTATE_NONE, position);
    /*  o3d.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);  // may need to change the appearance (see below)
      o3d.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);  // may need to change the geometry (see below)
      o3d.clearCapabilityIsFrequent(Shape3D.ALLOW_APPEARANCE_WRITE);
      o3d.clearCapabilityIsFrequent(Shape3D.ALLOW_GEOMETRY_WRITE);*/
      
    

        Transform3D t = new Transform3D();
        t.setScale(1);
        t.setTranslation(new Vector3f(position.x, position.y, position.z));
        TransformGroup labelTG = new TransformGroup(t);
        labelTG.addChild(o3d);
      
      
      branchGroup.addChild(labelTG);
   }
   private float getXOffset(String label, float fontsize, float fontScaleFactor){
   	return (((float)label.length())*fontsize*fontScaleFactor)+OFFSET;
   }

}

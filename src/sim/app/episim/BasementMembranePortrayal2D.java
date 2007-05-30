package sim.app.episim;

import sim.portrayal.*;


import java.awt.*;
import java.awt.geom.*;


public class BasementMembranePortrayal2D extends SimplePortrayal2D{
	
	   
	    private double width;
	    private double height;
	   
	    
	
	    
	    public BasementMembranePortrayal2D(double width, double height) {
	   	 this.width = width;
	   	 this.height = height;
	   	 
	   
	   	 
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	    {            
	       graphics = (Graphics2D) graphics.create(); 
	   	 
	      	 
	      	  GeneralPath polygon = new GeneralPath();
	     	 		final int STEPSIZE = 1;
	     	 		((GeneralPath)polygon).moveTo(0, BasementMembrane.lowerBound(0));
	     	 		for(double i = 0; i <= BasementMembrane.getWidth()+10; i += STEPSIZE){
	     	 		((GeneralPath)polygon).lineTo(i, BasementMembrane.lowerBound(i));
	     	 		}
	
	      	  
	      	  graphics.setColor(new Color(255, 99, 0));
	      	  graphics.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	      	  
	      	        AffineTransform transform = new AffineTransform();
	      	      
	      	        
	      	        
	      	
	      	        transform.scale(info.draw.width, info.draw.height);
	      	        polygon = (GeneralPath) polygon.createTransformedShape(transform);
	      	       
	      	       
	      	        transform.setToTranslation(getTranslationX(info), getTranslationY(info));
	    
		      	     polygon = (GeneralPath) polygon.createTransformedShape(transform);
		      	     
	      	        graphics.draw(polygon);
	      	        
	      	       
	      	        
	      	
	    }
	    
	    

	    private double getTranslationX(DrawInfo2D info){
	   	 if(info.clip.width< width){
	   		 return 0;
	   	 }
	   	 else return info.clip.getMinX();
	    }
	    
	    private double getTranslationY(DrawInfo2D info){
	   	 final int BIAS = 20;
	   	 if(info.clip.height < height){
	   		 return BIAS;
	   	 }
	   	 else return info.clip.getMinY()+BIAS;
	    }
	  
	   
}

package sim.app.episim.datamonitoring.xml;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;


public class ColorAdapter extends XmlAdapter<AdaptedColor, Color>  implements java.io.Serializable{

	
   public Color unmarshal(AdaptedColor v) throws Exception {
	   return new Color(v.getRed(), v.getGreen(), v.getBlue());
   }
	
   public AdaptedColor marshal(Color v) throws Exception {
		AdaptedColor ac = new AdaptedColor();
		ac.setRed(v.getRed());
		ac.setGreen(v.getGreen());
		ac.setBlue(v.getBlue());	   
	   return ac;
   }

}

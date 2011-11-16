package sim.util;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class SimplePropertiesHack extends SimpleProperties {
	
	public SimplePropertiesHack(Object o, boolean includeSuperclasses, boolean includeGetClass, boolean includeExtensions) {

	   super(o, includeSuperclasses, includeGetClass, includeExtensions);
	   
   }

	public SimplePropertiesHack(Object o, boolean includeSuperclasses, boolean includeGetClass) {
		this(o,includeSuperclasses,includeGetClass,true);
   }
	
	public Method getHidden(Method m, Class c, boolean includeExtensions){ return super.getHidden(m, c, includeExtensions);}
	
	protected Properties getAuxillary(){ return this.auxillary; }
	protected ArrayList getHideMethods(){ return this.hideMethods; }
	
	

}
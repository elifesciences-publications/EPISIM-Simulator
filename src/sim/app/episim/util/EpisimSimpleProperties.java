package sim.app.episim.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;



import sim.app.episim.ExceptionDisplayer;
import sim.util.SimplePropertiesHack;


public class EpisimSimpleProperties extends SimplePropertiesHack {
	
	public EpisimSimpleProperties(Object o, boolean includeSuperclasses, boolean includeGetClass, boolean includeExtensions) {

	   super(o, includeSuperclasses, includeGetClass, includeExtensions);
	   
   }

	public EpisimSimpleProperties(Object o, boolean includeSuperclasses, boolean includeGetClass) {
		this(o,includeSuperclasses,includeGetClass,true);
   }

	/* If it exists, returns a method of the form 'public boolean hideFoo() { ...}'.  In this method the developer can declare
   whether or not he wants to hide this property.  If there is no such method, we must assume that the property is to be
   shown. */
	public Method getHidden(Method m, Class c, boolean includeExtensions)
  {
	 if (!includeExtensions) return null;
    try
    {     
   	 
   	 if (m.getName().startsWith("get"))
        {
            Method m2 = c.getMethod("hide" + (m.getName().substring(3)), new Class[] { });
            if (m2 != null && m2.getReturnType() == Boolean.TYPE) return m2;
        }
        else if (m.getName().startsWith("is"))
        {
            Method m2 = c.getMethod("hide" + (m.getName().substring(2)), new Class[] { });
            if (m2 != null &&m2.getReturnType() == Boolean.TYPE) return m2;
        }
        
        
    }  	  	  
    catch (Exception e)
    {
   	 
   	 
   	 if((m.getName().startsWith("get")||m.getName().startsWith("is")) &&  m.getAnnotation(NoUserModification.class)!=null){
     	  try{
	      return (new Object(){ public boolean getThisMethodReturnsAlwaysTrue(){
	      	  return true;
	      	  }}).getClass().getMethod(Names.TRUE_RETURNING_METHOD);
     	  }
     	  catch (SecurityException e1){
     		  ExceptionDisplayer.getInstance().displayException(e);
     	  }
     	  catch (NoSuchMethodException e1){
     		  ExceptionDisplayer.getInstance().displayException(e);
     	  }	
    }
    }
    return null;
  }
	public boolean isHidden(int index)
   {
		if (getAuxillary()!=null) return getAuxillary().isHidden(index);
		if (index < 0 || index > numProperties()) return false;
		try
		{
			if (getHideMethods().get(index) == null) return false;
			else{
				Method m = ((Method)(getHideMethods().get(index)));
				return m.getName().equals(Names.TRUE_RETURNING_METHOD) ? true : ((Boolean)m.invoke(object, new Object[0])).booleanValue();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
      	return false;
		}
    }
	
	
}



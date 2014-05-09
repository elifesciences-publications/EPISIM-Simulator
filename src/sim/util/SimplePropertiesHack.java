package sim.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class SimplePropertiesHack extends SimpleProperties {
	
	public SimplePropertiesHack(Object o, boolean includeSuperclasses, boolean includeGetClass, boolean includeExtensions) {

	   super(o, includeSuperclasses, includeGetClass, includeExtensions);
	   Comparator<Object> comp = new Comparator<Object>(){			
         public int compare(Object o1, Object o2) {
         	if(o1 instanceof Method && o2 instanceof Method){
         		return getMethodName(((Method)o1).getName()).compareTo(getMethodName(((Method)o2).getName()));
         	}
	         return 0;
         }
	   	
	   };
	   HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
	   for(int i = 0; i < getMethods.size(); i++){
	   	if(getMethods.get(i) != null && getMethods.get(i)instanceof Method){
	   		indexMap.put(((Method)getMethods.get(i)).getName(), i);
	   	}
	   	
	   }
	   Collections.sort(getMethods, comp);
	   ArrayList setMethodsNew = new ArrayList();
	   ArrayList domMethodsNew = new ArrayList();
	   ArrayList hideMethodsNew = new ArrayList();
	   for(int i = 0; i < getMethods.size(); i++){
	   	if(getMethods.get(i) != null && getMethods.get(i) instanceof Method){
	   		String methodName = ((Method)getMethods.get(i)).getName();
	   		if(indexMap.containsKey(methodName)){
	   			setMethodsNew.add(i, setMethods.get(indexMap.get(methodName)));
	   			domMethodsNew.add(i, domMethods.get(indexMap.get(methodName)));
	   			hideMethodsNew.add(i, hideMethods.get(indexMap.get(methodName)));
	   		}
	   	}
	   }
	   setMethods=setMethodsNew;
	   domMethods=domMethodsNew;
	   hideMethods=hideMethodsNew;	 	   
   }
	
	public String getMethodName(String originalName){
		if(originalName.startsWith("get") || originalName.startsWith("set"))return originalName.substring(3);
		if(originalName.startsWith("is"))return originalName.substring(2);
		
		return originalName;
	}

	public SimplePropertiesHack(Object o, boolean includeSuperclasses, boolean includeGetClass) {
		this(o,includeSuperclasses,includeGetClass,true);
   }
	
	public Method getHidden(Method m, Class c, boolean includeExtensions){ return super.getHidden(m, c, includeExtensions);}
	
	protected Properties getAuxillary(){ return this.auxillary; }
	protected ArrayList getHideMethods(){ return this.hideMethods; }
	
	/**
	 * This override fixes the locale bug, double values should be always in the english format
	 */
	public Object setValue(int index, String value)
   {
   try
       {
       Class type = getType(index);
       if ( type == Boolean.TYPE ) return _setValue(index,Boolean.valueOf(value));
       else if ( type == Byte.TYPE ) 
           {
           try { return _setValue(index,Byte.valueOf(value)); }
           catch (NumberFormatException e) // try again for x.0 stuff
               { 
               double d = Double.parseDouble(value); 
               byte b = (byte) d; 
               if (b==d) return _setValue(index,new Byte(b)); 
               else throw e; 
               }
           }
       else if ( type == Short.TYPE )
           {
           try { return _setValue(index,Short.valueOf(value)); }
           catch (NumberFormatException e) // try again for x.0 stuff
               { 
               double d = Double.parseDouble(value); 
               short b = (short) d; 
               if (b==d) return _setValue(index,new Short(b)); 
               else throw e; 
               }
           }
       else if ( type == Integer.TYPE )
           {
           try { return _setValue(index,Integer.valueOf(value)); }
           catch (NumberFormatException e) // try again for x.0 stuff
               { 
               double d = Double.parseDouble(value); 
               int b = (int) d; 
               if (b==d) return _setValue(index,new Integer(b)); 
               else throw e; 
               }
           }
       else if ( type == Long.TYPE )
           {
           try { return _setValue(index,Long.valueOf(value)); }
           catch (NumberFormatException e) // try again for x.0 stuff
               { 
               double d = Double.parseDouble(value); 
               long b = (long) d; 
               if (b==d) return _setValue(index,new Long(b)); 
               else throw e; 
               }
           }
       else if ( type == Float.TYPE ) return _setValue(index,Float.valueOf(value));
       else if ( type == Double.TYPE ) return _setValue(index, Double.valueOf(value));
       else if ( type == Character.TYPE ) return _setValue(index,new Character(value.charAt(0)));
       else if ( type == String.class ) return _setValue(index,value);
       else return null;
       }
   catch (Exception e)
       {
       e.printStackTrace();
       return null;
       }
   }	

}

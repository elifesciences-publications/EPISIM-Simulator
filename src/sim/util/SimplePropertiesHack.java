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
	
	

}

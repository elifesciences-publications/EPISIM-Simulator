package sim.app.episim.util;

import java.util.Collection;
import java.util.Map;

import sim.util.CollectionProperties;
import sim.util.Indexed;
import sim.util.Properties;
import sim.util.SimpleProperties;


public abstract class EpisimProperties extends Properties {

	/**	Returns a Properties object for the given object.  
		   If expandCollections is true, then if object is a Map, Indexed, or Collection,
		   then it will be treated using CollectionProperties.  Otherwise it will be
		   treated using SimpleProperties. Arrays are always treated using CollectionProperties. 
		   If includeSuperclasses is true, then any SimpleProperties will include superclasses.
		   If includeGetClass is true, then the Class property will be included.
		   No finite domains will be produced (that is, getDomain(index) will return null for
		   all properties).
	 */
	public static Properties getProperties(Object object, boolean expandCollections, boolean includeSuperclasses, boolean includeGetClass)
   {
   	return getProperties(object,expandCollections,includeSuperclasses,includeGetClass,true);
   }
   
	/**	Returns a Properties object for the given object.  
		   If expandCollections is true, then if object is a Map, Indexed, or Collection,
		   then it will be treated using CollectionProperties.  Otherwise it will be
		   treated using SimpleProperties.   Arrays are always treated using CollectionProperties. 
		   If includeSuperclasses is true, then any SimpleProperties will include superclasses.
		   If includeGetClass is true, then the Class property will be included.
		   If includeDomains is true (which requires a CollectionProperties), then domains
		   will be produced for properties according to the rules in the comments in getDomain(index)
		   below. Otherwise all objects will return a null (infinite) domain.
	*/
	public static Properties getProperties(Object object, boolean expandCollections, boolean includeSuperclasses, boolean includeGetClass, boolean includeExtensions)
   {
	   if (object == null) return new EpisimSimpleProperties(object, includeSuperclasses, includeGetClass, includeExtensions);
	   Class c = object.getClass();
	   if (c.isArray()) return new CollectionProperties(object);
	   else if (expandCollections && (Collection.class.isAssignableFrom(c) ||
	           Indexed.class.isAssignableFrom(c) ||
	           Map.class.isAssignableFrom(c)))
	       return new CollectionProperties(object);
	   else return new EpisimSimpleProperties(object, includeSuperclasses, includeGetClass, includeExtensions);
   }
	



}

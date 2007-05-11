package sim.util;
import java.util.*;
import java.lang.reflect.*;

// stars down the side maintain formatting
/**
 *  A very simple class for getting and setting object properties.  You create this class by passing in the 
 *  object you'd like to modify.  The only properties that are considered are ones which are simple (or boxed)
 *  booleans, bytes, shorts, ints, longs, floats, doubles, characters, or strings.  
 *  Alternatively, you can get a class like this by calling Properties.getProperties(...), which will return 
 *  either a SimpleProperties or a CollectionProperties, depending on which is appropriate and the flags you 
 *  have passed in.
 *
 *  <p>A property Foo exists in a class if there is a getFoo() or isFoo() method.  ReadWrite properties
 *  are ones for which there is ALSO a setFoo(prop) method.  If the property is a numerical one, you can
 *  also provide a <i>domain</i> in the form of a function called domFoo(), which returns either an array
 *  of Objects or a <tt>sim.util.Interval</tt>.  If no domain function exists, or if the domain function
 *  returns null, then it is assumed the property has no domain (it can take on any value).
 *
 *  <p>The idea behind domains is to make it easy to create graphical interfaces (sliders, pop-up menus)
 *  for the user to set properties, where it's often convenient to know beforehand what values the property
 *  can be set to in order to construct the GUI widget appropriately.  Here are the domain rules (other than null).
 *  If the domain is an Interval, then it is assumed that the property can only take on the values defined
 *  within that Interval.  Intervals can have both Doubles and Longs as min and max values: if the Interval
 *  has Double min/max values, then the interval is assumed to be real-valued, but if the Interval has
 *  Long min/max values, then the interval is assumed have integer values only (2.3 wouldn't be a valid
 *  setting for the property).  If the domain is an array of objects, then the property <i>must</i>
 *  be an integer (or long, or short, or byte) property.  In this case, it is presumed
 *  that the only values the property can take on are the integers 0 through array.length-1, and that the array's
 *  elements have toString() values which are representative of those integers.  For example, a property
 *  called Format might allow the number values 0, 1, and 2, and might have "names" in the array called
 *  "Left Justified", "Right Justified", and "Centered" respectively.
 *  
 *  <p>This class allows you to set and get properties on the object via boxing the property (java.lang.Integer
 *  for int, for example).  You can also pass in a String, and SimpleProperties will parse the appropriate
 *  value out of the string automatically without you having to bother checking the type.
 *
 *  <p>If any errors arise from generating the properties, setting them, or getting their values, then
 *  the error is printed to the console, but is not thrown: instead typically null is returned.
 *
 *  <p>If the object provided to SimpleProperties is sim.util.Proxiable, then SimpleProperties will call
 *  propertiesProxy() on that object to get the "true" object whose properties are to be inspected.  This
 *  makes it possible for objects to create filter objects which only permit access to certain properties.
 *  Generally speaking, such proxies (and indeed any object whose properties will be inspected) should be
 *  public, non-anonymous classes.  For example, imagine that you've got get/set methods on some property
 *  Foo, but you only want SimpleProperties to recognize the get method.  Furthermore you have another property
 *  called Bar that you want hidden entirely.  You can easily do this by making
 *  your class Proxiable and providing an inner-class proxy like this:
 *  
 * <pre><tt>
 * import sim.util.Proxiable;
 *
 * public class MyClass extends Proxiable
 *     {        
 *     int foo;
 *     float bar;
 *    
 *     public int getFoo() { return foo; }
 *     public void setFoo(int val) { foo = val; }
 *     public float getBar() { return bar; }
 *     public void setBar(float val) { bar = val; }
 *    
 *     public class MyProxy
 *         {
 *         public int getFoo() { return foo; }
 *         }
 *   
 *     public Object propertiesProxy() { return new MyProxy(); }
 *     }
 * </tt></pre>
 *
 */

public class SimpleProperties extends Properties implements java.io.Serializable
    {
    Object object;
    
    ArrayList getMethods = new ArrayList();
    ArrayList setMethods = new ArrayList(); // if no setters, that corresponding spot will be null
    ArrayList domMethods = new ArrayList(); // if no domain, that corresponding spot will be null
    
    /** Gathers all properties for the object, including ones defined in superclasses. 
        SimpleProperties will search the object for methods of the form <tt>public Object dom<i>Property</i>()</tt>
        which define the domain of the property.  See <tt>getDomain(int index)</tt> for a description of
        the domain format.
    */
    public SimpleProperties(Object o) { this(o,true,true,true); }
    
    /** Gathers all properties for the object, possibly including ones defined in superclasses. 
        If includeGetClass is true, then the Class property will be included. 
        SimpleProperties will search the object for methods of the form <tt>public Object dom<i>Property</i>()</tt>
        which define the domain of the property.  See <tt>getDomain(int index)</tt> for a description of
        the domain format.
    */
    public SimpleProperties(Object o, boolean includeSuperclasses, boolean includeGetClass)
        {
        object = o;
        if (o!=null && o instanceof sim.util.Proxiable)
            object = ((sim.util.Proxiable)(o)).propertiesProxy();
        generateProperties(includeSuperclasses,includeGetClass,true);
        }
    
    /** Gathers all properties for the object, possibly including ones defined in superclasses. 
        If includeGetClass is true, then the Class property will be included. If includeDomains is true, then
        SimpleProperties will search the object for methods of the form <tt>public Object dom<i>Property</i>()</tt>
        which define the domain of the property.  See <tt>getDomain(int index)</tt> for a description of
        the domain format.
    */
    public SimpleProperties(Object o, boolean includeSuperclasses, boolean includeGetClass, boolean includeDomains)
        {
        object = o;
        if (o!=null && o instanceof sim.util.Proxiable)
            object = ((sim.util.Proxiable)(o)).propertiesProxy();
        generateProperties(includeSuperclasses,includeGetClass,includeDomains);
        }
    
    void generateProperties(boolean includeSuperclasses, boolean includeGetClass, boolean includeDomains)
        {
        if (object != null) try
            {
            // generate the properties
            Class c = object.getClass();
            Method[] m = (includeSuperclasses ? c.getMethods() : c.getDeclaredMethods());
            for(int x = 0 ; x < m.length; x++)
                {
                if (m[x].getName().startsWith("get") || m[x].getName().startsWith("is")) // corrrect syntax?
                    {
                    int modifier = m[x].getModifiers();
                    if ((includeGetClass || !m[x].getName().equals("getClass")) &&
                        m[x].getParameterTypes().length == 0 &&
                        Modifier.isPublic(modifier)) // no arguments, and public, non-abstract?
                        {
                        //// Add all properties...
                        Class returnType = m[x].getReturnType();
                        if (returnType!= Void.TYPE)
                            {
                            getMethods.add(m[x]);
                            setMethods.add(getWriteProperty(m[x],c));
                            domMethods.add(getDomain(m[x],c,includeDomains));
                            }
                        }
                    }
                }
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }
        }
        
    Method getWriteProperty(Method m, Class c)
        {
        try
            {
            if (m.getName().startsWith("get"))
                {
                return c.getMethod("set" + (m.getName().substring(3)), new Class[] { m.getReturnType() });
                }
            else if (m.getName().startsWith("is"))
                {
                return c.getMethod("set" + (m.getName().substring(2)), new Class[] { m.getReturnType() });
                }
            else return null;
            }
        catch (Exception e)
            {
            // couldn't find a setter
            return null;
            }
        }
    
    Method getDomain(Method m, Class c, boolean includeDomains)
        {
        if (!includeDomains) return null;
        try
            {
            if (m.getName().startsWith("get"))
                {
                return c.getMethod("dom" + (m.getName().substring(3)), new Class[] {});
                }
            else if (m.getName().startsWith("is"))
                {
                return c.getMethod("dom" + (m.getName().substring(2)), new Class[] { });
                }
            else return null;
            }
        catch (Exception e)
            {
            // couldn't find a domain
            return null;
            }
        }
    
    public boolean isVolatile() { return false; }

    /** Returns the number of properties discovered */
    public int numProperties()
        {
        return getMethods.size();
        }

    /** Returns the name of the given property.
        Returns null if the index is out of the range [0 ... numProperties() - 1 ]*/
    public String getName(int index)
        {
        if (index < 0 || index > numProperties()) return null;
        if (((Method)(getMethods.get(index))).getName().startsWith("is"))
            return ((Method)(getMethods.get(index))).getName().substring(2);
        else return ((Method)(getMethods.get(index))).getName().substring(3);
        }
        
    /** Returns whether or not the property can be written as well as read
        Returns false if the index is out of the range [0 ... numProperties() - 1 ]*/
    public boolean isReadWrite(int index)
        {
        if (index < 0 || index > numProperties()) return false;
        if (isComposite(index)) return false;
        return (setMethods.get(index)!=null);
        }

    /** Returns the return type of the property (see the TYPE_... values)
        Returns -1 if the index is out of the range [0 ... numProperties() - 1 ]*/
    public Class getType(int index)
        {
        if (index < 0 || index > numProperties()) return null;
        Class returnType = ((Method)(getMethods.get(index))).getReturnType();

        return getTypeConversion(returnType);
        }

    /** Returns the current value of the property.  Simple values (byte, int, etc.)
        are boxed (into Byte, Integer, etc.).
        Returns null if an error occurs or if the index is out of the range [0 ... numProperties() - 1 ]*/
    public Object getValue(int index)
        {
        if (index < 0 || index > numProperties()) return null;
        try
            {
            return ((Method)(getMethods.get(index))).invoke(object, new Object[0]);
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return null;
            }
        }
    
    Object _setValue(int index, Object value)
        {
        try
            {
            if (setMethods.get(index) == null) return null;
            ((Method)(setMethods.get(index))).invoke(object, new Object[] { value });
            return getValue(index);
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return null;
            }
        }

    public Object getDomain(int index)
        {
        if (index < 0 || index > numProperties()) return null;
        try
            {
            if (domMethods.get(index) == null) return null;
            return ((Method)(domMethods.get(index))).invoke(object, new Object[0]);
            }
        catch (Exception e)
            {
            e.printStackTrace();
            return null;
            }
        }
    }

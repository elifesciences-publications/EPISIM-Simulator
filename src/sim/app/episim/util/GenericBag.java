package sim.app.episim.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import sim.app.episim.ExceptionDisplayer;
import sim.util.Bag;
import sim.util.Indexed;



public class GenericBag<T> implements java.util.Collection<T>, java.io.Serializable, Cloneable
   {
	
	private Bag bag;
   
   public GenericBag() { 
   	bag = new Bag();
   }
   
   
   public GenericBag(int capacity) { 
   	bag = new Bag(capacity);
   }
       
  
   public GenericBag(final GenericBag other)
   {
   	bag = new Bag(other.bag);
   }
   
   
   
   public boolean addAll(final Collection<? extends T> other) 
   { 
       if (other instanceof Bag) return bag.addAll((Bag)other);// avoid an array build
       else if (other instanceof GenericBag) return bag.addAll(((GenericBag)other).bag);// avoid an array build
       return bag.addAll(bag.numObjs, other.toArray()); 
   }

   public boolean addAll(final int index, final Collection<? extends T> other)
   {
   	 if (other instanceof Bag) return bag.addAll(index, (Bag)other);// avoid an array build
       else if (other instanceof GenericBag) return bag.addAll(index, ((GenericBag)other).bag);// avoid an array build
       
       return bag.addAll(index, other.toArray());
   }

   public boolean addAll(final int index, final T[] other)
   {
      return bag.addAll(index, other);
   }
   
   public boolean addAll(final GenericBag<T> other) { return bag.addAll(bag.numObjs,other.bag); }

   public boolean addAll(final int index, final GenericBag<T> other)
   {
       
   	
   	return bag.addAll(index, other.bag);
   }

   public Object clone() throws CloneNotSupportedException
   {
   	GenericBag<T> gen = new GenericBag<T>();
   	gen.bag = (Bag)(bag.clone());
  
   	return gen;
   }

   /** Resizes the internal array to at least the requested size. */
   public void resize(int toAtLeast)
   {
   	bag.resize(toAtLeast);
   }
   
  
       
   /** Resizes the objs array to max(numObjs, desiredLength), unless that value is greater than or equal to objs.length,
       in which case no resizing is done (this operation only shrinks -- use resize() instead).
       This is an O(n) operation, so use it sparingly. */
   public void shrink(int desiredLength)
   {
       bag.shrink(desiredLength);
   }
   
   /** Returns null if the Bag is empty, else returns the topmost object. */
   public T top()
   {
       return (T)bag.top();
   }
   
   /** Returns null if the Bag is empty, else removes and returns the topmost object. */
   public T pop()
   {
      
       return (T) bag.pop();
   }
   
   /** Synonym for add(obj) -- stylistically, you should add instead unless you
       want to think of the Bag as a stack. */
   public boolean push(final T obj)
   {
       return bag.push(obj);
   }
       
   public boolean add(final T obj)
   {
       return bag.add(obj);
   }
       
   

   public boolean contains(final Object o)
   {
      
       return bag.contains(o);
   }
       
   public boolean containsAll(final Collection<?> c)
   {
      
       return bag.containsAll(c);
   }

   public T get(final int index)
   {
     
       return (T)bag.get(index);
   }
   
   public <R extends T> R getRandomItemOfClass(Class<R> classOfElement)
   {
     Random rand = new Random();
     int start = rand.nextInt(bag.size());
     for (int i = start; i < (bag.size()+ start); i++){
   	  T t = (T)bag.get((i%bag.size()));
   	  if(classOfElement.isAssignableFrom(t.getClass())) return (R) t;
     }
     return null;
    
   }

   /** identical to get(index) */
   public T getValue(final int index)
   {
    
      return (T) bag.getValue(index) ;
   }

   public T set(final int index, final T element)
   {
       
       return (T) bag.set(index, element);
   }

   
   public T setValue(final int index, final T element)
   {
   	
       return (T) bag.setValue(index, element);
   }

   public boolean removeAll(final Collection<?> c)
   {
      
       return bag.removeAll(c);
   }
   
   public Bag cloneToBag(){
   	try{
	      return (Bag) bag.clone();
      }
      catch (CloneNotSupportedException e){
	      ExceptionDisplayer.getInstance().displayException(e);
	      return null;
      }
   }

   public boolean retainAll(final Collection<?> c)
   {
      return bag.retainAll(c);
   }

   /** Removes the object at the given index, shifting the other objects down. */
   public Object removeNondestructively(final int index)
   {
       return bag.removeNondestructively(index);
   }
   
   /** Removes the object, moving the topmost object into its position. */
   public boolean remove(final Object o)
   {
       return bag.remove(o);
   }
       
   /** Removes multiple instantiations of an object */
   public boolean removeMultiply(final Object o)
   {
   	return bag.removeMultiply(o);
   }

   /** Removes the object at the given index, moving the topmost object into its position. */
   public T remove(final int index)
   {
      return (T) bag.remove(index);
    }
                              
   public void clear()
   {
      bag.clear();
   }
       
   public T[] toArray()
   {
      return (T[])bag.toArray();
   }
   
   // ArrayList.toArray(Object[]) generates an error if the array passed in is null.
   // So I do the same thing.
   public <T>T[] toArray(T[] o)
   {
       
       return (T[])bag.toArray(o);
   }

   /** NOT fail-fast.  Use this method only if you're
       concerned about accessing numObjs and objs directly.  */
   public Iterator<T> iterator()
       {
       return bag.iterator();
       }
   
   /** Always returns null.  This method is to adhere to Indexed. */
   public Class componentType()
       {
       return null;
       }

   /** Sorts the bag according to the provided comparator */
   public void sort(Comparator<T> c) 
   {
       bag.sort(c);
   }

   /** Replaces all elements in the bag with the provided object. */
   public void fill(T o)
   {
   	bag.fill(o);
   }   

   /** Shuffles (randomizes the order of) the Bag */
   public void shuffle(Random random)
   {
      bag.shuffle(random);
   }
   
   /** Shuffles (randomizes the order of) the Bag */
   public void shuffle(ec.util.MersenneTwisterFast random)
   {
       bag.shuffle(random);
   }
   
   /** Reverses order of the elements in the Bag */
   public void reverse()
   {
       bag.reverse();
   }


	public boolean isEmpty() {

	   
	   return bag.isEmpty();
   }


	public int size() {
	   return bag.size();
   }

  
	
   }
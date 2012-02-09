package sim.app.episim.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.BagChangeEvent.BagChangeEventType;
import sim.util.Bag;
import sim.util.Indexed;



public class GenericBag<T> implements java.util.Collection<T>, java.io.Serializable, Cloneable
   {
	
	private Bag bag;
	private HashSet<BagChangeListener> changeListener = new HashSet<BagChangeListener>();
   
   public GenericBag() { 
   	bag = new Bag();
   }
   
   
   public GenericBag(int capacity) { 
   	bag = new Bag(capacity);
   }
       
  
   public GenericBag(final GenericBag<T> other)
   {
   	bag = new Bag(other.bag);
   }
   
   
   
   public boolean addAll(final Collection<? extends T> other) 
   { 
       if (other instanceof Bag) return bag.addAll((Bag)other);// avoid an array build
       else if (other instanceof GenericBag) return bag.addAll(((GenericBag)other).bag);// avoid an array build
       boolean result = bag.addAll(bag.numObjs, other.toArray()); 
       notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, other));
       return result;
   }

   public boolean addAll(final int index, final Collection<? extends T> other)
   {
   	 if (other instanceof Bag) return bag.addAll(index, (Bag)other);// avoid an array build
       else if (other instanceof GenericBag) return bag.addAll(index, ((GenericBag<T>)other).bag);// avoid an array build
       
       boolean result = bag.addAll(index, other.toArray());
       notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, other));
       return result;
   }

   public boolean addAll(final int index, final T[] other)
   {
      boolean result= bag.addAll(index, other);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, Arrays.asList(other)));
      return result;
   }
   
   public boolean addAll(final GenericBag<T> other) { 
   	boolean result = bag.addAll(bag.numObjs,other.bag);
   	notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, other));
   	return result;
   }

   public boolean addAll(final int index, final GenericBag<T> other) {
   	boolean result = bag.addAll(index, other.bag);
   	notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, other));
   	return result;
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
       ArrayList<T> objects = new ArrayList<T>();
       objects.add(top());
       T result = (T) bag.pop();
       notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, objects));
       return result;
   }
   
   /** Synonym for add(obj) -- stylistically, you should add instead unless you
       want to think of the Bag as a stack. */
   public boolean push(final T obj)
   {
   	ArrayList<T> objects = new ArrayList<T>();
      objects.add(obj);
      boolean result = bag.push(obj);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, objects)); 
   	return result;
   }
       
   public boolean add(final T obj)
   {
   	ArrayList<T> objects = new ArrayList<T>();
      objects.add(obj);
      boolean result = bag.add(obj);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, objects)); 
   	return result;
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
   	ArrayList<T> objects = new ArrayList<T>();
      objects.add(element);
      T result = (T) bag.set(index, element);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, objects)); 
   	
      return result;
   }

   
   public T setValue(final int index, final T element)
   {
   	ArrayList<T> objects = new ArrayList<T>();
      objects.add(element);
      T result = (T) bag.setValue(index, element);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, objects)); 
      return result;
   }

   public boolean removeAll(final Collection<?> c)
   {   	
      boolean result = bag.removeAll(c);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT,(Collection<? extends T>) c)); 	
      return result;
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
   	notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, bag));
   	boolean result = bag.retainAll(c);
   	notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.ADD_EVENT, (Collection<? extends T>)c));
   	return result;
   }

   /** Removes the object at the given index, shifting the other objects down. */
   public Object removeNondestructively(final int index)
   {
       Object result = bag.removeNondestructively(index);
       ArrayList<T> objects = new ArrayList<T>();
       objects.add((T)result);
       notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, objects)); 
       return result;
   }
   
   /** Removes the object, moving the topmost object into its position. */
   public boolean remove(final Object o)
   {
   	 ArrayList<T> objects = new ArrayList<T>();
       objects.add((T)o);
       boolean result = bag.remove(o);
       notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, objects));  
       return result;
   }
       
   /** Removes multiple instantiations of an object */
   public boolean removeMultiply(final Object o)
   {
   	 ArrayList<T> objects = new ArrayList<T>();
       objects.add((T)o);
       boolean result = bag.removeMultiply(o);
       notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, objects));
   	return result;
   }

   /** Removes the object at the given index, moving the topmost object into its position. */
   public T remove(final int index)
   {
   	ArrayList<T> objects = new ArrayList<T>();
      T result = (T) bag.remove(index);
      objects.add(result);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, objects));
      return result;
    }
                              
   public void clear()
   {
   	T[] deletedObjects = (T[]) bag.toArray();
   	bag.clear();
   	notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REMOVE_EVENT, Arrays.asList(deletedObjects)));
   }
       
   public Object[] toArray()
   {
      return bag.toArray();
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
   	ArrayList<T> objects = new ArrayList<T>();
      objects.add((T)o);
      notifyAllBagChangeListeners(new BagChangeEvent<T>(BagChangeEventType.REPLACE_EVENT, objects));
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
	
	public void addBagChangeListener(BagChangeListener listener){
		this.changeListener.add(listener);
	}
	
	public void removeBagChangeListener(BagChangeListener listener){
		this.changeListener.remove(listener);
	}
	
	private void notifyAllBagChangeListeners(BagChangeEvent<T> changeEvent){
		for(BagChangeListener listener: this.changeListener){
			listener.bagHasChanged(changeEvent);
		}
	}
  
	
   }
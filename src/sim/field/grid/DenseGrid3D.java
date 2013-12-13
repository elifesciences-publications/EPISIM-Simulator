/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.field.grid;
import sim.util.*;
import java.util.*;

/**
   A wrapper for 3D arrays of Objects.

   <p>This object expects that the 3D arrays are rectangular.  You are encouraged to access the array
   directly.  The object
   implements all of the Grid3D interface.  See Grid3D for rules on how to properly implement toroidal
   or hexagonal grids.
    
   <p>The width and height of the object are provided to avoid having to say field[x].length, etc.
    
   <p>We very strongly encourage you to examine <b>SparseGrid3D</b> first to see if it's more appropriate to your task.  If you need arbitrary numbers of Objects to be able to occupy the same location in the grid, or if you have very few Objects and a very large grid, or if your space is unbounded, you should probably use SparseGrid3D instead.
*/

public class DenseGrid3D extends AbstractGrid3D
    {
    private static final long serialVersionUID = 1;

    /** Should we remove bags in the field if they have been emptied, and let them GC, or should
        we keep them around? */
    public boolean removeEmptyBags = true;
    
    /** When a bag drops to one quarter capacity, should we replace it with a new bag? */
    public boolean replaceLargeBags = true;

    /** The size of an initial bag */
    public static final int INITIAL_BAG_SIZE = 16;

    /** No bags smaller than this size will be replaced regardless of the setting of <tt>replaceLargeBags</tt> */
    public static final int MIN_BAG_SIZE = 32;

    /** A bag must be larger than its contents by this ratio to be replaced <tt>replaceLargeBags</tt> is true*/
    public static final int LARGE_BAG_RATIO = 4;
    
    /** A bag to be replaced will be shrunk to this ratio if <tt>replaceLargeBags</tt> is true*/
    public static final int REPLACEMENT_BAG_RATIO = 2;

    public Bag[/**x*/][/**y*/][/**z**/] field;
    
    public DenseGrid3D (int width, int height, int length)
        {
        this.width = width;
        this.height = height;
        this.length = length;
        field = new Bag[width][height][length];
        }
    
    /** Returns a bag containing all the objects at a given location, or null when there are no objects at the location.
        You should NOT MODIFY THIS BAG. This is the actual container bag, and modifying it will almost certainly break
        the Dense Field object.   If you want to modify the bag, make a copy and modify the copy instead,
        using something along the lines of <b> new Bag(<i>foo</i>.getObjectsAtLocation(<i>location</i>)) </b>.
        Furthermore, changing values in the Dense Field may result in a different bag being used -- so you should
        not rely on this bag staying valid.
    */
    public Bag getObjectsAtLocation(final int x, final int y, final int z)
        {
        return field[x][y][z];
        }
                
    /** Returns a bag containing all the objects at a given location, or null when there are no objects at the location.
        You should NOT MODIFY THIS BAG. This is the actual container bag, and modifying it will almost certainly break
        the Dense Field object.   If you want to modify the bag, make a copy and modify the copy instead,
        using something along the lines of <b> new Bag(<i>foo</i>.getObjectsAtLocation(<i>location</i>)) </b>.
        Furthermore, changing values in the Dense Field may result in a different bag being used -- so you should
        not rely on this bag staying valid.
    */
    public Bag getObjectsAtLocation(Int3D location) { return getObjectsAtLocation(location.x, location.y, location.z); }
        
    /** Removes all the objects stored at the given location and returns them as a Bag (which you are free to modify). 
        The location is set to null (the bag is removed) regardless of the setting of removeEmptyBags.  */
    public Bag removeObjectsAtLocation(final int x, final int y, final int z)
        {
        Bag b = field[x][y][z];
        field[x][y][z] = null;
        return b;
        }
    
    /** Removes all the objects stored at the given location and returns them as a Bag (which you are free to modify). 
        The location is set to null (the bag is removed) regardless of the setting of removeEmptyBags.  */
    public Bag removeObjectsAtLocation(Int3D location) { return removeObjectsAtLocation(location.x, location.y, location.z); }


    public boolean removeObjectAtLocation(final Object obj, final int x, final int y, final int z)
        {
        Bag b = field[x][y][z];
        if (b==null) return false;
        boolean result = b.remove(obj);
        int objsNumObjs = b.numObjs;
        if (removeEmptyBags && objsNumObjs==0) b = null;
        else if (replaceLargeBags && objsNumObjs >= MIN_BAG_SIZE && objsNumObjs * LARGE_BAG_RATIO <= b.objs.length)
            b.shrink(objsNumObjs * REPLACEMENT_BAG_RATIO);
        return result;
        }

    public boolean removeObjectAtLocation(final Object obj, Int3D location) { return removeObjectAtLocation(obj, location.x, location.y, location.z); }
    
    public boolean removeObjectMultiplyAtLocation(final Object obj, final int x, final int y, final int z)
        {
        Bag b = field[x][y][z];
        if (b==null) return false;
        boolean result = b.removeMultiply(obj);
        int objsNumObjs = b.numObjs;
        if (removeEmptyBags && objsNumObjs==0) b = null;
        else if (replaceLargeBags && objsNumObjs >= MIN_BAG_SIZE && objsNumObjs * LARGE_BAG_RATIO <= b.objs.length)
            b.shrink(objsNumObjs * REPLACEMENT_BAG_RATIO);
        return result;
        }

    public boolean removeObjectMultiplyAtLocation(final Object obj, Int3D location) { return removeObjectMultiplyAtLocation(obj, location.x, location.y, location.z); }

    /** If the object is not at [fromX, fromY], then it's simply inserted into [toX, toY], and FALSE is returned.  
        Else it is removed ONCE from [fromX, fromY] and inserted into [toX, toY] and TRUE is returned.
        If the object exists multiply at [fromX, fromY], only one instance of the object is moved.*/
    public boolean moveObject(final Object obj, final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ)
        {
        boolean result = removeObjectAtLocation(obj, fromX, fromY, fromZ);
        addObjectToLocation(obj, toX, toY, toZ);
        return result;
        }
                
    /** If the object is not at FROM, then it's simply inserted into TO, and FALSE is returned.  
        Else it is removed ONCE from FROM and inserted into TO and TRUE is returned.
        If the object exists multiply at FROM, only one instance of the object is moved.*/
    public boolean moveObject(final Object obj, Int3D from, Int3D to) { return moveObject(obj, from.x, from.y, from.z, to.x, to.y, to.z); }
        

    public void moveObjects(final int fromX, final int fromY, final int fromZ, final int toX, final int toY, final int toZ)
        {
        addObjectsToLocation(removeObjectsAtLocation(fromX, fromY, fromZ), toX, toY, toZ);
        }

    public void moveObjects(Int3D from, Int3D to) { moveObjects(from.x, from.y, from.z, to.x, to.y, to.z); }
        
    public int numObjectsAtLocation(final int x, final int y, final int z)
        {
        Bag b = field[x][y][z];
        if (b == null) return 0;
        return b.numObjs;
        }

    public int numObjectsAtLocation(Int3D location) { return numObjectsAtLocation(location.x, location.y, location.z); }
    
    void buildBag(final Bag[] fieldxy, final int z)
        {
        fieldxy[z] = new Bag(INITIAL_BAG_SIZE);
        }
    
    /** Adds an object to a given location. */
    // this odd construction allows us to get under 32 bytes
    public void addObjectToLocation(final Object obj, final int x, final int y, final int z)
        {
        Bag[] fieldxy = field[x][y];
        if (fieldxy[z] == null) buildBag(fieldxy, z);
        fieldxy[z].add(obj);
        }
                
    public void addObjectToLocation(final Object obj, Int3D location) { addObjectToLocation(obj, location.x, location.y, location.z); }

    /** Adds an object to a given location. */
    public void addObjectsToLocation(final Bag objs, final int x, final int y, final int z)
        {
        if (objs==null) return;
        Bag[] fieldxy = field[x][y];
        if (fieldxy[z] == null) buildBag(fieldxy, z);
        fieldxy[z].addAll(objs);
        }

    public void addObjectsToLocation(final Bag objs, Int3D location) { addObjectsToLocation( objs, location.x, location.y, location.z); }

    /** Adds an object to a given location. */
    public void addObjectsToLocation(final Object[] objs, final int x, final int y, final int z)
        {
        if (objs==null) return;
        Bag[] fieldxy = field[x][y];
        if (fieldxy[z] == null) buildBag(fieldxy, z);
        fieldxy[z].addAll(0, objs);
        }

    public void addObjectsToLocation(final Object[] objs, Int3D location) { addObjectsToLocation( objs, location.x, location.y, location.z); }

    /** Adds an object to a given location. */
    public void addObjectsToLocation(final Collection objs, final int x, final int y, final int z)
        {
        if (objs==null) return;
        Bag[] fieldxy = field[x][y];
        if (fieldxy[z] == null) buildBag(fieldxy, z);
        fieldxy[z].addAll(objs);
        }

    /** Sets all the locations in the grid to null, and returns in a Bag all stored objects 
        (including duplicates but not null values).  You are free to modify the Bag. */
    public final Bag clear()
        {
        Bag bag = new Bag();
        Bag[][] fieldx = null;
        Bag[] fieldxy = null;
        final int width = this.width;
        final int height = this.height;
        for(int x=0;x<width;x++)
            {
            fieldx = field[x];
            for(int y = 0; y<height;y++)
                {
                fieldxy = fieldx[y];
                for(int z = 0; z < length; z++)
                    {
                    if (fieldxy[z]!=null) 
                        bag.addAll((Bag)(fieldxy[z]));
                    fieldxy[z]=null;
                    }
                }
            }
        return bag;
        }



       



    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist, This region forms a
     * square 2*dist+1 cells across, centered at (X,Y).  If dist==1, this
     * is equivalent to the so-called "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     *
     * <p>Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * <b>Note that the order and size of the result Bag may not correspond to the X and Y bags.</b>  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsMaxDistance(...)
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     *
     * <p> This function may only run in two modes: toroidal or bounded.  Unbounded lookup is not permitted, and so
     * this function is deprecated: instead you should use the other version of this function which has more functionality.
     * If "bounded",
     * then the neighbors are restricted to be only those which lie within the box ranging from (0,0) to (width, height), 
     * that is, the width and height of the grid.   if "toroidal",
     * then the environment is assumed to be toroidal, that is, wrap-around, and neighbors are computed in this fashion.  Toroidal
     * locations will not appear multiple times: specifically, if the neighborhood distance is so large that it wraps completely around
     * the width or height of the box, neighbors will not be counted multiple times.  Note that to ensure this, subclasses may need to
     * resort to expensive duplicate removal, so it's not suggested you use so unreasonably large distances.
     *
     * <p>The origin -- that is, the (x,y) point at the center of the neighborhood -- is always included in the results.
     *
     * <p>This function is equivalent to: <tt>getNeighborsMaxDistance(x,y,dist,toroidal ? Grid3D.TOROIDAL : Grid3D.BOUNDED, true, result, xPos, yPos);</tt>
     * 
     * @deprecated
     */
    public Bag getNeighborsMaxDistance( final int x, final int y, final int z, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos, IntBag zPos )
        {
        return getMooreNeighbors(x, y, z, dist, toroidal ? TOROIDAL : BOUNDED, true, result, xPos, yPos, zPos);
        }


    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist, This region forms a
     * square 2*dist+1 cells across, centered at (X,Y).  If dist==1, this
     * is equivalent to the so-called "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y) itself.
     * Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     *
     * <p>Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * <b>Note that the order and size of the result Bag may not correspond to the X and Y bags.</b>  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsMaxDistance(...)
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     *
     * <p>This function may be run in one of three modes: Grid3D.BOUNDED, Grid3D.UNBOUNDED, and Grid3D.TOROIDAL.  If "bounded",
     * then the neighbors are restricted to be only those which lie within the box ranging from (0,0) to (width, height), 
     * that is, the width and height of the grid.  If "unbounded", then the neighbors are not so restricted.  Note that unbounded
     * neighborhood lookup only makes sense if your grid allows locations to actually <i>be</i> outside this box.  For example,
     * SparseGrid3D permits this but ObjectGrid3D and DoubleGrid3D and IntGrid3D and DenseGrid3D do not.  Finally if "toroidal",
     * then the environment is assumed to be toroidal, that is, wrap-around, and neighbors are computed in this fashion.  Toroidal
     * locations will not appear multiple times: specifically, if the neighborhood distance is so large that it wraps completely around
     * the width or height of the box, neighbors will not be counted multiple times.  Note that to ensure this, subclasses may need to
     * resort to expensive duplicate removal, so it's not suggested you use so unreasonably large distances.
     *
     * <p>You can also opt to include the origin -- that is, the (x,y) point at the center of the neighborhood -- in the neighborhood results.
     */
    public Bag getMooreNeighbors( final int x, final int y, final int z, final int dist, int mode, boolean includeOrigin, Bag result, IntBag xPos, IntBag yPos, IntBag zPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();
        if( zPos == null )
            zPos = new IntBag();

        getMooreLocations( x, y, z, dist, mode, includeOrigin, xPos, yPos, zPos );
        return getObjectsAtLocations(xPos,yPos,zPos, result);
        }


    /**
     * Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist.  This region forms a
     * square 2*dist+1 cells across, centered at (X,Y).  If dist==1, this
     * is equivalent to the so-called "Moore Neighborhood" (the eight neighbors surrounding (X,Y)), plus (X,Y) itself.
     *
     * <p>For each Object which falls within this distance, adds the X position, Y position, and Object into the
     * xPos, yPos, and result Bag, clearing them first.  
     * Some <X,Y> positions may not appear
     * and that others may appear multiply if multiple objects share that positions.  Compare this function
     * with getNeighborsMaxDistance(...).
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     *
     * <p>This function may be run in one of three modes: Grid3D.BOUNDED, Grid3D.UNBOUNDED, and Grid3D.TOROIDAL.  If "bounded",
     * then the neighbors are restricted to be only those which lie within the box ranging from (0,0) to (width, height), 
     * that is, the width and height of the grid.  If "unbounded", then the neighbors are not so restricted.  Note that unbounded
     * neighborhood lookup only makes sense if your grid allows locations to actually <i>be</i> outside this box.  For example,
     * SparseGrid3D permits this but ObjectGrid3D and DoubleGrid3D and IntGrid3D and DenseGrid3D do not.  Finally if "toroidal",
     * then the environment is assumed to be toroidal, that is, wrap-around, and neighbors are computed in this fashion.  Toroidal
     * locations will not appear multiple times: specifically, if the neighborhood distance is so large that it wraps completely around
     * the width or height of the box, neighbors will not be counted multiple times.  Note that to ensure this, subclasses may need to
     * resort to expensive duplicate removal, so it's not suggested you use so unreasonably large distances.
     *
     * <p>You can also opt to include the origin -- that is, the (x,y) point at the center of the neighborhood -- in the neighborhood results.
     */
    public Bag getMooreNeighborsAndLocations(final int x, final int y, int z, final int dist, int mode, boolean includeOrigin, Bag result, IntBag xPos, IntBag yPos, IntBag zPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();
        if( zPos == null )
            zPos = new IntBag();

        getMooreLocations( x, y, z, dist, mode, includeOrigin, xPos, yPos, zPos);
        reduceObjectsAtLocations( xPos,  yPos,  zPos, result);
        return result;
        }



    /**
     * Gets all neighbors of a location that satisfy abs(x-X) + abs(y-Y) <= dist.  This region forms a diamond
     * 2*dist+1 cells from point to opposite point inclusive, centered at (X,Y).  If dist==1 this is
     * equivalent to the so-called "Von-Neumann Neighborhood" (the four neighbors above, below, left, and right of (X,Y)),
     * plus (X,Y) itself.
     *
     * <p>Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * Note that the order and size of the result Bag may not correspond to the X and Y bags.  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsHamiltonianDistance(...)
     * Returns the result Bag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     *
     * <p> This function may only run in two modes: toroidal or bounded.  Unbounded lookup is not permitted, and so
     * this function is deprecated: instead you should use the other version of this function which has more functionality.
     * If "bounded",
     * then the neighbors are restricted to be only those which lie within the box ranging from (0,0) to (width, height), 
     * that is, the width and height of the grid.   if "toroidal",
     * then the environment is assumed to be toroidal, that is, wrap-around, and neighbors are computed in this fashion.  Toroidal
     * locations will not appear multiple times: specifically, if the neighborhood distance is so large that it wraps completely around
     * the width or height of the box, neighbors will not be counted multiple times.  Note that to ensure this, subclasses may need to
     * resort to expensive duplicate removal, so it's not suggested you use so unreasonably large distances.
     *
     * <p>The origin -- that is, the (x,y) point at the center of the neighborhood -- is always included in the results.
     *
     * <p>This function is equivalent to: <tt>getNeighborsHamiltonianDistance(x,y,dist,toroidal ? Grid3D.TOROIDAL : Grid3D.BOUNDED, true, result, xPos, yPos);</tt>
     * 
     * @deprecated
     */
    public void getNeighborsHamiltonianDistance( final int x, final int y, final int z, final int dist, final boolean toroidal, Bag result, IntBag xPos, IntBag yPos, IntBag zPos)
        {
        getVonNeumannNeighbors(x, y, z, dist, toroidal ? TOROIDAL : BOUNDED, true,result, xPos, yPos, zPos);
        }


    /**
     * Gets all neighbors of a location that satisfy abs(x-X) + abs(y-Y) <= dist.  This region forms a diamond
     * 2*dist+1 cells from point to opposite point inclusive, centered at (X,Y).  If dist==1 this is
     * equivalent to the so-called "Von-Neumann Neighborhood" (the four neighbors above, below, left, and right of (X,Y)),
     * plus (X,Y) itself.
     *
     * <p>Places each x and y value of these locations in the provided IntBags xPos and yPos, clearing the bags first.
     * Then places into the result Bag any Objects which fall on one of these <x,y> locations, clearning it first.
     * Note that the order and size of the result Bag may not correspond to the X and Y bags.  If you want
     * all three bags to correspond (x, y, object) then use getNeighborsAndCorrespondingPositionsHamiltonianDistance(...)
     * Returns the result Bag (constructing one if null had been passed in).
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     *
     * <p>This function may be run in one of three modes: Grid3D.BOUNDED, Grid3D.UNBOUNDED, and Grid3D.TOROIDAL.  If "bounded",
     * then the neighbors are restricted to be only those which lie within the box ranging from (0,0) to (width, height), 
     * that is, the width and height of the grid.  If "unbounded", then the neighbors are not so restricted.  Note that unbounded
     * neighborhood lookup only makes sense if your grid allows locations to actually <i>be</i> outside this box.  For example,
     * SparseGrid3D permits this but ObjectGrid3D and DoubleGrid3D and IntGrid3D and DenseGrid3D do not.  Finally if "toroidal",
     * then the environment is assumed to be toroidal, that is, wrap-around, and neighbors are computed in this fashion.  Toroidal
     * locations will not appear multiple times: specifically, if the neighborhood distance is so large that it wraps completely around
     * the width or height of the box, neighbors will not be counted multiple times.  Note that to ensure this, subclasses may need to
     * resort to expensive duplicate removal, so it's not suggested you use so unreasonably large distances.
     *
     * <p>You can also opt to include the origin -- that is, the (x,y) point at the center of the neighborhood -- in the neighborhood results.
     */
    public Bag getVonNeumannNeighbors( final int x, final int y, int z, final int dist, int mode, boolean includeOrigin, Bag result, IntBag xPos, IntBag yPos, IntBag zPos )
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();
        if( zPos == null )
            zPos = new IntBag();

        getVonNeumannLocations( x, y, z, dist, mode, includeOrigin, xPos, yPos, zPos);
        return getObjectsAtLocations(xPos,yPos,zPos, result);
        }



    /**
     * Gets all neighbors of a location that satisfy abs(x-X) + abs(y-Y) <= dist.  This region forms a diamond
     * 2*dist+1 cells from point to opposite point inclusive, centered at (X,Y).  If dist==1 this is
     * equivalent to the so-called "Von-Neumann Neighborhood" (the four neighbors above, below, left, and right of (X,Y)),
     * plus (X,Y) itself.
     *
     * <p>For each Object which falls within this distance, adds the X position, Y position, and Object into the
     * xPos, yPos, and result Bag, clearing them first.  
     * Some <X,Y> positions may not appear
     * and that others may appear multiply if multiple objects share that positions.  Compare this function
     * with getNeighborsMaxDistance(...).
     * Returns the result Bag.
     * null may be passed in for the various bags, though it is more efficient to pass in a 'scratch bag' for
     * each one.
     *
     * <p>This function may be run in one of three modes: Grid3D.BOUNDED, Grid3D.UNBOUNDED, and Grid3D.TOROIDAL.  If "bounded",
     * then the neighbors are restricted to be only those which lie within the box ranging from (0,0) to (width, height), 
     * that is, the width and height of the grid.  If "unbounded", then the neighbors are not so restricted.  Note that unbounded
     * neighborhood lookup only makes sense if your grid allows locations to actually <i>be</i> outside this box.  For example,
     * SparseGrid3D permits this but ObjectGrid3D and DoubleGrid3D and IntGrid3D and DenseGrid3D do not.  Finally if "toroidal",
     * then the environment is assumed to be toroidal, that is, wrap-around, and neighbors are computed in this fashion.  Toroidal
     * locations will not appear multiple times: specifically, if the neighborhood distance is so large that it wraps completely around
     * the width or height of the box, neighbors will not be counted multiple times.  Note that to ensure this, subclasses may need to
     * resort to expensive duplicate removal, so it's not suggested you use so unreasonably large distances.
     *
     * <p>You can also opt to include the origin -- that is, the (x,y) point at the center of the neighborhood -- in the neighborhood results.
     */
    public Bag getVonNeumannNeighborsAndLocations(final int x, final int y, final int z, final int dist, int mode, boolean includeOrigin, Bag result, IntBag xPos, IntBag yPos, IntBag zPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();
        if( zPos == null )
            zPos = new IntBag();

        getVonNeumannLocations( x, y, z, dist, mode, includeOrigin, xPos, yPos, zPos );
        reduceObjectsAtLocations( xPos,  yPos, zPos, result);
        return result;
        }



    public Bag getRadialNeighbors( final int x, final int y, final int z, final int dist, int mode, boolean includeOrigin,  Bag result, IntBag xPos, IntBag yPos, IntBag zPos )
        {
        return getRadialNeighbors(x, y, z, dist, mode, includeOrigin, Grid2D.ANY, true, result, xPos, yPos, zPos);
        }


    public Bag getRadialNeighborsAndLocations( final int x, final int y, final int z, final int dist, int mode, boolean includeOrigin, Bag result, IntBag xPos, IntBag yPos, IntBag zPos )
        {
        return getRadialNeighborsAndLocations(x, y, z, dist, mode, includeOrigin, Grid2D.ANY, true, result, xPos, yPos, zPos);
        }


    public Bag getRadialNeighbors( final int x, final int y, int z, final int dist, int mode, boolean includeOrigin,  int measurementRule, boolean closed,  Bag result, IntBag xPos, IntBag yPos, IntBag zPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();
        if( zPos == null )
            zPos = new IntBag();

        getRadialLocations( x, y, z, dist, mode, includeOrigin, measurementRule, closed, xPos, yPos, zPos );
        return getObjectsAtLocations(xPos,yPos,zPos,result);
        }

    public Bag getRadialNeighborsAndLocations( final int x, final int y, int z, final int dist, int mode, boolean includeOrigin,  int measurementRule, boolean closed,  Bag result, IntBag xPos, IntBag yPos, IntBag zPos)
        {
        if( xPos == null )
            xPos = new IntBag();
        if( yPos == null )
            yPos = new IntBag();
        if( zPos == null )
            zPos = new IntBag();

        getRadialLocations( x, y, z, dist, mode, includeOrigin, measurementRule, closed, xPos, yPos, zPos );
        reduceObjectsAtLocations( xPos,  yPos, zPos, result);
        return result;
        }



    // For each <xPos, yPos> location, puts all such objects into the result bag.  Modifies
    // the xPos and yPos bags so that each position corresponds to the equivalent result in
    // in the result bag.
    void reduceObjectsAtLocations(final IntBag xPos, final IntBag yPos, final IntBag zPos, Bag result)
        {
        if (result==null) result = new Bag();
        else result.clear();

        // build new bags with <x,y> locations one per each result
        IntBag newXPos = new IntBag();
        IntBag newYPos = new IntBag();
        IntBag newZPos = new IntBag();

        final int len = xPos.numObjs;
        final int[] xs = xPos.objs;
        final int[] ys = yPos.objs;
        final int[] zs = zPos.objs;

        // for each location...
        for(int i=0; i < len; i++)
            {
            Bag temp = field[xPos.objs[i]][yPos.objs[i]][zPos.objs[i]] ;
            final int size = temp.numObjs;
            final Object[] os = temp.objs;
            // for each object at that location...
            for(int j = 0; j < size; j++)
                {
                // add the result, the x, and the y
                result.add(os[j]);
                newXPos.add(xs[i]);
                newYPos.add(ys[i]);
                newZPos.add(zs[i]);
                }
            }
                
        // dump the new IntBags into the old ones
        xPos.clear();
        xPos.addAll(newXPos);
        yPos.clear();
        yPos.addAll(newYPos);
        zPos.clear();
        zPos.addAll(newZPos);
        }
                

    /* For each <xPos,yPos> location, puts all such objects into the result bag.  Returns the result bag.
       If the provided result bag is null, one will be created and returned. */
    Bag getObjectsAtLocations(final IntBag xPos, final IntBag yPos, final IntBag zPos, Bag result)
        {
        if (result==null) result = new Bag();
        else result.clear();

        final int len = xPos.numObjs;
        final int[] xs = xPos.objs;
        final int[] ys = yPos.objs;
        final int[] zs = zPos.objs;
        for(int i=0; i < len; i++)
            {
            // a little efficiency: add if we're 1, addAll if we're > 1, 
            // do nothing if we're 0
            Bag temp = field[xPos.objs[i]][yPos.objs[i]][zPos.objs[i]];
            if (temp!=null)
                {
                int n = temp.numObjs;
                if (n==1) result.add(temp.objs[0]);
                else if (n > 1) result.addAll(temp);
                }
            }
        return result;
        }


    }

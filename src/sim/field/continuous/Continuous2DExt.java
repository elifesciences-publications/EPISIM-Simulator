package sim.field.continuous;

import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableInt2D;


public class Continuous2DExt extends Continuous2D {
	 public Continuous2DExt(final double discretization, double width, double height)
    {
		 super(discretization, width, height);
    }
	  public Continuous2DExt(Continuous2D other)
     {
		  super(other);
     }
	  
	  /** Returns a bag containing AT LEAST those objects within the bounding box surrounding the
     specified distance of the specified position.  The bag could include other objects than this.
     If toroidal, then wrap-around possibilities are also considered.
     If nonPointObjects, then it is presumed that
     the object isn't just a point in space, but in fact fills an area in space where the x/y point location
     could be at the extreme corner of a bounding box of the object.  In this case we include the object if
     any part of the bounding box could overlap into the desired region.  To do this, if nonPointObjects is
     true, we extend the search space by one extra discretization in all directions.  For small distances within
     a single bucket, this returns nine bucket's worth rather than 1, so if you know you only care about the
     actual x/y points stored, rather than possible object overlap into the distance sphere you specified,
     you'd want to set nonPointObjects to FALSE. 
     
     <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
     to within the boundaries before computation.
 */
     
 public Bag getNeighborsWithinDistance( final Double2D position, final double distanceX, final double distanceY, final boolean toroidal,
     final boolean nonPointObjects)
     { return getNeighborsWithinDistance(position, distanceX, distanceY, toroidal, nonPointObjects, null); }
 
 /** Puts into the result Bag (and returns it) AT LEAST those objects within the bounding box surrounding the
 specified distance of the specified position.  If the result Bag is null, then a Bag is created.
 
 <p>The bag could include other objects than this.
 If toroidal, then wrap-around possibilities are also considered.
 If nonPointObjects, then it is presumed that
 the object isn't just a point in space, but in fact fills an area in space where the x/y point location
 could be at the extreme corner of a bounding box of the object.  In this case we include the object if
 any part of the bounding box could overlap into the desired region.  To do this, if nonPointObjects is
 true, we extend the search space by one extra discretization in all directions.  For small distances within
 a single bucket, this returns nine bucket's worth rather than 1, so if you know you only care about the
 actual x/y points stored, rather than possible object overlap into the distance sphere you specified,
 you'd want to set nonPointObjects to FALSE. 
 
 <p> Note: if the field is toroidal, and position is outside the boundaries, it will be wrapped
 to within the boundaries before computation.
*/

public Bag getNeighborsWithinDistance( Double2D position, final double distanceX, final double distanceY, final boolean toroidal,
 final boolean nonPointObjects, Bag result)
 {
 // push location to within legal boundaries
 if (toroidal && (position.x >= width || position.y >= height || position.x < 0 || position.y < 0))
     position = new Double2D(tx(position.x), ty(position.y));
 
 double discDistanceX = distanceX / discretization;
 double discDistanceY = distanceY / discretization;
 double discX = position.x / discretization;
 double discY = position.y / discretization;
 
 if (nonPointObjects)
     {
     // We assume that the discretization is larger than the bounding
     // box width or height for the object in question.  In this case, then
     // we can just increase the range by 1 in each direction and we are
     // guaranteed to have the location of the object in our collection.
     discDistanceX++;
     discDistanceY++;
     }

 final int expectedBagSize = 1;  // in the future, pick a smarter bag size?
 if (result!=null) result.clear();
 else result = new Bag(expectedBagSize);
 Bag temp;

 MutableInt2D speedyMutableInt2D = this.speedyMutableInt2D;  // a little faster (local)

     
 // do the loop
 if( toroidal )
     {
     final int iWidth = (int)(StrictMath.ceil(width / discretization));
     final int iHeight = (int)(StrictMath.ceil(height / discretization));

     // we're using StrictMath.floor instead of Math.floor because
     // Math.floor just calls StrictMath.floor, and so using the
     // StrictMath version may help in the inlining (one function
     // to inline, not two).  They should be identical in function anyway.
     
     int minX = (int) StrictMath.floor(discX - discDistanceX);
     int maxX = (int) StrictMath.floor(discX + discDistanceX);
     int minY = (int) StrictMath.floor(discY - discDistanceY);
     int maxY = (int) StrictMath.floor(discY + discDistanceY);

     if (position.x + distanceX >= width && maxX == iWidth - 1)  // oops, need to recompute wrap-around if width is not a multiple of discretization
         maxX = 0;

     if (position.y + distanceX >= height && maxY == iHeight - 1)  // oops, need to recompute wrap-around if height is not a multiple of discretization
         maxY = 0;



     // we promote to longs so that maxX - minX can't totally wrap around by accident
     if ((long)maxX - (long)minX >= iWidth)  // total wrap-around.
         { minX = 0; maxX = iWidth-1; }
     if ((long)maxY - (long)minY >= iHeight) // similar
         { minY = 0; maxY = iHeight-1; }

     // okay, now tx 'em.
     final int tmaxX = toroidal(maxX,iWidth);
     final int tmaxY = toroidal(maxY,iHeight);
     final int tminX = toroidal(minX,iWidth);
     final int tminY = toroidal(minY,iHeight);
                 
     int x = tminX ;
     do
         {
         int y = tminY;
         do
             {
             // grab location
             speedyMutableInt2D.x=x;
             speedyMutableInt2D.y=y;
             temp = getRawObjectsAtLocation(speedyMutableInt2D);
             if( temp != null && !temp.isEmpty())
                 {
                 // a little efficiency: add if we're 1, addAll if we're > 1, 
                 // do nothing if we're <= 0 (we're empty)
                 final int n = temp.numObjs;
                 if (n==1) result.add(temp.objs[0]);
                 else result.addAll(temp);
                 }

             // update y
             if( y == tmaxY )
                 break;
             else if( y == iHeight-1 )
                 y = 0;
             else
                 y++;
             }
         while(true);

         // update x
         if( x == tmaxX )
             break;
         else if( x == iWidth-1 )
             x = 0;
         else
             x++;
         }
     while(true);
     }
 else
     {
     // we're using StrictMath.floor instead of Math.floor because
     // Math.floor just calls StrictMath.floor, and so using the
     // StrictMath version may help in the inlining (one function
     // to inline, not two).  They should be identical in function anyway.
     
     int minX = (int) StrictMath.floor(discX - discDistanceX);
     int maxX = (int) StrictMath.floor(discX + discDistanceX);
     int minY = (int) StrictMath.floor(discY - discDistanceY);
     int maxY = (int) StrictMath.floor(discY + discDistanceY);

     // for non-toroidal, it is easier to do the inclusive for-loops
     for(int x = minX; x<= maxX; x++)
         for(int y = minY ; y <= maxY; y++)
             {
             // grab location
             speedyMutableInt2D.x=x;
             speedyMutableInt2D.y=y;
             temp = getRawObjectsAtLocation(speedyMutableInt2D);
             if( temp != null && !temp.isEmpty())
                 {
                 // a little efficiency: add if we're 1, addAll if we're > 1, 
                 // do nothing if we're <= 0 (we're empty)
                 final int n = temp.numObjs;
                 if (n==1) result.add(temp.objs[0]);
                 else result.addAll(temp);
                 }
             }
     }

 return result;
 }
}

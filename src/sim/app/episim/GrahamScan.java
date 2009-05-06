/*
 * Class.java
 *
 * Created on 16. Februar 2005, 15:30
 */

package sim.app.episim;


public class GrahamScan
{
    private static GrahamPoint[] p; // array of points
    private static int n;     // number of points
    private static int m;     // number of points of the polygon
    

    /** rearranges points in the array such that the
     *  first m points are the nodes of the convex hull
     *  polygon in order; returns m
     */
    public static int computeHull(GrahamPoint[] q, int numberfields)
    {
        p=q;
        n=numberfields;
        if (numberfields>p.length)
        {
           // System.out.println("Grahamscan: numberfields too large:"+numberfields+">"+p.length);
        }
        else
            grahamScan();
        return m;
    }


    /** exchanges two points p[i] and p[j]
     */
    private static void exchange(int i, int j)
    {
        GrahamPoint t=p[i];
        p[i]=p[j];
        p[j]=t;
    }


    /** computes coordinates of all points relative to point p0
     */
    private static void makeRelativeTo(GrahamPoint p0)
    {
        int i;
        GrahamPoint p1=new GrahamPoint(p0); // notwendig, weil p0 in p[] sein kann
        for (i=0; i<n; i++)
            p[i].makeRelativeTo(p1);
    }


    /** determines index of a point with minimum y-coordinate
     */
    private static int indexOfLowestGrahamPoint()
    {
        int i, min=0;
        for (i=1; i<n; i++)
            if (p[i].isLower(p[min]))
                min=i;
        return min;
    }


    /** tests if the i-th node of the polygon is convex
     */
    private static boolean isConvex(int i)
    {
        return p[i].relativeTo(p[i-1]).isLess(p[i+1].relativeTo(p[i-1]));
    }


    /** sorts points except p[0] according to their angle
     */
    private static void sort()
    {
        quicksort(1, n - 1); // ohne Punkt 0
    }


    /** sorts points according to their angle
     */
    protected static void quicksort(int lo, int hi)
    {
        int i=lo, j=hi;
        GrahamPoint q=p[(lo+hi)/2];
        while (i<=j)
        {
            while (p[i].isLess(q)) i++;
            while (q.isLess(p[j])) j--;
            if (i<=j) exchange(i++, j--);
        }
        if (lo<j) quicksort(lo, j);
        if (i<hi) quicksort(i, hi);
    }


    /** computes hull points with the Graham-Scan algorithm
     */
    private static void grahamScan()
    {
        exchange(0, indexOfLowestGrahamPoint());
        GrahamPoint q=new GrahamPoint(p[0]);
        makeRelativeTo(q);
        sort();
        makeRelativeTo(q.reversed());
        int i=3, k=3;
        while (k<n)
        {
            exchange(i, k);
            while (!isConvex(i-1))
                exchange(i-1, i--);
            i++;
            k++;
        }
        m=i;
    }    

}   // end class GrahamScan


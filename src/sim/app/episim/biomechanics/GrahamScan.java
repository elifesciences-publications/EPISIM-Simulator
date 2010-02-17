package sim.app.episim.biomechanics;




public class GrahamScan
{
    private Vertex[] v;
    private int n;
    private int h;

    public int computeHull(Vertex[] v)
    {
        this.v=v;
        n=v.length;
        if (n<3) return n;
        h=0;
        grahamScan();
        return h;
    }

    private void grahamScan()
    {
        exchange(0, indexOfLowestVertex());
        Vertex pl=new Vertex(v[0]);
        makeRelTo(pl);
        sort();
        makeRelTo(pl.reversed());
        int i=3, k=3;
        while (k<n)
        {
            exchange(i, k);
            while (!isConvex(i-1))
                exchange(i-1, i--);
            k++;
            i++;
        }
        h=i;
    }

    private void exchange(int i, int j)
    {
        Vertex t=v[i];
        v[i]=v[j];
        v[j]=t;
    }

    private void makeRelTo(Vertex v0)
    {
        int i;
        Vertex v1=new Vertex(v0); // notwendig, weil v0 in v[] sein kann
        for (i=0; i<n; i++)
            v[i].makeRelTo(v1);
    }

    private int indexOfLowestVertex()
    {
        int i, min=0;
        for (i=1; i<n; i++)
            if (v[i].getDoubleY()<v[min].getDoubleY() || v[i].getDoubleY()==v[min].getDoubleY() && v[i].getDoubleX()<v[min].getDoubleX())
                min=i;
        return min;
    }

    private boolean isConvex(int i)
    {
        return v[i].isConvex(v[i-1], v[i+1]);
    }

    private void sort()
    {
        quicksort(1, n-1); // ohne Punkt 0
    }

    private void quicksort(int lo, int hi)
    {
        int i=lo, j=hi;
        Vertex q=v[(lo+hi)/2];
        while (i<=j)
        {
            while (v[i].isLess(q)) i++;
            while (q.isLess(v[j])) j--;
            if (i<=j) exchange(i++, j--);
        }
        if (lo<j) quicksort(lo, j);
        if (i<hi) quicksort(i, hi);
    }

}
package sim.app.episim;



/**
 *
 * @author  Administrator
 */

public class GrahamPoint implements java.io.Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5484521194994996656L;
	public double x, y;    // Koordinaten
    public int nr;
    public boolean marked=false;


    public GrahamPoint(int nr, double x, double y)
    {
        this.nr=nr;
        this.x=x;
        this.y=y;
    }


    public GrahamPoint(double x, double y)
    {
        this(0, x, y);
    }


    public GrahamPoint(GrahamPoint p)
    {
        this(0, p.x, p.y);
    }


    public GrahamPoint()
    {
        this(0, 0, 0);
    }


    /** berechnet das Quadrat des Euklidischen Abstands
     *  dieses Punkts zum Nullpunkt
     */
    public double distance2()
    {
        return x*x + y*y;
    }


    /** transformiert diesen Punkt
     *  relativ zu p als Nullpunkt
     */
    public void makeRelativeTo(GrahamPoint p)
    {
        x-=p.x;
        y-=p.y;
    }


    /** erzeugt einen neuen Punkt, der diesen Punkt
     *  relativ zu p darstellt
     */
    public GrahamPoint relativeTo(GrahamPoint p)
    {
        return new GrahamPoint(x-p.x, y-p.y);
    }


    /** erzeut einen am Nullpunkt zu diesem Punkt gespiegelten Punkt
     */
    public GrahamPoint reversed()
    {
        return new GrahamPoint(-x, -y);
    }


    /** gibt true zurück, wenn der Ortsvektor dieses Punkts
     *  einen kleineren Winkel als der Ortsvektor von p hat
     *  oder, bei gleichem Winkel, wenn er kürzer ist als p
     */
    public boolean isLess(GrahamPoint p)
    {
        double f=x*p.y - p.x*y;    // cross product
        return f>0 || f==0 && distance2()<p.distance2();
    }


    /** gibt true zurück, wenn dieser Punkt eine kleinere
     *  y-Koordinate als p hat oder, bei gleicher y-Koordinate,
     *  wenn er eine kleinere x-Koordinate als p hat
     */
    public boolean isLower(GrahamPoint p)
    {
        return y<p.y || y==p.y && x<p.x;
    }

}    // end class GrahamPoint

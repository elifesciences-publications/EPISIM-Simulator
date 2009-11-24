package sim.app.episim.util;

import java.util.EventObject;

public class WriteEvent extends EventObject
{
  private static final long serialVersionUID = -6012449972049350340L;

 
  /**
   * Konstruktor schleift Parameter an
   * den parent-Konstruktor durch.
   *
   * @param source Objekt
   */
  public WriteEvent(Object source)
  {
     super(source);
  }
}
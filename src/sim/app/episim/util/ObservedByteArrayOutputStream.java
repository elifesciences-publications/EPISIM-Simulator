package sim.app.episim.util;
import java.io.ByteArrayOutputStream;
import java.util.Vector;



public class ObservedByteArrayOutputStream extends ByteArrayOutputStream
{
  
  Vector<ByteArrayWriteListener> listeners = new Vector<ByteArrayWriteListener>();
 
 
  
  public void addWriteListener(ByteArrayWriteListener listener)
  {
     if (listeners == null)
     {
        listeners = new Vector<ByteArrayWriteListener>();
     }
     listeners.add(listener);
  }
 
 
  public void removeWriteListener(ByteArrayWriteListener listener)
  {
     if (listeners != null)
     {
        listeners.remove(listener);
     }  
  }
 
  
  private void fireWriteEvent(Object object)
  {
     WriteEvent event = new WriteEvent(object);
     
     for (ByteArrayWriteListener listener: listeners)
     {
        listener.textWasWritten(event);
     }
  }
 
 
  public void write(byte[] array, int offset, int length)
  {
     super.write(array, offset, length);
     
     
     fireWriteEvent(this);
  }
}
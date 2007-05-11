package sim.portrayal;
import sim.engine.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** An Inspector is a JPanel containing information about some object,
    and updates its displayed information when updateInspector() is called.
    In-between calls to updateInspector(), the Inspector should show the same
    information despite repeated repaints() etc.  
    Inspectors commonly also allow the user to change the information,
    but this is not required: they can be "read-only" if desired.

    <p>If your inspector is not volatile (it doesn't change frame-to-frame)
    then you should setInspectorVolatile(false).  It's more efficient.
    
    <p>Your inspector should also update itself at time or when otherwise attached
    to the object it's supposed to inspect.  Generally speaking, a non-volatile
    inspector never has its updateInspector method called except manually (via
    the button created in makeUpdateButton() for example).  If you like, you can
    simply call updateInspector() in your own constructor.
*/

public abstract class Inspector extends JPanel
    {
    boolean _volatile = true;
        
    /** Set to true (default) if the inspector should be updated every time step.  Else set to false. */
    public void setVolatile(boolean val) {_volatile = val;}
        
    /** Returns true (default) if the inspector should be updated every time step.  Else returns false. */
    public boolean isVolatile() { return _volatile; }
        
    public abstract void updateInspector();
    
    /*
      public void submitInspector() { }
    */
    
    public Steppable getUpdateSteppable()
        {
        return new Steppable()
            {
            public void step(final SimState state)
                {
                SwingUtilities.invokeLater(new Runnable()
                    {
                    public void run()
                        {
                        synchronized(state.schedule)
                            {
                            Inspector.this.updateInspector();
                            Inspector.this.repaint();
                            }
                        }
                    });
                }
            };
        }
    
    /** If you've added an UpdateButton with makeUpdateButton(), it will call updateButtonPressed
        when it is pressed, which by default will call updateInspector().  Override this
        method if that's not the behavior you want. */
    protected void updateButtonPressed()
        {
        updateInspector();
        }
    
    /*
      protected void submitButtonPressed()
      {
      submitInspector();
      }
    
      protected JButton submitButton;
    
      public void makeSubmitButtonDefault()
      {
      submitButton.getRootPane().setDefaultButton(submitButton);
      }
    
      public Component makeSubmitAndUpdateButtons()
      {
      JPanel p = new JPanel();
      JScrollPane scroll = new JScrollPane(this);
      scroll.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
      p.add(scroll,BorderLayout.CENTER);
      Box b = new Box(BoxLayout.X_AXIS);
      p.add(b,BorderLayout.SOUTH);
      JButton jb = new JButton(UPDATE_ICON);
      jb.setText("Refresh");
      jb.addActionListener(new ActionListener()
      {
      public void actionPerformed(ActionEvent e)
      {
      updateButtonPressed();
      }
      });
            
      b.add(jb);
        
      submitButton = new JButton("Set");
      jb.addActionListener(new ActionListener()
      {
      public void actionPerformed(ActionEvent e)
      {
      submitButtonPressed();
      }
      });
      b.add(submitButton);
      b.add(b.createGlue());
      return p;
      }
    */
    
    /** A convenient function to create UpdateButton which you might add to the bottom of the JPanel
        (assuming it still is using BorderLayout).
        This is helpful for the user if your inspector isn't volatile. */
    public Component makeUpdateButton()
        {
        JButton jb = new JButton(UPDATE_ICON);
        jb.setText("Refresh");
        //jb.setPressedIcon(UPDATE_ICON_P);
        //jb.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
                
        jb.setToolTipText("Refreshes this inspector to reflect the current underlying values in the model.");

        jb.addActionListener(new ActionListener()
            {
            public void actionPerformed(ActionEvent e)
                {
                updateButtonPressed();
                }
            });
        return jb;
        }

    public static final ImageIcon UPDATE_ICON = iconFor("Update.png");
    //public static final ImageIcon UPDATE_ICON_P = iconFor("UpdatePressed.png");

    /** Returns icons for a given filename, such as "Layers.png". A utility function. */
    static ImageIcon iconFor(String name)
        {
        return new ImageIcon(Inspector.class.getResource(name));
        }

    }

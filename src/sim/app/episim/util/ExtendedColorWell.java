package sim.app.episim.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JPanel;


public class ExtendedColorWell extends JPanel
   {
   Color color;
               
   public ExtendedColorWell(Component parent) 
       {
       this(parent, new Color(0,0,0,0));
       }
                       
   public ExtendedColorWell(final Component parent, Color c)
       {
       color = c;
       addMouseListener(new MouseAdapter()
           {
           public void mouseReleased(MouseEvent e)
               {
               Color col = JColorChooser.showDialog(parent, "Choose Color", getBackground());
               setColor(col);
               }
           });
       setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
       }
               
   // maybe in the future we'll add an opacity mechanism
   public void paintComponent(Graphics g)
       {
       g.setColor(color);
       g.fillRect(0,0,getWidth(),getHeight());
       }

   public void setColor(Color c)
       {
       if (c != null) 
           color = changeColor(c);
       repaint();
       }
                       
   public Color getColor()
       {
       return color;
       }
                       
   public Color changeColor(Color c) 
       {
       return c;
       }
}


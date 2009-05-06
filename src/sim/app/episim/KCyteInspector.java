package sim.app.episim;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
//import sim.portrayal.grid.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;


import sim.display.*;
import sim.engine.SimState;

public class KCyteInspector extends Inspector
    {
    public Inspector originalInspector;
    
    public KCyteInspector(Inspector originalInspector,
                                LocationWrapper wrapper,
                                GUIState guiState)
        {
   	 
       this.originalInspector = originalInspector;
       add(originalInspector, BorderLayout.CENTER);
      
      
        }
        
    public void updateInspector()
        {
        originalInspector.updateInspector();
        }
    }

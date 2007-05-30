package sim.app.episim;
import sim.portrayal.*;
//import sim.portrayal.grid.*;

import java.awt.*;
import sim.display.*;

public class KCyteClassInspector extends Inspector
    {
    public Inspector originalInspector;
    
    public KCyteClassInspector(Inspector originalInspector,
                                LocationWrapper wrapper,
                                GUIState guiState)
        {
        this.originalInspector = originalInspector;
        
      
        setLayout(new BorderLayout());
        add(originalInspector, BorderLayout.CENTER);
     
        
      
        }
        
    public void updateInspector()
        {
        originalInspector.updateInspector();
        }
    }

package sim.util.gui;
import java.awt.*;
import javax.swing.*;

/** LabelledList is a JPanel which makes it easy to set up two columns of
    Components.  Most commonly, you would have the right column be widgets and
    the left column be the labels describing those widgets.  Two special methods
    are provided: <code>add(Component,Component)</code> lets you explicitly add
    a new row consisting of a component in the left column and another in the
    right column.  <code>addLabelled(String,Component)</code> lets you add a
    new row with a component in the right column and a JLabel showing the given
    string in the left column.
    
    <p>LabelledList stretches the right column to fill as much space as possible;
    the left column only receives the minimum space necessary to fully display every
    element in the column.  Left column elements are right-justified, and Right
    column elements are left justified.  The columns are anchored to the top of
    the LabelledList component; any extra space appears at the bottom of the component.
    
    <p>LabelledList uses BorderLayout as its layout manager.  Please do not change it.
    However, your are welcome to add subcomponents anywhere but BorderLayout.NORTH,
    which is where the columns hang down from.
    
    <p>There is no way to remove components (this is a very simple class!).  What did
    you want, a JTable?  Sheesh!
*/

public class LabelledList extends JComponent
    {
    JComponent consolePanel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    int y =0;
    
    public LabelledList()
        {
        super.setLayout(new BorderLayout());
        consolePanel.setLayout(gridbag);
        super.add(consolePanel, BorderLayout.NORTH);
        gbc.ipady=0; gbc.ipady = 0; gbc.weighty = 0;
        }
    
    /** Creates a Labelled List with a provided border label.  If label is null, just does new LabelledList()*/
    public LabelledList(String borderLabel)
        {
        this();
        if (borderLabel != null) setBorder(new javax.swing.border.TitledBorder(borderLabel));
        }

    /* Creates a new row, with the given components in the right row, and a JLabel of the given string in the left row. */
    public void addLabelled(String left, Component right)
        {
        add(new JLabel(left),right);
        }

    /* Creates a new row, with the given components in the left and right columns of the row respectively. */
    public void add(Component left, Component right)
        {
        // set up the left component
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; gbc.anchor=GridBagConstraints.EAST; gbc.fill=GridBagConstraints.NONE; gbc.gridwidth = 1; gbc.insets = new Insets(0,4,0,2);
        gridbag.setConstraints(left,gbc);
        consolePanel.add(left);
        
        // set up the right component
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor=GridBagConstraints.WEST; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.insets = new Insets(0,2,0,4);
        gridbag.setConstraints(right,gbc);
        consolePanel.add(right);
        
        // increment the count
        y++;
        }

    /* Inserts a component spanning both columns. */
    public Component add(Component comp)
        {
        addComponent(comp);
        return comp;
        }
        
    /* Inserts a component spanning both columns.  Synonym for add(comp) */
    public void addComponent(Component comp)
        {
        // set as a "left" component, but spanning using HORIZONTAL/REMAINDER
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 1; gbc.anchor=GridBagConstraints.EAST; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.insets = new Insets(0,4,0,4);
        gridbag.setConstraints(comp, gbc);
        consolePanel.add(comp);
        
        // increment the count
        y++;
        }
    }

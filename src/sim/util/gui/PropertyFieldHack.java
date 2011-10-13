package sim.util.gui;

import javax.swing.JCheckBox;


public class PropertyFieldHack extends PropertyField{
	
	 /** Constructs a PropertyField as just a writeable, empty text field. */
   public PropertyFieldHack()
       {
       this(null,"",true);
       }
       
   /** Constructs a PropertyField as a writeable text field with the provided initial value. */
   public PropertyFieldHack(String initialValue)
       {
       this(null,initialValue,true);
       }
   
   /** Constructs a PropertyField as a text field with the provided initial value, either writeable or not. */
   public PropertyFieldHack(String initialValue, boolean isReadWrite)
       {
       this(null,initialValue,isReadWrite);
       }
   
   /** Constructs a labelled PropertyField as a writeable text field with the provided initial value. */
   public PropertyFieldHack(String label, String initialValue)
       {
       this(label,initialValue,true);
       }

   /** Constructs a labelled PropertyField as a text field with the provided initial value, either writeable or not. */
   public PropertyFieldHack(String label, String initialValue, boolean isReadWrite)
       {
       this(label,initialValue,isReadWrite, null, SHOW_TEXTFIELD);
       }
   
   /** Constructs a PropertyField with an optional label, an initial value, a "writeable" flag, an optional domain
       (for the slider and list options), and a display form (checkboxes, view buttons, text fields, sliders, or lists).
       <ul>
       <li>If show is SHOW_CHECKBOX, a checkbox will be shown (expecting "true" and "false" string values); pass in null for domain.
       <li>If show is SHOW_VIEWBUTTON, a view button will be shown (expecting a true object); pass in null for domain.
       <li>If show is SHOW_TEXTFIELD, a textfield will be shown; pass in null for domain.
       <li>If show is SHOW_SLIDER, both a textfield and a slider will be shown; the initialValue must be a number, and
       domain must be a sim.util.Interval. 
       In this case, newValue(...) will be passed a String holding a number in the Interval range and must return
       a number.  PropertyField will automatically make certain that the numbers are integral or real-valued; you
       do not need to check this so long as the Interval returns Longs or Doubles respectively.  If isReadWrite is false,
       then the slider is not shown -- only the textfield.
       <li>If show is SHOW_LIST, a list will be shown; the initialValue must be an integer specifying the number in the list, and domain must be an array of Objects (strings, whatnot) or a java.util.List providing the objects in the list.
       In this case, newValue(...) will be passed a String holding a number; that number is the index in the list
       which the user has checked.  newValue(...) must also return a String with the desired index for the list to be
       set to.  */
   public PropertyFieldHack(String label, String initialValue, boolean isReadWrite, Object domain, int show)
       {
        super(label, initialValue, isReadWrite, domain, show);
       }
	
	public JCheckBox getCheckField(){ return this.checkField; }

}

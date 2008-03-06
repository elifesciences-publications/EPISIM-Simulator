package sim.app.episim.util;


import java.util.Comparator;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;


public class SortedJList extends JList
{
	private Comparator comparator;
	
	public SortedJList(Comparator comparator)
	{
		super(new DefaultListModel());
		this.comparator = comparator;
	}
	
	public SortedJList(ListModel lm, Comparator comparator)
	{
		super(lm);
		this.comparator = comparator;
	}
	
	public SortedJList(DefaultListModel lm, Comparator comparator)
	{
		super(lm);
		this.comparator = comparator;
	}

	public void setComparator(Comparator comparator)
	{
		this.comparator = comparator;
	}

	public Comparator getComparator()
	{
		return this.comparator;
	}

	
	public void add(Object o)
	{
		int index = findIndex(o);
		((DefaultListModel)this.getModel()).insertElementAt(o, index);
	}

	
	private int findIndex(Object o)
	{
		int i = 0;
		for (; i < ((DefaultListModel)this.getModel()).getSize(); i++)
		{
			if (comparator.compare(((DefaultListModel)this.getModel()).get(i), o) >= 0)
			{
				return i;
			}
		}
		return i;
	}
	
	public Vector getData()
	{
		Vector vec = new Vector();
		
		for(int i = 0; i < ((DefaultListModel)this.getModel()).getSize(); i++)
		{
			vec.add(((DefaultListModel)this.getModel()).get(i));
		}
		
		return vec;
	}
	
	
	public void remove(Object o)
	{
		DefaultListModel newModel = new DefaultListModel();
		
		for(int i = 0; i < ((DefaultListModel)this.getModel()).getSize(); i++)
		{
			if(!((DefaultListModel)this.getModel()).get(i).equals(o))
			{
				newModel.addElement(((DefaultListModel)this.getModel()).get(i));
			}
		}
		this.setModel(newModel);
	}
	
	public void removeAll(){
		if(this.getModel() instanceof DefaultListModel) {
			((DefaultListModel) this.getModel()).removeAllElements();
		}
		else super.removeAll();
	}
}
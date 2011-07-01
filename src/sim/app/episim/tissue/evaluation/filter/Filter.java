package sim.app.episim.tissue.evaluation.filter;

import java.util.ArrayList;

import sim.app.episim.tissue.evaluation.tabledata.Cell;

public class Filter {
	
	private ArrayList<Condition> conditions = new ArrayList<Condition>();
	
	public Filter(Condition...conditions) {
		for(Condition c : conditions){
			this.conditions.add(c);
		}
	}
	
	public void add(Condition c){
		conditions.add(c);
	}
	
	public boolean match(Cell cell){
		for(Condition c : conditions){
			if(!c.match(cell)) return false;
		} return true;
	}
}

package sendreceive;

import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.SendReceiveAlgorithm;


public class StandardSendReceiveAlgorithm implements SendReceiveAlgorithm{
	
	/**
	 * @param propertycode
	 * @param requestedAmount
	 * @param toNeighbour
	 * @param neighbours
	 */
	public void sendRegular(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours){
		
		double amount = cell.returnNumberProperty(propertycode); //aktuelle Anzahl Einheiten an "var"
		
		double amountPossible = amount - cell.returnMinNumberProperty(propertycode); //Menge, die abgegeben werden kann
		
		//an Nachbarn abgeben			
		double amountForEachNeighbour = 0;
		if(amountPossible >= 0 && amountPossible < requestedAmount){//falls Menge kleiner als die zu abzugebende Menge
			amountForEachNeighbour = amountPossible / neighbours.length;
		}else{
			amountForEachNeighbour = requestedAmount / neighbours.length;
		}
		if(amountPossible >= 0){		
			for(int i = 0; i <  neighbours.length; i++){
				double actNeighbourAmount = neighbours[i].returnNumberProperty(propertycode);
				double actNeigboursRemainingCapacity = cell.returnMaxNumberProperty(propertycode) - actNeighbourAmount;
										
				if(actNeigboursRemainingCapacity <   amountForEachNeighbour){//falls Menge kleiner als die  abzugebende Menge
					neighbours[i].setNumberProperty(propertycode, neighbours[i].returnNumberProperty(propertycode)+actNeigboursRemainingCapacity);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - actNeigboursRemainingCapacity));		
				}else{
					neighbours[i].setNumberProperty(propertycode, neighbours[i].returnNumberProperty(propertycode)+amountForEachNeighbour);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - amountForEachNeighbour));
				}					
			}
		}	
	}
	
	/**
	 * @param propertycode
	 * @param requestedAmount
	 * @param toNeighbour
	 * @param neighbours
	 */
	public void sendModelCycle(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour){
		
		double amount = cell.returnNumberProperty(propertycode); //aktuelle Anzahl Einheiten an "var"
		
		double amountPossible = amount - cell.returnMinNumberProperty(propertycode); //Menge, die abgegeben werden kann
		
		//an Nachbarn abgeben		
			
		double amountForEachNeighbour = 0;
		if(amountPossible >= 0 && amountPossible < requestedAmount){//falls Menge kleiner als die zu abzugebende Menge
			amountForEachNeighbour = amountPossible;
		}else{
			amountForEachNeighbour = requestedAmount;
		}
		if(amountPossible >= 0){		
			
			double actNeighbourAmount = neighbours[actNeighbour].returnNumberProperty(propertycode);
			double actNeigboursRemainingCapacity = cell.returnMaxNumberProperty(propertycode) - actNeighbourAmount;
											
			if(actNeigboursRemainingCapacity <   amountForEachNeighbour){//falls Menge kleiner als die  abzugebende Menge
				neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)+actNeigboursRemainingCapacity);
				cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - actNeigboursRemainingCapacity));							
			}else{
				neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)+amountForEachNeighbour);
				cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - amountForEachNeighbour));
			}								
		}
	}

	/**
	 * 
	 * @param propertycode
	 * @param requestedAmount
	 * @param toNeighbour
	 * @param neighbours
	 * 
	 */	
	public void receiveRegular(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours){
		//if(!modelCycleActivated.contains(callingMethodId)&& fromNeighbour)requestedAmount = requestedAmount* neighbours.length; //jeder Nachbar bekommt gleich viel
		double amount = cell.returnNumberProperty(propertycode); //aktuelle Anzahl Einheiten an "var"
		double amountPossible = cell.returnMaxNumberProperty(propertycode) - amount; //Menge, die aufgenommen werden kann
		
		//von Nachbarn		
		double amountFromEachNeighbour = 0;
		
		if(amountPossible >= 0 && amountPossible <  requestedAmount){//falls Menge kleiner als die zu aufzunehmende Menge
			amountFromEachNeighbour = amountPossible / neighbours.length;
		}else{
			amountFromEachNeighbour = requestedAmount / neighbours.length;
		}
		if(amountPossible >= 0){
			for(int i = 0; i <  neighbours.length; i++){
				double actNeighbourAmount = neighbours[i].returnNumberProperty(propertycode);
				double actNeigboursRemainingCapacity = actNeighbourAmount - cell.returnMinNumberProperty(propertycode);				
				if(actNeigboursRemainingCapacity <  amountFromEachNeighbour){//falls Menge kleiner als die aufzunehmende Menge
					neighbours[i].setNumberProperty(propertycode, neighbours[i].returnNumberProperty(propertycode)-actNeigboursRemainingCapacity);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + actNeigboursRemainingCapacity));
				}else{
					neighbours[i].setNumberProperty(propertycode, neighbours[i].returnNumberProperty(propertycode)- amountFromEachNeighbour);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + amountFromEachNeighbour));
				}					
			}
		}								
	}
	
	
	/**
	 * 
	 * @param propertycode
	 * @param requestedAmount
	 * @param toNeighbour
	 * @param neighbours
	 * 
	 */	
	public void receiveModelCycle(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour){
		double amount = cell.returnNumberProperty(propertycode); //aktuelle Anzahl Einheiten an "var"
		double amountPossible = cell.returnMaxNumberProperty(propertycode) - amount; //Menge, die aufgenommen werden kann
		
		//von Nachbarn		
		double amountFromEachNeighbour = 0;
			
		if(amountPossible >= 0 &&amountPossible <  requestedAmount){//falls Menge kleiner als die zu aufzunehmende Menge
			amountFromEachNeighbour = amountPossible;
		}else {
			amountFromEachNeighbour = requestedAmount;
		}				
		if(amountPossible >= 0){
			double actNeighbourAmount = neighbours[actNeighbour].returnNumberProperty(propertycode);
			double actNeigboursRemainingCapacity = actNeighbourAmount - cell.returnMinNumberProperty(propertycode);					
			if(actNeigboursRemainingCapacity < amountFromEachNeighbour){//falls Menge kleiner als die  aufzunehmende Menge
				neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)-actNeigboursRemainingCapacity);
				cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + actNeigboursRemainingCapacity));
			}else{
				neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)- amountFromEachNeighbour);
				cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + amountFromEachNeighbour));
			}					
		}
	}
	
   public void sendDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {

	   // TODO Auto-generated method stub
	   
   }
	
   public void receiveDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {

	   // TODO Auto-generated method stub	   
   }
}

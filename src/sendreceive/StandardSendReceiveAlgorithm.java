package sendreceive;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import sim.app.episim.AbstractCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.tissue.TissueController;
import sim.util.DoubleBag;
import sim.util.IntBag;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.SendReceiveAlgorithm;


public class StandardSendReceiveAlgorithm implements SendReceiveAlgorithm{
	
//	public static TestFrame frame = new TestFrame();
	
	
	
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
										
				if(actNeigboursRemainingCapacity < amountForEachNeighbour){//falls Menge kleiner als die  abzugebende Menge
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

   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField diffField = ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   	   		
   		double amountPossible = cell.returnNumberProperty(propertycode) - cell.returnMinNumberProperty(propertycode); //amount that can be sent 		
   		
   		DoubleBag fieldXPos = new DoubleBag();
   		DoubleBag fieldYPos = new DoubleBag();
   	//	frame.paintShape(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron());
   		double remainingCapacity = diffField.getTotalLocalFieldRemainingCapacity(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(), fieldXPos, fieldYPos);
   	
   		if(remainingCapacity < amountPossible) amountPossible = remainingCapacity;
   		double amountToBeSent = amount < amountPossible ? amount : amountPossible;
   		double remainingAmountToBeSent = amountToBeSent;
   		final double minRemainingAmountToBeSent= (amountToBeSent*0.00001);
   		if(amountToBeSent > 0){
   		
   			while(remainingAmountToBeSent >= minRemainingAmountToBeSent &&!fieldXPos.isEmpty() && !fieldYPos.isEmpty()){
   				DoubleBag newFieldXPos = new DoubleBag();
   	   		DoubleBag newFieldYPos = new DoubleBag();
   	   		final int numberOfFieldPos = fieldXPos.size();
   	   		double amountForEachFieldPos = remainingAmountToBeSent / ((double)numberOfFieldPos);
   	   		for(int i = 0; i < numberOfFieldPos; i++){
   	   			double realAmountSent = diffField.addConcentration(fieldXPos.get(i), fieldYPos.get(i), amountForEachFieldPos);
   	   			if((amountForEachFieldPos-realAmountSent)<= 0){
   	   				newFieldXPos.add(fieldXPos.get(i));
   	   				newFieldYPos.add(fieldYPos.get(i));
   	   			}
   	   			remainingAmountToBeSent-=realAmountSent;
   	   		}
   	   		fieldXPos = newFieldXPos;
   	   		fieldYPos = newFieldYPos;
   	   		
   			}
   			double amountSent = amountToBeSent-remainingAmountToBeSent;
   			cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - amountSent));
   		}   	
   	}	   
   }
	
   public void receiveDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {
   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField diffField = ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   	   		
   		double amountPossible =  cell.returnMaxNumberProperty(propertycode)-cell.returnNumberProperty(propertycode); //amount that can be received 		
   		
   		DoubleBag fieldXPos = new DoubleBag();
   		DoubleBag fieldYPos = new DoubleBag();
   		double freeFieldConcentration = diffField.getTotalLocalFreeFieldConcentration(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(), fieldXPos, fieldYPos);
   	
   		if(freeFieldConcentration < amountPossible) amountPossible = freeFieldConcentration;
   		double amountToBeReceived = amount < amountPossible ? amount : amountPossible;
   		double remainingAmountToBeReceived = amountToBeReceived;
   		final double minRemainingAmountToBeReceived= (amountToBeReceived*0.00001);
   		if(amountToBeReceived > 0){   			
   			while(remainingAmountToBeReceived >= minRemainingAmountToBeReceived &&!fieldXPos.isEmpty() && !fieldYPos.isEmpty()){
   				DoubleBag newFieldXPos = new DoubleBag();
   	   		DoubleBag newFieldYPos = new DoubleBag();
   	   		final int numberOfFieldPos = fieldXPos.size();
   	   		double amountFromEachFieldPos = remainingAmountToBeReceived / ((double)numberOfFieldPos);
   	   		for(int i = 0; i < numberOfFieldPos; i++){
   	   			double realAmountReceived = diffField.removeConcentration(fieldXPos.get(i), fieldYPos.get(i), amountFromEachFieldPos);
   	   			if((amountFromEachFieldPos-realAmountReceived)<= 0){
   	   				newFieldXPos.add(fieldXPos.get(i));
   	   				newFieldYPos.add(fieldYPos.get(i));
   	   			}
   	   			remainingAmountToBeReceived-=realAmountReceived;
   	   		}
   	   		fieldXPos = newFieldXPos;
   	   		fieldYPos = newFieldYPos;
   			}
   			double amountReceived = amountToBeReceived-remainingAmountToBeReceived;
   			cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + amountReceived));
   		}   	
   	}	     
   } 
}

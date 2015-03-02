package sendreceive;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import sim.app.episim.SimStateServer;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.util.GenericBag;
import sim.util.DoubleBag;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.SendReceiveAlgorithm;
import episiminterfaces.SendReceiveAlgorithmExt;


public class StandardSendReceiveAlgorithm implements SendReceiveAlgorithmExt{
	
//	public static TestFrame frame = new TestFrame();
	
	private interface SendReceiveDiffusionFieldConnector{
		void sendToDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell);
		void receiveFromDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell);
		double senseFromDF(String ecDiffusionFieldName, EpisimCellBehavioralModel cell);
	}
	
	private SendReceiveDiffusionFieldConnector sendReceiveDFConnector = null;
	
	public StandardSendReceiveAlgorithm(){
		if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
			sendReceiveDFConnector = new SendReceiveDiffusionFieldConnector(){
				public void sendToDF(String ecDiffusionFieldName, int propertycode, double amount,
						EpisimCellBehavioralModel cell) {					
					sendTo2DDiffField(ecDiffusionFieldName, propertycode, amount, cell);
				}
				public void receiveFromDF(String ecDiffusionFieldName, int propertycode, double amount,
						EpisimCellBehavioralModel cell) {
					receiveFrom2DDiffField(ecDiffusionFieldName, propertycode, amount, cell);					
				}
				public double senseFromDF(String ecDiffusionFieldName, EpisimCellBehavioralModel cell){ 
					return senseFrom2DDiffField(ecDiffusionFieldName, cell);
				}};
		}
		else if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
			sendReceiveDFConnector = new SendReceiveDiffusionFieldConnector(){			
				public void sendToDF(String ecDiffusionFieldName, int propertycode, double amount,
						EpisimCellBehavioralModel cell) {
					sendTo3DDiffField(ecDiffusionFieldName, propertycode, amount, cell);
				}				
				public void receiveFromDF(String ecDiffusionFieldName, int propertycode, double amount,
						EpisimCellBehavioralModel cell) {
					receiveFrom3DDiffField(ecDiffusionFieldName, propertycode, amount, cell);
				}
				public double senseFromDF(String ecDiffusionFieldName, EpisimCellBehavioralModel cell){ 
					return senseFrom3DDiffField(ecDiffusionFieldName, cell);
				}};
		}
	}
	
	
	/**
	 * @param propertycode
	 * @param requestedAmount
	 * @param toNeighbour
	 * @param neighbours
	 */
	public void sendRegular(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours){
		if(requestedAmount < 0){
			receiveRegular(propertycode, (-1*requestedAmount), cell, neighbours);
		}
		else if(neighbours.length>0){
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
				GenericBag<EpisimCellBehavioralModel> neighbourBag = new GenericBag<EpisimCellBehavioralModel>();
				neighbourBag.addAll(0, neighbours);
				neighbourBag.shuffle(SimStateServer.getInstance().getEpisimGUIState().state.random);
				for(int i = 0; i <  neighbourBag.size(); i++){
					double actNeighbourAmount = neighbourBag.get(i).returnNumberProperty(propertycode);
					double actNeigboursRemainingCapacity = neighbourBag.get(i).returnMaxNumberProperty(propertycode) - actNeighbourAmount;
											
					if(actNeigboursRemainingCapacity < amountForEachNeighbour){//falls Menge kleiner als die  abzugebende Menge
						neighbourBag.get(i).setNumberProperty(propertycode, neighbourBag.get(i).returnNumberProperty(propertycode)+actNeigboursRemainingCapacity);
						cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - actNeigboursRemainingCapacity));		
					}else{
						neighbourBag.get(i).setNumberProperty(propertycode, neighbourBag.get(i).returnNumberProperty(propertycode)+amountForEachNeighbour);
						cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - amountForEachNeighbour));
					}					
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
		if(requestedAmount < 0){
			receiveModelCycle(propertycode, (-1*requestedAmount), cell, neighbours, actNeighbour);
		}
		else{
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
				double actNeigboursRemainingCapacity = neighbours[actNeighbour].returnMaxNumberProperty(propertycode) - actNeighbourAmount;
												
				if(actNeigboursRemainingCapacity <   amountForEachNeighbour){//falls Menge kleiner als die  abzugebende Menge
					neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)+actNeigboursRemainingCapacity);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - actNeigboursRemainingCapacity));							
				}else{
					neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)+amountForEachNeighbour);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - amountForEachNeighbour));
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
	public void receiveRegular(int propertycode, double requestedAmount, EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours){
		if(requestedAmount < 0){
			sendRegular(propertycode, (-1*requestedAmount), cell, neighbours);
		}
		else if(neighbours.length>0){
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
				GenericBag<EpisimCellBehavioralModel> neighbourBag = new GenericBag<EpisimCellBehavioralModel>();
				neighbourBag.addAll(0, neighbours);
				neighbourBag.shuffle(SimStateServer.getInstance().getEpisimGUIState().state.random);
				for(int i = 0; i <  neighbourBag.size(); i++){
					double actNeighbourAmount = neighbourBag.get(i).returnNumberProperty(propertycode);
					double actNeigboursRemainingCapacity = actNeighbourAmount - neighbourBag.get(i).returnMinNumberProperty(propertycode);				
					if(actNeigboursRemainingCapacity <  amountFromEachNeighbour){//falls Menge kleiner als die aufzunehmende Menge
						neighbourBag.get(i).setNumberProperty(propertycode, neighbourBag.get(i).returnNumberProperty(propertycode)-actNeigboursRemainingCapacity);
						cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + actNeigboursRemainingCapacity));
					}else{
						neighbourBag.get(i).setNumberProperty(propertycode, neighbourBag.get(i).returnNumberProperty(propertycode)- amountFromEachNeighbour);
						cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + amountFromEachNeighbour));
					}					
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
		if(requestedAmount < 0){
			sendModelCycle(propertycode, (-1*requestedAmount), cell, neighbours, actNeighbour);
		}
		else{
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
				double actNeigboursRemainingCapacity = actNeighbourAmount - neighbours[actNeighbour].returnMinNumberProperty(propertycode);					
				if(actNeigboursRemainingCapacity < amountFromEachNeighbour){//falls Menge kleiner als die  aufzunehmende Menge
					neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)-actNeigboursRemainingCapacity);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + actNeigboursRemainingCapacity));
				}else{
					neighbours[actNeighbour].setNumberProperty(propertycode, neighbours[actNeighbour].returnNumberProperty(propertycode)- amountFromEachNeighbour);
					cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + amountFromEachNeighbour));
				}					
			}
		}
	}
	
	public void sendDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {
		if(amount <0)this.sendReceiveDFConnector.receiveFromDF(ecDiffusionFieldName, propertycode, (-1*amount), cell);
		else this.sendReceiveDFConnector.sendToDF(ecDiffusionFieldName, propertycode, amount, cell);
   }
	
   public void receiveDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {
   	if(amount <0)this.sendReceiveDFConnector.sendToDF(ecDiffusionFieldName, propertycode, (-1*amount), cell);
   	else this.sendReceiveDFConnector.receiveFromDF(ecDiffusionFieldName, propertycode, amount, cell);
   }
   
   private void sendTo2DDiffField(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {

   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField2D diffField = (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   	   		
   		double amountPossible = cell.returnNumberProperty(propertycode) - cell.returnMinNumberProperty(propertycode); //amount that can be sent 		
   		
   		DoubleBag fieldXPos = new DoubleBag();
   		DoubleBag fieldYPos = new DoubleBag();
   	//	frame.paintShape(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron());
   		double remainingCapacity = diffField.getTotalLocalFieldRemainingCapacity(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0), fieldXPos, fieldYPos);
   	
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
	   
   private void receiveFrom2DDiffField(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {
   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField2D diffField = (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   	   		
   		double amountPossible =  cell.returnMaxNumberProperty(propertycode)-cell.returnNumberProperty(propertycode); //amount that can be received 		
   		
   		DoubleBag fieldXPos = new DoubleBag();
   		DoubleBag fieldYPos = new DoubleBag();
   		double freeFieldConcentration = diffField.getTotalLocalFreeFieldConcentration(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0), fieldXPos, fieldYPos);
   	
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
   
   private double senseFrom2DDiffField(String ecDiffusionFieldName, EpisimCellBehavioralModel cell) {
   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField2D diffField = (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   		return diffField.getTotalConcentrationInArea(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0));  	 	
   	}
   	return 0;
   } 
   
   
   
   
   private void sendTo3DDiffField(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {

   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField3D diffField = (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   	   		
   		double amountPossible = cell.returnNumberProperty(propertycode) - cell.returnMinNumberProperty(propertycode); //amount that can be sent 		
   		
   		DoubleBag fieldXPos = new DoubleBag();
   		DoubleBag fieldYPos = new DoubleBag();
   		DoubleBag fieldZPos = new DoubleBag();
   	
   		double remainingCapacity = diffField.getTotalLocalFieldRemainingCapacity(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(diffField.getFieldConfiguration().getLatticeSiteSizeInMikron()), fieldXPos, fieldYPos, fieldZPos);
   	
   		if(remainingCapacity < amountPossible) amountPossible = remainingCapacity;
   		double amountToBeSent = amount < amountPossible ? amount : amountPossible;
   		double remainingAmountToBeSent = amountToBeSent;
   		final double minRemainingAmountToBeSent= (amountToBeSent*0.00001);
   		if(amountToBeSent > 0){
   		
   			while(remainingAmountToBeSent >= minRemainingAmountToBeSent &&!fieldXPos.isEmpty() && !fieldYPos.isEmpty() && !fieldZPos.isEmpty()){
   				DoubleBag newFieldXPos = new DoubleBag();
   	   		DoubleBag newFieldYPos = new DoubleBag();
   	   		DoubleBag newFieldZPos = new DoubleBag();
   	   		final int numberOfFieldPos = fieldXPos.size();
   	   		double amountForEachFieldPos = remainingAmountToBeSent / ((double)numberOfFieldPos);
   	   		for(int i = 0; i < numberOfFieldPos; i++){
   	   			double realAmountSent = diffField.addConcentration(fieldXPos.get(i), fieldYPos.get(i), fieldZPos.get(i), amountForEachFieldPos);
   	   			if((amountForEachFieldPos-realAmountSent)<= 0){
   	   				newFieldXPos.add(fieldXPos.get(i));
   	   				newFieldYPos.add(fieldYPos.get(i));
   	   				newFieldZPos.add(fieldZPos.get(i));
   	   			}
   	   			remainingAmountToBeSent-=realAmountSent;
   	   		}
   	   		fieldXPos = newFieldXPos;
   	   		fieldYPos = newFieldYPos;
   	   		fieldZPos = newFieldZPos;   	   		
   			}
   			double amountSent = amountToBeSent-remainingAmountToBeSent;
   			cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) - amountSent));
   		}   	
   	}	   
   }
   private void receiveFrom3DDiffField(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {
   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField3D diffField = (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   	   		
   		double amountPossible =  cell.returnMaxNumberProperty(propertycode)-cell.returnNumberProperty(propertycode); //amount that can be received 		
   		
   		DoubleBag fieldXPos = new DoubleBag();
   		DoubleBag fieldYPos = new DoubleBag();
   		DoubleBag fieldZPos = new DoubleBag();
   		double freeFieldConcentration = diffField.getTotalLocalFreeFieldConcentration(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(diffField.getFieldConfiguration().getLatticeSiteSizeInMikron()), fieldXPos, fieldYPos, fieldZPos);
   	
   		if(freeFieldConcentration < amountPossible) amountPossible = freeFieldConcentration;
   		double amountToBeReceived = amount < amountPossible ? amount : amountPossible;
   		double remainingAmountToBeReceived = amountToBeReceived;
   		final double minRemainingAmountToBeReceived= (amountToBeReceived*0.00001);
   		if(amountToBeReceived > 0){   			
   			while(remainingAmountToBeReceived >= minRemainingAmountToBeReceived &&!fieldXPos.isEmpty() && !fieldYPos.isEmpty() && !fieldZPos.isEmpty()){
   				DoubleBag newFieldXPos = new DoubleBag();
   	   		DoubleBag newFieldYPos = new DoubleBag();
   	   		DoubleBag newFieldZPos = new DoubleBag();
   	   		final int numberOfFieldPos = fieldXPos.size();
   	   		double amountFromEachFieldPos = remainingAmountToBeReceived / ((double)numberOfFieldPos);
   	   		for(int i = 0; i < numberOfFieldPos; i++){
   	   			double realAmountReceived = diffField.removeConcentration(fieldXPos.get(i), fieldYPos.get(i), fieldZPos.get(i), amountFromEachFieldPos);
   	   			if((amountFromEachFieldPos-realAmountReceived)<= 0){
   	   				newFieldXPos.add(fieldXPos.get(i));
   	   				newFieldYPos.add(fieldYPos.get(i));
   	   				newFieldZPos.add(fieldZPos.get(i));
   	   			}
   	   			remainingAmountToBeReceived-=realAmountReceived;
   	   		}
   	   		fieldXPos = newFieldXPos;
   	   		fieldYPos = newFieldYPos;
   	   		fieldZPos = newFieldZPos;
   			}
   			double amountReceived = amountToBeReceived-remainingAmountToBeReceived;
   			cell.setNumberProperty(propertycode, (cell.returnNumberProperty(propertycode) + amountReceived));
   		}   	
   	}	     
   }
   private double senseFrom3DDiffField(String ecDiffusionFieldName, EpisimCellBehavioralModel cell) {
   	AbstractCell cellObj = TissueController.getInstance().getActEpidermalTissue().getCell(cell.getId());
   	ExtraCellularDiffusionField3D diffField = (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(ecDiffusionFieldName);
   	
   	if(cellObj != null && diffField != null){
   		return diffField.getTotalConcentrationInArea(cellObj.getEpisimBioMechanicalModelObject().getCellBoundariesInMikron(0));  	 	
   	}
   	return 0;
   } 

	
   public double sense(String ecDiffusionFieldName, EpisimCellBehavioralModel cell) {
	   return this.sendReceiveDFConnector.senseFromDF(ecDiffusionFieldName, cell);
   } 
}

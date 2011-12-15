package sendreceive;

import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.SendReceiveAlgorithm;


public class DummySendReceiveAlgorithm implements SendReceiveAlgorithm {

	public void sendRegular(int propertycode, double requestedAmount, 
	      EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours) {

		System.out.println("SendRegular was called");

	}

	public void sendModelCycle(int propertycode, double requestedAmount,
	      EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour) {

		System.out.println("SendModelCycle was called");

	}

	public void receiveRegular(int propertycode, double requestedAmount,
	      EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours) {

		System.out.println("ReceiveRegular was called");

	}

	public void receiveModelCycle(int propertycode, double requestedAmount, 
	      EpisimCellBehavioralModel cell, EpisimCellBehavioralModel[] neighbours, int actNeighbour) {

		System.out.println("ReceiveModelCycle was called");

	}

	
   public void sendDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {

   	System.out.println("SendDF was called");
	   
   }

	
   public void receiveDF(String ecDiffusionFieldName, int propertycode, double amount, EpisimCellBehavioralModel cell) {

   	System.out.println("ReceiveDF was called");
	   
   }

}

package sim.app.episim.model.diffusion;

import sim.field.grid.DoubleGrid2D;


public class ForwardEulerDiffusionReaction {
	
	private final double DIFFUSION_COEFFICIENT_THRESHHOLD = 0.16;
	
	private ExtraCellularDiffusionField extraCellularField;
	
	
	
	public ForwardEulerDiffusionReaction(ExtraCellularDiffusionField extraCellularField){
		this.extraCellularField = extraCellularField;
	}
	
	public void updateExtraCellularField(){
		//assume Dirichlet Boundary Conditions if not toroidal
		DoubleGrid2D currentValueField = extraCellularField.getExtraCellularField();
		DoubleGrid2D newValueField = new DoubleGrid2D(currentValueField.getWidth(), currentValueField.getHeight());
		
		int startX = extraCellularField.isToroidal() ? 0 : 1;
		int startY = startX;
		int stopX = extraCellularField.isToroidal() ? currentValueField.getWidth() : (currentValueField.getWidth()-1);
		int stopY = extraCellularField.isToroidal() ? currentValueField.getHeight() : (currentValueField.getHeight()-1);
		final double latticeSize = extraCellularField.getFieldConfiguration().getLatticeSiteSizeInMikron()/ Math.pow(10, 6);
		double dt = extraCellularField.getFieldConfiguration().getDeltaTimeInSecondsPerIteration();
		double dt_dx2 = (dt / (latticeSize*latticeSize));
		double diffConst = extraCellularField.getFieldConfiguration().getDiffusionCoefficient();
		double decayConst = extraCellularField.getFieldConfiguration().getDegradationRate();
		
		double maxConcentration = extraCellularField.getFieldConfiguration().getMaximumConcentration();
		double minConcentration = extraCellularField.getFieldConfiguration().getMinimumConcentration();
		
		double numberOfIterations = 1;
		//manipulate delta_time if diffusion coefficient is too large
		if(dt_dx2*diffConst > DIFFUSION_COEFFICIENT_THRESHHOLD){
			numberOfIterations = Math.floor((dt_dx2*diffConst)/DIFFUSION_COEFFICIENT_THRESHHOLD);
			dt /= numberOfIterations;
			dt_dx2 /= numberOfIterations;
		}	
		double currentConcentration = 0;
		double newConcentration = 0;
		long start = System.currentTimeMillis();
		for(int i = 0; i < numberOfIterations; i++){
			if(!extraCellularField.isToroidal())setDirichletBoundaryConditionsInNewField(currentValueField, newValueField);
			for(int yPos = startY; yPos < stopY; yPos++){
				for(int xPos = startX; xPos < stopX; xPos++){
					currentConcentration = currentValueField.get(xPos, yPos);
					
					newConcentration = dt_dx2*diffConst*laplacian(xPos,yPos, currentValueField)+currentConcentration;
					newConcentration -= dt*(decayConst*currentConcentration);
					if(newConcentration > maxConcentration) newConcentration = maxConcentration;
					if(newConcentration < minConcentration) newConcentration = minConcentration;
					newValueField.set(xPos, yPos, newConcentration);
				}
			}
			currentValueField.setTo(newValueField);
			newValueField.setTo(0);
		}
		long end = System.currentTimeMillis();
		//System.out.println("Calculation Time in miliseconds for Diffusion Field: " +((end-start)));
	}
	
	private void setDirichletBoundaryConditionsInNewField(DoubleGrid2D currentValueField, DoubleGrid2D newValueField){
		for(int y = 0; y < currentValueField.getHeight(); y++){
			for(int x = 0; x < currentValueField.getWidth(); x++){
				if(x==0 || x ==(currentValueField.getWidth()-1) || y==0 || y==(currentValueField.getHeight()-1))
					newValueField.set(x, y, currentValueField.get(x, y));
			}						
		}
	}
	
	private double laplacian(int x, int y, DoubleGrid2D doubleField){
		double neighbourConcentrationSum =  doubleField.field[doubleField.stx(x+1)][doubleField.sty(y)]
		                                  + doubleField.field[doubleField.stx(x-1)][doubleField.sty(y)]
		                                  + doubleField.field[doubleField.stx(x)][doubleField.sty(y+1)]
		                                  + doubleField.field[doubleField.stx(x)][doubleField.sty(y-1)];
		neighbourConcentrationSum -= 4*doubleField.field[doubleField.stx(x)][doubleField.sty(y)];
		return neighbourConcentrationSum;
	}
	
	
	

}

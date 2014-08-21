package sim.app.episim.model.diffusion;

import sim.app.episim.EpisimProperties;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D.BoundaryCondition;
import sim.app.episim.util.Loop;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;


public class ForwardEulerDiffusionReaction {
	
	private final double DIFFUSION_COEFFICIENT_THRESHHOLD = 0.16;
	
	private ExtraCellularDiffusionField2D extraCellularField2D;
	private ExtraCellularDiffusionField3D extraCellularField3D;
	
	
	private boolean parallelizationOn = false;
	
	public ForwardEulerDiffusionReaction(ExtraCellularDiffusionField2D extraCellularField){
		this.extraCellularField2D = extraCellularField;
		parallelizationOn = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION) == null || EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION).equalsIgnoreCase(EpisimProperties.ON);
	}
	
	public ForwardEulerDiffusionReaction(ExtraCellularDiffusionField3D extraCellularField){
		this.extraCellularField3D = extraCellularField;
		parallelizationOn = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION) == null || EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION).equalsIgnoreCase(EpisimProperties.ON);
	}
	
	
	public void updateExtraCellularField(){
		if(this.extraCellularField2D != null) update2DField();
		if(this.extraCellularField3D != null) update3DField();
	}
	
	
	private void update2DField(){
		//assume Dirichlet Boundary Conditions if not toroidal
		DoubleGrid2D currentValueField = extraCellularField2D.getExtraCellularField();
		DoubleGrid2D newValueField = new DoubleGrid2D(currentValueField.getWidth(), currentValueField.getHeight());
		
		int startX = extraCellularField2D.isToroidalX() ? 0 : 1;
		int startY = extraCellularField2D.isToroidalY() ? 0 : 1;
		int stopX = extraCellularField2D.isToroidalX() ? currentValueField.getWidth() : (currentValueField.getWidth()-1);
		int stopY = extraCellularField2D.isToroidalY() ? currentValueField.getHeight() : (currentValueField.getHeight()-1);
		final double latticeSize = extraCellularField2D.getFieldConfiguration().getLatticeSiteSizeInMikron()/ Math.pow(10, 6);
		double dt = extraCellularField2D.getFieldConfiguration().getDeltaTimeInSecondsPerIteration();
		double dt_dx2 = (dt / (latticeSize*latticeSize));
		double diffConst = extraCellularField2D.getFieldConfiguration().getDiffusionCoefficient();
		double decayConst = extraCellularField2D.getFieldConfiguration().getDegradationRate();
		
		double maxConcentration = extraCellularField2D.getFieldConfiguration().getMaximumConcentration();
		double minConcentration = extraCellularField2D.getFieldConfiguration().getMinimumConcentration();
		
		double numberOfIterations = 1;
		//manipulate delta_time if diffusion coefficient is too large
		if(dt_dx2*diffConst > DIFFUSION_COEFFICIENT_THRESHHOLD){
			numberOfIterations = Math.floor((dt_dx2*diffConst)/DIFFUSION_COEFFICIENT_THRESHHOLD);
			dt_dx2 /= numberOfIterations;
		}	
		double currentConcentration = 0;
		double newConcentration = 0;
		
		for(int i = 0; i < numberOfIterations; i++){			
			if(!extraCellularField2D.isToroidalX())setDirichletOrNeumannXAxisBC(currentValueField, newValueField);
			if(!extraCellularField2D.isToroidalY())setDirichletOrNeumannYAxisBC(currentValueField, newValueField);
			for(int yPos = startY; yPos < stopY; yPos++){
				for(int xPos = startX; xPos < stopX; xPos++){
					currentConcentration = currentValueField.get(xPos, yPos);
					newConcentration = 0;
					newConcentration = dt_dx2*diffConst*laplacian(xPos,yPos, currentValueField)+currentConcentration;
					if(i==(numberOfIterations-1))newConcentration -= dt*(decayConst*newConcentration);
					if(newConcentration > maxConcentration) newConcentration = maxConcentration;
					if(newConcentration < minConcentration) newConcentration = minConcentration;
					newValueField.set(xPos, yPos, newConcentration);
				}
			}
			currentValueField.setTo(newValueField);
			newValueField.setTo(0);			
		}	
		
		
	}
	
	private void update3DField(){
		//assume Dirichlet Boundary Conditions if not toroidal
		final DoubleGrid3D currentValueField = extraCellularField3D.getExtraCellularField();
		final DoubleGrid3D newValueField = new DoubleGrid3D(currentValueField.getWidth(), currentValueField.getHeight(), currentValueField.getLength());
		
		final int startX = extraCellularField3D.isToroidalX() ? 0 : 1;
		final int startY = extraCellularField3D.isToroidalY() ? 0 : 1;
		final int startZ = extraCellularField3D.isToroidalZ() ? 0 : 1;
		
		final int stopX = extraCellularField3D.isToroidalX() ? currentValueField.getWidth() : (currentValueField.getWidth()-1);
		final int stopY = extraCellularField3D.isToroidalY() ? currentValueField.getHeight() : (currentValueField.getHeight()-1);
		final int stopZ = extraCellularField3D.isToroidalZ() ? currentValueField.getLength() : (currentValueField.getLength()-1);
		
		
		final double latticeSize = extraCellularField3D.getFieldConfiguration().getLatticeSiteSizeInMikron()/ Math.pow(10, 6);
		final double dt = extraCellularField3D.getFieldConfiguration().getDeltaTimeInSecondsPerIteration();
		double dt_dx2 = (dt / (latticeSize*latticeSize));
		final double diffConst = extraCellularField3D.getFieldConfiguration().getDiffusionCoefficient();
		final double decayConst = extraCellularField3D.getFieldConfiguration().getDegradationRate();
		
		final double maxConcentration = extraCellularField3D.getFieldConfiguration().getMaximumConcentration();
		final double minConcentration = extraCellularField3D.getFieldConfiguration().getMinimumConcentration();
		
		double numberOfIterations = 1;
		//manipulate delta_time if diffusion coefficient is too large
		if(dt_dx2*diffConst > DIFFUSION_COEFFICIENT_THRESHHOLD){
			numberOfIterations = Math.floor((dt_dx2*diffConst)/DIFFUSION_COEFFICIENT_THRESHHOLD);
			dt_dx2 /= numberOfIterations;
		}
		final double DT_DX2 = dt_dx2;
		double currentConcentration = 0;
		double newConcentration = 0;
		
		for(int i = 0; i < numberOfIterations; i++){
			long start = System.currentTimeMillis();
			if(!extraCellularField3D.isToroidalX())setDirichletOrNeumannXAxisBC(currentValueField, newValueField);
			if(!extraCellularField3D.isToroidalY())setDirichletOrNeumannYAxisBC(currentValueField, newValueField);
			if(!extraCellularField3D.isToroidalZ())setDirichletOrNeumannZAxisBC(currentValueField, newValueField);
			
			final int iterationNo = i;
			final double NUMBER_OF_ITERATIONS=numberOfIterations;
			if(parallelizationOn){
				Loop.withIndex(startZ, stopZ, new Loop.Each() {
	             public void run(int zPos) {
	            	double currentConcentration = 0;
	            	double newConcentration = 0;
	            	 for(int yPos = startY; yPos < stopY; yPos++){
	 						for(int xPos = startX; xPos < stopX; xPos++){
	 							currentConcentration = currentValueField.get(xPos, yPos, zPos);
	 							newConcentration = 0;
	 							newConcentration = DT_DX2*diffConst*laplacian(xPos,yPos, zPos, currentValueField)+currentConcentration;
	 							if(iterationNo==(NUMBER_OF_ITERATIONS-1))newConcentration -= dt*(decayConst*newConcentration);
	 							if(newConcentration > maxConcentration) newConcentration = maxConcentration;
	 							if(newConcentration < minConcentration) newConcentration = minConcentration;
	 							newValueField.set(xPos, yPos, zPos, newConcentration);
	 						}
	 					}
	             }
	         });
	            
			}
			else{
				for(int zPos = startZ; zPos < stopZ; zPos++){
					for(int yPos = startY; yPos < stopY; yPos++){
						for(int xPos = startX; xPos < stopX; xPos++){
							currentConcentration = currentValueField.get(xPos, yPos, zPos);
							newConcentration = 0;
							newConcentration = dt_dx2*diffConst*laplacian(xPos,yPos, zPos, currentValueField)+currentConcentration;
							if(i==(numberOfIterations-1))newConcentration -= dt*(decayConst*newConcentration);
							if(newConcentration > maxConcentration) newConcentration = maxConcentration;
							if(newConcentration < minConcentration) newConcentration = minConcentration;
							newValueField.set(xPos, yPos, zPos, newConcentration);
						}
					}
				}
			}
			long end = System.currentTimeMillis();
			//System.out.println("Calculation Time in miliseconds for Diffusion Field: " +((end-start)));
			currentValueField.setTo(newValueField);
			newValueField.setTo(0);
			
	
		}
		
	}
	
	private void setDirichletOrNeumannXAxisBC(DoubleGrid2D currentValueField, DoubleGrid2D newValueField){
		BoundaryCondition cond = this.extraCellularField2D.getFieldBCConfig().getBoundaryConditionX();
		if(cond==BoundaryCondition.DIRICHLET){
			int y = 0;
			double constValue = this.extraCellularField2D.getFieldBCConfig().getConstantValueX();
			for(int x = 0; x < currentValueField.getWidth(); x++) newValueField.set(x, y, constValue);
			y=(currentValueField.getHeight()-1);
			for(int x = 0; x < currentValueField.getWidth(); x++) newValueField.set(x, y, constValue);
		}
		else if(cond==BoundaryCondition.NEUMANN){
			int y = 0;
			double constFlow = this.extraCellularField2D.getFieldBCConfig().getConstantFlowX();
			double minValue = this.extraCellularField2D.getFieldConfiguration().getMinimumConcentration();
			double maxValue = this.extraCellularField2D.getFieldConfiguration().getMaximumConcentration();
			for(int x = 0; x < currentValueField.getWidth(); x++){
				double newValue = currentValueField.get(x, y+1)-constFlow;
				newValueField.set(x, y, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);
			}
			y=(currentValueField.getHeight()-1);
			for(int x = 0; x < currentValueField.getWidth(); x++){
				double newValue = currentValueField.get(x, y-1)-constFlow;
				newValueField.set(x, y, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);
			}
		}		
	}
	
	private void setDirichletOrNeumannYAxisBC(DoubleGrid2D currentValueField, DoubleGrid2D newValueField){
		BoundaryCondition cond = this.extraCellularField2D.getFieldBCConfig().getBoundaryConditionY();
		if(cond==BoundaryCondition.DIRICHLET){
			int x = 0;
			double constValue = this.extraCellularField2D.getFieldBCConfig().getConstantValueY();
			for(int y = 0; y < currentValueField.getHeight(); y++) newValueField.set(x, y, constValue);
			x=(currentValueField.getWidth()-1);
			for(int y = 0; y < currentValueField.getHeight(); y++) newValueField.set(x, y, constValue);
		}
		else if(cond==BoundaryCondition.NEUMANN){
			int x = 0;
			double constFlow = this.extraCellularField2D.getFieldBCConfig().getConstantFlowY();
			double minValue = this.extraCellularField2D.getFieldConfiguration().getMinimumConcentration();
			double maxValue = this.extraCellularField2D.getFieldConfiguration().getMaximumConcentration();
			for(int y = 0; y < currentValueField.getHeight(); y++){
				double newValue = currentValueField.get(x+1, y)-constFlow;
				newValueField.set(x, y, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);
			}
			x=(currentValueField.getWidth()-1);
			for(int y = 0; y < currentValueField.getHeight(); y++){
				double newValue = currentValueField.get(x-1, y)-constFlow;
				newValueField.set(x, y, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);
			}
		}
	}
	
	private void setDirichletOrNeumannXAxisBC(DoubleGrid3D currentValueField, DoubleGrid3D newValueField){
		BoundaryCondition cond = this.extraCellularField3D.getFieldBCConfig3D().getBoundaryConditionX();
		if(cond==BoundaryCondition.DIRICHLET){
			int y = 0;
			double constValue = this.extraCellularField3D.getFieldBCConfig3D().getConstantValueX();
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int x = 0; x < currentValueField.getWidth(); x++){						
					newValueField.set(x, y, z, constValue);											
				}
			}
			y =(currentValueField.getHeight()-1);
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int x = 0; x < currentValueField.getWidth(); x++){		
							newValueField.set(x, y, z, constValue);											
				}
			}
		}
		else if(cond==BoundaryCondition.NEUMANN){
			int y = 0;
			double constFlow = this.extraCellularField3D.getFieldBCConfig3D().getConstantFlowX();
			double minValue = this.extraCellularField3D.getFieldConfiguration().getMinimumConcentration();
			double maxValue = this.extraCellularField3D.getFieldConfiguration().getMaximumConcentration();
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int x = 0; x < currentValueField.getWidth(); x++){
					double newValue = currentValueField.get(x, y+1, z)-constFlow;
					newValueField.set(x, y, z, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);											
				}
			}
			y =(currentValueField.getHeight()-1);
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int x = 0; x < currentValueField.getWidth(); x++){		
					double newValue = currentValueField.get(x, y-1, z)-constFlow;
					newValueField.set(x, y, z, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);											
				}
			}			
		}		
	}
	
	private void setDirichletOrNeumannYAxisBC(DoubleGrid3D currentValueField, DoubleGrid3D newValueField){
		BoundaryCondition cond = this.extraCellularField3D.getFieldBCConfig3D().getBoundaryConditionY();
		if(cond==BoundaryCondition.DIRICHLET){
			int x = 0;
			double constValue = this.extraCellularField3D.getFieldBCConfig3D().getConstantValueY();
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int y = 0; y < currentValueField.getHeight(); y++){						
					newValueField.set(x, y, z, constValue);											
				}
			}
			x =(currentValueField.getHeight()-1);
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int y = 0; y < currentValueField.getHeight(); y++){		
							newValueField.set(x, y, z, constValue);											
				}
			}
		}
		else if(cond==BoundaryCondition.NEUMANN){
			int x = 0;
			double constFlow = this.extraCellularField3D.getFieldBCConfig3D().getConstantFlowY();
			double minValue = this.extraCellularField3D.getFieldConfiguration().getMinimumConcentration();
			double maxValue = this.extraCellularField3D.getFieldConfiguration().getMaximumConcentration();
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int y = 0; y < currentValueField.getHeight(); y++){
					double newValue = currentValueField.get(x+1, y, z)-constFlow;
					newValueField.set(x, y, z, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);											
				}
			}
			x =(currentValueField.getWidth()-1);
			for(int z = 0; z < currentValueField.getLength(); z++){
				for(int y = 0; y < currentValueField.getHeight(); y++){		
					double newValue = currentValueField.get(x-1, y, z)-constFlow;
					newValueField.set(x, y, z, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);											
				}
			}			
		}		
	}
	private void setDirichletOrNeumannZAxisBC(DoubleGrid3D currentValueField, DoubleGrid3D newValueField){
		BoundaryCondition cond = this.extraCellularField3D.getFieldBCConfig3D().getBoundaryConditionZ();
		if(cond==BoundaryCondition.DIRICHLET){
			int z = 0;
			double constValue = this.extraCellularField3D.getFieldBCConfig3D().getConstantValueZ();
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){						
					newValueField.set(x, y, z, constValue);											
				}
			}
			z =(currentValueField.getLength()-1);
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){		
							newValueField.set(x, y, z, constValue);											
				}
			}
		}
		else if(cond==BoundaryCondition.NEUMANN){
			int z = 0;
			double constFlow = this.extraCellularField3D.getFieldBCConfig3D().getConstantFlowZ();
			double minValue = this.extraCellularField3D.getFieldConfiguration().getMinimumConcentration();
			double maxValue = this.extraCellularField3D.getFieldConfiguration().getMaximumConcentration();
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){
					double newValue = currentValueField.get(x, y, z+1)-constFlow;
					newValueField.set(x, y, z, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);											
				}
			}
			z =(currentValueField.getLength()-1);
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){		
					double newValue = currentValueField.get(x, y, z-1)-constFlow;
					newValueField.set(x, y, z, newValue < minValue ? minValue : newValue > maxValue ? maxValue : newValue);											
				}
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
	
	private double laplacian(int x, int y, int z, DoubleGrid3D doubleField){
		double neighbourConcentrationSum =  doubleField.field[doubleField.stx(x+1)][doubleField.sty(y)][doubleField.stz(z)]
		                                  + doubleField.field[doubleField.stx(x-1)][doubleField.sty(y)][doubleField.stz(z)]
		                                  + doubleField.field[doubleField.stx(x)][doubleField.sty(y+1)][doubleField.stz(z)]
		                                  + doubleField.field[doubleField.stx(x)][doubleField.sty(y-1)][doubleField.stz(z)]
		                                  + doubleField.field[doubleField.stx(x)][doubleField.sty(y)][doubleField.stz(z+1)]
		                                  + doubleField.field[doubleField.stx(x)][doubleField.sty(y)][doubleField.stz(z-1)];
		neighbourConcentrationSum -= 6*doubleField.field[doubleField.stx(x)][doubleField.sty(y)][doubleField.stz(z)];
		return neighbourConcentrationSum;
	}
	
	
	

}

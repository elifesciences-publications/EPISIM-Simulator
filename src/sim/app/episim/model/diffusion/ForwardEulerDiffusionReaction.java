package sim.app.episim.model.diffusion;

import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;


public class ForwardEulerDiffusionReaction {
	
	private final double DIFFUSION_COEFFICIENT_THRESHHOLD = 0.16;
	
	private ExtraCellularDiffusionField2D extraCellularField2D;
	private ExtraCellularDiffusionField3D extraCellularField3D;
	
	
	public ForwardEulerDiffusionReaction(ExtraCellularDiffusionField2D extraCellularField){
		this.extraCellularField2D = extraCellularField;
	}
	
	public ForwardEulerDiffusionReaction(ExtraCellularDiffusionField3D extraCellularField){
		this.extraCellularField3D = extraCellularField;
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
		long start = System.currentTimeMillis();
		for(int i = 0; i < numberOfIterations; i++){
			if(!extraCellularField2D.isToroidalX())setDirichletXBoundaryConditionsInNewField(currentValueField, newValueField);
			if(!extraCellularField2D.isToroidalY())setDirichletYBoundaryConditionsInNewField(currentValueField, newValueField);
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
		long end = System.currentTimeMillis();
		//System.out.println("Calculation Time in miliseconds for Diffusion Field: " +((end-start)));
	}
	
	private void update3DField(){
		//assume Dirichlet Boundary Conditions if not toroidal
		DoubleGrid3D currentValueField = extraCellularField3D.getExtraCellularField();
		DoubleGrid3D newValueField = new DoubleGrid3D(currentValueField.getWidth(), currentValueField.getHeight(), currentValueField.getLength());
		
		int startX = extraCellularField3D.isToroidalX() ? 0 : 1;
		int startY = extraCellularField3D.isToroidalY() ? 0 : 1;
		int startZ = extraCellularField3D.isToroidalZ() ? 0 : 1;
		
		int stopX = extraCellularField3D.isToroidalX() ? currentValueField.getWidth() : (currentValueField.getWidth()-1);
		int stopY = extraCellularField3D.isToroidalY() ? currentValueField.getHeight() : (currentValueField.getHeight()-1);
		int stopZ = extraCellularField3D.isToroidalZ() ? currentValueField.getLength() : (currentValueField.getLength()-1);
		
		
		final double latticeSize = extraCellularField3D.getFieldConfiguration().getLatticeSiteSizeInMikron()/ Math.pow(10, 6);
		double dt = extraCellularField3D.getFieldConfiguration().getDeltaTimeInSecondsPerIteration();
		double dt_dx2 = (dt / (latticeSize*latticeSize));
		double diffConst = extraCellularField3D.getFieldConfiguration().getDiffusionCoefficient();
		double decayConst = extraCellularField3D.getFieldConfiguration().getDegradationRate();
		
		double maxConcentration = extraCellularField3D.getFieldConfiguration().getMaximumConcentration();
		double minConcentration = extraCellularField3D.getFieldConfiguration().getMinimumConcentration();
		
		double numberOfIterations = 1;
		//manipulate delta_time if diffusion coefficient is too large
		if(dt_dx2*diffConst > DIFFUSION_COEFFICIENT_THRESHHOLD){
			numberOfIterations = Math.floor((dt_dx2*diffConst)/DIFFUSION_COEFFICIENT_THRESHHOLD);
			dt_dx2 /= numberOfIterations;
		}	
		double currentConcentration = 0;
		double newConcentration = 0;
		long start = System.currentTimeMillis();
		for(int i = 0; i < numberOfIterations; i++){
			if(!extraCellularField3D.isToroidalX())setDirichletXBoundaryConditionsInNewField(currentValueField, newValueField);
			if(!extraCellularField3D.isToroidalY())setDirichletYBoundaryConditionsInNewField(currentValueField, newValueField);
			if(!extraCellularField3D.isToroidalZ())setDirichletZBoundaryConditionsInNewField(currentValueField, newValueField);
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
			currentValueField.setTo(newValueField);
			newValueField.setTo(0);
		}
		long end = System.currentTimeMillis();
		//System.out.println("Calculation Time in miliseconds for Diffusion Field: " +((end-start)));
	}	
	
	private void setDirichletXBoundaryConditionsInNewField(DoubleGrid2D currentValueField, DoubleGrid2D newValueField){
		for(int y = 0; y < currentValueField.getHeight(); y++){
			for(int x = 0; x < currentValueField.getWidth(); x++){
				if(x==0 || x ==(currentValueField.getWidth()-1))
					newValueField.set(x, y, currentValueField.get(x, y));
			}						
		}
	}
	
	private void setDirichletYBoundaryConditionsInNewField(DoubleGrid2D currentValueField, DoubleGrid2D newValueField){
		for(int y = 0; y < currentValueField.getHeight(); y++){
			for(int x = 0; x < currentValueField.getWidth(); x++){
				if(y==0 || y==(currentValueField.getHeight()-1))
					newValueField.set(x, y, currentValueField.get(x, y));
			}						
		}
	}
	
	private void setDirichletXBoundaryConditionsInNewField(DoubleGrid3D currentValueField, DoubleGrid3D newValueField){
		for(int z = 0; z < currentValueField.getLength(); z++){
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){
					if(x==0 || x ==(currentValueField.getWidth()-1))
						newValueField.set(x, y, z, currentValueField.get(x, y, z));
				}						
			}
		}
	}
	
	private void setDirichletYBoundaryConditionsInNewField(DoubleGrid3D currentValueField, DoubleGrid3D newValueField){
		for(int z = 0; z < currentValueField.getLength(); z++){
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){
					if(y==0 || y==(currentValueField.getHeight()-1))
						newValueField.set(x, y, z, currentValueField.get(x, y, z));
				}						
			}
		}
	}
	private void setDirichletZBoundaryConditionsInNewField(DoubleGrid3D currentValueField, DoubleGrid3D newValueField){
		for(int z = 0; z < currentValueField.getLength(); z++){
			for(int y = 0; y < currentValueField.getHeight(); y++){
				for(int x = 0; x < currentValueField.getWidth(); x++){
					if(z==0 || z==(currentValueField.getLength()-1))
						newValueField.set(x, y, z, currentValueField.get(x, y, z));
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

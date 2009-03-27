package sim.app.episim.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sim.app.episim.model.ModelController;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;


public class TysonRungeCuttaCalculator {
	
	private static final double KAPPA = 0.015;
	
	private static double k4 = 180;
	private static final double K4_PRIME = 0.018;
	
	private static  double k6 = 1;
	
	
	private static final double STEPSIZE = 0.25;
	
	private static double du = 0;
	private static double dz = 0;
	
	
	private static double u = 0;
	private static double z = 0;
	
	
	
	
	
	public static void assignRandomCellcyleState(EpisimCellDiffModel cellDiff, int numberOfSteps){
		double [] u_temp = new double[3];
		double [] z_temp = new double[3];
				
		double [] du_temp = new double[3];
		double [] dz_temp = new double[3];
		
		EpisimCellDiffModelGlobalParameters globalParameters = ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters();
		try{
		
		 Method m = globalParameters.getClass().getMethod("getK6", null);
		
		 Object result = null;
		 if(m != null) result = m.invoke(globalParameters, null);
		 
		if(result != null && result.getClass().isAssignableFrom(Double.class)){
			k6 = ((Double) result).doubleValue();
			
		}
		
		m = globalParameters.getClass().getMethod("getK4", null);
		
		result = null;
		if(m != null) result = m.invoke(globalParameters, null);
		 
		if(result != null && result.getClass().isAssignableFrom(Double.class)){
			k4 = ((Double) result).doubleValue();
			
		}
		
		
		for (long i = 1; i <= numberOfSteps; i++){
			
			du = -1*(k6*u)+k4*(K4_PRIME/k4 + Math.pow(u, 2))*z;
			dz = KAPPA - k4*(K4_PRIME/k4 + Math.pow(u, 2))*z;
			
			
			u_temp[0] = u + 0.5 * STEPSIZE* du;
			z_temp[0] = z + 0.5 * STEPSIZE* dz;
			
			
			du_temp[0] = -1*(k6*u_temp[0])+k4*(K4_PRIME/k4 + Math.pow(u_temp[0], 2))*z_temp[0];
			dz_temp[0] = KAPPA - k4*(K4_PRIME/k4 + Math.pow(u_temp[0], 2))*z_temp[0];
		
			
			u_temp[1] = u + 0.5 * STEPSIZE* du_temp[0];
			z_temp[1] = z + 0.5 * STEPSIZE* dz_temp[0];
			
			
			du_temp[1] = -1*(k6*u_temp[1])+k4*(K4_PRIME/k4 + Math.pow(u_temp[1], 2))*z_temp[1];
			dz_temp[1] = KAPPA - k4*(K4_PRIME/k4 + Math.pow(u_temp[1], 2))*z_temp[1];
					
			
			u_temp[2] = u + STEPSIZE* du_temp[1];
			z_temp[2] = z + STEPSIZE* dz_temp[1];
			
			
			du_temp[2] = -1*(k6*u_temp[2])+k4*(K4_PRIME/k4 + Math.pow(u_temp[2], 2))*z_temp[2];
			dz_temp[2] = KAPPA - k4*(K4_PRIME/k4 + Math.pow(u_temp[2], 2))*z_temp[2];
			
			
			u = u + (STEPSIZE/6)*(du + 2*(du_temp[0]+ du_temp[1]) + du_temp[2]);
			z = z + (STEPSIZE/6)*(dz + 2*(dz_temp[0]+ dz_temp[1]) + dz_temp[2]);
		}
	
	
	   m = cellDiff.getClass().getMethod("setU", new Class[]{Double.TYPE});
     
		if(m != null) {
			m.invoke(cellDiff, new Object[]{u});
        	
		}
		
		m = cellDiff.getClass().getMethod("setZ", new Class[]{Double.TYPE});
		if(m != null) {
			m.invoke(cellDiff, new Object[]{z});
			
		}
		
		 }
      catch (SecurityException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
      catch (NoSuchMethodException e){
	      System.out.println("Tyson Cellcycle is not available!");
      }
      catch (IllegalArgumentException e){
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IllegalAccessException e){
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (InvocationTargetException e){
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
		
		
	}

}

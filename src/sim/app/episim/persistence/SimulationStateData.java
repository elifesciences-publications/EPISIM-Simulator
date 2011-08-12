package sim.app.episim.persistence;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import episiminterfaces.NoExport;
import sim.app.episim.UniversalCell;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;

public class SimulationStateData {

	public ArrayList<HashMap<String, Object>> cells = new ArrayList<HashMap<String, Object>>();

	private static SimulationStateData instance = null;

	private List<SnapshotListener> listeners;

	private SimulationStateData() {
		listeners = new LinkedList<SnapshotListener>();
	}

	public static synchronized SimulationStateData getInstance() {
		if (instance == null) {
			instance = new SimulationStateData();
		}
		return instance;
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void addSnapshotListener(SnapshotListener listener) {
		listeners.add(listener);
	}

	public void updateData() {
		for (SnapshotListener listener : listeners) {
			for (SnapshotObject object : listener.collectSnapshotObjects()) {
				if (object.getIdentifier().equals(SnapshotObject.CELL)) {
					UniversalCell cell = (UniversalCell) object
							.getSnapshotObject();
					HashMap<String, Object> cellParameters  = new HashMap<String, Object>();
					for(Method m : getParameterMethodsFromCell(cell)){
						Object returnValue = invokeGetMethod(cell,m);
						if(returnValue != null)
							cellParameters.put(m.getName(),returnValue);
					}
					cells.add(cellParameters);
				}
				
			}
		}
		// TODO snapshot listener
		// TODO CellBehavioralModelController.getActLoadedModelFile (Pfad zum Modell)
	}

	public void restoreData() {
		// TODO ObjectManipulations.resetInitialGlobalValues
	}

	private List<Method> getParameterMethodsFromCell(UniversalCell cell) {

		List<Method> methods = new ArrayList<Method>();
		for (Method m : cell.getClass().getMethods()) {
			if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
					&& m.getAnnotation(NoExport.class) == null)
				methods.add(m);
		}
		for (Method m : cell.getEpisimCellBehavioralModelClass().getMethods()) {
			if ((m.getName().startsWith("get") && !m.getName().equals(
					"getParameters"))
					|| m.getName().startsWith("is"))
				methods.add(m);
		}
		for (Method m : cell.getEpisimBioMechanicalModelObject().getClass()
				.getMethods()) {
			if (((m.getName().startsWith("get") && !m.getName().equals(
					"getParameters")) || m.getName().startsWith("is"))
					&& m.getAnnotation(NoExport.class) == null)
				methods.add(m);
		}
		return methods;
	}

	private Object invokeGetMethod(Object object, Method actMethod) {
		Object obj = null;
		if (actMethod.getParameterTypes().length == 0) {
			try {
				obj = actMethod.invoke(object, new Object[0]);
			} catch (Exception e) {
				return null;
			}
		}
		return obj;
	}
}

package sim.app.episim.persistence;

import java.awt.geom.Rectangle2D.Double;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.NoExport;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.MiscalleneousGlobalParameters;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.engine.SimStateHack.TimeSteps;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

public class SimulationStateData {

	private static SimulationStateData instance = null;

	private List<SnapshotListener> listeners;

	public ArrayList<CellObjectData> cells = new ArrayList<CellObjectData>();

	public HashMap<String, Object> cellContinuouss = new HashMap<String, Object>();
	private Continuous2D cellContinuous; // TODO

	public HashMap<String, Object> timeStepss = new HashMap<String, Object>();
	private TimeSteps timeSteps; // TODO

	public HashMap<String, Object> behavioralModelGlobalParameterss = new HashMap<String, Object>();
	private EpisimCellBehavioralModelGlobalParameters behavioralModelGlobalParameters; // TODO

	public HashMap<String, Object> mechModelGlobalParameterss = new HashMap<String, Object>();
	private EpisimBiomechanicalModelGlobalParameters mechModelGlobalParameters; // TODO

	public List<Double2D> woundRegionCoordinates; // TODO

	public Double[] deltaInfo; // TODO

	public HashMap<String, Object> miscalleneousGlobalParameterss = new HashMap<String, Object>(); //TODO

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
					CellObjectData cod = new CellObjectData();
					cod.cellData = getParameterObjectsFromObject(cell);
					cod.cellBehavioralModelObjectData = getParameterObjectsFromObject(cell
							.getEpisimCellBehavioralModelObject());
					cod.bioMechanicalModelObjectData = getParameterObjectsFromObject(cell
							.getEpisimBioMechanicalModelObject());
					cells.add(cod);
				} else if (object.getIdentifier().equals(
						SnapshotObject.CELLCONTINUOUS)) {
					this.cellContinuous = (Continuous2D) object
							.getSnapshotObject();
					this.cellContinuouss = getParameterObjectsFromObject(cellContinuous);

				} else if (object.getIdentifier().equals(
						SnapshotObject.TIMESTEPS)) {
					this.timeSteps = (TimeSteps) object.getSnapshotObject();
					this.timeStepss = getParameterObjectsFromObject(timeSteps);

				} else if (object.getIdentifier().equals(
						SnapshotObject.CELLBEHAVIORALMODELGLOBALPARAMETERS)) {
					behavioralModelGlobalParameters = (EpisimCellBehavioralModelGlobalParameters) object
							.getSnapshotObject();
					this.behavioralModelGlobalParameterss = getParameterObjectsFromObject(behavioralModelGlobalParameters);

				} else if (object.getIdentifier().equals(
						SnapshotObject.MECHANICALMODELGLOBALPARAMETERS)) {
					mechModelGlobalParameters = (EpisimBiomechanicalModelGlobalParameters) object
							.getSnapshotObject();
					this.mechModelGlobalParameterss = getParameterObjectsFromObject(mechModelGlobalParameters);

				} else if (object.getIdentifier().equals(
						SnapshotObject.MISCALLENEOUSGLOBALPARAMETERS)) {
					MiscalleneousGlobalParameters.instance()
							.reloadMiscalleneousGlobalParametersObject(
									(MiscalleneousGlobalParameters) object
											.getSnapshotObject());
					this.miscalleneousGlobalParameterss = getParameterObjectsFromObject(MiscalleneousGlobalParameters.instance());

				} else if (object.getIdentifier().equals(SnapshotObject.WOUND)) {
					Object obj = null;
					if ((obj = object.getSnapshotObject()) instanceof List)
						woundRegionCoordinates = (List<Double2D>) obj;
					else
						deltaInfo = (java.awt.geom.Rectangle2D.Double[]) object
								.getSnapshotObject();

				}
			}
		}
		// TODO snapshot listener
		// TODO CellBehavioralModelController.getActLoadedModelFile (Pfad zum
		// Modell)
	}

	public void restoreData() {
		// TODO ObjectManipulations.resetInitialGlobalValues
	}

	private String methodToName(String methodName) {
		String parameterName = methodName;
		if (methodName.startsWith("get"))
			parameterName = parameterName.substring(3);
		else if (methodName.startsWith("is"))
			parameterName = parameterName.substring(2);
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(parameterName.charAt(0)));
		sb.append(parameterName.substring(1));
		parameterName = sb.toString();
		return parameterName;
	}

	private HashMap<String, Object> getParameterObjectsFromObject(Object object) {

		HashMap<String, Object> objects = new HashMap<String, Object>();
		for (Method m : object.getClass().getMethods()) {
			if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
					&& m.getAnnotation(NoExport.class) == null
					&& !m.getName().equals("getClass")) {

				try {
					if (m.getParameterTypes().length == 0) {

						objects.put(methodToName(m.getName()),
								m.invoke(object, new Object[] {}));
						// System.out.println(object.getClass().getName() +
						// " : "
						// + m.getName() + " -> " +
						// objects.get(methodToName(m.getName())));
					}
				} catch (IllegalAccessException e) {
					ExceptionDisplayer.getInstance().displayException(e);
				} catch (IllegalArgumentException e) {
					ExceptionDisplayer.getInstance().displayException(e);
				} catch (InvocationTargetException e) {
					ExceptionDisplayer.getInstance().displayException(
							e.getCause());
				}
			}
		}
		return objects;
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

	public class CellObjectData {
		public HashMap<String, Object> cellData = new HashMap<String, Object>();
		public HashMap<String, Object> cellBehavioralModelObjectData = new HashMap<String, Object>();
		public HashMap<String, Object> bioMechanicalModelObjectData = new HashMap<String, Object>();
	}
}

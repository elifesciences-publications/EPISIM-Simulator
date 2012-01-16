package sim.app.episim.persistence;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.SimStateServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.gui.ShowLog;

public class ImportLog implements SimulationStateChangeListener{

	public static final String WARNING = "WARNING";
	public static final String SUCCESSFUL = "SUCCESSFUL";
	public static final String ERROR = "ERROR";
	public static final String IGNORED = "IGNORED";

	private int ignoreCounter = 0;
	private int warningCounter = 0;
	private int errorCounter = 0;
	private int successfulCounter = 0;

	private ImportEntryNode processedLog = null;
	private Node root = null;

	private HashMap<Node, ImportEntryNode> logNodeMap;

	public static void success(Node loggedNode) {
		log(SUCCESSFUL, "Node was not Ignored", loggedNode);
	}

	public static void log(String type, String message, Node loggedNode){
		getInstance().logEvent(type, message, loggedNode);
	}
	
	
	public void logEvent(String type, String message, Node loggedNode) {
		ImportEntryNode log = logNodeMap.get(loggedNode);
		if (log == null) {
			log = new ImportEntryNode(loggedNode.getNodeName());
			logNodeMap.put(loggedNode, log);
		}
		log.log(type, message);
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	private void postprocess(Node rootNode) {
		ignoreCounter = 0;
		warningCounter = 0;
		errorCounter = 0;
		successfulCounter = 0;

		processedLog = logNodeMap.get(rootNode);
		if (processedLog == null) {
			processedLog = new ImportEntryNode(rootNode.getNodeName());
			logNodeMap.put(rootNode, processedLog);
		}
		postprocess(processedLog, rootNode);
	}

	private void postprocess(ImportEntryNode importEntryNodeParent,
			Node loggedNodeParent) {
		NodeList nl = loggedNodeParent.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (!nl.item(i).getNodeName().equals("#text")) {
				ImportEntryNode attributeEntry = logNodeMap.get(nl.item(i));
				if (attributeEntry == null) {
					attributeEntry = new ImportEntryNode(nl.item(i)
							.getNodeName());
					attributeEntry
							.log(IGNORED,
									"this Node has not been logged. Maybe it hasn't been processed.");
				}
				importEntryNodeParent.addEntryNode(attributeEntry);
				postprocess(attributeEntry, nl.item(i));
			}
		}

	}

	public String toString() {
		if (root != null) {
			postprocess(root);
			if (processedLog != null)
				return processedLog.toString();
		}
		return "log not processed";
	}

	private ImportLog() {
		logNodeMap = new HashMap<Node, ImportLog.ImportEntryNode>();
	}

	public class ImportEntryNode {
		private String nodeName;
		private ArrayList<ImportEntryNode> subEntryNodes;
		private ArrayList<ImportEntry> entrys;

		public ImportEntryNode(String nodeName) {
			subEntryNodes = new ArrayList<ImportLog.ImportEntryNode>();
			entrys = new ArrayList<ImportLog.ImportEntry>();
			this.nodeName = nodeName;
		}

		public boolean isCritical() {
			boolean isCritical = false;
			boolean isIgnored = false;
			boolean isSuccess = false;
			for (ImportEntry ie : entrys) {
				if (ie.importEntryType.equals(ERROR)) {
					return true;
				} else if (ie.importEntryType.equals(IGNORED)) {
					return true;
				} else if (ie.importEntryType.equals(SUCCESSFUL)) {
					isSuccess = true;
				}
			}
			if (!isSuccess)
				return true;
			return false;
		}

		public boolean hasCriticalSubnodes() {
			if (isCritical())
				return true;
			for (ImportEntryNode ieN : subEntryNodes) {
				if (ieN.hasCriticalSubnodes())
					return true;
			}
			return false;
		}

		public String toString() {
			return toString(0);
		}

		public void log(String type, String message) {
			entrys.add(new ImportEntry(type, message));
		}

		public void addEntryNode(ImportEntryNode node) {
			subEntryNodes.add(node);
		}

		public String toString(int depth) {
			if (!hasCriticalSubnodes())
				return "";
			StringBuffer returnString = new StringBuffer();
			for (int i = 0; i < depth; i++) {
				returnString.append("\t");
			}
			returnString.append(nodeName + "\n");

			if (isCritical())
				for (ImportEntry ie : entrys) {
					for (int i = 0; i < depth + 1; i++) {
						returnString.append("\t");
					}
					returnString.append(ie.toString() + "\n");
				}

			for (ImportEntryNode ieN : subEntryNodes) {
				returnString.append(ieN.toString(depth + 1));
			}

			return returnString.toString();
		}
	}

	private class ImportEntry {
		private String importEntryType;
		private String message;

		public ImportEntry(String type, String message) {
			this.importEntryType = type;
			this.message = message;
		}

		public String toString() {
			return importEntryType + " : " + message;
		}
	}

	private static ImportLog instance = null;

	public static ImportLog getInstance() {
		if (instance == null)
			instance = new ImportLog();
		SimStateServer.getInstance().addSimulationStateChangeListener(instance);
		return instance;
	}

	@Override
	public void simulationWasStarted() {
		
		
	}

	@Override
	public void simulationWasPaused() {
		
		
	}

	@Override
	public void simulationWasStopped() {
//		System.out.println(toString());
//		ShowLog sl = new ShowLog(processedLog);
//		sl.setVisible(true); TODO Log anzeigen
	}

}

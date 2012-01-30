package sim.app.episim.persistence;

import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ImportLog {

	private int nodeReadCounter = 0;
	private int nodeRecognizedCounter = 0;

	private HashMap<Node, Boolean> logNodeMap;

	public static void nodeRead(Node node) {
		if (!node.getNodeName().equals("#text")) {
			getInstance().logNodeMap.put(node, false);
			getInstance().nodeReadCounter++;
		}
	}

	public static void nodeRecognized(Node node) {
		if (!node.getNodeName().equals("#text")) {
			getInstance().logNodeMap.put(node, true);
			getInstance().nodeRecognizedCounter++;
		}
	}

	private static ImportLog instance = null;

	private ImportLog() {
		logNodeMap = new HashMap<Node, Boolean>();
	}

	public static ImportLog getInstance() {
		if (instance == null)
			instance = new ImportLog();
		return instance;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Nodes in File: ").append(nodeReadCounter)
				.append(" Nodes recognized: ").append(nodeRecognizedCounter)
				.append("\n");
		for (Node node : logNodeMap.keySet()) {
			if (!logNodeMap.get(node)) {
				sb.append(getXPath(node)).append(" could not be recognized.")
						.append("\n");
			}
		}

		return sb.toString();
	}

	private String getXPath(Node node) {
		StringBuffer sb = new StringBuffer();
		Node parent = node.getParentNode();

		if (parent == null)
			return "//" + node.getNodeName();

		sb.append(getXPath(parent));

		int number = 0;
		NodeList neighbors = parent.getChildNodes();
		for (int i = 0; i < neighbors.getLength(); i++) {
			if (neighbors.item(i).getNodeName().equals(node.getNodeName())) {
				number++;
			}
			if (neighbors.item(i) == node) {
				sb.append("/").append(node.getNodeName()).append("[")
						.append(number).append("]");
				continue;
			}
		}

		return sb.toString();
	}
}

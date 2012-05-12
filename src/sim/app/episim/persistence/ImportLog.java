package sim.app.episim.persistence;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;

public class ImportLog implements ClassLoaderChangeListener{

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
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
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

	public static void showLog() {
		JScrollPane scrollPane = null;
		JTextArea logArea = null;
		JFrame frame = new JFrame("Nodes read: "
				+ getInstance().nodeReadCounter + " Recognized: "
				+ getInstance().nodeRecognizedCounter);
		scrollPane = new JScrollPane();
		logArea = new JTextArea();
		scrollPane.setViewportView(logArea);
		frame.add(scrollPane);

		logArea.setText(getInstance().toString());
		frame.setSize(300, 600);
		frame.setVisible(true);
	}

	
   public void classLoaderHasChanged() {
      instance = null;
	   
   }
}

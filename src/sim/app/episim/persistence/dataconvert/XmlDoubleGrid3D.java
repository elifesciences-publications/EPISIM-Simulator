package sim.app.episim.persistence.dataconvert;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.app.episim.persistence.ExportException;
import sim.app.episim.persistence.XmlFile;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.DoubleGrid3D;

public class XmlDoubleGrid3D extends XmlObject<DoubleGrid3D> {
	private static final String DEPTH = "depth";
	private static final String INDEX = "i";
	private static final String LAYER = "layer";
	private static final String COLUMNS = "columns";
	private static final String ROWS = "rows";

	private ArrayList<XmlDoubleGrid2D> dataLayers;

	public XmlDoubleGrid3D(DoubleGrid3D obj) throws ExportException {
		super(obj);
	}

	public XmlDoubleGrid3D(Node objectNode) {
		super(objectNode);
	}

	@Override
	protected void exportSubXmlObjectsFromParameters() {
		dataLayers = new ArrayList<XmlDoubleGrid2D>();
		for (int z = 0; z < getObject().getLength(); z++) {
			DoubleGrid2D layer = new DoubleGrid2D(getObject().getWidth(),
					getObject().getHeight());
			for (int x = 0; x < getObject().getWidth(); x++) {
				for (int y = 0; y < getObject().getHeight(); y++) {
					layer.set(x, y, getObject().get(x, y, z));
				}
			}
			dataLayers.add(new XmlDoubleGrid2D(layer));
		}
	}

	@Override
	public Element toXMLNode(String nodeName, XmlFile xmlFile) {

		int depth = 0, rows = 0, columns = 0;
		if (getObject() != null) {
			depth = getObject().getLength();

			rows = getObject().getHeight();

			columns = getObject().getWidth();
		}

		Element arrayNode = xmlFile.createElement(nodeName);
		arrayNode.setAttribute(DEPTH, depth + "");
		arrayNode.setAttribute(ROWS, rows + "");
		arrayNode.setAttribute(COLUMNS, columns + "");

		for (int i = 0; i < depth; i++) {
			XmlDoubleGrid2D layer = dataLayers.get(i);
			if (layer != null)
				arrayNode.appendChild(layer.toXMLNode(LAYER, xmlFile));
		}

		return arrayNode;
	}

	@Override
	protected void importParametersFromXml(Class<?> clazz) {
		int rows = 0, columns = 0, depth = 0;
		DoubleGrid3D array3d = new DoubleGrid3D(columns, rows, depth);
		if (getObjectNode() != null) {

			rows = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(ROWS).getNodeValue());
			columns = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(COLUMNS).getNodeValue());
			depth = Integer.parseInt(getObjectNode().getAttributes()
					.getNamedItem(DEPTH).getNodeValue());
			array3d = new DoubleGrid3D(columns, rows, depth);

			NodeList layersNL = getObjectNode().getChildNodes();
			for (int layerNodeIndex = 0; layerNodeIndex < layersNL.getLength(); layerNodeIndex++) {
				Node layerNode = layersNL.item(layerNodeIndex);
				if (layerNode.getNodeName().equalsIgnoreCase(LAYER)) {
					int layerN = 0;
					layerN = Integer.parseInt(layerNode.getAttributes()
							.getNamedItem(LAYER).getNodeValue());
					DoubleGrid2D layer = new DoubleGrid2D(columns, rows);
					layer = (new XmlDoubleGrid2D(layerNode)).copyValuesToTarget(layer);
					if(layer!=null){
						for (int x = 0; x < layer.getWidth(); x++) {
							for (int y = 0; y < layer.getHeight(); y++) {
								array3d.set(x, y, layerN, layer.get(x, y));
							}
						}
					}
				}
			}
		}
		setObject(array3d);
	}

	@Override
	public DoubleGrid3D copyValuesToTarget(DoubleGrid3D target) {
		importParametersFromXml(null);
		return target.setTo(getObject());
	}
}

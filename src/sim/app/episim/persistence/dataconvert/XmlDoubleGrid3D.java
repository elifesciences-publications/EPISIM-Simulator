package sim.app.episim.persistence.dataconvert;

import org.w3c.dom.Node;

import sim.app.episim.persistence.ExportException;
import sim.field.grid.DoubleGrid3D;



public class XmlDoubleGrid3D extends XmlObject<DoubleGrid3D> {
	public XmlDoubleGrid3D(DoubleGrid3D obj) throws ExportException {
		super(obj);
	}

	public XmlDoubleGrid3D(Node objectNode) {
		super(objectNode);
	}
}

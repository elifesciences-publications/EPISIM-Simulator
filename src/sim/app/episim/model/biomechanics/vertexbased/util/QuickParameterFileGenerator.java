package sim.app.episim.model.biomechanics.vertexbased.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sim.app.episim.propfilegenerator.PropertyDescriptor;
import sim.app.episim.propfilegenerator.PropertyFileGenerator;


public class QuickParameterFileGenerator {

	
	public static void main(String[] args) {
		List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
		
		propertyDescriptors.add(new PropertyDescriptor("kappa",Double.TYPE, 150, 50, 10));
		PropertyFileGenerator generator = new PropertyFileGenerator();
		generator.generatePropertyFiles(new File("z:/simulation-input/siminput_2.properties"), propertyDescriptors);

	}

}

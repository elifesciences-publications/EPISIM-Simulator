package sim.portrayal3d.simple;

import java.awt.Color;
import java.awt.Image;

import javax.media.j3d.Appearance;


public class SpherePortrayal3DHack extends SpherePortrayal3D {
	
	public SpherePortrayal3DHack() {

		this(1f);
	}

	/**
	 * Constructs a SpherePortrayal3D with a default (flat opaque white)
	 * appearance and the given scale.
	 */
	public SpherePortrayal3DHack(double scale) {

		this(Color.white, scale);
	}

	/**
	 * Constructs a SpherePortrayal3D with a flat opaque appearance of the given
	 * color and a scale of 1.0.
	 */
	public SpherePortrayal3DHack(Color color) {

		this(color, 1f);
	}

	/**
	 * Constructs a SpherePortrayal3D with a flat opaque appearance of the given
	 * color and the given scale.
	 */
	public SpherePortrayal3DHack(Color color, double scale) {

		this(color, scale, DEFAULT_DIVISIONS);
	}

	/**
	 * Constructs a SpherePortrayal3D with a flat opaque appearance of the given
	 * color, scale, and divisions.
	 */
	public SpherePortrayal3DHack(Color color, double scale, int divisions) {

		super(appearanceForColor(color), true, false, scale, divisions);
	}

	/**
	 * Constructs a SpherePortrayal3D with the given (opaque) image and a scale
	 * of 1.0.
	 */
	public SpherePortrayal3DHack(Image image) {

		this(image, 1f);
	}

	/** Constructs a SpherePortrayal3D with the given (opaque) image and scale. */
	public SpherePortrayal3DHack(Image image, double scale) {

		this(image, scale, DEFAULT_DIVISIONS);
	}

	/**
	 * Constructs a SpherePortrayal3D with the given (opaque) image, scale, and
	 * divisions.
	 */
	public SpherePortrayal3DHack(Image image, double scale, int divisions) {

		super(appearanceForImage(image, true), false, true, scale, divisions);
	}

	/**
	 * Constructs a SpherePortrayal3D with the given appearance, and scale, plus
	 * whether or not to generate normals or texture coordinates. Without texture
	 * coordiantes, a texture will not be displayed
	 */
	public SpherePortrayal3DHack(Appearance appearance, boolean generateNormals, boolean generateTextureCoordinates, double scale) {

		super(appearance, generateNormals, generateTextureCoordinates, scale, DEFAULT_DIVISIONS);
	}
	
	public void setAppearance(Appearance appearance){
		this.appearance = appearance;
	}

}

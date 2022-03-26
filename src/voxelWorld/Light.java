package voxelWorld;

import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;

public class Light {
	private Vector3f position;
	protected Vector3f color;
	protected Vector3f originalColor;
	private Vector3f attenuation = new Vector3f(1, -0.35f, 0.1f);
	protected float secondCount = 0f;
	protected float low = 1.0f;

	public Light(Vector3f position, Vector3f color) {
		super();
		this.position = position;
		this.color = color;
	}

	public Light(Vector3f position, Vector3f originalColor, Vector3f attenuation) {
		super();
		this.position = position;
		this.originalColor = originalColor;
		this.color = originalColor;
		this.attenuation = attenuation;
	}

	public Light(Vector3f position, Vector3f originalColor, Vector3f attenuation, float low) {
		super();
		this.position = position;
		this.originalColor = originalColor;
		this.color = originalColor;
		this.attenuation = attenuation;
		this.low = low;
	}

	public Vector3f getAttenuation() {
		return this.attenuation;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public Vector3f getColor() {
		return color;
	}

	public void setColor(Vector3f color) {
		this.color = color;
	}
}

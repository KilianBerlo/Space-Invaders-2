package guis;

import org.lwjgl.util.vector.Vector2f;

public class GuiTexture {
	private int textureID;
	private Vector2f position;
	private Vector2f scale;
	
	public GuiTexture(int texture, Vector2f position, Vector2f scale) {
		this.textureID = texture;
		this.position = position;
		this.scale = scale;
	}

	public int getTexture() {
		return textureID;
	}

	public Vector2f getPosition() {
		return position;
	}

	public Vector2f getScale() {
		return scale;
	}
}

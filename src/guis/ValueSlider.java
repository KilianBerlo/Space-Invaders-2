package guis;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class ValueSlider{
	private GuiTexture bar;
	private GuiTexture mask;
	private int maskID;
	private float percentage = -1;
	private Vector2f position;
	private Vector2f scale;

	public ValueSlider(int barID, int maskID, float percentage, Vector2f position, Vector2f scale) {
		super();
		this.bar = new GuiTexture(barID, position, new Vector2f(0.25f, 0.25f));
		this.maskID = maskID;
		this.position = position;
		this.scale = scale;
		
		setValue(percentage);
	}
	
	public GuiTexture getBar() {
		return bar;
	}
	public GuiTexture getMask() {
		return mask;
	}
	
	public void setValue(float percentage) {
		percentage = Math.round(percentage / 10f) * 10f;
		if(Math.abs(percentage - this.percentage) < 0.01f) return; // within margin of error
		
		Vector2f pos = new Vector2f(position);
		
		this.percentage = percentage;
		float hScale = 0.0049f * (100f - percentage);
		
		pos.x = position.x + scale.x - 0.005f;
		
		this.mask = new GuiTexture(maskID, pos, new Vector2f(hScale, 0.025f));
	}
}

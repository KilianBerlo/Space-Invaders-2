package voxelWorld;

import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import renderEngine.DisplayManager;

public class BlockLight extends Light {
	Entity blockEntity;
	
	public BlockLight(Vector3f position, Vector3f originalColor, Vector3f attenuation, float low, Entity blockEntity) {
		super(position, originalColor, attenuation, low);
		
		this.blockEntity = blockEntity;
	}
	
	public void update() {
		float phase = getPhase();
		this.blockEntity.getModel().getTexture().setWhiteBlending(phase);
		this.color = (Vector3f) new Vector3f(this.originalColor).scale(phase);
	}
	
	private float getPhase() {
		secondCount += DisplayManager.getDeltaTimeSeconds();
		
		return 1 - 0.5f * (1 - low) * (float) (Math.sin(secondCount * 5f) + 1.0);
	}
	
	public Entity getBlockEntity() {
		return this.blockEntity;
	}
}

package entities;

import org.lwjgl.util.vector.Vector3f;

import audioEngine.AudioSource;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import voxelWorld.VoxelWorld;

public class Coin extends Entity {
	private static final float SCALE = 0.5f;
	private static final float ROTATION_SPEED = 180f; // degrees per second
	
	public boolean isDie = false;
	public boolean isRegisteredShadowCaster = false;
	
	public int listIndex = -1;
	
	private AudioSource pickup;

	public Coin(TexturedModel model, Vector3f position, VoxelWorld world, Loader loader, int index, AudioSource pickup) {
		super(model, position, 0, 0, 0, SCALE);
		listIndex = index;
		this.pickup = pickup;
	}
	
	public void update(Player player) {
		this.increaseRotation(0, DisplayManager.getDeltaTimeSeconds() * ROTATION_SPEED, 0);
		
		if(Vector3f.sub(player.getPosition(), getPosition(), null).length() < 2) {
			isDie = true;
			player.objectivePickedUp();
			pickup.play();
			
			player.oneUp();
		}
	}
}

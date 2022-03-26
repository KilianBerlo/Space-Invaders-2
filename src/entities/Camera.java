package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import audioEngine.AudioSource;
import renderEngine.DisplayManager;
import voxelWorld.VoxelHitbox;

public class Camera {
	private static final float SENSITIVITY = 0.1f;
	
	// position and orientation
	private Vector3f position;
	private float pitch = 0;
	private float yaw = 0;
	private float roll = 0;

	public Camera(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	public void reset() {
		pitch = 0;
		yaw = 0;
		roll = 0;

	}
	
	public void updatePosition(Player player) {
		position = player.getPosition();
	}
	
	public void doMouseMovement() {	
		// update orientation
		float dx = SENSITIVITY * Mouse.getDX() * DisplayManager.getSpeedMultiplier();
		float dy = -SENSITIVITY * Mouse.getDY() * DisplayManager.getSpeedMultiplier();
		
		this.yaw += dx;
		this.pitch += dy;
		
		// make it not go above the top and below the bottom
		pitch = Math.max(pitch, -90);
		pitch = Math.min(pitch, 90);
	}
}

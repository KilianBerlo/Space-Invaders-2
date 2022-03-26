package entities;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import voxelWorld.VoxelType;

public class Voxel {
	public Vector3f corner1;
	public Vector3f corner2;
	
	private VoxelType type;
	private Vector3f position;
	private float size;

	public Voxel(VoxelType type, Vector3f position, float size) {
		corner1 = new Vector3f(position.x - size/2f, position.y - size/2f, position.z - size/2f);
		corner2 = new Vector3f(position.x + size/2f, position.y + size/2f, position.z + size/2f);
		this.type = type;
		this.position = position;
		this.size = size;
	}
	
	public VoxelType getVoxelType() {
		return type;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getSize() {
		return size;
	}
	
	
}

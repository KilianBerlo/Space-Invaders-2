package voxelWorld;

import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;

public class VoxelHitbox {
	private static final float COLLISIONDISTANCE = 0.1f;
	
	public Vector3f corner1;
	public Vector3f corner2;
	public Vector3f centerPos;

	public VoxelHitbox(Vector3f position, float scale, float size) {
		corner1 = new Vector3f(position.x - size/2f, position.y - size/2f, position.z - size/2f);
		corner2 = new Vector3f(position.x + size/2f, position.y + size/2f, position.z + size/2f);
		centerPos = position;
	}
	
	public boolean[] checkCollision(Vector3f playerCorner1, Vector3f playerCorner2) {
		boolean[] collisions = new boolean[7];
		collisions[0] = Math.abs(corner2.x - playerCorner1.x) <= COLLISIONDISTANCE;
		collisions[1] = Math.abs(corner1.x - playerCorner2.x) <= COLLISIONDISTANCE;
		collisions[2] = Math.abs(corner2.y - playerCorner1.y) <= COLLISIONDISTANCE;
		collisions[3] = Math.abs(corner1.y - playerCorner2.y) <= COLLISIONDISTANCE;
		collisions[4] = Math.abs(corner2.z - playerCorner1.z) <= COLLISIONDISTANCE;
		collisions[5] = Math.abs(corner1.z - playerCorner2.z) <= COLLISIONDISTANCE;
		collisions[6] = (collisions[0] || collisions[1]) && (collisions[2] || collisions[3]) && (collisions[4] || collisions[5]);
		
		return collisions;
	}
	
	public boolean xOverlap(float x1, float x2) {
		return corner1.x <= x2 && x1 <= corner2.x;
	}
	
	public boolean yOverlap(float y1, float y2) {
		return corner1.y <= y2 && y1 <= corner2.y;
	}
	
	public boolean zOverlap(float z1, float z2) {
		return corner1.z <= z2 && z1 <= corner2.z;
	}
}

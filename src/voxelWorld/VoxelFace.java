package voxelWorld;

import org.lwjgl.util.vector.Vector3f;

public class VoxelFace {
	private static final float MARGIN = 0.01f;
	
	private float[] vertices;
	private float[] normals;
	private float[] tangents;
	private float[] texCoords;
	private float[] matProps;
	private int[] indices;
	
	private float avgX;
	private float avgY;
	private float avgZ;
	
	public boolean good = true; // makes culling faster
	
	public VoxelFace(float[] vertices, float[] normals, float[] tangents, float[] texCoords, float[] matProps, int[] indices) {
		this.vertices = vertices;
		this.normals = normals;
		this.tangents = tangents;
		this.texCoords = texCoords;
		this.matProps = matProps;
		this.indices = indices;
		
		// for culling
		avgX = (vertices[0] + vertices[3] + vertices[6] + vertices[9]) / 4f;
		avgY = (vertices[1] + vertices[4] + vertices[7] + vertices[10]) / 4f;
		avgZ = (vertices[2] + vertices[5] + vertices[8] + vertices[11]) / 4f;
	}

	public float[] getVertices() {
		return vertices;
	}

	public float[] getNormals() {
		return normals;
	}
	
	public float[] getTangents() {
		return tangents;
	}

	public float[] getTexCoords() {
		return texCoords;
	}

	public float[] getMatProps() {
		return matProps;
	}

	public int[] getIndices() {
		return indices;
	}
	
	public float getAvgX() {
		return avgX;
	}

	public float getAvgY() {
		return avgY;
	}

	public float getAvgZ() {
		return avgZ;
	}

	public static boolean samePlace(VoxelFace face1, VoxelFace face2) {
		return (
				Math.abs(face1.avgX - face2.avgX) < MARGIN &&
				Math.abs(face1.avgY - face2.avgY) < MARGIN &&
				Math.abs(face1.avgZ - face2.avgZ) < MARGIN
		);
	}
}

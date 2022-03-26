package voxelWorld;

import renderEngine.Loader;
import renderEngine.OBJLoader;
import textures.ModelTexture;

// get a textured model for a voxel type
public class VoxelModel {
	public static final float SCALE = 0.5f;
	
	// vertices of a voxel
	public static final float[] VERTICES = {			
			-0.5f,0.5f,-0.5f,	
			-0.5f,-0.5f,-0.5f,	
			0.5f,-0.5f,-0.5f,	
			0.5f,0.5f,-0.5f,		
			
			-0.5f,0.5f,0.5f,	
			-0.5f,-0.5f,0.5f,	
			0.5f,-0.5f,0.5f,	
			0.5f,0.5f,0.5f,
			
			0.5f,0.5f,-0.5f,	
			0.5f,-0.5f,-0.5f,	
			0.5f,-0.5f,0.5f,	
			0.5f,0.5f,0.5f,
			
			-0.5f,0.5f,-0.5f,	
			-0.5f,-0.5f,-0.5f,	
			-0.5f,-0.5f,0.5f,	
			-0.5f,0.5f,0.5f,
			
			-0.5f,0.5f,0.5f,
			-0.5f,0.5f,-0.5f,
			0.5f,0.5f,-0.5f,
			0.5f,0.5f,0.5f,
			
			-0.5f,-0.5f,0.5f,
			-0.5f,-0.5f,-0.5f,
			0.5f,-0.5f,-0.5f,
			0.5f,-0.5f,0.5f
			
	};
	
	// normals of a voxel
	public static final float[] NORMALS = {			
			0,0,-1,	
			0,0,-1,
			0,0,-1,
			0,0,-1,	
			
			0,0,1,	
			0,0,1,
			0,0,1,
			0,0,1,	
			
			1,0,0,	
			1,0,0,
			1,0,0,
			1,0,0,	
			
			-1,0,0,	
			-1,0,0,
			-1,0,0,
			-1,0,0,	
			
			0,1,0,	
			0,1,0,
			0,1,0,
			0,1,0,	
			
			0,-1,0,	
			0,-1,0,
			0,-1,0,
			0,-1,0,	
	};
	
	// tangents of a voxel
	public static final float[] TANGENTS = {						
			1,0,0,	
			1,0,0,
			1,0,0,
			1,0,0,	
			
			-1,0,0,	
			-1,0,0,
			-1,0,0,
			-1,0,0,	
			
			0,1,0,	
			0,1,0,
			0,1,0,
			0,1,0,	
			
			0,-1,0,	
			0,-1,0,
			0,-1,0,
			0,-1,0,	
			
			0,0,-1,	
			0,0,-1,
			0,0,-1,
			0,0,-1,	
			
			0,0,1,	
			0,0,1,
			0,0,1,
			0,0,1,
	};
	
	// texture coordinates of a voxel
	public static final float[] TEXCOORDS = {
			
			0,0,
			0,1,
			1,1,
			1,0,			
			0,0,
			0,1,
			1,1,
			1,0,			
			0,0,
			0,1,
			1,1,
			1,0,
			0,0,
			0,1,
			1,1,
			1,0,
			0,0,
			0,1,
			1,1,
			1,0,
			0,0,
			0,1,
			1,1,
			1,0

			
	};
	
	// indices of a voxel
	public static final int[] INDICES = {
			3,1,0,	
			2,1,3,	
			
			4,5,7,
			7,5,6,
			
			11,9,8,
			10,9,11,
			
			12,13,15,
			15,13,14,	
			
			19,17,16,
			18,17,19,
			
			20,21,23,
			23,21,22
	};
}

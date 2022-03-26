package utils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Player;

public class Maths {
	
	// approximate a point using the barry centric method
	public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}

	// create a 3d transformation matrix
	public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale),  matrix, matrix);
		
		return matrix;
	}
	
	// create a 2d transformation matrix
	public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}
	
	// create the view matrix using the camera
    public static Matrix4f createViewMatrix(Camera camera) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();
        Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix,
                viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
        Vector3f cameraPos = camera.getPosition();
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x,-cameraPos.y,-cameraPos.z);
        Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
        return viewMatrix;
    }
    
	// create the view matrix using the camera
    public static Matrix4f createTranslationlessViewMatrix(Camera camera) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();
        Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix,
                viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
        return viewMatrix;
    }
    
    // rotate 3d vector by an angle in radians along the x axis
    public static Vector3f rotateVectorX(Vector3f vector, float angle) {
    	  float y = (float)(vector.y * Math.cos(angle) - vector.z * Math.sin(angle));

    	  float z = (float)(vector.y * Math.sin(angle) + vector.z * Math.cos(angle)) ;

    	  return new Vector3f(vector.x, y, z);
    }
    
    // rotate 3d vector by an angle in radians along the y axis
    public static Vector3f rotateVectorY(Vector3f vector, float angle) {
    	  float x = (float)(vector.x * Math.cos(angle) - vector.z * Math.sin(angle));

    	  float z = (float)(vector.x * Math.sin(angle) + vector.z * Math.cos(angle)) ;

    	  return new Vector3f(x, vector.y, z);
    }
    
    // rotate 3d vector by an angle in radians along the z axis
    public static Vector3f rotateVectorZ(Vector3f vector, float angle) {
    	  float x = (float)(vector.x * Math.cos(angle) - vector.y * Math.sin(angle));

    	  float y = (float)(vector.x * Math.sin(angle) + vector.y * Math.cos(angle)) ;

    	  return new Vector3f(x, y, vector.z);
    }
}

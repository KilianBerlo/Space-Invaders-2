package shadowMapping;

import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import models.TexturedModel;
import voxelWorld.Light;

public class ShadowMapMasterRenderer {
	// shadow rendering variables
	public static final int SHADOW_MAP_SIZE = 4048;
	public static final int PCFCOUNT = 1;
	public static final float NOISEAMPLIFIER = 1.0f;
	public static final float[] SHADOWBOXDISTANCES = {5.0f, 10.0f, 20.0f, 50.0f};
	
	private ShadowFrameBuffer[] shadowFbo = new ShadowFrameBuffer[4];
	private ShadowShader shader;
	private ShadowBox[] shadowBox = new ShadowBox[4];
	
	public Matrix4f projectionMatrix[] = { new Matrix4f(), new Matrix4f(), new Matrix4f(), new Matrix4f()};
	private Matrix4f lightViewMatrix[] = { new Matrix4f(), new Matrix4f(), new Matrix4f(), new Matrix4f()};
	private Matrix4f projectionViewMatrix[] = { new Matrix4f(), new Matrix4f(), new Matrix4f(), new Matrix4f()};
	private Matrix4f offset = getOffset();

	private ShadowMapEntityRenderer[] entityRenderer = new ShadowMapEntityRenderer[4];

	public ShadowMapMasterRenderer(Camera camera) {
		shader = new ShadowShader();
		
		for(int i = 0; i < 4; i++) {
			shadowBox[i] = new ShadowBox(lightViewMatrix[i], camera, SHADOWBOXDISTANCES[i]);
			shadowFbo[i] = new ShadowFrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
			entityRenderer[i] = new ShadowMapEntityRenderer(shader, projectionViewMatrix[i]);
		}
	}
	
	private boolean doRender = true;
	private boolean mIsDown = false;
	public void render(Map<TexturedModel, List<Entity>> entities, Light sun) {
		for(int i = 0; i < 4; i++) {
			shadowBox[i].update();
			Vector3f sunPosition = sun.getPosition();
			Vector3f lightDirection = new Vector3f(-sunPosition.x, -sunPosition.y, -sunPosition.z);
			prepare(lightDirection, shadowBox[i], i);
			entityRenderer[i].render(entities);
			finish(i);
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_M)) {
			if(!mIsDown) doRender = !doRender;
			
			mIsDown = true;
		} else {
			mIsDown = false;
		}
	}

	public Matrix4f[] getToShadowMapSpaceMatrix() {
		Matrix4f[] result = new Matrix4f[4];

		if(doRender) {
			for(int i = 0; i < 4; i++) {
				result[i] = Matrix4f.mul(offset, projectionViewMatrix[i], null);
			}
		} else {
			// if not rendering, return zero shadowmap matrix
			for(int i = 0; i < 4; i++) {
				result[i] = new Matrix4f();
				result[i].setZero();
			}
		}
		
		return result;
	}

	public void cleanUp() {
		shader.cleanUp();
		
		for(int i = 0; i < 4; i++) {
			shadowFbo[i].cleanUp();
		}
	}

	public int[] getShadowMap() {
		int[] result = new int[4];
		for(int i = 0; i < 4; i++) {
			result[i] = shadowFbo[i].getShadowMap();
		}
		return result;
	}

	public Matrix4f[] getLightSpaceTransform() {	
		return lightViewMatrix;
	}

	private void prepare(Vector3f lightDirection, ShadowBox box, int index) {
		updateOrthoProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength(), index);
		updateLightViewMatrix(lightDirection, box.getCenter(), index);
		
		Matrix4f.mul(projectionMatrix[index], lightViewMatrix[index], projectionViewMatrix[index]); // create matrix
		
		shadowFbo[index].bindFrameBuffer();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		shader.start();
	}

	private void finish(int index) {
		shader.stop();
		shadowFbo[index].unbindFrameBuffer();
	}

	// create/update ortho projection matrix
	private void updateOrthoProjectionMatrix(float width, float height, float length, int index) {
		projectionMatrix[index].setIdentity();
		projectionMatrix[index].m00 = 2f / width;
		projectionMatrix[index].m11 = 2f / height;
		projectionMatrix[index].m22 = -2f / length;
		projectionMatrix[index].m33 = 1;
	}
	
	// view matrix from the light
	private void updateLightViewMatrix(Vector3f direction, Vector3f center, int index) {
		direction.normalise();
		center.negate();
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;
		float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
		
		lightViewMatrix[index].setIdentity();
		Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix[index], lightViewMatrix[index]);
		Matrix4f.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0), lightViewMatrix[index], lightViewMatrix[index]);
		Matrix4f.translate(center, lightViewMatrix[index], lightViewMatrix[index]);
	}

	private static Matrix4f getOffset() {
		Matrix4f offset = new Matrix4f();
		offset.translate(new Vector3f(0.5f, 0.5f, 0.5f));
		offset.scale(new Vector3f(0.5f, 0.5f, 0.5f));
		return offset;
	}
}

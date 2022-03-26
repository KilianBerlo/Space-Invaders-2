package renderEngine;

import shaders.EntityShader;
import shadowMapping.ShadowMapEntityRenderer;
import shadowMapping.ShadowMapMasterRenderer;
import skybox.SkyboxRenderer;
import utils.Maths;
import voxelWorld.BlockLight;
import voxelWorld.Light;
import voxelWorld.VoxelWorld;
import voxelWorld.VoxelWorldShader;
import models.TexturedModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;

import enemy.Enemy;
import enemy.EnemyRenderer;
import enemy.EnemyShader;
import entities.Camera;
import entities.Entity;
import guis.GuiTexture;

public class MasterRenderer {
	public static final float DEFAULT_FOV = 80f;
	public static final float ZOOM_FOV = 60f;
	public static final float NEAR_PLANE = 0.1f;
	public static final float FAR_PLANE = 1000f;
	
	public float fov = DEFAULT_FOV;
	
	// sky rgb
	private static final float RED = 0.2f;
	private static final float GREEN = 0.2f;
	private static final float BLUE = 0.2f;
	
	private EntityShader entityShader = new EntityShader();
	private VoxelWorldShader voxelWorldShader = new VoxelWorldShader();
	private EnemyShader enemyShader = new EnemyShader();
	
	// renderers
	private EntityRenderer entityRenderer;
	private VoxelWorldRenderer voxelWorldRenderer;
	private EnemyRenderer enemyRenderer;
	private GuiRenderer guiRenderer;
	private SkyboxRenderer skyboxRenderer;
	private ShadowMapMasterRenderer shadowMapRenderer;
	
	private Matrix4f projectionMatrix = new Matrix4f();
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel,List<Entity>>();
	private List<Enemy> enemies = new ArrayList<Enemy>();
	private List<Entity> voxelWorlds = new ArrayList<Entity>();
	List<Entity> shadowCasters = new ArrayList<Entity>();
	
	public MasterRenderer(Loader loader, Camera camera) {
		// enable backface culling
		enableCulling();
		
		createProjectionMatrix();
		entityRenderer = new EntityRenderer(entityShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		voxelWorldRenderer = new VoxelWorldRenderer(voxelWorldShader, projectionMatrix);
		enemyRenderer = new EnemyRenderer(enemyShader, projectionMatrix);
		
		guiRenderer = new GuiRenderer(loader);
		shadowMapRenderer = new ShadowMapMasterRenderer(camera);
	}
	
	private void updateProjectionMatrix() {
		voxelWorldRenderer.updateProjectionMatrix(projectionMatrix);
		skyboxRenderer.updateProjectionMatrix(projectionMatrix);
		entityRenderer.updateProjectionMatrix(projectionMatrix);
		enemyRenderer.updateProjectionMatrix(projectionMatrix);
	}
	
	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	public void render(Light sun, ArrayList<BlockLight> arrayList, Camera camera) {
		prepare();
		
		// render voxel world
		voxelWorldShader.start();
		voxelWorldShader.loadSkyColor(RED, GREEN, BLUE);
		voxelWorldShader.loadLights(sun, arrayList, Maths.createViewMatrix(camera));
		voxelWorldShader.loadViewMatrix(camera);
		voxelWorldRenderer.render(voxelWorlds, shadowMapRenderer.getToShadowMapSpaceMatrix());
		voxelWorldShader.stop();
		
		// render entities
		entityShader.start();
		entityShader.loadSkyColor(RED, GREEN, BLUE);
		entityShader.loadLights(sun, arrayList);
		entityShader.loadViewMatrix(camera);		
		entityRenderer.render(entities, shadowMapRenderer.getToShadowMapSpaceMatrix());
		entityShader.stop();
		
		// render enemies
		enemyShader.start();
		enemyShader.loadSkyColor(RED, GREEN, BLUE);
		enemyShader.loadLights(sun, arrayList);
		enemyShader.loadViewMatrix(camera);		
		enemyRenderer.render(enemies, shadowMapRenderer.getToShadowMapSpaceMatrix());
		enemyShader.stop();
		
		
		// skybox
		skyboxRenderer.render(camera);
		
		entities.clear();
		voxelWorlds.clear();
		enemies.clear();
	}
	
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		
		if(batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void processVoxelWorld(Entity world) {
		this.voxelWorlds.add(world);
	}
		
	public void processGuis(List<GuiTexture> guis) {
		guiRenderer.render(guis);
	}
	
	public void renderShadowMap(Light sun) {
		for(Entity entity : this.shadowCasters) {
			processEntity(entity);
		}
		
		shadowMapRenderer.render(entities, sun);
		
		entities.clear();
	}
	
	public float getFov() {
		return this.fov;
	}
	
	public int[] getShadowMapTexture() {
		return shadowMapRenderer.getShadowMap();
	}
	
	public void cleanUp() {
		entityShader.cleanUp();
		guiRenderer.cleanUp();
		enemyShader.cleanUp();
		shadowMapRenderer.cleanUp();
	}
	
	public void processEnemy(Enemy enemy) {
		this.enemies.add(enemy);
	}
	
	public void prepare() {
		// clean up
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClearColor(RED, GREEN, BLUE, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE10);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture()[0]);
		GL13.glActiveTexture(GL13.GL_TEXTURE11);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture()[1]);
		GL13.glActiveTexture(GL13.GL_TEXTURE12);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture()[2]);
		GL13.glActiveTexture(GL13.GL_TEXTURE13);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture()[3]);
	}
	
	public void registerShadowCaster(Entity shadowCaster) {
		this.shadowCasters.add(shadowCaster);
	}
	
	public void deregisterShadowCaster(Entity shadowCaster) {
		this.shadowCasters.remove(shadowCaster);
	}
	
    private void createProjectionMatrix(){
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(fov / 2f))));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;
 
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }
    
    public void changeFov(float fov) {
    	// so it doesnt get too crazy
    	if(fov > DEFAULT_FOV) fov = DEFAULT_FOV;
    	if(fov < ZOOM_FOV) fov = ZOOM_FOV;
    	
    	this.fov = fov;
    	createProjectionMatrix();
    	updateProjectionMatrix();
    }
}

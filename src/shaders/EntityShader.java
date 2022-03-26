package shaders;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import shadowMapping.ShadowMapMasterRenderer;
import utils.Maths;
import voxelWorld.BlockLight;
import voxelWorld.Light;

public class EntityShader extends ShaderProgram {
	private static final int MAX_LIGHTS= 4;
	
	private static final String VERTEX_FILE = "/shaders/entityVertexShader.txt";
	private static final String FRAGMENT_FILE = "/shaders/entityFragmentShader.txt";

	private int location_transformation;
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_sunLight;
	private int location_lightPosition[]; // for each light
	private int location_lightColor[]; // for each light
	private int location_attenuation[]; // for each light
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_useFakeLighting;
	private int location_skyColor;
	private int location_toShadowMapSpace[];
	private int location_shadowMap[];
	private int location_textureSampler;
	private int location_shadowMapDistances[];
	private int location_pcfCount;
	private int location_noiseAmplifier;
	private int location_shadowMapSize;
	private int location_doShading;
	private int location_whiteBlending;
	
	public EntityShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
		super.bindAttribute(2, "normal");
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformation = super.getUniformLocation("transformationMatrix");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_shineDamper = super.getUniformLocation("shineDamper");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_useFakeLighting = super.getUniformLocation("useFakeLighting");
		location_skyColor = super.getUniformLocation("skyColor");
		location_textureSampler = super.getUniformLocation("textureSampler");
		
		location_lightPosition = new int[MAX_LIGHTS];
		location_lightColor = new int[MAX_LIGHTS];
		location_attenuation = new int[MAX_LIGHTS];
		for(int i = 0; i < MAX_LIGHTS; i++) {
			location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
			location_lightColor[i] = super.getUniformLocation("lightColor[" + i + "]");
			location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
		}
		location_sunLight = super.getUniformLocation("sunLight");
		
		location_shadowMap = new int[4];
		location_shadowMapDistances = new int[4];
		location_toShadowMapSpace = new int[4];
		for(int i = 0; i < 4; i++) {
			location_shadowMap[i] = super.getUniformLocation("shadowMap[" + i + "]");
			location_shadowMapDistances[i] = super.getUniformLocation("shadowMapDistances[" + i + "]");
			location_toShadowMapSpace[i] = super.getUniformLocation("toShadowMapSpace[" + i + "]");
		}
		
		location_pcfCount = super.getUniformLocation("pcfCount");
		location_noiseAmplifier = super.getUniformLocation("noiseAmplifier");
		location_shadowMapSize = super.getUniformLocation("shadowMapSize");
		
		location_doShading = super.getUniformLocation("doShading");
		location_whiteBlending = super.getUniformLocation("whiteBlending");
	}
	
	public void connectTextures() {
		super.loadInt(location_shadowMap[0], 10);
		super.loadInt(location_shadowMap[1], 11);
		super.loadInt(location_shadowMap[2], 12);
		super.loadInt(location_shadowMap[3], 13);
		
		super.loadInt(location_textureSampler, 0);
	}
	
	public void loadSkyColor(float r, float g, float b) {
		super.loadVector(location_skyColor, new Vector3f(r,g,b));
	}
	
	public void loadDoShading(boolean doShading) {
		super.loadBoolean(location_doShading, doShading);
	}
	
	public void loadWhiteBlending(float lightBlending) {
		super.loadFloat(location_whiteBlending, lightBlending);
	}
	
	public void loadFakeLightingVariable(boolean useFake) {
		this.loadBoolean(location_useFakeLighting, useFake);
	}
	
	public void loadShineVariables(float damper, float reflectivity) {
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}
	
	public void loadTransformationMatrix(Matrix4f matrix) {
		super.loadMatrix(location_transformation, matrix);
	}
	
	public void loadToShadowSpaceMatrix(Matrix4f[] toShadowSpace) {
		for(int i = 0; i < 4; i++) {
			super.loadMatrix(location_toShadowMapSpace[i], toShadowSpace[i]);
		}
	}
	
	public void loadLights(Light sun, ArrayList<BlockLight> arrayList) {
		super.loadVector(location_sunLight, sun.getPosition());
		
		for(int i = 0; i < MAX_LIGHTS; i++) {
			if(i < arrayList.size()) {
				super.loadVector(location_lightPosition[i], arrayList.get(i).getPosition());
				super.loadVector(location_lightColor[i], arrayList.get(i).getColor());
				super.loadVector(location_attenuation[i], arrayList.get(i).getAttenuation());
			} else {
				// need to load something
				super.loadVector(location_lightPosition[i], new Vector3f(0, 0, 0));
				super.loadVector(location_lightColor[i], new Vector3f(0, 0, 0));
				super.loadVector(location_attenuation[i], new Vector3f(1, 0, 0)); // if its zero, its gonna divide by 0, which is bad news
			}
		}
	}
	
	public void loadShadowMapUniforms() {
		for(int i = 0; i < 4; i++) {
			super.loadFloat(location_shadowMapDistances[i], ShadowMapMasterRenderer.SHADOWBOXDISTANCES[i]);
		}
		
		super.loadInt(location_pcfCount, ShadowMapMasterRenderer.PCFCOUNT);
		super.loadFloat(location_noiseAmplifier, ShadowMapMasterRenderer.NOISEAMPLIFIER);
		super.loadFloat(location_shadowMapSize, ShadowMapMasterRenderer.SHADOW_MAP_SIZE);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix) {
		super.loadMatrix(location_projectionMatrix, matrix);
	}
	
	public void loadViewMatrix(Camera camera) {
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}
}

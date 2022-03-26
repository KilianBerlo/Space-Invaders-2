package voxelWorld;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jnbt.CompoundTag;
import org.jnbt.IntTag;
import org.jnbt.ListTag;
import org.jnbt.NBTInputStream;
import org.jnbt.StringTag;
import org.jnbt.Tag;
import org.lwjgl.util.vector.Vector3f;

import audioEngine.AudioSource;
import enemy.Enemy;
import entities.Coin;
import entities.Entity;
import entities.Player;
import entities.Voxel;
import models.RawModel;
import models.TexturedModel;
import pathfinding.VoxelGraph;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import skybox.SkyboxShader;
import textures.ModelTexture;
import utils.KdTree;
import utils.KdTree.Entry;
import utils.Maths;

import java.util.Timer;

public class VoxelWorld {
	private static final String TEXTUREPACK = "textureAtlas";
	private static final String TEXTURENORMALSPACK = "textureNormalsAtlas";
	private static final int TEXTUREPACKROWS = 8;
	private static final int HITBOXESTOFIND = 500;
	private static final int KDTREE_LIMIT = 100000;
	private static final String COIN_MODEL = "coin";
	private static final String COIN_TEXTURE = "coin";
	private static final boolean REUSE_OBJECTIVE_SPAWNS = false; // if true, coins respawn on already used spawnpoints

	private static final float OBJECTIVES_COLLECTED_TO_CAP_RATIO = 2; // 2 enemies in the enemy spanw cap per objective that the player has collected
	private static final float AVG_ENEMY_SPAWNS_PER_SECOND = 0.5f; // while under limit
	private static final float AVG_COIN_SPAWNS_PER_SECOND = 0.75f; // while under limit (limit is number of spawnpoints)
	
	private static final Vector3f BLOCK_LIGHT_ATTENUATION= new Vector3f(1, -0.3f, 0.1f);
	
	private Vector3f origin; // origin is where the map is centered on
	private Vector3f spawnPoint = new Vector3f(0, 0, 0);
	private List<Vector3f> spawnPointList = new ArrayList<Vector3f>(); // spawnPoint is chosen from this
	
	private Player player;
	private VoxelGraph graph;
	
	private VoxelHitbox[] voxelHitboxes;
	private TexturedModel worldModel;
	private TexturedModel transparentWorldModel;
	private TexturedModel brightWorldModel;
	private Entity worldEntity;
	private Entity transparentWorldEntity;
	private Entity brightWorldEntity;
	
	private KdTree<VoxelHitbox> tree;
	
	private Light sun;
	private ArrayList<BlockLight> spotLights = new ArrayList<BlockLight>();
	
	private String file;

	private Loader loader; 
	
	private List<Vector3f> enemySpawnPoints = new ArrayList<Vector3f>();
	private List<Enemy> enemies = new ArrayList<Enemy>();
	private float enemySpawnCumsum = 0.0f;
	private Random random = new Random();
	private TexturedModel enemyModel;
	private AudioSource enemyAttack;
	private AudioSource enemyHit;
	
	private List<Vector3f> objectiveSpawnPoints = new ArrayList<Vector3f>();
	private List<Boolean> objectiveSpawnPointsIsUsed = new ArrayList<Boolean>();
	private List<Coin> coins = new ArrayList<Coin>();
	private float coinSpawnCumsum = 0.0f;
	private TexturedModel coinModel;
	private AudioSource coinPickup;
	
	public VoxelWorld(Vector3f origin, String file, Loader loader) {
		this.origin = origin;
		this.file = file;
		this.loader = loader;
		
		System.out.println("Starting loading world");
		this.loadWorld(loader);
		
		System.out.println("Creating hitbox tree");
		tree = new KdTree.SqrEuclid<VoxelHitbox>(3, KDTREE_LIMIT);
		for(VoxelHitbox hitbox : voxelHitboxes) {
			tree.addPoint(new double[] {hitbox.centerPos.x, hitbox.centerPos.y, hitbox.centerPos.z}, hitbox);
		}
		
		sun = new Light(new Vector3f(4912.7716f, 6819.9664f, 5417.8166f), new Vector3f(1.0f, 1.0f, 1.0f)); // white light
		
		// load slime model
		ModelTexture slimeTexture = new ModelTexture(loader.loadTexture(Enemy.TEXTURE_FILE));
		slimeTexture.setReflectivity(1);
		slimeTexture.setShineDamper(5);
		enemyModel = new TexturedModel(OBJLoader.loadObjModel(Enemy.MODEL_FILE, loader), slimeTexture);
		
		// load coin model
		ModelTexture coinTexture = new ModelTexture(loader.loadTexture(COIN_TEXTURE));
		coinTexture.setReflectivity(1);
		coinTexture.setShineDamper(5);
		coinModel = new TexturedModel(OBJLoader.loadObjModel(COIN_MODEL, loader), coinTexture);
	
		
	}
	
	public void createGraph() {
		graph = new VoxelGraph(this, this.getPlayer());
	}
	
	public void setSounds(AudioSource enemyAttack, AudioSource enemyHit, AudioSource coinPickup) {
		this.enemyAttack = enemyAttack;
		this.enemyHit = enemyHit;
		this.coinPickup = coinPickup;
	}
	
	public void reset(Player player) {
		player.setPosition(getSpawnPoint());
		
		for(Enemy enemy : enemies) {
			enemy.isDie = true;
		}
		
		for(Coin coin : coins) {
			coin.isDie = true;
		}
		
		for(int i = 0 ; i < objectiveSpawnPointsIsUsed.size(); i++) {
			objectiveSpawnPointsIsUsed.set(i, false);
		}
		
		chooseSpawnpoint();
	}
	
	public void update(Player player) {
		doEnemySpawningTick();
		doObjectiveSpawningTick();
		
		for(BlockLight spotlight : spotLights) {
			spotlight.update();
		}
		
		graph.changePlayerVertex();
		for(Enemy enemy : enemies) {
			enemy.update(player);
		}
		
		for(Coin coin : coins) {
			coin.update(player);
		}
	}
	
	public List<Enemy> getEnemies() {
		return enemies;
	}
	
	private void doObjectiveSpawningTick() {
		if(coins.size() >= objectiveSpawnPoints.size()) return;
		
		coinSpawnCumsum += AVG_COIN_SPAWNS_PER_SECOND * DisplayManager.getDeltaTimeSeconds();
		
		if(coinSpawnCumsum >= 1) {
			int toSpawn = (int) Math.floor(coinSpawnCumsum);
			
			
			// spawn toSpawn coin
			for(int i = 0; i < toSpawn; i++) {
				int spawnBlockIndex = random.nextInt(objectiveSpawnPoints.size());
				Vector3f spawnPos = objectiveSpawnPoints.get(spawnBlockIndex);
				
				if(objectiveSpawnPointsIsUsed.get(spawnBlockIndex)) continue;
				
				coins.add(new Coin(coinModel, spawnPos, this, loader, spawnBlockIndex, coinPickup));
				objectiveSpawnPointsIsUsed.set(spawnBlockIndex, true);
				coinSpawnCumsum--;
			}
		}
		
		return;
	}
	
	private void doEnemySpawningTick() {
		if(enemies.size() >= player.objectivesCollected * OBJECTIVES_COLLECTED_TO_CAP_RATIO) return;
		
		enemySpawnCumsum += AVG_ENEMY_SPAWNS_PER_SECOND * DisplayManager.getDeltaTimeSeconds();
		
		if(enemySpawnCumsum >= 1) {
			int toSpawn = (int) Math.floor(enemySpawnCumsum);
			enemySpawnCumsum -= toSpawn;
			
			// spawn toSpawn enemies
			for(int i = 0; i < toSpawn; i++) {
				int spawnBlockIndex = random.nextInt(enemySpawnPoints.size());
				Vector3f spawnPos = enemySpawnPoints.get(spawnBlockIndex);
				enemies.add(new Enemy(enemyModel, spawnPos, this, loader, enemyHit, enemyAttack, graph));
			}
		}
		
		return;
	}
	
	public VoxelHitbox[] getVoxelHitboxes() { // return list of all the voxel hitboxes
		return this.voxelHitboxes;
	}
	
	public Light getSun() {
		return sun;
	}
	
	public ArrayList<BlockLight> getClosestLights(Player player) {
		spotLights.sort(new Comparator<Light>() {

			@Override
			public int compare(Light arg0, Light arg1) {
				float l0 = Vector3f.sub(arg0.getPosition(), player.getPosition(), null).length();
				float l1 = Vector3f.sub(arg1.getPosition(), player.getPosition(), null).length();
				return (int) (l0 - l1);
			}
		});

		return spotLights;
	}
	
	private List<VoxelHitbox> getNearHitboxes(Vector3f position) {
		List<VoxelHitbox> result = new ArrayList<VoxelHitbox>();
		
		double[] pos = new double[] {position.x, position.y, position.z};
		List<Entry<VoxelHitbox>> queryResult = tree.nearestNeighbor(pos, HITBOXESTOFIND, true);
		
		for(Entry<VoxelHitbox> entry : queryResult) {
			result.add(entry.value);
		}
		
		return result;
	}
	
	public TexturedModel getModel() { // get the world model
		return worldModel;
	}
	
	public TexturedModel getTransparentWorldModel() { // get the world model
		return transparentWorldModel;
	}
	
	public Vector3f getSpawnPoint() {
		return new Vector3f(spawnPoint.x + origin.x, spawnPoint.y + origin.y + VoxelModel.SCALE / 2, spawnPoint.z + origin.z);
	}
	
	public Vector3f getEnemySpawnPoint() {
		return new Vector3f(spawnPoint.x + origin.x - 5, spawnPoint.y + origin.y + VoxelModel.SCALE / 2, spawnPoint.z + origin.z);
	}
	
	public void render(MasterRenderer renderer) {
		sun.setPosition(Maths.rotateVectorY(sun.getPosition(), (float) -Math.toRadians(360f/SkyboxShader.ROTATION_PERIOD * DisplayManager.getDeltaTimeSeconds())));
		
		for(BlockLight spotlight : spotLights) {
			renderer.processEntity(spotlight.getBlockEntity());
		}
		
		List<Enemy> toDeleteEnemy = new ArrayList<Enemy>();
		for(Enemy enemy : enemies) {
			renderer.processEnemy(enemy);
			
			if(!enemy.isRegisteredShadowCaster) {
				renderer.registerShadowCaster(enemy);
				enemy.isRegisteredShadowCaster = true;
			}
			
			if(enemy.isDie) toDeleteEnemy.add(enemy);
		}
		
		
		for(Enemy enemy : toDeleteEnemy) {
			enemies.remove(enemy);
			renderer.deregisterShadowCaster(enemy);
		}
		
		List<Coin> toDeleteCoin = new ArrayList<Coin>();
		for(Coin coin : coins) {
			renderer.processEntity(coin);
			
			if(!coin.isRegisteredShadowCaster) {
				renderer.registerShadowCaster(coin);
				coin.isRegisteredShadowCaster = true;
			}
			
			if(coin.isDie) toDeleteCoin.add(coin);
		}
		
		
		for(Coin coin : toDeleteCoin) {
			coins.remove(coin);
			objectiveSpawnPointsIsUsed.set(coin.listIndex, !REUSE_OBJECTIVE_SPAWNS); // this space is available again for spawning
			renderer.deregisterShadowCaster(coin);
		}
		
		renderer.processVoxelWorld(worldEntity);
		renderer.processVoxelWorld(transparentWorldEntity);
	}
	
	public TexturedModel getBrightWorldModel() { // get the world model
		return brightWorldModel;
	}
	
	public void loadWorld(Loader loader) {
		ArrayList<Voxel> world = new ArrayList<Voxel>();
		ArrayList<Voxel> transparentWorld = new ArrayList<Voxel>();
		ArrayList<Voxel> brightWorld = new ArrayList<Voxel>();
		
        try {
            File f = new File("res/schematics/" + file + ".nbt");
            FileInputStream fis = new FileInputStream(f);
            NBTInputStream nbt = new NBTInputStream(fis);
            System.out.println("	nbt stream loaded, starting parsing");
            Map<String, Tag> tags = ((CompoundTag) nbt.readTag()).getValue();
            List<Tag> blocks = ((ListTag) tags.get("blocks")).getValue();
            List<Tag> rawPallete = ((ListTag) tags.get("palette")).getValue();
            VoxelType[] pallete = createPallete(rawPallete);
            
            System.out.println("	Parsing nbt file...");
            for(Tag block : blocks) {
            	parseBlock(block, world, transparentWorld, brightWorld, pallete);
            }

            nbt.close();
            fis.close();
        } catch (Exception e) {
        	System.out.println("Error: Could not load schematic.");
            e.printStackTrace();
        }
        
		// finally process the created voxels
        System.out.println("	Creating voxel lists");
		this.worldModel = this.addVoxels(world, loader, false);
		this.transparentWorldModel = this.addVoxels(transparentWorld, loader, false);
		this.brightWorldModel = this.addVoxels(brightWorld, loader, true);
		
		world.addAll(transparentWorld);
		world.addAll(brightWorld);
		System.out.println("	Creating hitboxes...");
		this.createHitboxes(world);
		
		worldEntity = new Entity(worldModel, new Vector3f(0, 0, 0), 0, 0, 0, 1);
		transparentWorldEntity = new Entity(transparentWorldModel, new Vector3f(0, 0, 0), 0, 0, 0, 1);
		brightWorldEntity = new Entity(brightWorldModel, new Vector3f(0, 0, 0), 0, 0, 0, 1);
		
		chooseSpawnpoint();
    }
	
	private void chooseSpawnpoint() {
		spawnPoint = spawnPointList.get(random.nextInt(spawnPointList.size()));
	}
	
	public float[] getAxisBounds(Vector3f corner1, Vector3f corner2, Vector3f position) {
		// ranges of availability for the axis. [xLeftMax,xRightMin] is the range of movement along the x-axis possible at this location
		float xLeftMax = -10.0e10f;
		float xRightMin = 10.0e10f;
		float yLeftMax = -10.0e10f;
		float yRightMin = 10.0e10f;
		float zLeftMax = -10.0e10f;
		float zRightMin = 10.0e10f;
		
		List<VoxelHitbox> hitboxes = getNearHitboxes(position);
		// calculate the ranges along each axis
		for(VoxelHitbox hitbox : hitboxes) {
			// find overlapping part per interval
			boolean xOverlap = hitbox.xOverlap(corner1.x + 0.05f, corner2.x - 0.05f); // add a small amount to avoid weirdness
			boolean yOverlap = hitbox.yOverlap(corner1.y + 0.05f, corner2.y - 0.05f);
			boolean zOverlap = hitbox.zOverlap(corner1.z + 0.05f, corner2.z - 0.05f);
			
			if(xOverlap && yOverlap) { // if this voxel is aligned along the other two axis, then consider updating the interval
				if(zLeftMax < hitbox.corner2.z && hitbox.corner2.z <= corner1.z) zLeftMax = hitbox.corner2.z;
				if(zRightMin > hitbox.corner1.z && hitbox.corner1.z >= corner2.z) zRightMin = hitbox.corner1.z;
			}
			
			if(xOverlap && zOverlap) { // if this voxel is aligned along the other two axis, then consider updating the interval
				if(yLeftMax < hitbox.corner2.y && hitbox.corner2.y <= corner1.y) yLeftMax = hitbox.corner2.y;
				if(yRightMin > hitbox.corner1.y && hitbox.corner1.y >= corner2.y) yRightMin = hitbox.corner1.y;
			}
			
			if(yOverlap && zOverlap) { // if this voxel is aligned along the other two axis, then consider updating the interval
				if(xLeftMax < hitbox.corner2.x && hitbox.corner2.x <= corner1.x) xLeftMax = hitbox.corner2.x;
				if(xRightMin > hitbox.corner1.x && hitbox.corner1.x >= corner2.x) xRightMin = hitbox.corner1.x;
			}
		}
		
		return new float[] {xLeftMax, xRightMin, yLeftMax, yRightMin, zLeftMax, zRightMin};
	}
	
	private TexturedModel addVoxel(Voxel voxel, Loader loader, boolean isLights) {
		ArrayList<Voxel> voxelInput = new ArrayList<Voxel>();
		voxelInput.add(voxel);
		
		return this.addVoxels(voxelInput, loader, isLights);
	}
	
	// creates the worldModel and voxelhitboxes list
	protected TexturedModel addVoxels(List<Voxel> voxels, Loader loader, boolean isLights) {	
		ArrayList<VoxelFace> faces = generateFaces(voxels);
		faces = cullFaces(faces); //remove unneeded faces
		
		ModelTexture texturePack = new ModelTexture(loader.loadTexture(TEXTUREPACK));
		texturePack.setNormalMapID(loader.loadTexture(TEXTURENORMALSPACK));
		texturePack.setNumberOfRows(TEXTUREPACKROWS);
		
		float[] vertices = new float[faces.size() * 4 * 3];
		float[] normals = new float[faces.size() * 4 * 3];
		float[] tangents = new float[faces.size() * 4 * 3];
		float[] texCoords = new float[faces.size() * 4 * 2];
		float[] matProps = new float[faces.size() * 4 * 2];
		int[] indices = new int[faces.size() * 6]; // 6 for each face
		
		// add each face
		for(int i = 0; i < faces.size(); i++) {
			for(int j = 0; j < 4 * 3; j++) {
				vertices[i * 4 * 3 + j] = faces.get(i).getVertices()[j];
				normals[i * 4 * 3 + j] = faces.get(i).getNormals()[j];
				tangents[i * 4 * 3 + j] = faces.get(i).getTangents()[j];
			}
			
			for(int j = 0; j < 4 * 2; j++) {
				texCoords[i * 4 * 2 + j] = faces.get(i).getTexCoords()[j];
				matProps[i * 4 * 2 + j] = faces.get(i).getMatProps()[j];
			}
			
			for(int j = 0; j < 6; j++) {
				indices[i * 6 + j] = faces.get(i).getIndices()[j] + i * 4; // adjust indices
			}
		}

		RawModel model = loader.loadToVAO(vertices, texCoords, matProps, tangents, normals, indices);

		return new TexturedModel(model, texturePack);
	}
	
	// populate the voxel hitboxes array for each voxel
	protected void createHitboxes(List<Voxel> voxels) {
		voxelHitboxes = new VoxelHitbox[voxels.size()];
		Voxel voxel;
		for(int i = 0; i < voxels.size(); i++) {
			voxel = voxels.get(i);
			voxelHitboxes[i] = new VoxelHitbox(voxel.getPosition(), voxel.getSize(), VoxelModel.SCALE);
		}
	}
	
	private ArrayList<VoxelFace> generateFaces(List<Voxel> voxels) {
		ArrayList<VoxelFace> faces = new ArrayList<VoxelFace>();
		
		for(Voxel voxel : voxels) {
			addFaces(voxel, faces);
		}
		
		return faces;
	}
	
	private void addFaces(Voxel voxel, List<VoxelFace> faces) {
		float[] vertices = new float[24 * 3];
		float[] normals = new float[24 * 3];
		float[] tangents = new float[24 * 3];
		float[] texCoords = new float[24 * 2];
		float[] matProps = new float[24 * 2];
		int[] indices = new int[36];
		
		// copy and adjust vertices
		for(int i = 0; i < VoxelModel.VERTICES.length; i += 3) {
			vertices[i] = VoxelModel.VERTICES[i] * voxel.getSize() + origin.x + voxel.getPosition().x;
			vertices[i + 1] = VoxelModel.VERTICES[i + 1] * voxel.getSize() + origin.y + voxel.getPosition().y;
			vertices[i + 2] = VoxelModel.VERTICES[i + 2] * voxel.getSize() + origin.z + voxel.getPosition().z;
		}
		
		// copy normals
		for(int i = 0; i < VoxelModel.NORMALS.length; i += 3) {
			normals[i] = VoxelModel.NORMALS[i];
			normals[i + 1] = VoxelModel.NORMALS[i + 1];
			normals[i + 2] = VoxelModel.NORMALS[i + 2];
		}
		
		// copy tangents
		for(int i = 0; i < VoxelModel.TANGENTS.length; i += 3) {
			tangents[i] = VoxelModel.TANGENTS[i];
			tangents[i + 1] = VoxelModel.TANGENTS[i + 1];
			tangents[i + 2] = VoxelModel.TANGENTS[i + 2];
		}
		
		int row = voxel.getVoxelType().textureNumber / TEXTUREPACKROWS;
		int column = voxel.getVoxelType().textureNumber % TEXTUREPACKROWS;
		float texScale = (1f / TEXTUREPACKROWS);
		float u = column * texScale;
		float v = row * texScale;
		
		// copy and adjust textures
		for(int i = 0; i < VoxelModel.TEXCOORDS.length; i += 2) {
			texCoords[i] = VoxelModel.TEXCOORDS[i] * texScale + u;
			texCoords[i + 1] = VoxelModel.TEXCOORDS[i + 1] * texScale + v;
			
			// load material properties
			matProps[i] = voxel.getVoxelType().reflectivity;
			matProps[i + 1] = voxel.getVoxelType().shineDamper;
		}
		
		// copy and adjust indices
		for(int i = 0; i < VoxelModel.INDICES.length; i++) {
			indices[i] = VoxelModel.INDICES[i];
		}
		
		for(int i = 0; i < 6; i++) {
			float[] faceVertices = new float[12];
			float[] faceNormals = new float[12];
			float[] faceTangents = new float[12];
			float[] faceTexCoords = new float[8];
			float[] faceMatProps = new float[8];
			int[] faceIndices = new int[6];
			
			for(int j = 0; j < 12; j++) {
				faceVertices[j] = vertices[i * 12 + j];
				faceNormals[j] = normals[i * 12 + j];
				faceTangents[j] = tangents[i * 12 + j];
			}
			for(int j = 0; j < 8; j++) {
				faceTexCoords[j] = texCoords[i * 8 + j];
				faceMatProps[j] = matProps[i * 8 + j];
			}
			for(int j = 0; j < 6; j++) {
				faceIndices[j] = indices[i * 6 + j] - 4 * i; // adjust for individual faces
			}
			
			faces.add(new VoxelFace(
				faceVertices,
				faceNormals,
				faceTangents,
				faceTexCoords,
				faceMatProps,
				faceIndices
			));
		}
	}

	// remove unneeded faces
	private ArrayList<VoxelFace> cullFaces(List<VoxelFace> faces) {
		System.out.println("	Culling faces");
		
		ArrayList<VoxelFace> culledFaces = new ArrayList<VoxelFace>();

		KdTree<VoxelFace> facesTree = new KdTree.SqrEuclid<VoxelFace>(3, 1000000);
		for(VoxelFace face : faces) {
			facesTree.addPoint(new double[] {face.getAvgX(), face.getAvgY(), face.getAvgZ()}, face);
		}
		
		for(VoxelFace face1 : faces) {
			if(face1.good) { // if a candidate for not culling, then check it
				List<KdTree.Entry<VoxelFace>> neighbours = facesTree.nearestNeighbor(new double[] {face1.getAvgX(), face1.getAvgY(), face1.getAvgZ()}, 5, false);
				for(KdTree.Entry<VoxelFace> neighbour : neighbours) {
					if(face1 != neighbour.value && VoxelFace.samePlace(face1, neighbour.value)) {
						neighbour.value.good = false;
						face1.good = false;
					}
				}
			}

			if(face1.good) culledFaces.add(face1);
		}
		return culledFaces;
	}
	
    private void parseBlock(Tag block, List<Voxel> world, List<Voxel> transparentWorld, List<Voxel> brightWorld, VoxelType[] pallete) {
    	Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    	
    	Map<String, Tag> blockTags = ((CompoundTag) block).getValue();
    	ListTag list = (ListTag) blockTags.get("pos");
    	IntTag state = (IntTag) blockTags.get("state");
    	List<Tag> coordinates = list.getValue();
    	
    	position.x = ((IntTag) coordinates.get(0)).getValue() * VoxelModel.SCALE;
    	position.y = ((IntTag) coordinates.get(1)).getValue() * VoxelModel.SCALE;
    	position.z = ((IntTag) coordinates.get(2)).getValue() * VoxelModel.SCALE;
    	
    	VoxelType type = pallete[state.getValue()];
    	if(type != null) {
    		if(isLightBlock(type)) {
    			Voxel blockLightVoxel = new Voxel(type, position, VoxelModel.SCALE);
    			Entity blockLightEntity = new Entity(this.addVoxel(blockLightVoxel, loader, true), new Vector3f(0, 0, 0), 0, 0, 0, 1);
    			BlockLight blockLight = new BlockLight(position, getLightColor(type), BLOCK_LIGHT_ATTENUATION, getLightDarkmakerFactor(type), blockLightEntity);
    			brightWorld.add(blockLightVoxel);
    			spotLights.add(blockLight);
    		} else if(type == VoxelType.GLASSBLOCK) {
    			transparentWorld.add(new Voxel(type, position, VoxelModel.SCALE));
    		} else {
    			world.add(new Voxel(type, position, VoxelModel.SCALE));
    		}
    		
    		if(type == VoxelType.LAPISBLOCK) {
    			spawnPointList.add(new Vector3f(position.x, position.y + 2, position.z));
    		}
    		
    		if(type == VoxelType.ALIENSPAWN) {
    			enemySpawnPoints.add(new Vector3f(position.x, position.y + 2, position.z));
    		}
    		
    		if(type == VoxelType.WHITEBLOCK) {
    			objectiveSpawnPoints.add(new Vector3f(position.x, position.y + 1, position.z));
    			objectiveSpawnPointsIsUsed.add(false);
    		}
    	}
    }
    
    private Vector3f getLightColor(VoxelType type) {    	
    	switch(type) {
			case GREENWOOL:
				return new Vector3f(0, 2f, 0);
			case BLUEWOOL:
				float blue = 1;
				return new Vector3f(0, blue, blue);
			case REDWOOL:
				return new Vector3f(5f, 0, 0);
			case YELLOWWOOL:
				float yellow = 1;
				return new Vector3f(yellow, yellow, 0);
			case WHITEWOOL:
				float white = 2;
				return new Vector3f(white, white, white);
    	}
    	
    	return new Vector3f(1,1,1);
    }
    
    private float getLightDarkmakerFactor(VoxelType type) {    	
    	switch(type) {
			case GREENWOOL:
				return 1.0f;
			case BLUEWOOL:
				return 0.8f;
			case REDWOOL:
				return 0.6f;
			case YELLOWWOOL:
				return 0.6f;
			case WHITEWOOL:
				return 1.0f;
    	}
    	
    	return 1.0f;
    }
    
    private VoxelType[] createPallete(List<Tag> rawPalette) {
    	VoxelType[] palette = new VoxelType[rawPalette.size()];
    	
        for(int i = 0; i < rawPalette.size(); i++) {
        	palette[i] = getVoxelType(rawPalette.get(i));
        }
    	
    	return palette;
    }
    
    private VoxelType getVoxelType(Tag type) {
    	Map<String, Tag> subTags = ((CompoundTag) type).getValue();
    	StringTag rawName = (StringTag) subTags.get("Name");
    	String name = rawName.getValue();
    	
    	switch(name) {
    		case "minecraft:air":
    			return null;
    		case "minecraft:iron_block":
    			return VoxelType.IRONBLOCK;
    		case "minecraft:gold_block":
    			return VoxelType.GOLDBLOCK;
    		case "minecraft:diamond_block":
    			return VoxelType.DIAMONDBLOCK;
    		case "minecraft:coal_block":
    			return VoxelType.COALBLOCK;
    		case "minecraft:redstone_block":
    			return VoxelType.REDSTONEBLOCK;
    		case "minecraft:quartz_block":
    			return VoxelType.WHITEBLOCK;
    		case "minecraft:lapis_block":
    			return VoxelType.LAPISBLOCK;
    		case "minecraft:glass":
    			return VoxelType.GLASSBLOCK;
    		case "minecraft:iron_ore":
    			return VoxelType.IRONORE;
    		case "minecraft:blue_wool":
    			return VoxelType.BLUEWOOL;
    		case "minecraft:red_wool":
    			return VoxelType.REDWOOL;
    		case "minecraft:yellow_wool":
    			return VoxelType.YELLOWWOOL;
    		case "minecraft:green_wool":
    			return VoxelType.GREENWOOL;
    		case "minecraft:white_wool":
    			return VoxelType.WHITEWOOL;
    		case "minecraft:stone_bricks":
    			return VoxelType.SHUTTERDOOR;
    		case "minecraft:diamond_ore":
    			return VoxelType.DIAMONDORE;
    		case "minecraft:oak_planks":
    			return VoxelType.PURPLEGOO;
    		case "minecraft:dirt":
    			return VoxelType.ALIENSPAWN;
    		case "minecraft:dark_prismarine":
    			return VoxelType.SCREEN;
    		case "minecraft:gold_ore":
    			return VoxelType.GOLDORE;
    		case "minecraft:obsidian":
    			return VoxelType.MOONBLACK;
    		case "minecraft:diorite":
    			return VoxelType.MOONWHITE;
    		case "minecraft:bedrock":
    			return VoxelType.BEDROCK;
		}
    	
    	return VoxelType.WHITEBLOCK; // default
    }
    
    private boolean isLightBlock(VoxelType type) {
    	return (
			type == VoxelType.REDWOOL ||	
			type == VoxelType.BLUEWOOL ||
			type == VoxelType.GREENWOOL ||
			type == VoxelType.YELLOWWOOL ||
			type == VoxelType.WHITEWOOL
		);
    }

	public Entity getWorldEntity() {
		return worldEntity;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Entity getBrightWorldEntity() {
		return brightWorldEntity;
	}

	public Entity getTransparentWorldEntity() {
		return transparentWorldEntity;
	}
}

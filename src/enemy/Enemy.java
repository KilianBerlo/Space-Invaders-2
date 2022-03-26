package enemy;

import java.util.Random;

import javax.swing.text.Position;

import org.lwjgl.util.vector.Vector3f;

import audioEngine.AudioSource;
import entities.Entity;
import entities.Player;
import models.TexturedModel;
import pathfinding.PathFinding;
import pathfinding.VoxelGraph;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.OBJLoader;
import textures.ModelTexture;
import voxelWorld.VoxelWorld;

public class Enemy extends Entity {
	public static final String TEXTURE_FILE = "slimer";
	public static final String MODEL_FILE = "slimersub";
	private static final float SCALE = 0.5f;
	private static final float BOBBLE_AMPLITUDE = 0.1f; // meters
	private static final float BOBBLE_PERIOD = 3f; // seconds
	private static final float DEFAULT_SPEED = 2.5f; // meters per second
	private static final float MAX_HEALTH = 100f;
	private static final float DMG_COLOR_TIME = 0.075f; // seconds it discolors after being hit 
	private static final float MELEE_DAMAGE = 10f;
	private static final float MELEE_RANGE = 1f; // meters
	private static final float MELEE_COOLDOWN = 1.5f; // seconds
	private static final float ASTAR_COOLDOWN = 0.1f; // seconds
	
	private float timeSinceMelee = 100f;
	
	private float health = MAX_HEALTH;
	
	private float phase = 0;
	private float originalY;
	
	private float morphingPhase = 0;
	
	private VoxelWorld world;
	private VoxelGraph graph;
	private PathFinding pathFinding;
	
	private float timeSinceHit = 100;
	
	public boolean isRegisteredShadowCaster = false;
	
	public boolean isDie = false;
	private boolean isWalking = false;
	private float timeSinceAStar = ASTAR_COOLDOWN;
	private float walkYRot = 0f;
	
	private boolean wallClose;
	
	private AudioSource hit;
	private AudioSource attack;
	
	private float speed;

	public Enemy(TexturedModel model, Vector3f position, VoxelWorld world, Loader loader, AudioSource hit, AudioSource attack, VoxelGraph graph) {
		super(model, position, 0, 0, 0, SCALE);
		this.world = world;
		
		originalY = position.getY();
		
		Random random = new Random();
		phase = (float) (random.nextFloat() * 2 * Math.PI);
		
		speed = DEFAULT_SPEED + random.nextFloat() - 0.5f;
		
		this.hit = hit;
		this.attack = attack;
		pathFinding = new PathFinding(graph, this); // create Graph from map
	}

	public void update(Player player) {
		// movement
		Vector3f centerPos = new Vector3f(getPosition());
		centerPos.y = originalY;
		Vector3f playerDirection = Vector3f.sub(player.getPosition(), centerPos, null);
		

		if(playerDirection.length() > 2f) { // will not get closer than 0.75 meters
			if(timeSinceAStar >= ASTAR_COOLDOWN) {
				timeSinceAStar = 0;
				
				isWalking = false;
				pathFinding.AStar();
				if(!isWalking) {
					isDie = true;
				}
			}
			move();
		} else if(playerDirection.length() > 0.75f) { // will not get closer than 0.75 meters
			playerDirection.normalise();
			playerDirection.scale(DisplayManager.getDeltaTimeSeconds() * speed);
			this.increasePosition(playerDirection.x, 0, playerDirection.z);
			//originalY = originalY + playerDirection.y; // update basis of the bobbing	
		}
			
		//originalY = originalY + playerDirection.y; // update basis of the bobbing
			
		// look at player
		setRotY((float) Math.toDegrees(Math.atan2(playerDirection.x, playerDirection.z)));
		
		if(Vector3f.sub(player.getPosition(), getPosition(), null).length() < MELEE_RANGE && timeSinceMelee > MELEE_COOLDOWN) {
			player.hit(MELEE_DAMAGE);
			timeSinceMelee = 0;
			attack.play();
		}

		
		// bobbing
		phase += DisplayManager.getDeltaTimeSeconds() * 2 * Math.PI;
		getPosition().y = (float) (originalY + Math.sin(phase / BOBBLE_PERIOD) * BOBBLE_AMPLITUDE);
		
		// morphing phase
		morphingPhase += DisplayManager.getDeltaTimeSeconds() * 1.5f * (1 + (MAX_HEALTH - health) / 50);
		
		// for damage color
		timeSinceHit += DisplayManager.getDeltaTimeSeconds();
		
		// for melee cooldown color
		timeSinceMelee += DisplayManager.getDeltaTimeSeconds();
		
		// for astar cooldown
		timeSinceAStar += DisplayManager.getDeltaTimeSeconds();
	}
	
	public void hit(float damage) {
		hit.play();
		
		this.health -= damage;
		this.timeSinceHit = 0;
		
		if(health <= 0) isDie = true;
	}
	
	public float getMorphingPhase() {
		return this.morphingPhase;
	}
	
	public boolean isHit() {
		return timeSinceHit < DMG_COLOR_TIME;
	}
	
	public void setDirection(int clock, boolean isWalking) {
		this.isWalking = isWalking;
		walkYRot = clock;
	}
	
	private void move() {
		float distance = speed * DisplayManager.getDeltaTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(walkYRot)));
		float dz = (float) (distance * Math.cos(Math.toRadians(walkYRot)));
		this.increasePosition(dx, 0, dz);
	}
}

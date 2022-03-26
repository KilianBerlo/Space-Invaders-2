package entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import audioEngine.AudioSource;
import models.RawModel;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import textures.ModelTexture;
import utils.Maths;
import voxelWorld.VoxelHitbox;
import voxelWorld.VoxelWorld;

public class Player extends Entity {
	private static final float RUN_SPEED = 5;
	private static final float GRAVITY = 20f;
	private static final float JUMPPOWER = 7f;
	private static final float STANDINGHEIGHT = 1.8f;
	private static final float CROUCHINGHEIGHT = 1.35f;
	private static final float STANDINGHITBOXHEIGHT = 2.0f;
	private static final float CROUCHINGHITBOXHEIGHT = 1.5f;
	private static final float FOVCHANGESPEED = 300f; // degrees per second
	private static final float CROUCHSPEED = 5f; // I dont know the units sorry
	private static final float MAX_VELOCITY = -50f;
	public static final int OBJECTIVE_GOAL = 15; // how many to win
	private static final float HEALTH_HEARTBEAT = 30; // when to do the heartbeat sound
	
	private static final float LOWER_MAP_LIMIT = -50f;
	
	private float height = STANDINGHEIGHT; // camera height
	
	private Camera camera;
	private Weapon weapon;
	private Vector3f shootDirection = new Vector3f(0, 0, 1);
	
	public VoxelWorld world;
	
	// for camera position and hitbox
	private float hitboxHeight = STANDINGHITBOXHEIGHT;
	private float width = 0.75f;	
	
	// for jumping
	private float verticalVelocity = 0;
	private boolean inAir = false;
	
	// crouching
	private boolean isCrouching = false;
	
	//Audio
	private AudioSource footsteps;
	private AudioSource damage;
	private AudioSource heartbeat;
	private boolean moving;
	
	// Mouse
	private boolean isGrapped = true;
	private boolean gIsDown = false;
	
	// Power
	private boolean eIsDown = false;
	private boolean powerPhase1 = false;
	private boolean powerPhase2 = false;
	private boolean powerPhase3 = false;
	private boolean powerPhase4 = false;
	
	private float health = 100f; // percentage
	
	public int objectivesCollected = 0;
	
	// shadows
	private Entity shadowEntity;

	public Player(VoxelWorld world, Loader loader) {		
		super(null, new Vector3f(world.getSpawnPoint()), 0, 0, 0, 1);
		//position.y += STANDINGHEIGHT;
		
		this.world = world;
		world.setPlayer(this);
		
		this.camera = new Camera(position);
		
		RawModel gunModel = OBJLoader.loadObjModel("gun", loader);
		ModelTexture gunTexture = new ModelTexture(loader.loadTexture("gunTexture"));
		TexturedModel gunTexturedModel = new TexturedModel(gunModel, gunTexture);
		this.weapon = new Weapon(gunTexturedModel, new Vector3f(position), 0, 0, 0, 0.15f, loader);
		
		RawModel personModel = OBJLoader.loadObjModel("person", loader);
		TexturedModel personTexturedModel = new TexturedModel(personModel, gunTexture); // texture doesn't matter for the shadows
		this.shadowEntity = new Entity(personTexturedModel, new Vector3f(position), 0, 0, 0, 0.18f);

		world.setPlayer(this);
		world.createGraph();
	}
	
	public void oneUp() {
		// 20 healing
		health += 20f;
		health = Math.min(health, 100);
		
		weapon.resetCooldowns();
		
		damageCumsum = 0;
	}
	
	public Entity getShadowEntity() {
		return this.shadowEntity;
	}
	
	public void update(Matrix4f projectionMatrix) { // voxels to consider for collision
		updateOrientation(); // process mouse movements
		
		updatePosition();
		
		// update shadow orientation n stuff
		this.shadowEntity.setPosition(new Vector3f(position.x, position.y - 1.8f, position.z));
		this.shadowEntity.setRotY(-camera.getYaw());

		// update the things
		camera.updatePosition(this);
		weapon.update(this);
		
		// move projectiles
		for(Projectile projectile : weapon.getProjectiles()) {
			projectile.update(this);
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_G)) {
			if(!gIsDown) {
				isGrapped = !isGrapped;
				Mouse.setGrabbed(isGrapped);
			}
			
			gIsDown = true;
		} else {
			gIsDown = false;
		}
		
		
		// power stuff
		if(Keyboard.isKeyDown(Keyboard.KEY_E) && weapon.canUsePower()) {
			if(!eIsDown) {
				powerPhase1 = true;
				weapon.timeSincePower = 0;
			}
			
			eIsDown = true;
		} else {
			eIsDown = false;
		}
		
		if(powerPhase1) {
			if(!DisplayManager.inTransition()) {
				DisplayManager.changeSpeed(0.1f, 1f);
			} else {
				powerPhase1 = false;
				powerPhase2 = true;
			}
		}
		
		// when done with the transition...
		if(powerPhase2 && !DisplayManager.inTransition()) {
			weapon.activatePower(this, projectionMatrix);
			powerPhase2 = false;
			powerPhase3 = true;
		}
		
		if(powerPhase3 && weapon.powerIsFinished()) {
			powerPhase3 = false;
			powerPhase4 = true;
		}
		
		if(powerPhase4 && !DisplayManager.inTransition()) {
			DisplayManager.changeSpeed(1.0f, 1f);
			powerPhase4 = false;
		}
	}
	
	public void hit(float damage) {
		this.health -= damage;
		this.damage.play();
		
		if(this.health < HEALTH_HEARTBEAT) {
			heartbeat.play();
		}
	}
	
	public boolean isAlive() {
		return this.health > 0;
	}
	
	public void setHeartbeatSound(AudioSource sound) {
		this.heartbeat = sound;
	}
	
	public void reset() {
		this.health = 100;
		this.objectivesCollected = 0;
		heartbeat.stop();
		
		weapon.reset();
		camera.reset();
	}
	
	public void objectivePickedUp() {
		this.objectivesCollected++;
	}
	
	@Override
	public void render(MasterRenderer renderer) {
		if(Mouse.isButtonDown(1) && renderer.fov > MasterRenderer.ZOOM_FOV && renderer.fov < MasterRenderer.DEFAULT_FOV + 10) {
			renderer.changeFov(renderer.getFov() - FOVCHANGESPEED * DisplayManager.getDeltaTimeSeconds());
		} else if(!Mouse.isButtonDown(1) && renderer.fov > MasterRenderer.ZOOM_FOV - 10 && renderer.fov < MasterRenderer.DEFAULT_FOV) {
			renderer.changeFov(renderer.getFov() + FOVCHANGESPEED * DisplayManager.getDeltaTimeSeconds());
		}
		
		this.weapon.render(renderer);
		
		ArrayList<Projectile> toRemove = new ArrayList<Projectile>(); // cant edit while looping
		for(Projectile projectile : this.weapon.getProjectiles()) {
			if(!projectile.isDie()) {
				projectile.render(renderer);
			} else {
				toRemove.add(projectile);
			}
		}
		
		for(Projectile projectile : toRemove) this.weapon.getProjectiles().remove(projectile);
	}
	
	private float damageCumsum = 0f;
	private void updatePosition() {
		// corner with lowest x,y,z coordinates and corner with highest x,y,z coordinates
		Vector3f corner1 = new Vector3f(position.x - width / 2, position.y - height + 0.01f, position.z  - width / 2);
		Vector3f corner2 = new Vector3f(position.x  + width / 2, position.y - height + hitboxHeight, position.z  + width / 2);

		float[] axisBounds = world.getAxisBounds(corner1, corner2, position);
		
		Vector3f delta = getDelta();
		position.x += delta.x;
		position.y += delta.y;
		position.z += delta.z;
		
		// clamp values to avoid collisions
		// x ranges
		position.x = Math.max(position.x, axisBounds[0] + width / 2f);
		position.x = Math.min(position.x, axisBounds[1] - width / 2f);
		// y ranges
		float oldY = position.y; // to check if the following line changes it, meaning it is standing on the ground
		position.y = Math.max(position.y, axisBounds[2] + height);
		// if we just hit the ground, set velocity to zero and stop falling
		if(oldY != position.y) {
			verticalVelocity = 0;
			inAir = false;
		} else {
			inAir = true;
		}
		
		position.y = Math.min(position.y, axisBounds[3] - hitboxHeight + height);
		// z ranges
		position.z = Math.max(position.z, axisBounds[4] + width / 2f);
		position.z = Math.min(position.z, axisBounds[5] - width / 2f);
		
		if(position.y < LOWER_MAP_LIMIT) {
			damageCumsum += DisplayManager.getDeltaTimeSeconds() * 20;
			
			if(damageCumsum >= 20) {
				hit(damageCumsum);
				damageCumsum = 0;
			}
		}
		
		updateCrouching(axisBounds);
	}
	
	private void updateCrouching(float[] axisBounds) {
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && height > CROUCHINGHEIGHT) {
			float deltaHeight = DisplayManager.getDeltaTimeSeconds() * CROUCHSPEED;
			deltaHeight = Math.min(deltaHeight, height - CROUCHINGHEIGHT);
			deltaHeight = Math.max(deltaHeight, 0f);
			height -= deltaHeight;
			this.position.y -= deltaHeight;
		} else if(!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && height < STANDINGHEIGHT && (axisBounds[3] - position.y + height) >= STANDINGHITBOXHEIGHT){
			float deltaHeight = DisplayManager.getDeltaTimeSeconds() * CROUCHSPEED;
			deltaHeight = Math.min(deltaHeight, STANDINGHEIGHT - height);
			deltaHeight = Math.max(deltaHeight, 0f);
			height += deltaHeight;
			this.position.y += deltaHeight;
		}
		
		// do crouch
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !isCrouching) { // if CTRL down and not crouching: start crouching
			hitboxHeight = CROUCHINGHITBOXHEIGHT;
			this.isCrouching = true;
		} else if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && isCrouching && (axisBounds[3] - position.y + height) >= STANDINGHITBOXHEIGHT){ // if crouching but CTRL up: stop crouching
			hitboxHeight = STANDINGHITBOXHEIGHT;
			this.isCrouching = false;
		}
	}

	private Vector3f getDelta() {
		float dx = 0;
		float dz = 0;
		float dy = verticalVelocity * DisplayManager.getDeltaTimeSeconds();
		
		verticalVelocity = verticalVelocity - GRAVITY * DisplayManager.getDeltaTimeSeconds(); // do gravity
		verticalVelocity = Math.max(verticalVelocity, MAX_VELOCITY);
		
		// calculate distance moved
		float distance = RUN_SPEED * DisplayManager.getDeltaTimeSeconds();
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) { // double speed
			distance *= 1.5;
		}
		
		if(isCrouching) { // half speed
			distance *= 0.5;
		}
		
		if(inAir) { // fast in the air
			distance *= 1.5;
		}
		
		// calculate deltas
		boolean stillMoving = false;
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			dx -= (float) Math.cos(Math.toRadians(rotY + 90));
			dz -= (float) Math.sin(Math.toRadians(rotY + 90));
			stillMoving = true;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			dx += (float) Math.cos(Math.toRadians(rotY + 90));
			dz += (float) Math.sin(Math.toRadians(rotY + 90));
			distance *= 0.75f; // little bit slower when walking backwards
			stillMoving = true;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			dx -= (float) Math.cos(Math.toRadians(rotY));
			dz -= (float) Math.sin(Math.toRadians(rotY));
			stillMoving = true;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
			dx += (float) Math.cos(Math.toRadians(rotY));
			dz += (float) Math.sin(Math.toRadians(rotY));
			stillMoving = true;
		}
		
		if(stillMoving && !moving) {
			moving = true;
			footsteps.play();
		}
		if(!stillMoving && moving) {
			moving = false;
			footsteps.stop();
		}
		
		if(dx != 0 && dz != 0) {
			// normalize and multiply by distance in delta time
			float length = (float) Math.sqrt(dx * dx + dz * dz);
			dx /= length;
			dz /= length;
			dx *= distance;
			dz *= distance;
		}
		
		// do jump
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !inAir) { // only if not in the air
			verticalVelocity = JUMPPOWER;
			inAir = true;
		}
		
		return new Vector3f(dx, dy, dz);
	}
	
	// use mouse movements for update
	private void updateOrientation() {
		camera.doMouseMovement();
		
		// update player rotation
		rotY = camera.getYaw();
		
		if(Mouse.isButtonDown(0)) {
			weapon.shoot(this);
		}
		
		shootDirection = new Vector3f(0, 0, -1);
		shootDirection = Maths.rotateVectorX(shootDirection, (float) Math.toRadians(-camera.getPitch()));
		shootDirection = Maths.rotateVectorY(shootDirection, (float) Math.toRadians(camera.getYaw()));
	}
	
	public boolean isWin() {
		return this.objectivesCollected >= OBJECTIVE_GOAL;
	}
	
	public AudioSource getFootsteps() {
		return footsteps;
	}
	
	public void setDamageSound(AudioSource sound) {
		this.damage = sound;
	}
	
	public Vector3f getShootDirection() {
		return this.shootDirection;
	}

	public void setFootsteps(AudioSource footsteps) {
		this.footsteps = footsteps;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}
	
	public float getHealth() {
		return health;
	}
}

package entities;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import audioEngine.AudioSource;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.OBJLoader;
import textures.ModelTexture;
import ultimatePower.TargetFinder;
import utils.Maths;
import voxelWorld.VoxelModel;

public class Weapon extends Entity {
	private static final float HEAT_PER_SHOT = 10;
	private static final float COOLING_PER_SECOND = 25;
	private static final float SHOOT_DELAY = 0.1f; // seconds between shots
	
	private static final float POWER_PROJECTILE_SPEED = 100f;
	private static final float POWER_PROJECTILE_DAMAGE = 100f;
	private static final float POWER_SLOWMO_TIME = 1.5f; //seconds
	private static final float POWER_COOLDOWN = 30; // 30 sec
	
	private List<Projectile> projectiles = new ArrayList<Projectile>();
	
	private TexturedModel projectileModel;
	private TexturedModel powerProjectileModel;
	
	private float heat = 0f; // out of 100
	private float timeSinceShot = SHOOT_DELAY;
	public float timeSincePower = POWER_COOLDOWN;
	
	private List<GuiTexture> crosshairs;
	private int crosshairTexture;
	
	private AudioSource laser;
	private AudioSource powerActivate;
	private AudioSource powerReady;
	private boolean wasReady = true; // if the power is ready before checking
		
	public Weapon(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, Loader loader) {
		super(model, position, rotX, rotY, rotZ, scale);
		
		RawModel rawModel = loader.loadToVAO(VoxelModel.VERTICES, VoxelModel.TEXCOORDS, VoxelModel.NORMALS, VoxelModel.INDICES);
		ModelTexture projectileTexture = new ModelTexture(loader.loadTexture("rblock"));
		projectileTexture.setDoShading(false);
		projectileModel = new TexturedModel(rawModel, projectileTexture);
		
		ModelTexture powerProjectileTexture = new ModelTexture(loader.loadTexture("gold_block"));
		powerProjectileTexture.setDoShading(false);
		powerProjectileModel = new TexturedModel(rawModel, powerProjectileTexture);
		crosshairTexture = loader.loadTexture("crosshair");
	}
	
	public void resetCooldowns() {
		this.timeSincePower = POWER_COOLDOWN;
		this.heat = 0;
	}

	public void update(Player player) {
		//Calculate position of gun
		Vector3f delta = new Vector3f(0.6f,-0.3f,-0.75f);
		delta = Maths.rotateVectorX(delta, (float) -Math.toRadians(player.getCamera().getPitch()));
		delta = Maths.rotateVectorY(delta, (float) Math.toRadians(player.getCamera().getYaw()));
		
		//Set Position
		position.x = player.getPosition().x + delta.x;
		position.y = player.getPosition().y + delta.y;
		position.z = player.getPosition().z + delta.z;
		
		rotY = -player.getCamera().getYaw();
		rotX = -player.getCamera().getPitch();
		
		// apply cooling
		heat = (float) Math.max(0, heat - COOLING_PER_SECOND * DisplayManager.getDeltaTimeSeconds());
		
		timeSincePower += DisplayManager.getTrueFrameTimeSeconds();
		timeSinceShot += DisplayManager.getDeltaTimeSeconds();
	}

	public List<Projectile> getProjectiles() {
		return projectiles;
	}
	
	public void shoot(Player player) {
		if(timeSinceShot < SHOOT_DELAY || heat > 100 - HEAT_PER_SHOT) {
			return;
		}
		
		Vector3f pos = new Vector3f(player.getPosition().x, player.getPosition().y - 0.1f, player.getPosition().z);
		Projectile projectile = new Projectile(projectileModel, pos, rotX, rotY, rotZ, 0.15f, player.getShootDirection());
		this.projectiles.add(projectile);
		
		heat += HEAT_PER_SHOT;
		timeSinceShot = 0;
		
		laser.play();
	}
	
	public boolean powerIsFinished() {
		if(timeSincePower > POWER_SLOWMO_TIME) {
			crosshairs = null;
			return true;
		}
		
		return false;
	}
	
	public boolean canUsePower() {
		boolean isReady = this.timeSincePower >= POWER_COOLDOWN;
		
		// play if it just became ready
		if(!wasReady && isReady) powerReady.play();
		
		wasReady = isReady;
		
		return isReady;
	}
	
	public List<GuiTexture> getCrosshairs() {
		return crosshairs;
	}
	
	public void activatePower(Player player, Matrix4f projectionMatrix) {
		powerActivate.play();
		
		try {
			List<Vector2f> targets = TargetFinder.getLocations();
			
			Matrix4f viewMatrix = Maths.createTranslationlessViewMatrix(player.getCamera());
			Matrix4f projectionViewMatrix = Matrix4f.mul(projectionMatrix, viewMatrix, null);
			Matrix4f inverse = Matrix4f.invert(projectionViewMatrix, null);
			
			List<Vector2f> expandedTargets = new ArrayList<Vector2f>();
			for(Vector2f target : targets) {
				expandedTargets.add(target);
				float offset = 0.05f;
				expandedTargets.add(new Vector2f(target.x - offset, target.y - offset));
				expandedTargets.add(new Vector2f(target.x - offset, target.y + offset));
				expandedTargets.add(new Vector2f(target.x - offset, target.y));
				expandedTargets.add(new Vector2f(target.x + offset, target.y - offset));
				expandedTargets.add(new Vector2f(target.x + offset, target.y + offset));
				expandedTargets.add(new Vector2f(target.x + offset, target.y));
				expandedTargets.add(new Vector2f(target.x, target.y - offset));
				expandedTargets.add(new Vector2f(target.x, target.y + offset));
				
				offset = 0.1f;
				expandedTargets.add(new Vector2f(target.x - offset, target.y - offset));
				expandedTargets.add(new Vector2f(target.x - offset, target.y + offset));
				expandedTargets.add(new Vector2f(target.x - offset, target.y));
				expandedTargets.add(new Vector2f(target.x + offset, target.y - offset));
				expandedTargets.add(new Vector2f(target.x + offset, target.y + offset));
				expandedTargets.add(new Vector2f(target.x + offset, target.y));
				expandedTargets.add(new Vector2f(target.x, target.y - offset));
				expandedTargets.add(new Vector2f(target.x, target.y + offset));
			}
			
			for(Vector2f target : expandedTargets) {
				Vector4f adjustedTarget = new Vector4f(target.x, target.y, 0, 1);
				Vector4f direction4f = Matrix4f.transform(inverse, adjustedTarget, null);
				Vector3f direction = new Vector3f(direction4f.x, direction4f.y, direction4f.z);
				direction.normalise();
				
				Vector3f pos = new Vector3f(player.getPosition().x, getPosition().y, player.getPosition().z);
				Projectile projectile = new Projectile(powerProjectileModel, pos, rotX, rotY, rotZ, 0.15f, direction, POWER_PROJECTILE_SPEED, POWER_PROJECTILE_DAMAGE, 0f);
				this.projectiles.add(projectile);
			}
			
			crosshairs = new ArrayList<GuiTexture>();
			for(Vector2f target : targets) {
				GuiTexture targetTex = new GuiTexture(crosshairTexture, target, new Vector2f(0.1f, 0.1f));
				crosshairs.add(targetTex);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		heat = 0;
	}

	public void setAudioSources(AudioSource laser, AudioSource powerActivate, AudioSource powerReady) {
		this.laser = laser;
		this.powerActivate = powerActivate;
		this.powerReady = powerReady;
	}
	
	public float getHeat() {
		return heat;
	}
}

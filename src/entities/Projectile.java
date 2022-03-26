package entities;

import org.lwjgl.util.vector.Vector3f;

import enemy.Enemy;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class Projectile extends Entity {
	private static final float DEFAULT_SPEED = 50f; // m/s
	private static final long MAX_LIFESPAN = 10; // seconds
	private static final float DEFAULT_DAMAGE = 34f;
	
	private Vector3f velocity;
	private boolean isDie = false;
	private long spawnTime;
	
	private float speed = DEFAULT_SPEED; // m/s
	private float damage = DEFAULT_DAMAGE;
	private float invisibleSpan = 0.05f;
	
	private float lifespan = 0f;
	
	public Projectile(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f velocity) {
		super(model, position, rotX, rotY, rotZ, scale);
		this.velocity = velocity;
		this.spawnTime = System.nanoTime();
	}
	
	public Projectile(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale, Vector3f velocity, float speed, float damage, float invisibleSpan) {
		super(model, position, rotX, rotY, rotZ, scale);
		this.velocity = velocity;
		this.spawnTime = System.nanoTime();
		this.speed = speed;
		this.damage = damage;
		this.invisibleSpan = invisibleSpan;
	}
	
	@Override
	public void render(MasterRenderer renderer) {
		if(lifespan >= invisibleSpan) {
			super.render(renderer);
		}
	}



	private void enemyCollision(Player player) {
		for(Enemy enemy : player.world.getEnemies()) {
			float distance = Vector3f.sub(getPosition(), enemy.getPosition(), null).length();
			if(distance < 0.5f) {
				enemy.hit(damage);
				this.isDie = true;
			}
		}
	}
	
	private void blockCollision(Player player) {
		Vector3f corner1 = new Vector3f(position.x - getScale() / 2f, position.y - getScale() / 2f, position.z  - getScale() / 2f);
		Vector3f corner2 = new Vector3f(position.x + getScale() / 2f, position.y + getScale() / 2f, position.z  + getScale() / 2f);
		
		float[] axisBounds = player.world.getAxisBounds(corner1, corner2, position);
		
		position.x += velocity.x * DisplayManager.getDeltaTimeSeconds() * speed;
		position.y += velocity.y * DisplayManager.getDeltaTimeSeconds() * speed;
		position.z += velocity.z * DisplayManager.getDeltaTimeSeconds() * speed;
		
		float oldX = position.x;
		float oldY = position.y;
		float oldZ = position.z;
		
		position.x = Math.max(position.x, axisBounds[0] + getScale() / 2f);
		position.x = Math.min(position.x, axisBounds[1] - getScale() / 2f);
		position.y = Math.max(position.y, axisBounds[2] + getScale() / 2f);
		position.y = Math.min(position.y, axisBounds[3] - getScale() / 2f);
		position.z = Math.max(position.z, axisBounds[4] + getScale() / 2f);
		position.z = Math.min(position.z, axisBounds[5] - getScale() / 2f);
		
		if(position.x != oldX || position.y != oldY || position.z != oldZ) isDie = true;
		if(System.nanoTime() - spawnTime > MAX_LIFESPAN * 1000000000) isDie = true;
	}
	
	public void update(Player player) {
		enemyCollision(player);
		blockCollision(player);
		
		lifespan += DisplayManager.getDeltaTimeSeconds();
		
		increaseRotation(15f, 15f, 15f);
	}

	public boolean isDie() {
		return isDie;
	}
}

package enemy;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Matrix4f;
import org.omg.CORBA.portable.IndirectionException;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import renderEngine.MasterRenderer;
import shaders.EntityShader;
import textures.ModelTexture;
import utils.Maths;

public class EnemyRenderer {	
	private EnemyShader shader;

	public EnemyRenderer(EnemyShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
				
		// load projection matrix into the shader
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextures();
		shader.loadShadowMapUniforms();
		shader.stop();
	}
	
	public void render(List<Enemy> enemies, Matrix4f[] toShadowSpace) {
		shader.loadToShadowSpaceMatrix(toShadowSpace);
		
		for(Enemy enemy : enemies) {
			prepareTexturedModel(enemy.getModel());
			shader.loadMorphingPhase(enemy.getMorphingPhase());
			shader.loadDmgDiscolor(enemy.isHit());
			
			prepareInstance(enemy);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, enemy.getModel().getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			unbindTexturedModel();
		}
	}
	
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		ModelTexture texture = model.getTexture();
		
		if(texture.isHasTransparency()) MasterRenderer.disableCulling();
		
		GL30.glBindVertexArray(rawModel.getVaoID()); // bind vao
		GL20.glEnableVertexAttribArray(0); // bind vertex vbo
		GL20.glEnableVertexAttribArray(1); // bind texture vbo
		GL20.glEnableVertexAttribArray(2); // bind normal vbo
			
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadDoShading(texture.getDoShading());
		shader.loadWhiteBlending(texture.getWhiteBlending());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
	}
	
	public void updateProjectionMatrix(Matrix4f projectionMatrix) {
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	
	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0); // unbind vertex vbo
		GL20.glDisableVertexAttribArray(1); // unbind texture vbo
		GL20.glDisableVertexAttribArray(2); // unbind normal vbo
		GL30.glBindVertexArray(0); // unbind vao
		
		MasterRenderer.enableCulling(); // in case it was disabled.
	}
	
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				entity.getPosition(), 
				entity.getRotX(), 
				entity.getRotY(), 
				entity.getRotZ(), 
				entity.getScale()
		);
		shader.loadTransformationMatrix(transformationMatrix);
	} 
}

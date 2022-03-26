package renderEngine;

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
import shaders.EntityShader;
import textures.ModelTexture;
import utils.Maths;
import voxelWorld.VoxelWorld;
import voxelWorld.VoxelWorldShader;

public class VoxelWorldRenderer {	
	private VoxelWorldShader shader;

	public VoxelWorldRenderer(VoxelWorldShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
				
		// load projection matrix into the shader
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.connectTextures();
		shader.loadShadowMapUniforms();
		shader.stop();
	}
	
	public void render(List<Entity> voxelWorlds, Matrix4f[] toShadowSpace) {
		shader.loadToShadowSpaceMatrix(toShadowSpace);
		
		for(Entity world : voxelWorlds) {
			prepareTexturedModel(world.getModel());
			prepareInstance(world);
			GL11.glDrawElements(GL11.GL_TRIANGLES, world.getModel().getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			unbindTexturedModel();
		}
	}
	
	public void updateProjectionMatrix(Matrix4f projectionMatrix) {
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		ModelTexture texture = model.getTexture();
		
		if(texture.isHasTransparency()) MasterRenderer.disableCulling();
		
		GL30.glBindVertexArray(rawModel.getVaoID()); // bind vao
		GL20.glEnableVertexAttribArray(0); // bind vertex vbo
		GL20.glEnableVertexAttribArray(1); // bind texture vbo
		GL20.glEnableVertexAttribArray(2); // bind normal vbo
		GL20.glEnableVertexAttribArray(3); // bind material properties vbo
		GL20.glEnableVertexAttribArray(4); // bind tangent vbo

		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getNormalMapID());
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
	}
	
	private void unbindTexturedModel() {
		GL20.glDisableVertexAttribArray(0); // unbind vertex vbo
		GL20.glDisableVertexAttribArray(1); // unbind texture vbo
		GL20.glDisableVertexAttribArray(2); // unbind normal vbo
		GL20.glDisableVertexAttribArray(3); // unbind material properties vbo
		GL20.glDisableVertexAttribArray(4); // unbind tangent vbo
		GL30.glBindVertexArray(0); // unbind vao
		
		MasterRenderer.enableCulling(); // in case it was disabled.
	}
	
	private void prepareInstance(Entity voxelWorld) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				voxelWorld.getPosition(), 
				voxelWorld.getRotX(), 
				voxelWorld.getRotY(), 
				voxelWorld.getRotZ(), 
				voxelWorld.getScale()
		);
		shader.loadTransformationMatrix(transformationMatrix);
	} 
}

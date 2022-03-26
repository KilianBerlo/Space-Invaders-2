package renderEngine;

import java.util.List;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

import guis.GuiShader;
import guis.GuiTexture;
import models.RawModel;
import utils.Maths;

public class GuiRenderer {
	private final RawModel quad;
	private GuiShader shader;
	
	public GuiRenderer(Loader loader) {
		float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1}; // vertex positions of the quad
		quad = loader.loadToVAO(positions, 2); // just xy, 2 dimensions
		shader = new GuiShader();
	}
	
	public void render(List<GuiTexture> guis) {
		shader.start();
		GL30.glBindVertexArray(quad.getVaoID()); // bind vao
		GL20.glEnableVertexAttribArray(0); // enable positions vbo
		GL11.glEnable(GL11.GL_BLEND); // for the transparent parts
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST); // so they can overlap
		
		for(GuiTexture gui: guis) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, gui.getTexture());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			
			float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
			Vector2f aspectRatioCorrectedScale = new Vector2f(gui.getScale().x, gui.getScale().y * aspectRatio); // avoid distortion by correcting here
			Matrix4f matrix = Maths.createTransformationMatrix(gui.getPosition(), aspectRatioCorrectedScale);
			
			shader.loadTransformation(matrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount()); // strips this time
		}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST); // so they can overlap
		GL11.glDisable(GL11.GL_BLEND); // for the transparent parts
		GL20.glDisableVertexAttribArray(0); // enable positions vbo
		GL30.glBindVertexArray(0); // unbind vao
		shader.stop();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
}

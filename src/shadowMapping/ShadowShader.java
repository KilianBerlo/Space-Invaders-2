package shadowMapping;

import org.lwjgl.util.vector.Matrix4f;

import shaders.ShaderProgram;

public class ShadowShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/shadowMapping/shadowVertexShader.txt";
	private static final String FRAGMENT_FILE = "/shadowMapping/shadowFragmentShader.txt";
	
	private int location_mvpMatrix;

	public ShadowShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	public void getAllUniformLocations() {
		location_mvpMatrix = super.getUniformLocation("mvpMatrix");	
	}
	
	@Override
	public void bindAttributes() {
		super.bindAttribute(0, "in_position");
	}
	
	public void loadModelViewProjectionMatrix(Matrix4f mvpMatrix){
		super.loadMatrix(location_mvpMatrix, mvpMatrix);
	}
}

package skybox;
 
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import renderEngine.DisplayManager;
import shaders.ShaderProgram;
import utils.Maths;
 
public class SkyboxShader extends ShaderProgram{
	public static final float ROTATION_PERIOD = 300; // seconds per rotation
    private static final String VERTEX_FILE = "/skybox/skyboxVertexShader.txt";
    private static final String FRAGMENT_FILE = "/skybox/skyboxFragmentShader.txt";
     
    private int location_projectionMatrix;
    private int location_viewMatrix;
    
    private float rotation = 0;
     
    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
     
    public void loadProjectionMatrix(Matrix4f matrix){
        super.loadMatrix(location_projectionMatrix, matrix);
    }
 
    public void loadViewMatrix(Camera camera){
        Matrix4f matrix = Maths.createViewMatrix(camera);
        // no translation
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        
        Matrix4f rot = Maths.createTransformationMatrix(new Vector3f(0,0,0), 0, rotation, 0, 1);
        rotation += 360f/ROTATION_PERIOD * DisplayManager.getDeltaTimeSeconds();
        matrix = Matrix4f.mul(matrix, rot, null);
        super.loadMatrix(location_viewMatrix, matrix);
    }
     
    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
    }
 
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
}
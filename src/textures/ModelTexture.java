package textures;

public class ModelTexture {
	private int textureID;
	private int normalMapID;
	
	private float shineDamper = 1;
	private float reflectivity = 0;
	
	private boolean hasTransparency = false;
	private boolean useFakeLighting = false;
	private boolean doShading = true;
	private float whiteBlending = 0.0f;
	
	private int numberOfRows = 1; // equal to number of columns, of course
	
	public ModelTexture(int id) {
		this.textureID = id;
	}
	
	public int getID() {
		return textureID;
	}

	public int getNormalMapID() {
		return normalMapID;
	}

	public void setNormalMapID(int normalMapID) {
		this.normalMapID = normalMapID;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}
	
	public boolean getDoShading() {
		return this.doShading;
	}
	
	public float getWhiteBlending() {
		return this.whiteBlending;
	}
	
	public void setWhiteBlending(float whiteBlending) {
		this.whiteBlending = whiteBlending;
	}
	
	public void setDoShading(boolean doShading) {
		this.doShading = doShading;
	}

	public boolean isHasTransparency() {
		return hasTransparency;
	}

	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

	public boolean isUseFakeLighting() {
		return useFakeLighting;
	}

	public void setUseFakeLighting(boolean useFakeLighting) {
		this.useFakeLighting = useFakeLighting;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
}

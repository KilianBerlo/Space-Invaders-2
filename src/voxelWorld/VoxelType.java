package voxelWorld;

public enum VoxelType {

	IRONBLOCK (32, 0.5f, 5, false),
	REDSTONEBLOCK (34, 0.1f, 1, false),
	WHITEBLOCK (32, 0.1f, 1, false),
	GOLDBLOCK (9, 0.7f, 1, false),
	GLASSBLOCK (2, 0.3f, 1, false),
	DIAMONDBLOCK (36, 0.8f, 5, false),
	LAPISBLOCK (15, 0.5f, 3, false),
	PURPLEGOO (20, 0.01f, 1, false),
	COALBLOCK (16, 0.3f, 1, false),
	TILEBLOCK (0, 0.5f, 5, false),
	REDWOOL (18, 0.5f, 5, false),
	BLUEWOOL (43, 0.5f, 5, false),
	GREENWOOL (38, 0.5f, 5, false),
	YELLOWWOOL (25, 0.5f, 5, false),
	WHITEWOOL (4, 0.5f, 5, false),
	SHUTTERDOOR (31, 0.7f, 5, false),
	ALIENSPAWN (27, 0.1f, 5, false),
	GOLDORE (29, 0.7f, 1, false),
	SCREEN (6, 1, 10, false),
	MOONWHITE (13, 0.1f, 5, false),
	MOONBLACK (22, 0.1f, 5, false),
	DIAMONDORE (11, 0.6f, 5, false),
	IRONORE(41, 0.01f, 1, false),
	BEDROCK (45, 0, 0, false);

	public int textureNumber; // number of the texture in the texture atlas (starting at 0)
	public float reflectivity; // percentage of light that is reflected
	public float shineDamper; // how precise the reflection is
	public boolean hasTransparancy; // to stop it from culling faces for this texture
	
	private VoxelType(int textureNumber, float reflectivity, float shineDamper, boolean hasTransparancy) {
		this.textureNumber = textureNumber;
		this.reflectivity = reflectivity;
		this.shineDamper = shineDamper;
		this.hasTransparancy = hasTransparancy;
	}
}

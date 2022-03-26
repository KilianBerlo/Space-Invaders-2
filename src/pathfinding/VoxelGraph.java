package pathfinding;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import enemy.Enemy;
import entities.Player;
import voxelWorld.VoxelHitbox;
import voxelWorld.VoxelModel;
import voxelWorld.VoxelWorld;

public class VoxelGraph {

	protected float maxX = Float.NEGATIVE_INFINITY;
	protected float minX = Float.POSITIVE_INFINITY;
	protected float maxY = Float.NEGATIVE_INFINITY;
	protected float minY = Float.POSITIVE_INFINITY;
	protected float maxZ = Float.NEGATIVE_INFINITY;
	protected float minZ = Float.POSITIVE_INFINITY;
	//set number of blocks of map (x,y,z)
	protected int blocksX = 0;
	protected int blocksY = 0;
	protected int blocksZ = 0;
	protected VoxelWorld world;
	protected Enemy enemy;
	protected Player player;
	private Vector3f enemyPos;
	private VoxelVertex enemyVertex;
	private Vector3f playerPos;
	private VoxelVertex playerVertex;
	//Create arrayList to save voxel vertices to
	protected VoxelVertex[][] voxelVerticesList;
	//Creates an arraylist to save all the accessible voxels.
	protected AccessibleVoxel[][] accessibleVoxelList;

	//Initiate VoxelGraph
	public VoxelGraph(VoxelWorld world, Player player) {
		this.world = world;
		this.player = player;
		setBordersWorld();
		this.blocksX = (int) ((maxX - minX) / VoxelModel.SCALE);
		this.blocksY = (int) ((maxY - minY) / VoxelModel.SCALE);
		this.blocksZ = (int) ((maxZ - minZ) / VoxelModel.SCALE);
		this.voxelVerticesList = new VoxelVertex[blocksX-1][blocksZ-1];
		this.accessibleVoxelList = new AccessibleVoxel[blocksX][blocksZ];
		createGraph();
	}

	//Create the graph
	private void createGraph() {
		playerPos = this.getPlayerPosition();
		//Initiate the map
		VoxelHitbox[][][]map = new VoxelHitbox[blocksX+1][blocksY+1][blocksZ+1];
		//Place each voxel in the map
		for(VoxelHitbox voxel: world.getVoxelHitboxes()) {
			map[(int) ((voxel.centerPos.x - minX -0.25) / VoxelModel.SCALE)][(int) ((voxel.centerPos.y - minY -0.25) / VoxelModel.SCALE )][(int) ((voxel.centerPos.z - minZ-0.25) / VoxelModel.SCALE)] = voxel;
		}
		
		int verticesCreated = 0;
		int outArrowsCreated = 0;
		System.out.println(playerPos.getY());
		float graphHeight = 13.75f; // Normal PlayerPos spawn position
		int y = (int) (((graphHeight - minY) / VoxelModel.SCALE)-5);
		// Check which voxels can be reached and save them in an arraylist
			for(int x = 0; x < blocksX; x++) {
				for(int z = 0; z < blocksZ; z++) {
					accessibleVoxelList[x][z] = null;
					if(map[x][y][z] != null && map[x][y+1][z] == null && map[x][y+2][z] == null && map[x][y+3][z] == null){
						verticesCreated++;
						AccessibleVoxel accessibleVoxel = new AccessibleVoxel(map[x][y][z]);
						this.accessibleVoxelList[x][z] = accessibleVoxel;
					}
				}
			}
			
			//Create the voxelVertices if the 4 adjacent voxels can be reached
			for(int x = 2; x < blocksX-3; x++) {
				for(int z = 2; z < blocksZ-3; z++) {
					voxelVerticesList[x][z] = null;
					if(accessibleVoxelList[x][z]!= null && accessibleVoxelList[x-1][z] != null && accessibleVoxelList[x+1][z] != null && accessibleVoxelList[x][z+1] != null && accessibleVoxelList[x][z-1] != null) {
						//System.out.println(accessibleVoxelList[x][z]);
						VoxelVertex voxelVertex = new VoxelVertex(accessibleVoxelList[x][z]);
						this.voxelVerticesList[x][z] = voxelVertex;
						//enemy vertex created
					}
				}
			}
			for(int x = 0; x < blocksX-1; x++) {
				for(int z = 0; z < blocksZ-1; z++) {
					VoxelVertex voxelVertex = voxelVerticesList[x][z];
					if(voxelVertex != null) {
						if(x != blocksX-2) {
							if(voxelVerticesList[x+1][z] != null) {
								voxelVertex.setVoxelEast(voxelVerticesList[x+1][z]);
								outArrowsCreated++;
								if(voxelVerticesList[x][z+1] != null) {
									if(z != blocksZ-2) {
										if(voxelVerticesList[x+1][z+1] != null) {
										voxelVertex.setVoxelNorthEast(voxelVerticesList[x+1][z+1]);
										outArrowsCreated++;
										}
									}
								}
								if(voxelVerticesList[x][z-1] != null) {
									if(z != 0) {
										if(voxelVerticesList[x+1][z-1] != null) {
										voxelVertex.setVoxelSouthEast(voxelVerticesList[x+1][z-1]);
										outArrowsCreated++;
										}
									}
								}
							}
						}

						if(x != 0) {
							if(voxelVerticesList[x-1][z] != null) {
								voxelVertex.setVoxelWest(voxelVerticesList[x-1][z]);
								outArrowsCreated++;
								if(voxelVerticesList[x][z+1] != null) {
									if(z != blocksZ-2) {
										if(voxelVerticesList[x-1][z+1] != null) {
										voxelVertex.setVoxelNorthWest(voxelVerticesList[x-1][z+1]);
										outArrowsCreated++;
										}
									}
								}
								if(voxelVerticesList[x][z-1] != null) {
									if(z != 0) {
										if(voxelVerticesList[x-1][z-1] != null) {
										voxelVertex.setVoxelSouthWest(voxelVerticesList[x-1][z-1]);
										outArrowsCreated++;
										}
									}
								}
							}
							


						}
						if(z != blocksZ-2) {
							if(voxelVerticesList[x][z+1] != null) {
							voxelVertex.setVoxelNorth(voxelVerticesList[x][z+1]);
							outArrowsCreated++;
							}
						}

						if(z != 0) {
							if(voxelVerticesList[x][z-1] != null) {
							voxelVertex.setVoxelSouth(voxelVerticesList[x][z-1]);
							outArrowsCreated++;
							}
						}
					}
				}

			}

//		System.out.println("vertices created " + verticesCreated);
//		System.out.println("outarrows created " + outArrowsCreated);
//		System.out.println("total amount of voxelHitboxes" + world.getVoxelHitboxes().length);
//		System.out.println("End graph creation");
	}
	
	private Vector3f getPlayerPosition() {
		return player.getPosition();
	}
	
	public void changePlayerVertex() {
		Vector3f playerPos = this.getPlayerPosition();
		int xVertex = (int) ((playerPos.x - minX) / VoxelModel.SCALE);
		int zVertex = (int) ((playerPos.z - minZ) / VoxelModel.SCALE);
		if(voxelVerticesList[xVertex][zVertex]!= null) {
			playerVertex = voxelVerticesList[xVertex][zVertex];
		} else { // Spawnplace is next to the border of the map, find temporary replacement vertex
			for(int x = 0; x<15; x++) {
				if(x%2==0) {
				xVertex = (int) ((playerPos.x - minX-(x/2)*1) / VoxelModel.SCALE);
				}else {
					xVertex = (int) ((playerPos.x - minX+((x+1)/2)*1) / VoxelModel.SCALE);
				}
				if(xVertex < voxelVerticesList[0].length && xVertex >=0) {
					for(int z = 0; z<15; z++) {
						if(z%2==0) {
							zVertex = (int) ((playerPos.z - minZ-(z/2)*1) / VoxelModel.SCALE);
							}else {
								zVertex = (int) ((playerPos.z - minZ+((z+1)/2)*1) / VoxelModel.SCALE);
							}
						if(zVertex < voxelVerticesList[1].length && zVertex >=0) {
							if (voxelVerticesList[xVertex][zVertex]!= null) {
								playerVertex = voxelVerticesList[xVertex][zVertex];
								x=15;
								z=15;
							}
						}
					}
				}
			}
		}
	}

	private void setBordersWorld() {
		for(VoxelHitbox voxel: world.getVoxelHitboxes()) {
			if(voxel.centerPos.x < minX) {
				minX = voxel.centerPos.x;
			} else if(voxel.centerPos.x > maxX) {
				maxX = voxel.centerPos.x;
			}

			if(voxel.centerPos.y < minY) {
				minY = voxel.centerPos.y;
			} else if(voxel.centerPos.y > maxY) {
				maxY = voxel.centerPos.y;
			}

			if(voxel.centerPos.z < minZ) {
				minZ = voxel.centerPos.z;
			} else if(voxel.centerPos.z > maxZ) {
				maxZ = voxel.centerPos.z;
			}
		}
	}



	public void setPlayerPos(Vector3f spawnPoint) {
        this.playerPos = spawnPoint;
    }

	

	public Vector3f getPlayerPos() {
		return playerPos;
	}

	public VoxelVertex getPlayerVertex() {
		return playerVertex;
	}
}

package pathfinding;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import enemy.Enemy;
import voxelWorld.VoxelModel;

public class PathFinding {
	
	VoxelGraph graph;
	Enemy enemy;
	VoxelVertex enemyVertex;
	Vector3f enemyPos;
	
	public PathFinding(VoxelGraph graph, Enemy enemy) {
		this.graph = graph;
		this.enemy = enemy;
		
	}

	private void changeEnemyVertex() {
		Vector3f enemyPos = this.getEnemyPosition();	
		int xVertex = (int) ((enemyPos.x - graph.minX) / VoxelModel.SCALE);
		int zVertex = (int) ((enemyPos.z - graph.minZ) / VoxelModel.SCALE);
		boolean vertexFound = false;
		if(graph.voxelVerticesList[xVertex][zVertex]!= null) {
			enemyVertex = graph.voxelVerticesList[xVertex][zVertex];
		} else { // Spawnplace is next to the border of the map, find temporary replacement vertex
			for(int x = 0; x<15; x++) {
				if(x%2==0) {
					xVertex = (int) ((enemyPos.x - graph.minX-(x/2)*1) / VoxelModel.SCALE);
					}else {
						xVertex = (int) ((enemyPos.x - graph.minX+((x+1)/2)*1) / VoxelModel.SCALE);
					}
				if(xVertex < graph.voxelVerticesList[0].length && xVertex >=0) {
					for(int z = 0; z<15; z++) {
						if(z%2==0) {
							zVertex = (int) ((enemyPos.z - graph.minZ-(z/2)*1) / VoxelModel.SCALE);
							}else {
								zVertex = (int) ((enemyPos.z - graph.minZ+((z+1)/2)*1) / VoxelModel.SCALE);
							}
						if(zVertex < graph.voxelVerticesList[1].length && zVertex >=0) {
							if (graph.voxelVerticesList[xVertex][zVertex]!= null) {
								enemyVertex = graph.voxelVerticesList[xVertex][zVertex];
								x=15;
								z=15;
							}
						}
					}
				}
			}
		}
		if(enemyVertex !=null) {
			enemyVertex.setParentPointer(null);
		}
	}
	
	private Vector3f getEnemyPosition() {
		return enemy.getPosition();
	}

	//runs A-star
	public void AStar(){
		graph.changePlayerVertex();
		changeEnemyVertex();
		MinHeap openList = new MinHeap(graph.blocksX * graph.blocksZ);
		ArrayList<VoxelVertex> closedList = new ArrayList<VoxelVertex>();
		openList.insertVertex(enemyVertex);
		int verticesSeen = 0;
		while(!openList.isEmpty() && verticesSeen < 1000) {
			verticesSeen++;
			if(graph.getPlayerVertex() == null) {
				enemy.isDie = true;
				break;
			}
			if(enemyVertex == null) {
				enemy.isDie = true;
				break;
				
			}
			
			/// ---find vertex with lowest f value ---///
			VoxelVertex currentVertex = openList.extractMin();
			if(currentVertex == null) {
				enemy.isDie = true;
				break;
			}
			closedList.add(currentVertex);
			/// ---check if this value is the goal state ---///
			if(currentVertex == graph.getPlayerVertex()) { 
				/// ---Build a path---///
				getPath(currentVertex);
				/// ---Change direction of enemy---///
				
				break;
			}
			else {
				addVoxels(currentVertex, openList, closedList);
			}
		}
	}
	public void getPath(VoxelVertex currentVertex) {
		ArrayList<VoxelVertex> path = new ArrayList<VoxelVertex>();
		path.add(currentVertex);
		VoxelVertex parent;
		while((parent = currentVertex.getParentPointer()) != null) {
			path.add(0, parent);
			currentVertex = parent;
		}
//		for(VoxelVertex vertex : path) {
//			System.out.println(vertex.position);
//		}
		setDirection(path);
	}
	
	public void addVoxels(VoxelVertex currentVertex, MinHeap openList, ArrayList<VoxelVertex> closedList) {
		/// ---generate each vertex that comes after node-current ---///
		currentVertex.setSuccessorVertices();
		ArrayList<VoxelVertex> successorVertices = currentVertex.getSuccessorVertices();
		for(VoxelVertex successorVertex: successorVertices) {
			//Set successor current cost = g(current_node) + 1
			ArrayList<VoxelVertex> diagonalSuccessorVertices = currentVertex.getDiagonalSuccessorVertices();
			double successorCurrentCost;
			if(diagonalSuccessorVertices.contains(successorVertex)){
				successorCurrentCost = currentVertex.getGValue()+1.5;
			} else {
				successorCurrentCost = currentVertex.getGValue()+1;
			}
			if(successorVertex != null && !closedList.contains(successorVertex)) {
				if(!openList.Contains(successorVertex)) {
					successorVertex.setGValue(successorCurrentCost);
					successorVertex.setParentPointer(currentVertex);
					//Set H successor to be the heuristic distance to node goal // For moving target I assume?
					successorVertex.updateHValue(successorVertex, graph.getPlayerVertex());
					successorVertex.updateFValue(successorVertex.getGValue(), successorVertex.getHValue());
					openList.insertVertex(successorVertex);
				}
				else {
					boolean changed = false;
					if(successorCurrentCost < successorVertex.getGValue()) {
						successorVertex.setGValue(successorCurrentCost);
						successorVertex.setParentPointer(currentVertex);
						successorVertex.updateFValue(successorVertex.getGValue(), successorVertex.getHValue());
						changed = true;
					}
					if (changed) {
						closedList.remove(successorVertex);
						// delete NodeVertex from closed list
						openList.insertVertex(successorVertex);
					}
				}
			}
		}
	}
	
	//Set the direction based on the path
	public void setDirection(ArrayList<VoxelVertex> path) {
		if(path!=null && path.size()> 1) {
			VoxelVertex thisVertex = path.get(0);
			VoxelVertex nextVertex = path.get(1);
			if(thisVertex.getVoxelEast() == nextVertex) {
					enemy.setDirection(90, true);
			}
			if(thisVertex.getVoxelNorth() == nextVertex) {
					enemy.setDirection(0, true);
			}
			if(thisVertex.getVoxelSouth() == nextVertex) {
					enemy.setDirection(180, true);
			}
			if(thisVertex.getVoxelWest() == nextVertex) {
					enemy.setDirection(270, true);
			}
			if(thisVertex.getVoxelNorthWest() == nextVertex) {
				enemy.setDirection(315, true);
			}
			if(thisVertex.getVoxelNorthEast() == nextVertex) {
				enemy.setDirection(45, true);
			}
			if(thisVertex.getVoxelSouthWest() == nextVertex) {
				enemy.setDirection(225, true);
		}
			if(thisVertex.getVoxelSouthEast() == nextVertex) {
				enemy.setDirection(135, true);
		}
		}
		
	}
	
	public void setEnemyPos(Vector3f goalPoint) {
        this.enemyPos = goalPoint;
    }

	public Vector3f getEnemyPos() {
		return enemyPos;
	}

	public VoxelVertex getEnemyVertex() {
		return enemyVertex;
	}
}

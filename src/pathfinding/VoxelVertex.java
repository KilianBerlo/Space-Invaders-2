package pathfinding;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import voxelWorld.VoxelHitbox;
import voxelWorld.VoxelWorld;

public class VoxelVertex{

	public ArrayList<VoxelVertex> successorVertices;
	public ArrayList<VoxelVertex> diagonalSuccessorVertices;
	public Vector3f position = null;
	public VoxelVertex voxelSouth = null;
	public VoxelVertex voxelWest = null;
	public VoxelVertex voxelNorth = null;
	public VoxelVertex voxelEast = null;
	public VoxelVertex voxelNorthEast = null;
	public VoxelVertex voxelSouthEast = null; 
	public VoxelVertex voxelNorthWest = null;
	public VoxelVertex voxelSouthWest = null;
	public VoxelVertex parentPointer;
	public double rhsValue;
	public double fValue;
	public double gValue;
	public double hValue;
	protected VoxelWorld world;
    private boolean isBlock;
	
	
	public VoxelVertex(AccessibleVoxel voxel) {
		setPosition(voxel);
		this.parentPointer = null;
		this.rhsValue = Double.POSITIVE_INFINITY;
		//G value set to 0 because distance between current vertex and start vertex is 0 since currentvertex==startvertex in the beginning
		this.gValue = 0.0;
		//H value set to positive infinity because it is the estimated distance from the (enemy) spawn point vertex to the goal vertex
		this.hValue = Double.POSITIVE_INFINITY;
		this.fValue = this.gValue + this.hValue;
	}
	
	// Update G value
	// G value is not estimated but calculated
	public void updateGValue(Vector3f spawnVoxel, Vector3f currentVertex) {
		//update G value to be the estimated shortest distance from the starting voxel to the current voxel (a^2 + b^2 = c^2)
		//once A* we should look at the parent voxel and add 1 to this (I think?)
		int updatedGValue = (int) (Math.pow((spawnVoxel.x-currentVertex.x),2) + Math.pow((spawnVoxel.z - currentVertex.z),2));
		setGValue(updatedGValue);
	}
	
	
		
	// Update H value
	public void updateHValue(VoxelVertex currentVoxel, VoxelVertex goalVoxel) {
		//update H value to be the estimated shortest distance from the current voxel to the goal voxel (a^2 + b^2 = c^2)
		//getGoalPoint is added in the VoxelWorld class 
		//Get goalVoxel
		Vector3f coordinates = goalVoxel.position;
		float distanceX = coordinates.x - currentVoxel.position.x;
		float distanceZ = coordinates.z - currentVoxel.position.z;
		int updatedHValue = (int) (Math.pow((distanceX),2) + Math.pow((distanceZ),2));
		setHValue(updatedHValue);
	}

	// Update F value (Based on the G and H values)
	public void updateFValue(double gValue, double hValue) {
		setFValue((int) (gValue + hValue));
	}
	
	

	public void setVoxelSouth(VoxelVertex voxelSouth) {
		this.voxelSouth = voxelSouth;
	}

	public void setVoxelWest(VoxelVertex voxelWest) {
		this.voxelWest = voxelWest;
	}

	public void setVoxelNorth(VoxelVertex voxelNorth) {
		this.voxelNorth = voxelNorth;
	}

	public void setVoxelEast(VoxelVertex voxelEast) {
		this.voxelEast = voxelEast;
	}
	
	public void setVoxelSouthWest(VoxelVertex voxelSouthWest) {
		this.voxelSouthWest = voxelSouthWest;
	}

	public void setVoxelNorthWest(VoxelVertex voxelNorthWest) {
		this.voxelNorthWest = voxelNorthWest;
	}

	public void setVoxelNorthEast(VoxelVertex voxelNorthEast) {
		this.voxelNorthEast = voxelNorthEast;
	}

	public void setVoxelSouthEast(VoxelVertex voxelSouthEast) {
		this.voxelSouthEast = voxelSouthEast;
	}
	
	public void setPosition(AccessibleVoxel voxel) {
		//System.out.println(voxel);
		position = voxel.thisVoxelHitbox.centerPos;	
	}
	
	public void setSuccessorVertices(){
		successorVertices = new ArrayList<VoxelVertex>();
		if(getVoxelSouth() != null) {
			successorVertices.add(getVoxelSouth());
		} if(getVoxelNorth() != null) {
			successorVertices.add(getVoxelNorth());
		} if(getVoxelEast() != null) {
			successorVertices.add(getVoxelEast());
		} if(getVoxelWest() != null) {
			successorVertices.add(getVoxelWest());
		} if(getVoxelNorthWest() != null) {
			successorVertices.add(getVoxelNorthWest());
		} if(getVoxelSouthWest() != null) {
			successorVertices.add(getVoxelSouthWest());
		} if(getVoxelNorthEast() != null) {
			successorVertices.add(getVoxelNorthEast());
		} if(getVoxelSouthEast() != null) {
			successorVertices.add(getVoxelSouthEast());
		} 
		setDiagonalSuccessorVertices();
	}
	public void setDiagonalSuccessorVertices() {
		diagonalSuccessorVertices = new ArrayList<VoxelVertex>();
		if(getVoxelNorthWest() != null) {
			diagonalSuccessorVertices.add(getVoxelNorthWest());
		} if(getVoxelSouthWest() != null) {
			diagonalSuccessorVertices.add(getVoxelSouthWest());
		} if(getVoxelNorthEast() != null) {
			diagonalSuccessorVertices.add(getVoxelNorthEast());
		} if(getVoxelSouthEast() != null) {
			diagonalSuccessorVertices.add(getVoxelSouthEast());
		} 
	}

	public void setParentPointer(VoxelVertex parentPointer) {
		this.parentPointer = parentPointer;
	}
	
	public void setRhsValue(int rhsValue) {
		this.rhsValue = rhsValue;
	}
	
	// Set G value
	public void setGValue(double gValue) {
		this.gValue = gValue;
	}
		
	// Set H value
	public void setHValue(double hValue) {
		this.hValue = hValue;
	}
	
	// Set F value
	public void setFValue(double fValue) {
		this.fValue = fValue;
	}
	
	public VoxelVertex getVoxelSouth() {
		return voxelSouth;
	}
	public VoxelVertex getVoxelWest() {
		return voxelWest;
	}
	public VoxelVertex getVoxelNorth() {
		return voxelNorth;
	}
	public VoxelVertex getVoxelEast() {
		return voxelEast;
	}
	public VoxelVertex getVoxelNorthWest() {
		return voxelNorthWest;
	}
	public VoxelVertex getVoxelNorthEast() {
		return voxelNorthEast;
	}
	public VoxelVertex getVoxelSouthWest() {
		return voxelSouthWest;
	}
	public VoxelVertex getVoxelSouthEast() {
		return voxelSouthEast;
	}
	public ArrayList<VoxelVertex> getSuccessorVertices() {
		return successorVertices;
	}
	public ArrayList<VoxelVertex> getDiagonalSuccessorVertices() {
		return diagonalSuccessorVertices;
	}
	public VoxelVertex getParentPointer() {
		return parentPointer;
	}
	public double getRhsValue() {
		return rhsValue;
	}
	public double getFValue() {
		return fValue;
	}
	public double getGValue() {
		return gValue;
	}
	public double getHValue() {
		return hValue;
	}

	public boolean isBlock() {
        return isBlock;
    }
	
	public void setBlock(boolean isBlock) {
	        this.isBlock = isBlock;
	    }

	
}

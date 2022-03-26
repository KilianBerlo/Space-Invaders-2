package pathfinding;

import java.util.ArrayList;

public class MinHeap {
	  
		ArrayList<VoxelVertex> array;
	    int capacity; // maximum possible size of min heap 
	    int heap_size; // Current number of elements in min heap 
	    int amountOfDeletions = 0;
	    
	    // Constructor 
		public MinHeap(int capacity) {
	    	heap_size = 0;
	    	this.capacity = capacity;
	    	array = new ArrayList<VoxelVertex>();
	    }
	  
	    public int parent(int i){ 
	    	return (i-1)/2; 
	    } 
	  
	    // to get index of left child of node at index i 
	    int left(int i) { return (2*i + 1); } 
	  
	    // to get index of right child of node at index i 
	    int right(int i) { return (2*i + 2); } 
	    
	    // Returns the minimum key (key at root) from min heap 
	    VoxelVertex getMin() { 
	    	return array.get(0); 
	    } 
	  
	// Inserts a new key 'k' 
	public boolean isEmpty() {
		return heap_size == 0;
	}
	    
	public void insertVertex(VoxelVertex k) 
	{ 
	    if (heap_size == capacity) 
	    { 
	        //System.out.println("Heap is full");
	        return; 
	    } 
	  
	    // First insert the new key at the end 
	    heap_size++; 
	    array.add(k);
	    
	    int i = heap_size -1;
	    // Fix the min heap property if it is violated 
	    checkAndFix(i);
	} 
	
	public VoxelVertex[] getElements() {
		VoxelVertex[] elements = new VoxelVertex[heap_size];
		int itemsLeftToSee = heap_size;
		int heapIndex = 0;
		int arrayIndex = 0;
		VoxelVertex currentVertex;
		while(itemsLeftToSee != 0) {
			currentVertex = array.get(heapIndex);
			if(currentVertex != null){
				elements[arrayIndex] = currentVertex; 
				itemsLeftToSee --;
				arrayIndex++;
			}
			heapIndex ++;
		}
		
		return elements;
	}
	
	public void swap(int i, int j) {
		// swaps element i and j
		VoxelVertex save = array.get(i);
		array.set(i, array.get(j));
		array.set(j, save);
	}
	
	public void checkAndFix(int i) {
		while (i != 0 && array.get(parent(i)).getFValue() > array.get(i).getFValue()) { 
		       swap(i, parent(i)); 
		       i = parent(i); 
		    } 
	}
	  
	public boolean Contains(VoxelVertex k) {
		int itemsLeftToSee = heap_size;
		int index = 0;
		VoxelVertex currentVertex;
		while(itemsLeftToSee != 0) {
			currentVertex = array.get(index);
			
			if(currentVertex == k) {
				return true;
			} else if(currentVertex != null){
				itemsLeftToSee --;
			}
			index ++;
		}
		
		return false;
	}
	// Decreases value of key at index 'i' to new_val.  It is assumed that 
	// new_val is smaller than harr[i]. 
	public void decreaseVertex(int i, int new_val) 
	{ 
		VoxelVertex vertex = array.get(i);
		vertex.setFValue(new_val);
	    array.set(i, vertex); 

	    checkAndFix(i);	    
	} 
	  
	// Method to remove minimum element (or root) from min heap 
	public VoxelVertex extractMin() 
	{ 
	    if (heap_size <= 0) 
	        return null; 
	    if (heap_size == 1) 
	    { 
	        heap_size--; 
	        return array.get(0); 
	    } 
	  
	    // Store the minimum value, and remove it from heap 
	    VoxelVertex rootVertex = array.get(0); 
	    array.set(0, array.get(heap_size-1)); 
	    array.remove(heap_size-1);
	    heap_size--; 
	    amountOfDeletions++;
	    
	    MinHeapify(0); 
	    return rootVertex; 
	} 
	  
	  
	// This function deletes key at index i. It first reduced value to minus 
	// infinite, then calls extractMin() 
	void deleteKey(int i) 
	{ 
	    decreaseVertex(i, -10); 
	    extractMin(); 
	} 
	  
	// A recursive method to heapify a subtree with the root at given index 
	// This method assumes that the subtrees are already heapified 
	void MinHeapify(int i) 
	{ 
	    int l = left(i); 
	    int r = right(i); 
	    int smallest = i; 
	    if (l < heap_size && array.get(l).getFValue() < array.get(i).getFValue()) 
	        smallest = l; 
	    if (r < heap_size && array.get(r).getFValue() < array.get(smallest).getFValue()) 
	        smallest = r; 
	    if (smallest != i) 
	    { 
	        swap(i, smallest); 
	        MinHeapify(smallest); 
	    } 
	} 
	
	public static int main() 
	{ 
//	    MinHeap h = new MinHeap(20); 
//	    VoxelVertex voxel0 = new VoxelVertex(null);
//	    voxel0.setFValue(0);
//	    VoxelVertex voxel3 = new VoxelVertex(null);
//	    voxel3.setFValue(3);
//	    h.insertVertex(voxel3); 
//	    VoxelVertex voxel2 = new VoxelVertex(null);
//	    voxel2.setFValue(2);
//	    h.insertVertex(voxel2); 
//	    h.deleteKey(1);
//	    VoxelVertex voxel15 = new VoxelVertex(null);
//	    voxel15.setFValue(15);
//	    h.insertVertex(voxel15); 
//	    VoxelVertex voxel5 = new VoxelVertex(null);
//	    voxel5.setFValue(5);
//	    h.insertVertex(voxel5); 
//	    VoxelVertex voxel4 = new VoxelVertex(null);
//	    voxel4.setFValue(4);
//	    h.insertVertex(voxel4);
//	    VoxelVertex voxel45 = new VoxelVertex(null);
//	    voxel45.setFValue(45);
//	    h.insertVertex(voxel45); 
//	    System.out.println(h.Contains(voxel0));
//	    //output  2 4 1
//	    System.out.println(h.extractMin().getFValue());
//	    System.out.println(h.getMin().getFValue());
//	    h.decreaseVertex(2, 1); 
//	    System.out.println(h.getMin().getFValue()); 
//	    System.out.println(h.heap_size);
//	    System.out.println(voxel45.getFValue());
//	    System.out.println("contains 45");
//	    System.out.println(h.Contains(voxel45));
//	    System.out.println("contains 2");
//	    System.out.println(h.Contains(voxel2));
//	    System.out.println(h.Contains(voxel0));
		return 0; 
		} 

}

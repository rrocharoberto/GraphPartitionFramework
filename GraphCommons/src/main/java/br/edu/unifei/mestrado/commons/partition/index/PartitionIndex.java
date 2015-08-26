package br.edu.unifei.mestrado.commons.partition.index;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

/**
 * Defines the interface to index the nodes according with their partition
 * (setId).
 * 
 * @author roberto
 * 
 */
public interface PartitionIndex {

	/**
	 * Initializes internal structures.
	 * @param k
	 */
	public void initialize(int k);
	
	/**
	 * Adds the node to the setId.
	 * 
	 * @param setId
	 * @param node
	 */
	public void addNodeToSet(int setId, NodeWrapper node);

	/**
	 * Removes the node from the setId.
	 * 
	 * @param setId
	 * @param node
	 */
	public void removeNodeFromSet(int setId, NodeWrapper node);

	/**
	 * Search nodes of setId.
	 * 
	 * @param key
	 * @param setId
	 * @return
	 */
	public Iterable<NodeWrapper> queryNodes(int setId);

	/**
	 * Gets the amount of nodes inside setId.
	 * 
	 * @param setId
	 * @return
	 */
	public int queryAmountOfNodes(int setId);

	/**
	 * Gets the sum of weights of the nodes in setId.
	 * @param setId
	 * @return
	 */
	public int queryTotalNodeWeightPerPartition(int setId);

	public void clear();//update diagram

}

package br.edu.unifei.mestrado.commons.graph;


public abstract class GraphWrapper implements TransactionInterface, PartitionKeeper {

	protected String graphFileName;
	protected int sizeNodes = 0;
	protected boolean sizeNodesChanged = false;
	private Integer level;

	public static final Integer NO_LEVEL = null;

	public GraphWrapper(String graphFileName) {
		this(graphFileName, GraphWrapper.NO_LEVEL);
	}
	
	public GraphWrapper(String graphFileName, Integer level) {
		this.level = level;
		this.graphFileName = graphFileName;
	}

	public abstract void shutdown(boolean cleanUp);//update diagram
	
	public abstract void passivate();//update diagram

	public abstract void activate();//update diagram
	
	public abstract NodeWrapper getNode(long id);

	public abstract NodeWrapper createNode(long id, int weight);

	public abstract Iterable<NodeWrapper> getAllNodes();

	public abstract Iterable<NodeWrapper> getAllNodesStatic();

	public abstract Iterable<EdgeWrapper> getAllEdges();

	public abstract Iterable<EdgeWrapper> getAllEdgesStatic();
	
	public abstract EdgeWrapper getEdgeLinking(long va, long vb);

	public abstract EdgeWrapper createEdge(long id, int weight, long startNode, long endNode);

//	protected abstract NodeWrapper getOrCreateNode(Long id, int weight);

	/**
	 * Reads the graph within a transaction.
	 */
	public abstract void readGraph();

	/**
	 * Set lock property to false for all nodes.
	 */
	//passado para as classes filhas por questẽos de performance
	public abstract void unlockAllNodes();
	
	/**
	 * Set partition property to NO_PARTITION for all nodes.
	 */
	public abstract void resetPartitionAllNodes();
	

//	public void createEdge(long idEdge, int edgeWeight, long id1, int weight1, long id2, int weight2) {
//		NodeWrapper firstNode = getOrCreateNode(id1, weight1);
//		NodeWrapper secondNode = getOrCreateNode(id2, weight2);
//		createEdge(idEdge, edgeWeight, firstNode.getId(), secondNode.getId());
//	}

	public int getSizeNodes() {
		return sizeNodes;
	}

	public Integer getLevel() {
		return level;
	}

	
	public String getGraphFileName() {
		return graphFileName;
	}
}

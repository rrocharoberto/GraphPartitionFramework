package br.edu.unifei.mestrado.commons.graph;

public interface NodeWrapper {
	
	public static final int DEFAULT_WEIGHT = 1;

	public Iterable<EdgeWrapper> getEdges();
	
	public Iterable<EdgeWrapper> getEdgesStatic();

	public void setWeight(Integer weight);

	public void setPartition(Integer partition);
	
	public void resetPartition();

	public void lock();

	public void unlock();

	public void setD(Integer d);

	public int getWeight();

	public int getPartition();

	public boolean isLocked();

	public long getId();

	public int getD();
	
	public int getDegree();

	public boolean hasProperty(String key);

	public void setInsideOf(Long coarsedNodeId);

	public long getInsideOf();
	
	public boolean hasInsideOf();
	
	public void resetInsideOf();//update diagram
	
	public boolean isCoarsed() ;
	
	public void setCoarsed(boolean coarsed);
	
	public void setDegree(Integer degree);
	
	void setInnerNode(Object innerObject);
}

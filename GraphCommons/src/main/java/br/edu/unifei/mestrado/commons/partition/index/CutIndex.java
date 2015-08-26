package br.edu.unifei.mestrado.commons.partition.index;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;

/**
 * Defines the interface to index the edges on cut.
 * 
 * @author roberto
 * 
 */
public interface CutIndex {
	
	public void addEdgeToCut(EdgeWrapper edge);

	public void removeEdgeFromCut(EdgeWrapper edge);

	public int getCutWeight();

	public void setCutWeight(int cutWeight);

	public Iterable<EdgeWrapper> queryEdgesOnCut();

	public void remove();//update diagram

}

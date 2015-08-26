package br.edu.unifei.mestrado.commons.partition.index;

import java.util.HashMap;
import java.util.Map;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.mem.EdgeMem;
import br.edu.unifei.mestrado.commons.graph.mem.Relationship;
import br.edu.unifei.mestrado.commons.iterable.EdgeStaticIterable;

/**
 * Armazena as arestas do corte. <br>
 * Também é usada para armazenar as arestas internas de um set. <br>
 * 
 * Essa classe é super classe de AbstractPartitionIndex para manter as arestas
 * do corte. E é usada na classe PartitionUnfold.
 * 
 * TODO: revisar este comentário
 * 
 * @author roberto
 * 
 */
public class CutIndexMem implements CutIndex {

	public static int NO_ID = -1;
	
	private int id;

	/**
	 * id_edge | edge
	 * 
	 * Lista de edges que já entraram no set
	 */
	private Map<Long, Relationship> mapEdges = new HashMap<Long, Relationship>();
	
	private boolean forcedCutWeight = false;

	private int edgeCutWeight = 0;

	public CutIndexMem() {
		this.id = CutIndexMem.NO_ID;
	}
	
	public CutIndexMem(int id) {
		this.id = id;
	}

	public final int getId() {
		return id;
	}

	@Override
	public void addEdgeToCut(EdgeWrapper edge) {
		Relationship oldRel = mapEdges.put(edge.getId(), ((EdgeMem)edge).getInnerRelationship());
		if (oldRel == null) {
			edgeCutWeight += edge.getWeight();
		}
	}

	@Override
	public void removeEdgeFromCut(EdgeWrapper edge) {
		Relationship oldRel = mapEdges.remove(edge.getId());
		if (oldRel != null) {
			edgeCutWeight -= edge.getWeight();
		}
	}

	@Override
	public int getCutWeight() {
		return edgeCutWeight;
	}

	@Override
	public void setCutWeight(int cutWeight) {
		this.forcedCutWeight = true;
		this.edgeCutWeight = cutWeight;
	}
	
	@Override
	public Iterable<EdgeWrapper> queryEdgesOnCut() {
		if(forcedCutWeight) {
			throw new RuntimeException("There is no edges here. The cutWeight was forced.");
		}
		return new EdgeStaticIterable<Relationship>(mapEdges.values().iterator(), new EdgeMem(null));
	}
	
	@Override
	public void remove() {
		mapEdges.clear();
		edgeCutWeight = -1;
	}
	
	@Override
	public String toString() {
		return "EdgeCut: " + edgeCutWeight;
	}

//	@Override
//	public String toString() {
//		return "SET_" + id + " E: " + mapEdges.size();
//	}
}

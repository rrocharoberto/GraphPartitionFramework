package br.edu.unifei.mestrado.commons.partition.index;

import java.util.Iterator;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.RelationshipIndex;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.db.EdgeDB;
import br.edu.unifei.mestrado.commons.iterable.EdgeStaticIterable;

/**
 * Keeps the partition index of the nodes, using the DB internal index.
 * 
 * @author roberto
 * 
 */
public class CutIndexDB implements CutIndex {

	private static final String EDGE_ON_CUT = "EC";

	private RelationshipIndex indexCut = null;
	// private Index<Relationship> indexCut = null;
	
	private boolean forcedCutWeight = false;

	private int edgeCutWeight = 0;

	public CutIndexDB(RelationshipIndex indexCut) {
		this.indexCut = indexCut;
	}

	@Override
	public void addEdgeToCut(EdgeWrapper edge) {
		indexCut.add(((EdgeDB) edge).getInnerEdge(), GraphProperties.REL, EDGE_ON_CUT);
		edgeCutWeight += edge.getWeight();
	}

	@Override
	public void removeEdgeFromCut(EdgeWrapper edge) {
		indexCut.remove(((EdgeDB) edge).getInnerEdge(), GraphProperties.REL, EDGE_ON_CUT);
		edgeCutWeight -= edge.getWeight();
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
		Iterator<Relationship> it = indexCut.query(GraphProperties.REL, EDGE_ON_CUT).iterator();
		return new EdgeStaticIterable<Relationship>(it, new EdgeDB(null));
	}
	
	@Override
	public void remove() {
		indexCut.delete();
		edgeCutWeight = -1;
	}
	
	@Override
	public String toString() {
		return "EdgeCut: " + edgeCutWeight;
	}
}

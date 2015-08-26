package br.edu.unifei.mestrado.view;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import edu.uci.ics.jung.graph.Graph;

public class PartitionClusterer implements Transformer<Graph<NodeWrapper, EdgeWrapper>, Set<Set<NodeWrapper>>> {

	private int qtdPartitions;

	public PartitionClusterer(int qtdPartitions) {
		this.qtdPartitions = qtdPartitions;
	}

	//Separa o grafo em conjuntos de arestas de acordo com a particao
	@Override
	public Set<Set<NodeWrapper>> transform(Graph<NodeWrapper, EdgeWrapper> graph) {

		Map<Integer, Set<NodeWrapper>> hash = new HashMap<Integer, Set<NodeWrapper>>();

		Set<Set<NodeWrapper>> result = new HashSet<Set<NodeWrapper>>();

		for (NodeWrapper node : graph.getVertices()) {
			int partition = 0;
			if(node.hasProperty(GraphProperties.PARTITION)) {
				partition = node.getPartition();
			}
			Set<NodeWrapper> set = hash.get(partition);
			if(set == null) {
				set = new HashSet<NodeWrapper>();
				hash.put(partition, set);
				result.add(set);
			}
			set.add(node);
		}
		return result;
	}

	public int getQtdPartitions() {
		return qtdPartitions;
	}
}

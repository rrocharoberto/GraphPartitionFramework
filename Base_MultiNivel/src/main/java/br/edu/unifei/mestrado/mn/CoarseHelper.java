package br.edu.unifei.mestrado.mn;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphUtil;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;

public class CoarseHelper {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Faz a contração do grafo gn(level) salvando os novos vertices e arestas em newGraph.
	 * 
	 * @param level
	 * @param gn
	 * @param newGraph
	 * @return retorna true se houve diminuição de nós, senão retorna false.
	 */
	public boolean coarseOneLevel(final int level, GraphWrapper oldGraph, GraphWrapper newGraph) {
		logger.warn("Inicio do coarse NIVEL: " + level);
		long delta = System.currentTimeMillis();

		//coarseGraphRandom(oldGraph, newGraph);
		//coarseGraphSequence(oldGraph, newGraph);
		coarseGraphLessNodeWeight(oldGraph, newGraph);
		delta = System.currentTimeMillis() - delta;
		GraphUtil.printGraph(newGraph);

		logger.warn("Qtd de nodes do nivel " + level + ": " + newGraph.getSizeNodes()
				+ " tempo de contração: " + delta + " ms");

		// UTIL: indica se diminuiu a quantidade de vertices
		return oldGraph.getSizeNodes() > newGraph.getSizeNodes();
	}

	/**
	 * Sort the nodes by their weight.
	 * Call {@link this#coarseGraphWithNodes(GraphWrapper, Map)}
	 * Call {@link this#setInsideOfForGraph(GraphWrapper)}
	 * 
	 * Faz a contração usando emparelhamento ordenado pelo menor peso primeiro.
	 */
	protected void coarseGraphLessNodeWeight(GraphWrapper oldGraph, GraphWrapper newGraph) {

		// UTIL: Esse treeMap é usado para saber se o nó já foi ou não locked.
		Map<NodeWrapper, Boolean> nodes = new TreeMap<NodeWrapper, Boolean>(new Comparator<NodeWrapper>() {
			@Override
			public int compare(NodeWrapper o1, NodeWrapper o2) {
				int diff = o1.getWeight() - o2.getWeight();
				if (diff == 0) {
					return (int) (o1.getId() - o2.getId());
				}
				return diff;
			}
		});

		TransactionControl tc = new TransactionControl(oldGraph);
		tc.beginTransaction();
		// UTIL: esse loop já ordena e faz unlock dos nós.
		for (NodeWrapper node : oldGraph.getAllNodes()) { //TODO: isso é um problema grave de uso de memória
			nodes.put(node, false);
			node.resetInsideOf();
			tc.intermediateCommit();
		}
		tc.commit();

		coarseGraphWithNodes(newGraph, oldGraph, nodes);
		
		nodes.clear();
		
//		setInsideOfForGraph(oldGraph);
	}

	//TODO: se usar muita memória, esse método pode ser feito usando lock direto no node.
	private void coarseGraphWithNodes(GraphWrapper newGraph, final GraphWrapper oldGraph, Map<NodeWrapper, Boolean> nodes) { //update digram
		
		Matching mathcing = new Matching();
		TransactionControl tcNew = new TransactionControl(newGraph);
		TransactionControl tcOld = new TransactionControl(oldGraph);
		try {
			NodeWrapper v1, v2;
			tcNew.beginTransaction();
			tcOld.beginTransaction();
			for (NodeWrapper node : nodes.keySet()) {
				if (!nodes.get(node)) { // --->>> !node.isLocked()
					for (EdgeWrapper aresta : node.getEdges()) {
	
						v1 = aresta.getStartNode();
						v2 = aresta.getEndNode();
						if (!nodes.get(v1) && !nodes.get(v2)) { // se ambos vertices não foram usados
							if (v1.getId() == v2.getId()) {
								// se a aresta tem as duas pontas no mesmo vertice
								logger.error("1 - Verificar pq gera aresta pra ele mesmo. id: " + v1.getId());
								// TODO: Verificar se realmente deve descartar a aresta para ele mesmo.
								// UTIL: SIM!!! pode descartar
								continue;
							}
							mathcing.coarseNodes(newGraph, v1, v2);

							//Lock v1 e v2
							nodes.put(v1, true);
							nodes.put(v2, true);
							tcNew.intermediateCommit();
							tcOld.intermediateCommit();
							break; // break do for de dento, que é de arestas
						}
					}
				}
			}
		} catch (Throwable e) {
			logger.error("Erro contraindo grafo nivel: " + newGraph.getLevel(), e);
		} finally {
			tcNew.commit();
			tcOld.commit();
		}
		mathcing.processRemainingNodes(nodes, newGraph);//processa os nodes brancos
	}

	//Comentado devido ao não uso mais do nodeContainement
	//Agora a classe matching já está fazendo o setInsedOf direto.
//	/**
//	 * Seta o atributo insideOf do node do grafo antigo, se ele foi contraido em algum node novo.
//	 * @param oldGrafo
//	 */
//	private void setInsideOfForGraph(GraphWrapper oldGrafo) {
//		TransactionControl transaction = new TransactionControl(oldGrafo);
//		try{
//			transaction.beginTransaction();
//			NodeContainement insideOf = mathcing.getInsideOf();
//			for (NodeWrapper node : oldGrafo.getAllNodes()) {
//				Long insideId = insideOf.getNodeInsideOf(node);
//				if (insideId != null) {
//					node.setInsideOf(insideId);
//					transaction.intermediateCommit();
//				}
//			}
//		} catch (Throwable e) {
//			logger.error("Erro salvando insideOf.", e);
//		} finally {
//			transaction.commit();
//		}
//	}

}

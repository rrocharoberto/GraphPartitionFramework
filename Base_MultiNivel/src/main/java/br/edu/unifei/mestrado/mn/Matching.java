package br.edu.unifei.mestrado.mn;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.mn.EdgesCoarsed;
import br.edu.unifei.mestrado.commons.mn.TempEdge;

public class Matching {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

//	protected NodeContainement nodeContainer = new NodeContainement();
	
	protected EdgesCoarsed edgesCoarsed = new EdgesCoarsed();
int count = 0;
	public void coarseNodes(GraphWrapper newGraph, NodeWrapper v1, NodeWrapper v2) {
		// UTIL: cria o novo vertice, com um id novo para o grafo novo. Esse id vem do startNode
		NodeWrapper coarsedVertex = newGraph.createNode(v1.getId(), v1.getWeight() + v2.getWeight());
		// seta aqui, no fim do método, porque o addArestasFromVertex pega o insideOf se já existir, e daí
		// sempre vai existir... NÂO: sempre não. pq ele verifica o insideOf do other ;)
		// senão vai gerar uma auto aresta para o coarsedVertex
		// TODO: precisa voltar para o começo do método, pq o método addArestasFromRemainingVertex usa ele
		// por causa do hasInsideOf do node contraido
count++;
		//nodeContainer.setInsideOf(v1, coarsedVertex.getId());
		//nodeContainer.setInsideOf(v2, coarsedVertex.getId());
		v1.setInsideOf(coarsedVertex.getId());
		v2.setInsideOf(coarsedVertex.getId());

		coarsedVertex.setCoarsed(true); // indica que ele foi contraido

		logger.debug("Contraindo vertices: " + v1.getId() + "-" + v2.getId() + " : Coarsed "
				+ coarsedVertex.getId() + " Peso: " + coarsedVertex.getWeight());

		addEdgesFromNode(v1, v2.getId(), coarsedVertex);
		edgesCoarsed.imprimeArestasTmp();

		addEdgesFromNode(v2, v1.getId(), coarsedVertex);
		edgesCoarsed.imprimeArestasTmp();

		for (TempEdge edge : edgesCoarsed) {
			// adiciona a aresta no grafo
			newGraph.createEdge(edge.getId(), edge.getWeight(), edge.getStartNode(), edge.getEndNode());
		}
		
		edgesCoarsed.clear();
	}

	/**
	 * Adiciona as arestas ligadas no vertice oldVertex, em coarsedVertex. <br>
	 * Só adiciona a aresta na lista newArestas se a outra ponta da aresta ligada ao vertice oldVertex, também
	 * já estiver sido contraída antes.<br>
	 * Isso ocorre porque o metodo {@link #addArestasFromRemainingVertex(NodeWrapper, NodeWrapper, ArrayList)}
	 * ira adicionar as arestas para os vertices nao contraidos
	 * 
	 * @param oldNode
	 *            vertice do nivel N - 1
	 * @param oldNodeOtherId
	 *            id do vertice do nivel N - 1 correspondente a outra ponta de oldVertex, usado para não
	 *            incluir a aresta que está sendo contraida no próximo nivel
	 * @param coarsedNode
	 *            vertice do nivel N
	 * @param newArestas
	 *            lista para armazenar as novas arestas contraídas
	 */
	private void addEdgesFromNode(NodeWrapper oldNode, long oldNodeOtherId,
			NodeWrapper coarsedNode) {

		// for para adicionar as arestas de oldNode
		for (EdgeWrapper edge : oldNode.getEdges()) {
			NodeWrapper otherOld = edge.getOtherNode(oldNode);
			// a aresta que está sendo contraida não deve ser processada, pois ela está sendo contraida em um
			// único nó
			// UTIL: melhor explicando: as arestas entre os dois nós contraídos não sao processadas.
			// UTIL: esse if evita que arestas entre os dois nós, criem auto-arestas dos vertices contraidos.
			// :)
			if (otherOld.getId() != oldNodeOtherId) {// se a outra ponta for branca, então processa a arestas
				// UTIL: So adiciona uma nova aresta se o outro lado já estiver contraido
				// se não foi contraído, pode ser que ele seja agrupado através de outra aresta.

//				if (nodeContainer.hasInsideOf(otherOld)) {
				if (otherOld.hasInsideOf()) {
					// se o vertice já foi contraido, então pega o vertice correspondente,
					// long otherCoarsed = otherOld.getInsideOf();
//					long otherCoarsed = nodeContainer.getNodeInsideOf(otherOld);
					long otherCoarsed = otherOld.getInsideOf();
					
					// Repassa o peso da aresta contraida para a aresta nova.
					int weight = edge.getWeight();
					// Cria uma nova aresta com o novo node contraído e o outro node contraido anteriormente.
					TempEdge newEdge = new TempEdge(edge.getId(), weight, coarsedNode
							.getId(), otherCoarsed);
					
					// add aresta
					edgesCoarsed.addEdge(coarsedNode.getId(), otherCoarsed, newEdge);
				}
			}
		}
	}

	//***************************************************************************************************
	//DAQUI PARA BAIXO SÃO MÉTODOS PARA TRATAR AS ARESTAS REMANECENTES APÓS A CONTRAÇÃO DE TODOS OS NODES
	//***************************************************************************************************
	
	/*
	 * Processa os vertices que não foram emparelhados. Repassa os vertices não processados e suas respectivas
	 * arestas
	 */
	public void processRemainingNodes(Map<NodeWrapper, Boolean> nodes, GraphWrapper newGraph) {

		TransactionControl transaction = new TransactionControl(newGraph);
		try{
			transaction.beginTransaction();
			int qtdRepassados = 0;
			for (NodeWrapper oldNode : nodes.keySet()) {
				if (!nodes.get(oldNode)) {//TODO: verificar esse if, pois parece que nunca vai entrar aqui.
					logger.debug("Repassando vertice: " + oldNode.getId());
					// cria um novo, para não usar a mesma instancia do grafo anterior
					NodeWrapper newNode = newGraph.createNode(oldNode.getId(), oldNode.getWeight());
	
					// passa o node, pq ele tem as arestas. O newVertice ainda não tem arestas.
					addEdgesFromRemainingNodes(oldNode, newNode);
					edgesCoarsed.imprimeArestasTmp();
	
					for (TempEdge edge : edgesCoarsed) {
						// adiciona a aresta no grafo
						newGraph.createEdge(edge.getId(), edge.getWeight(), edge.getStartNode(), edge.getEndNode());
					}
					edgesCoarsed.clear();
					qtdRepassados++;
					transaction.intermediateCommit();
				}
			}
			logger.debug("Qtd total de novos nodes: {}  Qtd nodes repassados: {} ", newGraph.getSizeNodes(), qtdRepassados);
		} finally {
			transaction.commit();
		}
	}

	/*
	 * Adiciona as arestas dos vertices que não foram emparelhados.
	 */
	private void addEdgesFromRemainingNodes(NodeWrapper oldNode, NodeWrapper newNode) {

		// for para adicionar as arestas de oldV
		for (EdgeWrapper edge : oldNode.getEdges()) {
			NodeWrapper other = edge.getOtherNode(oldNode);
			long otherId = other.getId();

//			if (nodeContainer.hasInsideOf(other)) {//se other é preta (contraido)
			if (other.hasInsideOf()) {//se other é preta (contraido)
				// pega o vertice correspondente
//				otherId = nodeContainer.getNodeInsideOf(other);
				otherId = other.getInsideOf();

				// cria aresta
				// o newVertice sempre vai na ponta A.
				TempEdge newEdge = new TempEdge(edge.getId(), edge.getWeight(),
						newNode.getId(), otherId);
				
				edgesCoarsed.addEdge(newNode.getId(), otherId, newEdge);
			} else {
				throw new RuntimeException("Problema! Não pode ter 2 nodes adjacentes sem emparelhar: oldNode: "
						+ oldNode.getId() + " otherNode: " + other.getId());
			}
		}
	}

//	public NodeContainement getInsideOf() {
//		return nodeContainer;
//	}
}

package br.edu.unifei.mestrado.commons.graph.mem;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphFileReader;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.iterable.EdgeIterable;
import br.edu.unifei.mestrado.commons.iterable.EdgeStaticIterable;
import br.edu.unifei.mestrado.commons.iterable.NodeIterable;
import br.edu.unifei.mestrado.commons.iterable.NodeStaticIterable;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.CutIndexMem;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndexMem;

public class GraphMem extends GraphWrapper {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
//	private long idEdge = 0;

	private Map<Long, Node> mapNodes = new HashMap<Long, Node>();
	private Map<Long, Relationship> mapEdges = new HashMap<Long, Relationship>();
	
	private CutIndexMem indexEdgesOnCut;
	private PartitionIndexMem indexNodePartitions;

	public GraphMem(String graphFileName) {
		super(graphFileName);
		this.initDB();
		this.readGraph();
	}

	public GraphMem(String graphFileName, Integer level) {
		super(graphFileName, level);
		this.initDB();
	}

	//*********************************************
	//início métodos da interface TransactionInterface
	@Override
	public void beginTransaction() {
	}

	@Override
	public void endTransaction() {
	}

	@Override
	public void finish() {
	}

	@Override
	public void failure() {
	}
	//*********************************************
	//fim métodos da interface TransactionInterface

	//*********************************************
	//início métodos da interface PartitionKeeper
	public void createNewCutIndex() {
		indexEdgesOnCut = new CutIndexMem();
	}
	
	@Override
	public void createNewPartitionIndex() {
		indexNodePartitions = new PartitionIndexMem();
	}
	
	@Override
	public CutIndex getCurrentCutIndex() {
		return indexEdgesOnCut;
	}
	
	@Override
	public PartitionIndex getCurrentPartitionIndex() {
		return indexNodePartitions;
	}

	//*********************************************
	//fim métodos da interface PartitionKeeper

	@Override
	public void shutdown(boolean cleanUp) {
	}
	
	@Override
	public void activate() {
	}
	
	@Override
	public void passivate() {
	}
	
	private void initDB() {
		createNewCutIndex();
		createNewPartitionIndex();
	}
	
	@Override
	public void readGraph() {		
		GraphFileReader reader = new GraphFileReader();
		reader.importGraph(this, graphFileName);
	}
	
	@Override
	public NodeMem createNode(long id, int weight) {
		Node node = mapNodes.get(id);
		if (node == null) {
			node = new Node(id, weight);
			mapNodes.put(id, node);
//			grafo.addVertex(node);
			sizeNodes++;
		}
		return new NodeMem(node);
	}

	@Override
	public NodeWrapper getNode(long id) {
		Node node = getInnerNode(id);
		return new NodeMem(node);
	}

//	@Override
//	protected NodeWrapper getOrCreateNode(Long id, int weight) {
//		return createNode(id, weight);
//	}

	@Override
	public EdgeWrapper createEdge(long id, int weight, long startNode, long endNode) {
		Node va = getInnerNode(startNode);
		Node vb = getInnerNode(endNode);
		Relationship aresta = new Relationship(id, weight, va, vb);

		if (mapEdges.containsKey(id)) {
			throw new RuntimeException("Check why edge " + id + " has already exist in the graph.");
			//logger.warn("Aresta " + aresta + " jah existe no grafo");
		}

		va.addEdge(aresta);
		vb.addEdge(aresta);
		mapEdges.put(aresta.getId(), aresta);
//		grafo.addEdge(aresta, aresta.getStartNode(), aresta.getEndNode());
		return new EdgeMem(aresta);
	};

	@Override
	public Iterable<EdgeWrapper> getAllEdges() {
		return new EdgeIterable<Relationship, EdgeMem>(mapEdges.values().iterator(), Relationship.class, EdgeMem.class);
	}
	
	@Override
	public Iterable<EdgeWrapper> getAllEdgesStatic() {
		return new EdgeStaticIterable<Relationship>(mapEdges.values().iterator(), new EdgeMem());
	}

	@Override
	public Iterable<NodeWrapper> getAllNodes() {
		return new NodeIterable<Node, NodeMem>(mapNodes.values().iterator(), Node.class, NodeMem.class);
	}
	
	@Override
	public Iterable<NodeWrapper> getAllNodesStatic() {
		return new NodeStaticIterable<Node>(mapNodes.values().iterator(), new NodeMem());
	}

	@Override
	public int getSizeNodes() {
		return mapNodes.size();
	}

	@Override
	public EdgeWrapper getEdgeLinking(long va, long vb) {
		Node na = getInnerNode(va);
		Node nb = getInnerNode(vb);
		Relationship edge = na.getArestaLinking(nb);//grafo.findEdge(na, nb);
		if (edge == null) {
			return null;
		}

		EdgeWrapper rel = new EdgeMem(edge);
		return rel;
	}

//	public UndirectedSparseGraph<Node, Relationship> getGraphToView() {
//		return grafo;
//	}
	
	public void unlockAllNodes() {
		double delta = System.currentTimeMillis();
		Iterable<Node> all = mapNodes.values();
		for (Node node : all) {
			node.unLock();
		}
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto para unlock all nodes: {} ms", delta);
	}

	public void resetPartitionAllNodes() {
		double delta = System.currentTimeMillis();
		Iterable<Node> all = mapNodes.values();
		for (Node node : all) {
			node.setPartition(AbstractPartition.NO_PARTITION);
		}
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto para reset partition for all nodes: {} ms", delta);
	}
	
	private Node getInnerNode(long id) {
		Node node = mapNodes.get(id);
		if (node == null) {
			throw new RuntimeException("Check why node " + id + " is null !");
		}
		return node;
	}
}

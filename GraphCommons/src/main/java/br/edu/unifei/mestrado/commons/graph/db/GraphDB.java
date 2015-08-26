package br.edu.unifei.mestrado.commons.graph.db;

import java.io.File;
import java.util.Iterator;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.iterable.EdgeIterable;
import br.edu.unifei.mestrado.commons.iterable.EdgeStaticIterable;
import br.edu.unifei.mestrado.commons.iterable.NodeIterable;
import br.edu.unifei.mestrado.commons.iterable.NodeStaticIterable;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.CutIndexDB;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndexDB;

public class GraphDB extends GraphWrapper {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// private static final String DB_PATH = "neo4j-";
	public static RelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");
	
	public static final boolean REUSE_DB_YES = true;
	public static final boolean REUSE_DB_NO = false;

	private GraphDatabaseService graphDb;
	
	//indice para todos os vértices, por id
	private Index<Node> indexNodesById;
	private RelationshipIndex indexRelationship;

	//indices de particionamento
	private Index<Node> indexNodePartitions;
	private RelationshipIndex indexEdgesOnCut;

	private Transaction tx = null;

	private boolean reuseDB = false;//TODO: revisar o reuseDB, acho que não precisa mais, pois todo grafo já estará no banco

	public GraphDB(String graphFileName) {
		super(graphFileName);
		this.reuseDB = true;
		this.initDB();
		this.readGraph();
	}

//	public GraphDB(String graphFileName, Integer level) {//update diagram
//		super(graphFileName, level);
//		this.reuseDB = true;
//		this.initDB();
//	}

	//used to import graphs and by 2-way multilevel
	public GraphDB(String dbFileName, Integer level, boolean reuseDB) {
		super(dbFileName, level);
		this.reuseDB = reuseDB;
		this.initDB();
	}
	
	@Override
	public void activate() {
		initDB();
	}
	
	@Override
	public void passivate() {
		shutdown(false);
	}

	//*********************************************
	//início métodos da interface TransactionInterface
	@Override
	public void beginTransaction() {
		tx = graphDb.beginTx();
	}

	@Override
	public void endTransaction() {
		if(sizeNodesChanged) {
		// UTIL: salvar o sizeNodes no referenceNode para saber ele na proxima execução.
			Node referenceNode = graphDb.getReferenceNode();
			referenceNode.setProperty(GraphProperties.SIZE_NODES, sizeNodes);
		}
		tx.success();
		sizeNodesChanged = false;
	}

	@Override
	public void finish() {
		if (tx != null) {
			tx.finish();
			//UTIL: pq seta tx = null? para não chamar o finish() mais de 1 vez
			tx = null;
		}
	}
	
	@Override
	public void failure() {
		if (tx != null) {
			tx.failure();
		}
	}
	//*********************************************
	//fim métodos da interface TransactionInterface

	//*********************************************
	//início métodos da interface PartitionKeeper
	
	public void createNewCutIndex() {
		indexEdgesOnCut = graphDb.index().forRelationships("indexEdgesOnCut");
	}
	
	@Override
	public void createNewPartitionIndex() {
		indexNodePartitions = graphDb.index().forNodes("nodePartitions");
	}
	
	@Override
	public CutIndex getCurrentCutIndex() {
		return new CutIndexDB(indexEdgesOnCut);
	}
	
	@Override
	public PartitionIndex getCurrentPartitionIndex() {
		return new PartitionIndexDB(indexNodePartitions);
	}

	//*********************************************
	//fim métodos da interface PartitionKeeper

	@Override
	public void shutdown(boolean cleanUp) {
		if (graphDb != null) {
			if(cleanUp) {
				if (getLevel() != null) {
					logger.warn("Shutting down database " + graphFileName + " level " + getLevel() + " ...");
				} else {
					logger.warn("Shutting down database " + graphFileName + " ...");
				}
			} else {
				if (getLevel() != null) {
					logger.warn("Passivating database " + graphFileName + " level " + getLevel() + " ...");
				} else {
					logger.warn("Passivating database " + graphFileName + " ...");
				}
			}
			graphDb.shutdown();
			graphDb = null;

			if(cleanUp) {
				if (getLevel() != null) {
					String dbFullPath = getDBFullPath();
					deleteFileOrDirectory(new File(dbFullPath));
				}
			}
		}
	}

	/**
	 * Retorna a aresta que liga os 2 vertices, se existir.
	 * 
	 * TODO: esse método não é eficiente, precisa melhorar.
	 */
	@Override
	public EdgeWrapper getEdgeLinking(long va, long vb) {
		Node na = indexNodesById.get(GraphProperties.ID, va).getSingle();
		Node nb = indexNodesById.get(GraphProperties.ID, vb).getSingle();
		Relationship edge = indexRelationship.query(GraphProperties.REL, KNOWS.name(), na, nb).getSingle();
//		Relationship edge = GraphDatabaseUtil.getExistingRelationshipBetween(na, nb,
//				Traversal.expanderForTypes(KNOWS));

		if (edge == null) {
			return null;
		}
		EdgeWrapper rel = new EdgeDB(edge);
		return rel;
	}

	private void initDB() {
		String dbFullPath = getDBFullPath();
		if (!reuseDB) {
			deleteFileOrDirectory(new File(dbFullPath));
		} else {
			if(!new File(dbFullPath).exists()) {
				throw new RuntimeException("Database directory " + dbFullPath + " does not exist.");
			}
		}
		graphDb = new EmbeddedGraphDatabase(dbFullPath);
		indexNodesById = graphDb.index().forNodes("nodes");
		indexRelationship = graphDb.index().forRelationships("relationships");
		
		createNewCutIndex();
		createNewPartitionIndex();

		registerShutdownHook();
	}

	private String getDBFullPath() {
		String dbFullPath = graphFileName;
		if (getLevel() != null) {
			dbFullPath += "_level-" + getLevel();
		}
		return dbFullPath;
	}

	@Override
	public void readGraph() {
		if (reuseDB) {
//			TransactionControl transaction = new TransactionControl(this);
//			try {
//				transaction.beginTransaction();
				// UTIL: recupera o sizeNodes do referenceNode para poder saber a quantidade de nós do banco.
				Node referenceNode = graphDb.getReferenceNode();
				Integer size = (Integer) referenceNode.getProperty(GraphProperties.SIZE_NODES);
				if (size != null) {
					sizeNodes = size;
				}
//			} finally {
//				transaction.commit();
//			}
			return;
		}

		// UTIL: não faz mais a leitura do arquivo para o banco dentro desse código, o banco já precisa existir.
		// super.readGraph();
	}

	@Override
	public NodeWrapper createNode(long id, int weight) {
		Node node = indexNodesById.get(GraphProperties.ID, id).getSingle();
		if (node == null) {
			sizeNodes++;
			sizeNodesChanged = true;
			node = graphDb.createNode();
			node.setProperty(GraphProperties.ID, id);
			node.setProperty(GraphProperties.LOCKED, false);
			node.setProperty(GraphProperties.PARTITION, -1);
			node.setProperty(GraphProperties.WEIGHT, weight);

			indexNodesById.add(node, GraphProperties.ID, id);
		}
		return new NodeDB(node);
	}

	@Override
	public NodeWrapper getNode(long id) {
		Node node = getInnerNode(id);
		return new NodeDB(node);
	}

//	@Override
//	protected NodeWrapper getOrCreateNode(Long id, int weight) {
//		return createNode(id, weight);
//	}

	private void registerShutdownHook() {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown(true);
			}
		});
	}

	private void deleteFileOrDirectory(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					deleteFileOrDirectory(child);
				}
			}
			file.delete();
		}
	}

	// TODO: Criar um método que já receba diretamente um objeto nodeWrapper
	@Override
	public EdgeWrapper createEdge(long id, int weight, long startNode, long endNode) {
		Node firstNode = getInnerNode(startNode);
		Node secondNode = getInnerNode(endNode);

		Relationship rel = firstNode.createRelationshipTo(secondNode, KNOWS);
		indexRelationship.add(rel, GraphProperties.REL, KNOWS.name());
		rel.setProperty(GraphProperties.ID, id);
		rel.setProperty(GraphProperties.WEIGHT, weight);

		return new EdgeDB(rel);
	};

	@Override
	public Iterable<EdgeWrapper> getAllEdges() {
		return new EdgeIterable<Relationship, EdgeDB>(indexRelationship.query(GraphProperties.REL, KNOWS.name()).iterator(), Relationship.class, EdgeDB.class);
	}

	@Override
	public Iterable<EdgeWrapper> getAllEdgesStatic() {
		return new EdgeStaticIterable<Relationship>(indexRelationship.query(GraphProperties.REL, KNOWS.name()).iterator(), new EdgeDB());
	}

	@Override
	public Iterable<NodeWrapper> getAllNodes() {
		Iterator<Node> iter = graphDb.getAllNodes().iterator();
		iter.next();
		return new NodeIterable<Node, NodeDB>(iter, Node.class, NodeDB.class);
	}

	@Override
	public Iterable<NodeWrapper> getAllNodesStatic() {
		Iterator<Node> iter = graphDb.getAllNodes().iterator();
		iter.next();
		return new NodeStaticIterable<Node>(iter, new NodeDB());
	}

	public void unlockAllNodes() {
		Node refNode = graphDb.getReferenceNode();
		double delta = System.currentTimeMillis();
		TransactionControl transaction = new TransactionControl(this);
		try {
			transaction.beginTransaction();
			
			Iterable<Node> all = graphDb.getAllNodes();
			for (Node node : all) {
				if (node.getId() != refNode.getId()) {//TODO: acho que pode melhorar fazendo igual de objetos
					node.setProperty(GraphProperties.LOCKED, false);
					transaction.intermediateCommit();
				}
			}
		} finally {
			transaction.commit();
		}
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto para unlock all nodes: {} ms", delta);
	}

	public void resetPartitionAllNodes() {
		Node refNode = graphDb.getReferenceNode();
		double delta = System.currentTimeMillis();
		TransactionControl transaction = new TransactionControl(this);
		try {
			transaction.beginTransaction();
			
			Iterable<Node> all = graphDb.getAllNodes();
			for (Node node : all) {
				if (node.getId() != refNode.getId()) {
					node.setProperty(GraphProperties.PARTITION, AbstractPartition.NO_PARTITION);
					transaction.intermediateCommit();
				}
			}
		} finally {
			transaction.commit();
		}
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto para reset partition for all nodes: {} ms", delta);
	}
	
	private Node getInnerNode(long id) {
		Node node = indexNodesById.get(GraphProperties.ID, id).getSingle();
		if (node == null) {
			throw new RuntimeException("Check why node " + id + " is null !");
		}
		return node;
	}
	/*
	 
	//TODO_OK: mover esse método para o abstractPartition -> já existe o método printSets lah
	public void printPartitions() {
		logger.debug("{}", new Object() {
			public String toString() {

				StringBuffer b = new StringBuffer();
				IndexHits<Node> part1 = indexPartition.query(GraphProperties.PARTITION,
						AbstractPartition.PART_1);
				b.append("Vertices from partition 1: ");
				for (Node node : part1) {
					b.append(node.getProperty(GraphProperties.ID) + ",");
				}
				b.append("\n");

				IndexHits<Node> part2 = indexPartition.query(GraphProperties.PARTITION,
						AbstractPartition.PART_1);
				b.append("Vertices from partition 2: ");
				for (Node node : part2) {
					b.append(node.getProperty(GraphProperties.ID) + ",");
				}
				return b.toString();
			}
		});
	}

	 */
}

package br.edu.univas.mestrado.grafo.generator;

import java.io.File;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import br.edu.unifei.mestrado.commons.graph.GraphProperties;

public class GrafoNeoDB {

	protected int sizeNodes = 0;
	private static RelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");

	private GraphDatabaseService graphDb;
	private Index<Node> indexName;
	private Index<Relationship> indexRelationship;

	private Transaction tx = null;

	private String databaseName;

	public GrafoNeoDB(String databaseName) {
		super();
		this.databaseName = databaseName;
	}

	public void beginTransaction() {
		tx = graphDb.beginTx();
	}

	public void endTransaction() {
		Node referenceNode = graphDb.getReferenceNode();
		referenceNode.setProperty(GraphProperties.SIZE_NODES, sizeNodes);

		tx.success();
	}

	public void finish() {
		if (tx != null) {
			tx.finish();
		}
	}

	public void shutdown() {
		System.out.println("Shutting down database " + databaseName + " ...");
		if (graphDb != null) {
			graphDb.shutdown();
		}
	}

	public void initDB() {
		String dbFullPath = databaseName;
		deleteFileOrDirectory(new File(dbFullPath));
		graphDb = new EmbeddedGraphDatabase(dbFullPath);
		indexName = graphDb.index().forNodes("nodes");
		indexRelationship = graphDb.index().forRelationships("relationships");

		registerShutdownHook();
	}

	public Node createNode(long id, int weight) {
		Node node = indexName.get(GraphProperties.ID, id).getSingle();
		if (node == null) {
			sizeNodes++;
			node = graphDb.createNode();
			node.setProperty(GraphProperties.ID, id);
			node.setProperty(GraphProperties.LOCKED, false);
			node.setProperty(GraphProperties.PARTITION, -1);
			node.setProperty(GraphProperties.WEIGHT, weight);
			node.setProperty(GraphProperties.D, 0);

			indexName.add(node, GraphProperties.ID, id);
		}
		return node;
	}

	public Node getNode(long id) {
		Node node = indexName.get(GraphProperties.ID, id).getSingle();
		if (node == null) {
			return null;
		}
		return node;
	}

	protected Node getOrCreateNode(Long id, int weight) {
		return createNode(id, weight);
	}

	private void registerShutdownHook() {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				System.out.println("Shutting down database " + databaseName + " ...");
				graphDb.shutdown();
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

	public Relationship createEdge(long idEdge, int newWeight, long startNode, long endNode) {
		Node firstNode = indexName.get(GraphProperties.ID, startNode).getSingle();
		Node secondNode = indexName.get(GraphProperties.ID, endNode).getSingle();

		Relationship rel = firstNode.createRelationshipTo(secondNode, KNOWS);
		indexRelationship.add(rel, GraphProperties.REL, KNOWS.name());
		rel.setProperty(GraphProperties.ID, idEdge);
		rel.setProperty(GraphProperties.WEIGHT, newWeight);

		return rel;
	}

	public void createChain(Long id1, Long id2, int nodeWeight, long idEdge, int relWeight) {
		getOrCreateNode(id1, nodeWeight);
		getOrCreateNode(id2, nodeWeight);
		createEdge(idEdge, relWeight, id1, id2);
	}

}

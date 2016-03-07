package br.edu.unifei.mestrado.generator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Graph;

public class ClusterGenerator extends BaseGenerator {

	private int amount;
	private int k;
	protected LinkedList<Data> nodes = new LinkedList<ClusterGenerator.Data>();
	private Map<String, String> edges = new HashMap<String, String>();
	protected int currentIndex = 0;
	int side = -1;
	private int minDegree;
	private int maxDegree;
	int cut;

	public ClusterGenerator(int amount, int minDegree, int maxDegree, int k) {
		this.amount = amount;
		this.minDegree = minDegree;
		this.maxDegree = maxDegree;
		this.k = k;
	}

	@Override
	public void begin() {
		nodes.clear();
		for (int i = 0; i < k; i++) {
			Data newData = new Data(newNodeId(), 0, true, nextSide());
			add(newData);
		}

		for (int i = 0; i < k - 1; i++) {
			Data source = nodes.get(i);
			Data connectTo = nodes.get(i + 1);
			connect(source, connectTo);
			cut++;
		}
		Data first = nodes.getFirst();
		Data last = nodes.getLast();
		connect(first, last);
		cut++;
		// while(edges.size() < k) {
		// Data source = nodes.get(random.nextInt(nodes.size()));
		//
		// Data connectTo = null;
		// do {
		// connectTo = nodes.get(random.nextInt(nodes.size()));
		// } while (connectTo.side != source.side);
		//
		// connect(source, connectTo);
		// }
	}

	@Override
	public boolean nextEvents() {

		Data connectTo = null;
		do {
			connectTo = nodes.get(random.nextInt(nodes.size()));
		} while (connectTo.side != getSide() || connectTo.degree() >= maxDegree);

		Data newData = new Data(newNodeId(), 0, true, connectTo.side);
		add(newData);
		connect(connectTo, newData);// sempre vai retornar true
		nextSide();
		return nodes.size() < amount;
	}

	@Override
	public void end() {
		addRemainingEdges();
		addCutEdges();
		super.end();
	}

	private void addCutEdges() {
		System.out.println("adding cut edge");

		int max = minDegree + random.nextInt(maxDegree);
		while (cut <= max) {

			Data source = nodes.get(random.nextInt(nodes.size()));

			Data connectTo = null;
			do {
				connectTo = nodes.get(random.nextInt(nodes.size()));
			} while (connectTo.side == source.side);

			if (connect(source, connectTo)) {
				cut++;
			}
		}
		System.out.println("cut: " + cut);
	}

	private void addRemainingEdges() {
		for (Data node : nodes) {
			if (getMinDegree() < minDegree) {
				while (node.degree() < minDegree) {
					addRandomEdge(node);
				}
			}
		}
	}

	private void addRandomEdge(Data source) {
		Data connectTo = null;
		do {
			do {
				connectTo = nodes.get(random.nextInt(nodes.size()));
			} while (connectTo.side != source.side || connectTo.id == source.id || connectTo.degree() >= maxDegree);

		} while (!connect(source, connectTo));
	}

	protected void add(Data data) {
		nodes.add(data);
		addNode(data.id);
	}

	protected boolean connect(Data d1, Data d2) {
		String key = String.format("%s_%s", d1.id, d2.id);
		if (!edges.containsKey(key)) {
			d1.addConnected(d2, d1);
			d2.addConnected(d1, d2);

			addEdge(getEdgeId(d1, d2), d1.id, d2.id);
			edges.put(key, key);
			return true;
		}
		return false;
	}

	protected String newNodeId() {
		return String.format("%d", currentIndex++);
	}

	protected String getEdgeId(Data d1, Data d2) {
		if (d1.hashCode() > d2.hashCode()) {
			Data t = d1;
			d1 = d2;
			d2 = t;
		}
		return String.format("%s--%s", d1.id, d2.id);
	}

	private int nextSide() {
		side++;
		return side % k;
	}

	private int getSide() {
		return side % k;
	}

	protected static class Data {
		String id;
		int distance;
		boolean path;
		int side;
		private Map<String, Data> connected;

		Data(String id, int distance, boolean path, int side) {
			this.id = id;
			this.distance = distance;
			this.connected = new HashMap<String, Data>();
			this.path = path;
			this.side = side;
		}

		public void addConnected(Data d1, Data d2) {
			connected.put(d1.id, d2);
		}

		public boolean isConnected(Data data) {
			return connected.containsKey(data.id);
		}

		int degree() {
			return connected.size();
		}
	}

	public int getMinDegree() {
		int degree = Integer.MAX_VALUE;
		for (Data data : nodes) {
			if (data.degree() < degree) {
				degree = data.degree();
			}
			// System.out.println("id: " + data.id + " degree: " +
			// data.degree());
		}
		// System.out.println("minDegree: " + degree);
		return degree;
	}

	public int countImportant = 0;

	public void configureNodes(Graph graph) {
		for (Data data : nodes) {
			if (data.side == 0) {
				countImportant++;
				// System.out.println("node " + data.id + " is important.");
				graph.getNode(data.id).addAttribute("ui.class", "important");
			}
		}
		// for (Data data : nodes) {
		// System.out.println("id: " + data.id + " degree: " + data.degree());
		// }
		System.out.println("countImportant: " + countImportant);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClusterGenerator [n=");
		builder.append(amount);
		builder.append(", k=");
		builder.append(k);
		builder.append(", minDegree=");
		builder.append(minDegree);
		builder.append(", cut=");
		builder.append(cut);
		builder.append("]");
		return builder.toString();
	}
}

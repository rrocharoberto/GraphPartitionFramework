package br.edu.unifei.mestrado.generator;

/*
 * Copyright 2006 - 2013
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Graph;

/**
 * Generate a Lobster tree. Lobster are trees where the distance between any
 * node and a root path is less than 2. In this generator, the max distance can
 * be customized.
 */
public class CustomLobsterGenerator extends BaseGenerator {
	private int amount;
	/**
	 * Max distance from any node to a node of the root path.
	 */
	protected int maxDistance = 2;
	/**
	 * Max degree of nodes.
	 */
	protected int maxDegree = 10;
	/**
	 * Delete some node in step.
	 */
	protected boolean delete = false;
	/**
	 * Average node count. Used in delete-mode to maintain an average count of
	 * nodes.
	 */
	protected int averageNodeCount = 200;
	/**
	 * Used to generate new node index.
	 */
	protected int currentIndex = 0;
	/**
	 * Node data.
	 */
	protected LinkedList<Data> nodes;

	/**
	 * Main constructor to a Lobster generator.
	 */
	public CustomLobsterGenerator(int amount) {
		this(amount, 2, -1);
	}

	/**
	 * Constructor allowing to customize maximum distance to the root path and
	 * maximum degree of nodes.
	 * 
	 * @param maxDistance
	 *            max distance to root path
	 * @param maxDegree
	 *            max degree of nodes
	 */
	public CustomLobsterGenerator(int amount, int maxDistance, int maxDegree) {
		this.amount = amount;
		this.maxDistance = maxDistance;
		if(maxDegree <= 0) {
			throw new RuntimeException("maxDegree must be greater than zero.");
		}
		this.maxDegree = maxDegree;
		this.nodes = new LinkedList<Data>();
	}

	/**
	 * Constructor allowing to customize maximum distance to the root path.
	 * 
	 * @param maxDistance
	 *            max distance to root path
	 */
	public CustomLobsterGenerator(int amount, int maxDistance) {
		this(amount, maxDistance, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	@Override
	public void begin() {
		nodes.clear();
		Data connectTo = new Data(newNodeId(), 0, true, true);
		Data newData = new Data(newNodeId(), 0, true, false);
		add(connectTo);
		add(newData);
		connect(connectTo, newData);
	}
	
	boolean side = true;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		Data connectTo = null;

		do {
			connectTo = nodes.get(random.nextInt(nodes.size()));
		} while (connectTo.distance >= maxDistance || connectTo.degree() >= maxDegree || connectTo.side != side);

		side = !side;
		Data newData = null;

		if (connectTo.path && connectTo.degree() <= 4) {//>= connectTo.distance) {
			newData = new Data(newNodeId(), 0, true, connectTo.side);
		} else {
			newData = new Data(newNodeId(), connectTo.distance + 1, false, connectTo.side);
		}

		add(newData);
		connect(connectTo, newData);
		
		addRemainingEdges();
		return nodes.size() < amount;
	}
	
	private void addRemainingEdges() {
		Data source = null;
		do {
			source = nodes.get(random.nextInt(nodes.size()));
		} while (source.distance >= maxDistance || source.degree() > maxDegree);

		while(source.degree() < maxDegree) {
			if(!addRandomEdge(source)) {
				return;
			}
		}
	}

	private boolean addRandomEdge(Data source) {
		int count = 0;
		
		Data connectTo = null;
//		do {
			do {
				connectTo = nodes.get(random.nextInt(nodes.size()));
				if(count == 1) {
					return false;
				}
				count++;
			} while (connectTo.distance >= maxDistance || connectTo.degree() > maxDegree || connectTo.side != source.side);
//		} while(source.isConnected(connectTo));
		
		if(source != null && connectTo != null) {
			connect(source, connectTo);
			return true;
		} else {
			return false;
		}
	}
	
	public int countImportant = 0;
	
	protected void add(Data data) {
		nodes.add(data);
		addNode(data.id);
	}

	protected void connect(Data d1, Data d2) {
		d1.addConnected(d2, d1);
		d2.addConnected(d1, d2);

		addEdge(getEdgeId(d1, d2), d1.id, d2.id);
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

	protected static class Data {
		String id;
		int distance;
		boolean path;
		boolean side;
		private Map<String, Data> connected;

		Data(String id, int distance, boolean path, boolean side) {
			this.id = id;
			this.distance = distance;
			this.connected = new HashMap<String, CustomLobsterGenerator.Data>();
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

	public void configureNodes(Graph graph) {
		for (Data data : nodes) {
			if(data.side) {
				countImportant++;
//				System.out.println("node " + data.id + " is important.");
				graph.getNode(data.id).addAttribute("ui.class", "important");
			}
		}
		System.out.println("countImportant: " + countImportant);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CustomLobsterGenerator [n=");
		builder.append(amount);
		builder.append(", maxDistance=");
		builder.append(maxDistance);
		builder.append(", maxDegree=");
		builder.append(maxDegree);
		builder.append("]");
		return builder.toString();
	}
	
	
}

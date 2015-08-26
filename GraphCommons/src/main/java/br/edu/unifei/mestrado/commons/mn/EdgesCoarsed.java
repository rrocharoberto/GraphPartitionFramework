package br.edu.unifei.mestrado.commons.mn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Classe para tratar as arestas paralelas.
 * 
 * @author roberto
 * 
 */
public class EdgesCoarsed implements Iterable<TempEdge> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// node1 | node2 | edge
	private Map<Long, Map<Long, TempEdge>> map;

	public EdgesCoarsed() {
		map = new HashMap<Long, Map<Long, TempEdge>>();
	}

	/**
	 * @param nodeA
	 *            ponta A
	 * @param nodeB
	 *            ponta B
	 * @param newEdge
	 *            Aresta.
	 */
	public void addEdge(long nodeA, long nodeB, TempEdge newEdge) {
		Long nodeId1, nodeId2;
		if (map.containsKey(nodeA)) {
			nodeId1 = nodeA;
			nodeId2 = nodeB;
		} else {
			nodeId1 = nodeB;
			nodeId2 = nodeA;
		}

		// handle Map<Long, Map<Long, TempEdges>>
		Map<Long, TempEdge> inMap = map.get(nodeId1);
		if (inMap == null) {
			inMap = new HashMap<Long, TempEdge>();
			map.put(nodeId1, inMap);
		}

		// handle Map<Long, TempEdges>
		TempEdge existingEdge = inMap.get(nodeId2);
		if (existingEdge == null) {
			inMap.put(nodeId2, newEdge);
		} else {
			existingEdge.sumWeight(newEdge.getWeight());
		}
	}

	@Override
	public Iterator<TempEdge> iterator() {
		return new EdgesCoarsedIterator();
	}

	public void clear() {
		for (Map<Long, TempEdge> v1 : map.values()) {
			v1.clear();
		}
		map.clear();
	}

	public void imprimeArestasTmp() {
		final EdgesCoarsed iter = this;
		logger.debug("Arestas: {}", new Object() {
			@Override
			public String toString() {
				StringBuffer b = new StringBuffer();
				for (TempEdge edge : iter) {
					b.append(edge + ", ");
				}
				return b.toString();
			}
		});
	}

	private class EdgesCoarsedIterator implements Iterator<TempEdge> {

		private Iterator<Map<Long, TempEdge>> iterV1 = map.values().iterator();
		private Iterator<TempEdge> iterEdge = null;

		public EdgesCoarsedIterator() {
			if (iterV1.hasNext()) {
				iterEdge = iterV1.next().values().iterator();
			}
		}

		@Override
		public boolean hasNext() {
			if (iterEdge != null && iterEdge.hasNext()) {
				return true;
			} else {
				if (iterV1 != null && iterV1.hasNext()) {
					iterEdge = iterV1.next().values().iterator(); // tenta pegar no V2
				} else {
					return false;
				}
			}
			return iterEdge.hasNext();
		}

		@Override
		public TempEdge next() {
			return iterEdge.next();
		}

		@Override
		public void remove() {
		}
	}

	// public TempEdges removeEdge(long nodeA, long nodeB, long edgeId) {
	// Long nodeId1, nodeId2;
	// if(map.containsKey(nodeA)) {
	// nodeId1 = nodeA;
	// nodeId2 = nodeB;
	// } else {
	// nodeId1 = nodeB;
	// nodeId2 = nodeA;
	// }
	//
	// Map<Long, TempEdges> inMap = map.get(nodeId1);
	// if(inMap != null) {
	// return inMap.remove(nodeId2);
	// }
	// return null;
	// }

}

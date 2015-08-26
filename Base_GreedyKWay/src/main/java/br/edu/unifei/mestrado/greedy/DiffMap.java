package br.edu.unifei.mestrado.greedy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Bucket que armazena os nodes para cada valor de diff.
 * @author roberto
 *
 */
public class DiffMap {

	/**
	 * Lista de nodes associados a cada diff.
	 * 
	 * valor_diff | nodeId | nodeId
	 */
	private TreeMap<Integer, Map<Long, Long>> map = new TreeMap<Integer, Map<Long, Long>>(
			new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
			});
	
	private Map<Long, Integer> diffMap = new HashMap<Long, Integer>();

	public Set<Long> getNodesWithDiff(int diff) {
		return map.get(diff).keySet();
	}

	public void updateDiffForNode(int newDiff, int oldDiff, long node) {
		removeNodeWithDiff(oldDiff, node);
		insertDiffForNode(newDiff, node);
	}
	
	public void insertDiffForNode(int diff, long node) {
		Map<Long, Long> nodes = map.get(diff);
		if (nodes == null) {
			nodes = new HashMap<Long, Long>();
			map.put(diff, nodes);
		}
		nodes.put(node, node);
		diffMap.put(node, diff);
	}

	public void removeNodeWithDiff(int diff, long node) {
		Map<Long, Long> nodes = map.get(diff);
		if (nodes != null) {
			if (nodes.containsKey(node)) {
				nodes.remove(node);
				if (nodes.size() == 0) {
					map.remove(diff);
				}
				return;
			}
			throw new RuntimeException("Node " + node + " não encontrado com diff " + diff + " DiffMap: " + map);
		}
		throw new RuntimeException("diff " + diff + " não encontrado");
	}


	public Integer getMinDiff() {
		if (map.isEmpty()) {
			return null;
		}
		return map.firstKey();
	}

	public Set<Integer> getDiffList() {
		return map.keySet();
	}

	public Integer getDiffOfNode(Long nodeId) {
		return diffMap.get(nodeId);
	}
	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (Integer diff : getDiffList()) {
			b.append("	Diff " + (diff >= 0 ? " " : "") + diff + ": ");
			for (Long nodeId : getNodesWithDiff(diff)) {
				b.append(nodeId + ", ");
			}
		}
		return b.toString();
	}
	
//	public Long getFirstNodeWithDiff(int diff) {
//		Map<Long, Long> nodes = map.get(diff);
//		if (nodes != null) {
//			if (!nodes.isEmpty()) {
//				return nodes.keySet().iterator().next();
//			}
//		}
//		return null;
//	}

}

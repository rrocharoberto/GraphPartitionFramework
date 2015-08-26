package br.edu.unifei.mestrado.fm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bucket {//update diagram

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Valor do gain associado a cada vertice. ou melhor: lista de vertices
	 * associados a cada gain e suas respectivas partições.
	 * 
	 * valor_gain | idNode | partition
	 */
	private TreeMap<Integer, Map<Long, Integer>> map = new TreeMap<Integer, Map<Long, Integer>>(
			new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
			});

//	public Set<Long> getVerticesWithGain(int gain) {
//		gain = Math.abs(gain);
//		return map.get(gain).keySet();
//	}

	public Map<Long, Integer> getNodesWithGain(int gain) {
		return map.get(gain);
	}

	public void insertGainForNode(int gain, Long nodeId, Integer partition) {
		gain = Math.abs(gain);
		Map<Long, Integer> nodes = map.get(gain);
		if (nodes == null) {
			nodes = new HashMap<Long, Integer>();
			map.put(gain, nodes);
		}
		nodes.put(nodeId, partition);
	}

	public void removeNodeWithGain(int gain, Long nodeId) {
		gain = Math.abs(gain);
		Map<Long, Integer> nodes = map.get(gain);
		if (nodes != null) {
			if (nodes.containsKey(nodeId)) {
				nodes.remove(nodeId);
				if (nodes.size() == 0) {
					map.remove(gain);
				}
				return;
			}
			throw new RuntimeException("Node " + nodeId + " not found with gain " + gain + " GainMap: "
					+ map);
		}
		throw new RuntimeException("gain " + gain + " não encontrado");
	}

	public NavigableSet<Integer> getGainsOrdered() {
		return map.descendingKeySet();
	}
	
//	public Integer getMaxGain() {
//		if (map.isEmpty()) {
//			return null;
//		}
//		return map.lastKey();
//	}

//	public Set<Integer> getGainList() {
//		return map.keySet();
//	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}

	public void print(String bucketName) {
		logger.info("{} {}", bucketName, new Object() {
			@Override
			public String toString() {
				StringBuffer b = new StringBuffer();
				for (Integer gain : map.keySet()) {
					b.append("\nG " + gain + ": ");
					for (Long nodeId : map.get(gain).keySet()) {
						b.append(nodeId + ", ");
					}
				}
				return b.toString();
			}
		});
	}
	
	@Override
	public String toString() {
		return "Bucket: " + getGainsOrdered();
	}
}

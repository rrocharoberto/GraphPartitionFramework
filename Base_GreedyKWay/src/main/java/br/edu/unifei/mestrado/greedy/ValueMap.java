package br.edu.unifei.mestrado.greedy;

import java.util.HashMap;
import java.util.Map;

public class ValueMap {

	/**
	 * Valor associado a cada node.
	 * 
	 * node | valor
	 */
	private Map<Long, Integer> map = new HashMap<Long, Integer>();

	public int getValue(Long nodeId) {
		Integer value = map.get(nodeId);
		if (value != null) {
			return value;
		} else {
			return 0;
//			throw new UnsupportedOperationException("O vertice: " + vertice + " não possui o valor.");
		}
	}

//	public void setValue(NodeWrapper vertice, Integer valor) {
//		map.put(vertice.getId(), valor);
//	}
	
	public void incrementN(Long nodeId) {
		Integer value = map.get(nodeId);
		if(value == null) {
			map.put(nodeId, 1);
		} else {
			map.put(nodeId, value + 1);
		}
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
}

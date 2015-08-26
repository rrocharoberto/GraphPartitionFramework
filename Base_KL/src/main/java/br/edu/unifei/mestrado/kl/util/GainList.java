package br.edu.unifei.mestrado.kl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GainList {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// UTIL: nao sao todos com todos, e sim todos de um lado com todos do outro lado
	private List<NodePair> gainArray = new ArrayList<NodePair>();

	// UTIL: atributo para armazenar o melhor ganho, ele evita de ser preciso ordenar os ganhos na hora de
	// escolher o melhor
	private NodePair bestPair = null;

	public void addPair(NodeDiff i, NodeDiff j, int gain) {
		NodePair pair = new NodePair();
		pair.setI(i);
		pair.setJ(j);
		pair.setActualGain(gain);
		pair.setGain(Math.abs(gain));
		logger.trace("Adicionando pair: " + pair);
		gainArray.add(pair);

		// UTIL: armazena o melhor par
		if (bestPair == null) {
			bestPair = pair;
		} else {
			if (pair.getGain() > bestPair.getGain()) {
				bestPair = pair;
			}
		}
	}

	public NodePair getBestPair() {
		if (gainArray.size() == 0) {
			return null;
		}
		return bestPair;
	}

//	public NodePair getBestGain() {
//		if (gainArray.size() == 0) {
//			return null;
//		}
//		// UTIL: já tem o bestPair
//		return bestPair;
//		// return gainArray.iterator().next();
//	}

	public void printGains(int max) {
		logger.debug("Lista de Ganhos: {}", this);
	}

	public int getSize() {
		return gainArray.size();
	}

	private String getStringGainOrdenado() {
		StringBuffer b = new StringBuffer();
		int i = 0;
		int max = 20;
		for (NodePair pair : gainArray) {
			b.append(pair.getActualGain()).append(", ");
			i++;
			if (i > max) {
				break;
			}
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return "Gain [size=" + getSize() + "]: " + getStringGainOrdenado();
	}


	public static void main(String[] args) {
		
		System.out.println(new Random(1).nextInt());
		System.out.println(new Random(2).nextInt());
		System.out.println(new Random(3).nextInt());
		System.out.println(new Random(4).nextInt());
		System.out.println(new Random(5).nextInt());
		System.out.println(new Random(1).nextInt());
	}
	
	// UTIL: por causa do bestPair, não precisa mais do sort
	// public void sort() {
	// Collections.sort(gainArray, new Comparator<PairGain>() {
	// @Override
	// public int compare(PairGain o1, PairGain o2) {
	// if (o1.getGain() < o2.getGain()) {
	// return 1;
	// } else if (o1.getGain() > o2.getGain()) {
	// return -1;
	// } else {
	// if (o1.getActualGain() > 0) {
	// return -1;
	// } else if (o2.getActualGain() > 0) {
	// return 1;
	// }
	// }
	// return 0;
	// }
	// });
	// }
	
	//	public Iterator<PairGain> iterator() {
	//	return gainArray.iterator();
	//}
	//
	
	//	public void clear() {
	//	gainArray.clear();
	//}
}

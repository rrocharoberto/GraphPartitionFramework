package br.edu.unifei.mestrado.generator;

import java.util.HashMap;
import java.util.Map;

import org.graphstream.algorithm.generator.IncompleteGridGenerator;

public class CustomGridGenerator extends IncompleteGridGenerator {

	private int amount;
	private long id;
	
	private Map<String, String> map = new HashMap<String, String>();
	private Map<String, String> nodes = new HashMap<String, String>();
	
	public CustomGridGenerator(int amount, boolean cross, float holeProbability,
			int holeMaxSize, int holesPerStep) {
		super(cross, holeProbability, holeMaxSize, holesPerStep);
		this.amount = amount;
	}
	
	protected String getNodeId(int x, int y) {
		String key = String.format("%d_%d", x, y);
		
		String value = map.get(key);
		if(value == null) {
			id++;
			value = Long.toString(id);
			map.put(key, value);
		}
		return value;
	}
	
	@Override
	protected void addNode(String id, double x, double y) {
		nodes.put(id, id);
		super.addNode(id, x, y);
	}
	
	@Override
	protected void delNode(String id) {
		nodes.remove(id);
		super.delNode(id);
	}
	
	@Override
	public boolean nextEvents() {
		super.nextEvents();
		return nodes.size() < amount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CustomGridGenerator [n=");
		builder.append(amount);
		builder.append(", holeProbability=");
		builder.append(holeProbability);
		builder.append(", holeMaxSize=");
		builder.append(holeMaxSize);
		builder.append(", holesPerStep=");
		builder.append(holesPerStep);
		builder.append(", cross=");
		builder.append(cross);
		builder.append("]");
		return builder.toString();
	}
	
	
}

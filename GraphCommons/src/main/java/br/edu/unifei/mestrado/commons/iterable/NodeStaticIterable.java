package br.edu.unifei.mestrado.commons.iterable;

import java.util.Iterator;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

public class NodeStaticIterable<T> implements Iterable<NodeWrapper> {

	private Iterator<T> nodes;
	private NodeWrapper nodeObject;
	
	public NodeStaticIterable(Iterator<T> nodes, NodeWrapper nodeObject) {
		this.nodes = nodes;
		this.nodeObject = nodeObject;
	}
	
	@Override
	public Iterator<NodeWrapper> iterator() {
		return new Iterator<NodeWrapper>() {

			@Override
			public NodeWrapper next() {
				T node = nodes.next();
				
				try {
					nodeObject.setInnerNode(node);
					return nodeObject;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean hasNext() {
				return nodes.hasNext();
			}

			@Override
			public void remove() {
			}
		};
	}
}

package br.edu.unifei.mestrado.commons.iterable;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;

public class NodeIterable<I, N> implements Iterable<NodeWrapper> {

	private Iterator<I> nodes;
	private Class<I> clsInner;
	private Class<N> clsNode;
	
	public NodeIterable(Iterator<I> nodes, Class<I> clsInner, Class<N> clsNode) {
		this.nodes = nodes;
		this.clsInner = clsInner;
		this.clsNode = clsNode;
	}
	
	@Override
	public Iterator<NodeWrapper> iterator() {
		return new Iterator<NodeWrapper>() {

			@Override
			public NodeWrapper next() {
				I node = nodes.next();
				
				try {
					Constructor<N> constr = clsNode.getConstructor(clsInner);
					NodeWrapper nw = (NodeWrapper)constr.newInstance(node);
					return nw;
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

package br.edu.unifei.mestrado.commons.iterable;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;

public class EdgeIterable<I, E> implements Iterable<EdgeWrapper> {

	private Iterator<I> edges;
	private Class<I> clsInner;
	private Class<E> clsEdge;
	
	public EdgeIterable(Iterator<I> edges, Class<I> clsInner, Class<E> clsEdge) {
		this.edges = edges;
		this.clsInner = clsInner;
		this.clsEdge = clsEdge;
	}
	
	@Override
	public Iterator<EdgeWrapper> iterator() {
		return new Iterator<EdgeWrapper>() {

			@Override
			public EdgeWrapper next() {
				I edge = edges.next();
				
				try {
					Constructor<E> constr = clsEdge.getConstructor(clsInner);
					EdgeWrapper rel = (EdgeWrapper)constr.newInstance(edge);
					return rel;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean hasNext() {
				return edges.hasNext();
			}

			@Override
			public void remove() {
			}
		};
	}
}

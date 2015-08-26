package br.edu.unifei.mestrado.commons.iterable;

import java.util.Iterator;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;

public class EdgeStaticIterable<T> implements Iterable<EdgeWrapper> {

	private Iterator<T> edges;
	private EdgeWrapper edgeObject;
	
	public EdgeStaticIterable(Iterator<T> edges, EdgeWrapper edgeObject) {
		this.edges = edges;
		this.edgeObject = edgeObject;
	}
	
	@Override
	public Iterator<EdgeWrapper> iterator() {
		return new Iterator<EdgeWrapper>() {

			@Override
			public EdgeWrapper next() {
				T edge = edges.next();
				
				try {
					edgeObject.setInnerEdge(edge);
					return edgeObject;
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

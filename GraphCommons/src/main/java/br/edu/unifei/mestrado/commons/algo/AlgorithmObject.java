package br.edu.unifei.mestrado.commons.algo;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.view.ViewListener;

//TODO: fazer os algoritmos herdarem dessa classe
public abstract class AlgorithmObject implements ViewListener {
	
	private GraphWrapper graph = null;
	protected ViewListener view = null;

	public AlgorithmObject(GraphWrapper graph) {
		this.graph = graph;
//		graph.readGraph(); //o readGraph foi para o construtor do grafo
		
//		if (getGraph().getSizeNodes() > 200) {
//			setShowView(false);
//			logger.warn("Desabilitando exibição dos do grafo. qtd de vertices > 200");
//		}
	}

	public AlgorithmObject(GraphWrapper graph, ViewListener view) {
		this.graph = graph;
		this.view = view;
//		graph.readGraph();
	}

	public GraphWrapper getGraph() {
		return graph;
	}

	public abstract void execute();

	// view methods
	
	@Override
	public void initView(GraphWrapper initialGraph, int k) {
		if (view != null) {
			view.initView(initialGraph, k);
		}
	}

	@Override
	public void repaint() {
		if (view != null) {
			view.repaint();
		}
	}
	
	@Override
	public void repaint(long nodeId) {
		if (view != null) {
			view.repaint(nodeId);
		}
	}

	@Override
	public void updateView(GraphWrapper graph, int cutWeight) {
		if (view != null) {
			view.updateView(graph, cutWeight);
		}
	}

	@Override
	public void updateViewCoarsed(GraphWrapper graph, int cutWeight) {
		if (view != null) {
			view.updateViewCoarsed(graph, cutWeight);
		}
	}
	
	@Override
	public void setIt(String it) {
		if (view != null) {
			view.setIt(it);
		}
	}

}

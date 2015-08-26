package br.edu.unifei.mestrado.commons.view;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;

public interface ViewListener {

	public void initView(GraphWrapper initialGraph, int k);

	public void updateView(GraphWrapper graph, int cutWeight);
	
	public void updateViewCoarsed(GraphWrapper graph, int cutWeight);

	public void repaint();

	public void repaint(long nodeId);

	public void setIt(String it);
}

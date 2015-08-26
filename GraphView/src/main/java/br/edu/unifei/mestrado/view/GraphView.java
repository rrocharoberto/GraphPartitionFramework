package br.edu.unifei.mestrado.view;

import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.view.ViewListener;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class GraphView extends GraphPartitioningVisualization implements ViewListener {

	private static final long serialVersionUID = 1L;
	
	private GraphWrapper innerGraph;

	private boolean multiNivel = false;

	private String it = "1";
	private UndirectedSparseGraph<NodeWrapper, EdgeWrapper> graph = null;
	private int cut = -1;

	private String itCoarsed = "1";
	private UndirectedSparseGraph<NodeWrapper, EdgeWrapper> graphCoarsed = null;
	private int cutCoarsed = -1;

	private GraphPartitioningVisualization viewCoarsed = null;

	public GraphView() {

	}

	public GraphView(boolean multiNivel) {
		this.multiNivel = multiNivel;
	}

	// GraphPartitioningVisualization methods
	@Override
	public String getIteracao() {
		return it;
	}

	@Override
	public edu.uci.ics.jung.graph.Graph<NodeWrapper, EdgeWrapper> getGraph() {
		return graph;
	}

	@Override
	public int getCut() {
		return cut;
	}
	
	//ViewListener methods
	@Override
	public void initView(GraphWrapper initialGraph, int k) {
		this.setName("1");
		this.innerGraph = initialGraph;

		graph = GraphJungConverter.convertGraph(initialGraph);
		it = "coarsening";
		cut = -1;
		this.executeView(k);

		if (multiNivel) {
			viewCoarsed = new GraphPartitioningVisualization() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getIteracao() {
					return itCoarsed;
				}

				@Override
				public edu.uci.ics.jung.graph.Graph<NodeWrapper, EdgeWrapper> getGraph() {
					return graphCoarsed;
				}

				@Override
				public int getCut() {
					return cutCoarsed;
				}
			};
			viewCoarsed.setName("2");

			graphCoarsed = GraphJungConverter.convertGraph(initialGraph);
			itCoarsed = "coarsening";
			cutCoarsed = -1;
			viewCoarsed.executeView(k);
		}
	}

	@Override
	public void updateView(GraphWrapper graph, int cutWeight) {
		this.innerGraph = graph;
		this.graph = GraphJungConverter.convertGraph(graph);
		cut = cutWeight;
		super.updateModel();
	}

	@Override
	public void updateViewCoarsed(GraphWrapper graph, int cutWeight) {
		if (multiNivel) {
			this.innerGraph = graph;
			graphCoarsed = GraphJungConverter.convertGraph(graph);
			cutCoarsed = cutWeight;
			this.itCoarsed = "" + graph.getLevel();
			viewCoarsed.updateModel();
		}
	}

	@Override
	public void repaint() {
		super.repaint();
		if (multiNivel) {
			viewCoarsed.repaint();
		}
	}

	@Override
	public void repaint(long nodeId) {
		NodeWrapper node = innerGraph.getNode(nodeId);
		super.changeNodePartition(node);
		super.repaint();
	}

	@Override
	public void setIt(String it) {
		this.it = it;
	}

}

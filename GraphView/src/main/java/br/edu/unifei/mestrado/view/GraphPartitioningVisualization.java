package br.edu.unifei.mestrado.view;

/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.commons.collections15.map.LazyMap;

import br.edu.unifei.mestrado.commons.graph.GraphProperties;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import edu.uci.ics.jung.algorithms.layout.AggregateLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.util.VertexShapeFactory;

/**
 * This simple app demonstrates how one can use our algorithms and visualization
 * libraries in unison. In this case, we generate use the Zachary karate club
 * data set, widely known in the social networks literature, then we cluster the
 * vertices using an edge-betweenness clusterer, and finally we visualize the
 * graph using Fruchtermain-Rheingold layout and provide a slider so that the
 * user can adjust the clustering granularity.
 * 
 * @author Scott White
 */
public abstract class GraphPartitioningVisualization extends JApplet {

	private static final long serialVersionUID = 1L;

	private VisualizationViewer<NodeWrapper, EdgeWrapper> vv;

	private Map<NodeWrapper, Paint> vertexPaints = LazyMap.<NodeWrapper, Paint> decorate(
			new HashMap<NodeWrapper, Paint>(), new ConstantTransformer(Color.white));
	// private Map<Relationship, Paint> edgePaints = LazyMap.<Relationship,
	// Paint> decorate(new HashMap<Relationship, Paint>(),
	// new ConstantTransformer(Color.blue));

	public static final Color[] similarColors = { Color.cyan, Color.blue, Color.magenta, Color.orange,
			Color.green, Color.red, Color.yellow, Color.black, Color.darkGray, Color.gray, Color.white, Color.pink };

	// { new Color(216, 134, 134), new Color(135, 137, 211),
	// new Color(134, 206, 189), new Color(206, 176, 134), new Color(194, 204,
	// 134),
	// new Color(145, 214, 134), new Color(133, 178, 209), new Color(103, 148,
	// 255),
	// new Color(60, 220, 220), new Color(30, 250, 100) };

	private VertexShapeFactory<NodeWrapper> vertexShapeFactory = new VertexShapeFactory<NodeWrapper>(
			new ConstantTransformer(20), new ConstantTransformer(1.0f));

	private AggregateLayout<NodeWrapper, EdgeWrapper> layout;

	private JToggleButton groupCircleVertices;
	private JToggleButton groupVertices;

	private JLabel itLabel;
	private JLabel cutLabel;

	private PartitionClusterer clusterer = null;

	private Transformer<NodeWrapper, String> vertexLabelTransformer;

	private Transformer<NodeWrapper, Paint> vertexFillPaintTransformer = MapTransformer
			.<NodeWrapper, Paint> getInstance(vertexPaints);

	private Transformer<NodeWrapper, Shape> vertexShapeTransformer = new Transformer<NodeWrapper, Shape>() {
		@Override
		public Shape transform(NodeWrapper v) {
			if (v.isCoarsed()) {
				return vertexShapeFactory.getRegularPolygon(v, 3);
			}
			// return vertexShapeFactory.getRoundRectangle(v);
			return new Ellipse2D.Float(-8, -8, 16, 16);
		}
	};

	private Transformer<NodeWrapper, Paint> vertexDrawPaintTransformer = new Transformer<NodeWrapper, Paint>() {
		@Override
		public Paint transform(NodeWrapper v) {
			if (vv.getPickedVertexState().isPicked(v)) {
				return Color.cyan;
			} else {
				if (v.isLocked()) {
					return Color.BLACK;
				} else {
					return Color.RED;
				}
			}
		}
	};

	private Transformer<EdgeWrapper, String> edgeLabelTransformer = new Transformer<EdgeWrapper, String>() {
		@Override
		public String transform(EdgeWrapper e) {
			return "E" + e.getId() + ":" + e.getWeight();
		}
	};

	private Transformer<NodeWrapper, Stroke> vertexStrokeTransformer = new Transformer<NodeWrapper, Stroke>() {
		protected final Stroke THIN = new BasicStroke(1);
		// protected final Stroke THICK = new BasicStroke(2);
		protected final Stroke STRONG_THICK = new BasicStroke(3);

		public Stroke transform(NodeWrapper v) {
			// if para mostrar arestas com as duas pontas no mesmo vertices
			if (v.isLocked()) {
				return STRONG_THICK;
			}
			return THIN;
		}
	};

	public void setVertexLabelTransformer(Transformer<NodeWrapper, String> vertexLabelTransformer) {
		this.vertexLabelTransformer = vertexLabelTransformer;
	}

	public void setVertexFillPaintTransformer(Transformer<NodeWrapper, Paint> vertexFillPaintTransformer) {
		this.vertexFillPaintTransformer = vertexFillPaintTransformer;
	}

	public void setEdgeLabelTransformer(Transformer<EdgeWrapper, String> edgeLabelTransformer) {
		this.edgeLabelTransformer = edgeLabelTransformer;
	}

	public static void main(String[] args) throws IOException {

		GraphPartitioningVisualization cd = new GraphPartitioningVisualization() {
			private static final long serialVersionUID = 1L;
			@Override
			public Graph<NodeWrapper, EdgeWrapper> getGraph() {
				return new SparseMultigraph<NodeWrapper, EdgeWrapper>();
			}

			@Override
			public String getIteracao() {
				return "2";// any
			}

			@Override
			public int getCut() {
				return 0;
			}
		};
		cd.executeView(2);
	}

	public void executeView(int partitions) {
		createView(partitions);
		JFrame jf = new JFrame();
		jf.getContentPane().add(this);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setSize(630, 800);
		jf.setVisible(true);
	}

	public void updateModel() {
		getIterationLabel();
		getCutLabel();
		final Graph<NodeWrapper, EdgeWrapper> graph = getGraph();
		layout.setGraph(graph);
		clusterAndRecolor(layout, groupVertices.isSelected(), getGroupCircleVertices().isSelected());
		vv.repaint();
	}

	private void createView(final int partitions) {

		clusterer = new PartitionClusterer(partitions);

		final Graph<NodeWrapper, EdgeWrapper> graph = getGraph();

		// Create a simple layout frame
		// specify the Fruchterman-Rheingold layout algorithm
		layout = new AggregateLayout<NodeWrapper, EdgeWrapper>(new FRLayout<NodeWrapper, EdgeWrapper>(graph));

		vv = new VisualizationViewer<NodeWrapper, EdgeWrapper>(layout);
		vv.setBackground(Color.white);

		if (vertexLabelTransformer == null) {
			vertexLabelTransformer = new Transformer<NodeWrapper, String>() {
				@Override
				public String transform(NodeWrapper v) {
					// TODO: voltar o D quando necessário
					return "V" + v.getId() + ":" + v.getWeight();// + ":" +
																	// v.getD();
				}
			};
		}
		vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);

		// MapTransformer.<NodeWrapper, String>
		// getInstance(LazyMap.<NodeWrapper, String> decorate(
		// new HashMap<NodeWrapper, String>(), new
		// ToStringLabeller<NodeWrapper>())));
		vv.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);

		// UTIL: Alguma coisa, talvez o LazyMap não estava repintando a aresta
		// com o peso alterado

		// MapTransformer.<EdgeWrapper, String>
		// getInstance(LazyMap.<EdgeWrapper, String> decorate(
		// new HashMap<EdgeWrapper, String>(), new
		// ToStringLabeller<EdgeWrapper>() {
		// @Override
		// public String transform(EdgeWrapper v) {
		// logger.warn("A:" + v.getId() + ":" + v.getWeight());
		// return "A:" + v.getId() + ":" + v.getWeight();
		// }
		// })));
		// Tell the renderer to use our own customized color rendering

		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<NodeWrapper, EdgeWrapper>());

		vv.getRenderContext().setVertexShapeTransformer(vertexShapeTransformer);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexFillPaintTransformer);
		vv.getRenderContext().setVertexDrawPaintTransformer(vertexDrawPaintTransformer);
		vv.getRenderContext().setVertexStrokeTransformer(vertexStrokeTransformer);

		vv.getRenderContext().setEdgeDrawPaintTransformer(new Transformer<EdgeWrapper, Paint>() {

			// UTIL: Define as cores das arestas
			@Override
			public Paint transform(EdgeWrapper linkToProcess) {
				// if para mostrar arestas com as duas pontas no mesmo vertices
				if (linkToProcess.getStartNode().getId() == linkToProcess.getEndNode().getId()) {
					return Color.red;
				}
				Set<NodeWrapper> nodes = vv.getPickedVertexState().getPicked();
				for (NodeWrapper node : nodes) {
					for (EdgeWrapper link : node.getEdges()) {
						if (link.getId() == linkToProcess.getId()) {
							return Color.orange;
						}
					}
				}
				if (linkToProcess.isEdgeOnCut()) {
					return Color.green;
				}
				return Color.blue;
			}
		});

		// MapTransformer.<EdgeWrapper, Paint> getInstance(edgePaints));

		vv.getRenderContext().setEdgeStrokeTransformer(new Transformer<EdgeWrapper, Stroke>() {
			protected final Stroke THIN = new BasicStroke(1);
			// protected final Stroke THICK = new BasicStroke(2);
			protected final Stroke STRONG_THICK = new BasicStroke(3);

			public Stroke transform(EdgeWrapper e) {
				// if para mostrar arestas com as duas pontas no mesmo vertices
				if (e.getStartNode().getId() == e.getEndNode().getId()) {
					return STRONG_THICK;
				}

				// Paint c = edgePaints.get(e);
				// if (c == Color.red)
				return THIN;
				// else
				// return THIN;
			}
		});

		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		vv.setGraphMouse(gm);

		groupVertices = new JToggleButton("Group Clusters");
		groupVertices.setSelected(true);

		groupVertices.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				clusterAndRecolor(layout, e.getStateChange() == ItemEvent.SELECTED, getGroupCircleVertices()
						.isSelected());
				vv.repaint();
			}
		});

		clusterAndRecolor(layout, groupVertices.isSelected(), getGroupCircleVertices().isSelected());

		Container content = getContentPane();
		content.add(new GraphZoomScrollPane(vv));
		JPanel south = new JPanel(new GridLayout(1, 4));
		JPanel grid1 = new JPanel(new GridLayout(2, 1));
		grid1.add(groupVertices);
		grid1.add(getGroupCircleVertices());
		south.add(grid1);

		final ScalingControl scaler = new CrossoverScalingControl();
		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1 / 1.1f, vv.getCenter());
			}
		});

		JPanel grid2 = new JPanel(new GridLayout(2, 1));
		grid2.setBorder(BorderFactory.createTitledBorder("Scale"));
		south.add(grid2);
		grid2.add(plus);
		grid2.add(minus);
		JPanel grid3 = new JPanel(new GridLayout(1, 2));
		south.add(grid3);
		grid3.add(getIterationLabel());
		grid3.add(getCutLabel());

		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		p.add(gm.getModeComboBox());
		south.add(p);
		content.add(south, BorderLayout.SOUTH);
		vv.repaint();
	}

	private JToggleButton getGroupCircleVertices() {
		if (groupCircleVertices == null) {
			groupCircleVertices = new JToggleButton("Group Circle Clusters");
			groupCircleVertices.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					clusterAndRecolor(layout, e.getStateChange() == ItemEvent.SELECTED,
							groupCircleVertices.isSelected());
					vv.repaint();
				}
			});
		}
		return groupCircleVertices;
	}

	private JLabel getIterationLabel() {
		if (itLabel == null) {
			itLabel = new JLabel();
		}
		itLabel.setText("It: " + getIteracao());
		return itLabel;
	}

	private JLabel getCutLabel() {
		if (cutLabel == null) {
			cutLabel = new JLabel();
		}
		cutLabel.setText("Cut: " + getCut());
		return cutLabel;
	}

	private void clusterAndRecolor(AggregateLayout<NodeWrapper, EdgeWrapper> layout, boolean groupClusters,
			boolean circleClusters) {

		// Now cluster the vertices by partition

		Graph<NodeWrapper, EdgeWrapper> g = layout.getGraph();
		layout.removeAll();

		Set<Set<NodeWrapper>> clusterSet = clusterer.transform(g);

		colorNodes(g);
//		int i = 0;
		// Set the colors of each node so that each cluster's vertices have the
		// same color
		for (Set<NodeWrapper> vertices : clusterSet) {
//			Color c = similarColors[i % similarColors.length];

//			colorCluster(vertices, c);
			if (groupClusters == true || circleClusters == true) {
				groupCluster(layout, vertices, circleClusters);
			}
//			i++;
		}
		// for (EdgeWrapper e : g.getEdges()) {
		// edgePaints.put(e, Color.black);
		// }
	}
	
	private void colorNodes(Graph<NodeWrapper, EdgeWrapper> graph) {
		for (NodeWrapper node : graph.getVertices()) {
			if(node.hasProperty(GraphProperties.PARTITION)) {
				if(node.getPartition() == AbstractPartition.NO_PARTITION) {
					vertexPaints.put(node, Color.lightGray);
				} else {
					Color c = similarColors[node.getPartition() % similarColors.length];
					vertexPaints.put(node, c);
				}
			}
		}
	}

	private void colorCluster(Set<NodeWrapper> vertices, Color c) {
		for (NodeWrapper v : vertices) {
			vertexPaints.put(v, c);
		}
	}

	private void groupCluster(AggregateLayout<NodeWrapper, EdgeWrapper> layout, Set<NodeWrapper> vertices,
			boolean circleClusters) {
		Point2D center = layout.transform(vertices.iterator().next());
		Graph<NodeWrapper, EdgeWrapper> subGraph = SparseMultigraph.<NodeWrapper, EdgeWrapper> getFactory().create();
		for (NodeWrapper v : vertices) {
			subGraph.addVertex(v);
		}
		Layout<NodeWrapper, EdgeWrapper> subLayout = null;

		if (circleClusters) {
			CircleLayout<NodeWrapper, EdgeWrapper> circleLayout = new CircleLayout<NodeWrapper, EdgeWrapper>(subGraph);
			circleLayout.setRadius(getGraph().getVertexCount() * 4);
			circleLayout.setSize(new Dimension(200, 400));
			circleLayout.setVertexOrder(new Comparator<NodeWrapper>() {

				@Override
				public int compare(NodeWrapper o1, NodeWrapper o2) {
					return o1.getId() < o2.getId() ? -1 : 1;
				}
			});
			subLayout = circleLayout;
		} else {
			subLayout = new KKLayout<NodeWrapper, EdgeWrapper>(subGraph);

			// UTIL: verificar o algoritmo e uso dessa classe: VoltageClusterer
			// subLayout = new VoltageClusterer<NodeWrapper,
			// EdgeWrapper>(subGraph, 2);
			// subLayout = new FRLayout2<NodeWrapper,
			// EdgeWrapper>(subGraph); //esse layout dá erro
		}
		subLayout.setInitializer(vv.getGraphLayout());
		subLayout.setSize(new Dimension((int) getSize().getWidth() / 2, (int) getSize().getHeight() / 2));

		layout.put(subLayout, center);
		vv.repaint();
	}

	@Override
	public void repaint() {
		vv.repaint();
	}

	public abstract Graph<NodeWrapper, EdgeWrapper> getGraph();

	public abstract String getIteracao();

	public abstract int getCut();

	public void changeNodePartition(NodeWrapper node) {
		int partition = AbstractPartition.NO_PARTITION;
		if(node.hasProperty(GraphProperties.PARTITION)) {
			partition = node.getPartition();
		}
		Color c = similarColors[Math.abs(partition) % similarColors.length];
		vertexPaints.put(node, c);
	}
}

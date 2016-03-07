package br.edu.unifei.mestrado.generator;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.swingViewer.ViewerListener;

public class MyViewListener implements ViewerListener {

	private Graph graph;
	private int n;
	private int k;
	private Object gen;

	public MyViewListener(Graph graph, int n, int k, Object gen) {
		this.graph = graph;
		this.n = n;
		this.k = k;
		this.gen = gen;
	}

	@Override
	public void viewClosed(String viewName) {
		int px = 12;
		if (n < 400) {
			px = 11;
		} else if (n < 800) {
			px = 10;
		} else if (n < 1500) {
			px = 9;
		} else {
			px = 8;
		}
		System.out.println("px: " + px);
		graph.addAttribute("graph.generator", gen.toString());
		graph.addAttribute("ui.stylesheet", "node.export {fill-color: blue; size: " + px + "px, " + px + "px; }");
		for (Node node : graph.getNodeSet()) {
			// node.removeAttribute("ui.class");
			node.addAttribute("ui.class", "export");
		}

		FileSink fs1 = new FileSinkJostle();

		FileSinkImages fsLO = new FileSinkImages(OutputType.PNG, Resolutions.PAL);
		fsLO.stabilizeLayout(1);
		fsLO.stabilizeLayout(1);
//		FileSinkImages fsHI = new FileSinkImages(OutputType.PNG, Resolutions.SVGA);
		
		fsLO.setQuality(Quality.LOW);
//		fsHI.setQuality(Quality.MEDIUM);
		fsLO.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
		try {
			String baseName = "target/grafo_" + n + "_" + k;
			System.out.println("writing txt file...");
			fs1.writeAll(graph, baseName + ".txt");

			System.out.println("writing png files...");
			fsLO.writeAll(graph, baseName + "_LO" + ".png");
//			fsHI.writeAll(graph, baseName + "_HI" + ".png");
			System.out.println("done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.exit(0);
	}

	@Override
	public void buttonReleased(String id) {
		// TODO Auto-generated method stub
		// System.out.println("Button released: " + id);
	}

	@Override
	public void buttonPushed(String id) {
		System.out.println("Button pushed: " + id);
		// Node node = graph.getNode(id);
		// node.addAttribute("ui.class", "important");
	}

}

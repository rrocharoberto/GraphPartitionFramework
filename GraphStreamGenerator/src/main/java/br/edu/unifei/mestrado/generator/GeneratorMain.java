package br.edu.unifei.mestrado.generator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;

public class GeneratorMain {
	// private boolean loop = true;

	public static void main(String[] args) {
		GeneratorMain main = new GeneratorMain();
		int i = 2;
		// for (i = 2; i <= 5; i++) {
//		main.execute(i, 100);
		main.execute(i, 500);
		// main.execute(i, 1000);
		// }
		// main.execute(2, 2000);
		
		Random rand = new Random();
		
		List<Integer> seeds = Arrays.asList(new Integer [] {
				rand.nextInt(), 
				rand.nextInt(), 
				rand.nextInt()
			});
	}

	public void execute(int k, int n) {
		// Toolkit.c
		final Graph graph = new SingleGraph("roberto");
		graph.setStrict(false);
		graph.addAttribute("ui.stylesheet", "node.important {fill-color: red;}");
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		BaseGenerator gen = null;

		// Between 1 and 3 new links per node added.
		// gen = new BarabasiAlbertGenerator(1);//talvez
		// gen = new DorogovtsevMendesGenerator();//talvez

		// gen = new PointsOfInterestGenerator();//gera desconexo
		// gen = new PreferentialAttachmentGenerator();//parece com lobster
		// gen = new RandomEuclideanGenerator(2);//+-
		// gen = new RandomFixedDegreeDynamicGraphGenerator(200, 9.0,
		// 0.5);//acho que não
		// gen = new WattsStrogatzGenerator(200, 4, 0.1);//+-

		// gen = new ClusterGeneratorOld(200);

		int minDegree = 3;
		int maxDegree = 6;
		// gen = new ClusterGenerator(n, minDegree, maxDegree, k); // sim
		// gen = new CustomGridGenerator(n, true, 0.9F, 15, 5);//usado para

		// gen = new LobsterGenerator(5, 5);// sim
		// gen = new CustomLobsterGenerator(n, 3, 5);// sim
		gen = new CustomFlowerSnarkGenerator(n);// sim
		// gen = new WattsStrogatzGenerator(20, 10, 0.1);//pode ser mas tem que
		// rodar 2 vezes

		// gen = new BarabasiAlbertGenerator(1);//talvez - parecido com lobster
		// gen = new DorogovtsevMendesGenerator();//talvez

		gen.setRandomSeed(12345);
		gen.addSink(graph);
		gen.begin();
		while (gen.nextEvents())
			;
		gen.end();
		System.out.println("" + graph.getNodeCount() + " " + graph.getEdgeCount());

		// gen.configureNodes(graph);
		// System.out.println("Amount of important: " + gen.countImportant);

		// Layout layoutAlgorithm = new SpringBox();
		// viewer.enableAutoLayout(layoutAlgorithm);

		ViewerListener vl = new MyViewListener(graph, n, k, gen);
		// vl.viewClosed("any");

		Viewer viewer = graph.display(true);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

		ViewerPipe fromViewer = viewer.newViewerPipe();
		fromViewer.addViewerListener(vl);
		fromViewer.addSink(graph);

		while (true) {
			fromViewer.pump(); // or fromViewer.blockingPump();

			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

	}

}
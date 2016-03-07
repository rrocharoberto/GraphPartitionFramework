package br.edu.unifei.mestrado.generator;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;

public class CopyOfGeneratorTestOld {

	private boolean loop = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new CopyOfGeneratorTestOld().execute();
	}

	public void execute() {
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

		int n = 100;
		// gen = new LobsterGenerator(5, 5);// sim
		// gen = new CustomLobsterGenerator(500, 3, 5);// sim
		// gen = new CustomFlowerSnarkGenerator(100);//sim
//		 gen = new ClusterGenerator(n, 3, 3); //sim
		// gen = new CustomGridGenerator(110, true, 0.9F, 15, 5);//usado para
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

		Viewer viewer = graph.display(true);
		// Layout layoutAlgorithm = new SpringBox();
		// viewer.enableAutoLayout(layoutAlgorithm);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

		ViewerListener vl = new MyViewListener(graph, n, 2, gen);
		ViewerPipe fromViewer = viewer.newViewerPipe();
		fromViewer.addViewerListener(vl);
		fromViewer.addSink(graph);

		while (loop) {
			fromViewer.pump(); // or fromViewer.blockingPump();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
	}

}

// gen = new FlowerSnarkGenerator();// legal - não mais
// gen = new IncompleteGridGenerator();//pode ser
// gen = new ChvatalGenerator();//não
// gen = new FullGenerator();//não
// gen = new GridGenerator();//não
// gen = new RandomGenerator();//não
// gen = new PetersenGraphGenerator();//não, pois só gerou 11 - é simples
// gen = new BarabasiAlbertGenerator(2);//talvez//não, só dá
// com 1, daí parece o lobster

// lcfgenerator
// LCF lcf = new LCF(10, 100);
// gen = new LCFGenerator(lcf, 100, false);
// gen = new Balaban10CageGraphGenerator();//não
// gen = new Balaban11CageGraphGenerator();//não
// gen = new BidiakisCubeGenerator();//não
// gen = new BiggsSmithGraphGenerator();//não
// gen = new CubicalGraphGenerator();//não
// gen = new DesarguesGraphGenerator();//não
// gen = new DodecahedralGraphGenerator();//não
// gen = new DyckGraphGenerator();//não
// gen = new F26AGraphGenerator();//não
// gen = new FosterGraphGenerator();//não
// gen = new FranklinGraphGenerator();//não
// gen = new FruchtGraphGenerator();//não
// gen = new GrayGraphGenerator()//não;
// gen = new HarriesGraphGenerator();//não
// gen = new HarriesWongGraphGenerator();//não
// gen = new HeawoodGraphGenerator();//não
// gen = new LjubljanaGraphGenerator();//não
// gen = new McGeeGraphGenerator();//não
// gen = new MobiusKantorGraphGenerator();//não
// gen = new NauruGraphGenerator();//não
// gen = new PappusGraphGenerator();//não
// gen = new TetrahedralGraphGenerator();//não
// gen = new TruncatedCubicalGraphGenerator();//não
// gen = new TruncatedDodecahedralGraphGenerator();//não
// gen = new TruncatedOctahedralGraphGenerator();//não
// gen = new TruncatedTetrahedralGraphGenerator();//não
// gen = new Tutte12CageGraphGenerator();//não
// gen = new TutteCoxeterGraphGenerator();//não
// gen = new UtilityGraphGenerator();//não
// gen = new WagnerGraphGenerator();/não
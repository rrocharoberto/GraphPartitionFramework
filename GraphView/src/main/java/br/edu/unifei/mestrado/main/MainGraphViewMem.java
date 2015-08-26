package br.edu.unifei.mestrado.main;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.view.GraphView;

public class MainGraphViewMem {

	public static void main(String[] args) {
		String graphFileName = null;

		// executar assim: main graphFileName
		if (args.length > 0) {
			graphFileName = args[0];
		} else {
			System.out.println("Uso: MainGraphViewMem graphFileName");
			System.exit(2);
		}

		System.out.println("Iniciando View com Memória... file: " + graphFileName);

		MainGraphViewMem main = new MainGraphViewMem();
		main.execute(graphFileName);
	}

	public void execute(String file) {
		GraphWrapper graph = new GraphMem(file);
		
		PartitionIndex partitionIdx = graph.getCurrentPartitionIndex();
		CutIndex cutIdx = graph.getCurrentCutIndex();
		TwoWayPartition partition = new TwoWayPartition(GraphWrapper.NO_LEVEL, partitionIdx, cutIdx);

		graph.readGraph();
		GraphView view = new GraphView();
		view.initView(graph, 2);
		view.updateView(graph, partition.getCutWeight());
	}
}

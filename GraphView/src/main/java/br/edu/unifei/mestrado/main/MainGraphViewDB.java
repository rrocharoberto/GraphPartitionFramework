package br.edu.unifei.mestrado.main;

import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.db.GraphDB;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;
import br.edu.unifei.mestrado.commons.partition.index.CutIndex;
import br.edu.unifei.mestrado.commons.partition.index.PartitionIndex;
import br.edu.unifei.mestrado.view.GraphView;

public class MainGraphViewDB {

	public static void main(String[] args) {
		String file = null;

		// executar assim: main fileName.txt
		// ou sem nada, daí entra tudo via teclado
		if (args.length > 0) {
			file = args[0];
		} else {
			System.out.println("Uso: MainGraphView path_database");
			System.exit(2);
		}

		System.out.println("Iniciando View com Neo4J... file: " + file);

		MainGraphViewDB main = new MainGraphViewDB();
		main.execute(file);
	}

	public void execute(String file) {
		GraphDB graph = new GraphDB(file);
		graph.readGraph();
		GraphView view = new GraphView();
		
		view.initView(graph, 2);
		
		PartitionIndex partitionIdx = graph.getCurrentPartitionIndex();
		CutIndex cutIdx = graph.getCurrentCutIndex();
		TwoWayPartition partition = new TwoWayPartition(GraphWrapper.NO_LEVEL, partitionIdx, cutIdx);

		// so para mostrar na tela
		view.updateView(graph, partition.getCutWeight());
	}
}

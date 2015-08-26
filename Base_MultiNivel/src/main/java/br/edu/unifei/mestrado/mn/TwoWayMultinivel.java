package br.edu.unifei.mestrado.mn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.algo.AlgorithmObject;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.mn.LevelInformation;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.partition.TwoWayPartition;
import br.edu.unifei.mestrado.commons.view.ViewListener;

public abstract class TwoWayMultinivel extends AlgorithmObject implements ViewListener{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final int NUMBER_OF_NODES_TO_COARSE = 500;

	private CoarseHelper coarseHelper;
	
	private LevelInformation levelInfo = new LevelInformation();
	
//	private TwoWayPartition partition;

	public TwoWayMultinivel(GraphWrapper graph) {//update diagram
		super(graph);
//		this.partition = new BKLPartition(GraphWrapper.FIRST_LEVEL, 
//				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
		coarseHelper = new CoarseHelper();
	}

	public TwoWayMultinivel(GraphWrapper graph, ViewListener view) {//update digram
		super(graph, view);
//		this.partition = new BKLPartition(GraphWrapper.FIRST_LEVEL, 
//				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
		coarseHelper = new CoarseHelper();
	}

	/**
	 * Creates a new graph with corresponding level
	 * 
	 * @param level
	 * @return
	 */
	protected abstract GraphWrapper createNewGraph(int level);
	
	protected abstract TwoWayPartition createNewPartition(final GraphWrapper graph, int level);
	
	/**
	 * Performs the partition refinement using the specific algorithm.
	 * The subclass must handle its own transaction.
	 * 
	 * @param graph
	 * @param partition is already a generated boundary partition
	 * @return
	 */
	protected abstract BestPartition refinePartition(GraphWrapper graph, TwoWayPartition partition); //update diagram
	
	/**
	 * 1) Execute the 2-way partitioning using the specific algorithm.
	 */
	protected abstract BestPartition executePartition(GraphWrapper graph);//update diagram

	/**
	 * Pre-process the initial graph.
	 * @param initialGraph
	 */
	protected void preprocess(GraphWrapper initialGraph) {
		// stub method that can be used by sub class.
	}

	/**
	 * Coarse the graph one level
	 */
	private void executeCoarsening() {
		logger.warn("Contraindo o grafo...");
		levelInfo.addGraph(getGraph());

		logger.warn("Qtd de nodes do nivel " + levelInfo.getCurrentLevel() + ": " + levelInfo.getSizeNodesOfGraph());

		// UTIL: usado quando o grafo não diminuir mais na contração, ou seja, é desconexo. 
		boolean gotLessNodes = true;
		
		// UTIL: contrai até não diminuir a qtd de vértices ou alcançar a qtd mínima desejada
		while (gotLessNodes && levelInfo.getSizeNodesOfGraph() > NUMBER_OF_NODES_TO_COARSE) {
			GraphWrapper newGraph = createNewGraph(levelInfo.getNextLevel());
			levelInfo.addGraph(newGraph);
			gotLessNodes = coarseHelper.coarseOneLevel(levelInfo.getCurrentLevel(), levelInfo.getPreviousGraph(), levelInfo.getCurrentGraph());
			
			if (gotLessNodes) { 
				updateView(levelInfo.getPreviousGraph(), -1);
				updateViewCoarsed(levelInfo.getCurrentGraph(), -1);
			}
			levelInfo.getPreviousGraph().passivate();
		}
	}

	/**
	 * Uncoarse the graph just one level.
	 * <br>
	 * 1) Repassa o particionamento de volta para o grafo mais fino. <br>
	 * 2) Melhora o particionamento usando o refinamento.
	 * @param bestPart
	 * @return
	 */
	private BestPartition uncoarseOneLevel() {
		logger.warn("Iniciando uncoarse do NIVEL: " + levelInfo.getCurrentLevel());
		
		levelInfo.getPreviousGraph().activate();
		
		//TODO_OK: Mudar! para projetar, precisa da partição do nível anterior, e não do grafo, talvez. No grafo os nodes já tem suas partições.
//		TwoWayPartition newPart = 
		projectPartitionBack(levelInfo.getPreviousGraph(), levelInfo.getCurrentGraph(), levelInfo.getPreviousLevel());

		// so para mostrar na tela
		updateView(levelInfo.getPreviousGraph(), -1);//levelInfo.getPreviousPartition().getCutWeight());
		updateViewCoarsed(levelInfo.getCurrentGraph(), -1);//newPart.getCutWeight());

		BestPartition improvedPart = improvePartition(levelInfo.getPreviousGraph(), levelInfo.getPreviousLevel());//, newPart);

		logger.warn("Qtd de vertices do nivel: " + levelInfo.getCurrentLevel() + " :" + levelInfo.getSizeNodesOfGraph()
				+ " Corte: " + improvedPart.getCutWeight());

		logger.debug("Partition of level {}", (levelInfo.getCurrentLevel() - 1));
		improvedPart.printSets();

		// so para mostrar na tela
		if (levelInfo.getCurrentLevel() > 0) { // para não dar erro no ultimo nivel
			updateView(levelInfo.getPreviousGraph(), -1);//levelInfo.getPreviousPartition().getCutWeight());
		}
		updateViewCoarsed(levelInfo.getCurrentGraph(), improvedPart.getCutWeight());
		return improvedPart;
	}

	/**
	 * Repassa o particionamento do grafo mais contraído para o grafo mais fino.
	 * 
	 * @param fineGraph
	 * @param coarsedGraph
	 * @param previousLevel
	 * @return
	 */
	protected void projectPartitionBack(GraphWrapper fineGraph, GraphWrapper coarsedGraph, int previousLevel) {//update diagram
		long delta = System.currentTimeMillis();

		//TODO_OK: rever isso: UTIL: a partição nova para a projeção de volta é feita em memória. Não. Cada mem/db faz o seu.
//		TwoWayPartition partition = createNewPartition(fineGraph, previousLevel);
		
		TransactionControl transaction = new TransactionControl(fineGraph);
		try {
			transaction.beginTransaction();
			for (NodeWrapper node : fineGraph.getAllNodesStatic()) {

				int partitionId = -1;
				// UTIL: repassa o particionamento para o grafo mais fino
				if (node.hasInsideOf()) {
					long insideOf = node.getInsideOf();
					NodeWrapper coarsed = coarsedGraph.getNode(insideOf);
					partitionId  = coarsed.getPartition();
				} else {

					// copia a partição vertice do grafo mais contraido para o
					// mais fino
					NodeWrapper v = coarsedGraph.getNode(node.getId());
					partitionId = v.getPartition();
				}
				node.setPartition(partitionId);
				
//				partition.insertNodeToIndex(partitionId, node);
				transaction.intermediateCommit();
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro repassando particionamento do grafo "
					+ coarsedGraph.getLevel() + " para " + fineGraph.getLevel(), e);
		} finally {
			transaction.commit();
		}
//		BestPartition bestPart = partition.createBestPartition(fineGraph, fineGraph);//TODO_OK: não precisa criar um best novo aqui. É só retornar o partition criado...
//		partition.createEdgeCut(fineGraph.getAllEdgesStatic(), fineGraph, fineGraph);
//		partition.calculateEdgeCut(fineGraph.getAllEdgesStatic());
		
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto para repassar o particionamento do grafo {} para {}: {} ms", new Object[] {
				coarsedGraph.getLevel(), fineGraph.getLevel(), delta });
//		return partition;
	}
	
	/**
	 * Performs the refinement phase using the abstract method.
	 * The subclass must handle its own transaction.
	 * 
	 * 
	 * @param graph
	 * @param level
	 * @param partitionToRefine used to ease take the edges on cut. 
	 * @return
	 */
	private BestPartition improvePartition(GraphWrapper graph, int level) {//update diagram
		BestPartition bestPart = null;
		try {
			TwoWayPartition partition = createNewPartition(graph, level);
			
			long deltaGenPart = System.currentTimeMillis();
	
			//UTIL: o objeto partitionToRefine serve para obter facilmente as arestas que já estão no corte.
			//TODO: testar esse generateBoundaryPartition para ver se não dá efeito colateral em outro lugar. Parece que não.
			partition.generateBoundaryPartition(graph.getAllEdgesStatic(), graph);//partitionToRefine.queryEdgesOnCut());

			deltaGenPart = System.currentTimeMillis() - deltaGenPart;
			logger.debug("Tempo gasto para BKL gerar boundary partition: " + deltaGenPart + " ms");
			
			bestPart = refinePartition(graph, partition);
		} catch (Exception e) {
			logger.error("Erro no refinamento. Verificar subclasse.", e);
		}
		return bestPart;
	}
	
//	/**
//	 * Improve the current graph partition.
//	 * @param graph
//	 * @param partition
//	 * @return
//	 */
//	private BestPartition improvePartition(GraphWrapper graph, TwoWayPartition partition) {
//
//		BestPartition part = null;
//		try {
//			part = refineGraph(graph, levelInfo.getCurrentLevel(), partition);
//		} catch (Exception e) {
//			logger.error("Erro no refinamento. Verificar subclasse.", e);
//		}
//		return part;
//	}

	private BestPartition executeUncoarse(BestPartition originalPartition) {
		BestPartition part = originalPartition;
		logger.warn("Expandindo o grafo...");

		while (levelInfo.hasMoreLevels()) {
			//TODO: liberar a memória da variável part, para os níveis intermediários
//			TransactionControl tc = new TransactionControl(levelInfo.getCurrentGraph());
//			tc.beginTransaction();
//			part.clear();
//			tc.commit();
			
			//deu este erro
			/*
			 java.lang.IllegalStateException: This index (Index[indexEdgesOnCut, relationship]) has been deleted
	at org.neo4j.index.impl.lucene.LuceneIndex.assertNotDeleted(LuceneIndex.java:85)
	at org.neo4j.index.impl.lucene.LuceneIndex.getConnection(LuceneIndex.java:73)
	at org.neo4j.index.impl.lucene.LuceneIndex.delete(LuceneIndex.java:182)
	at br.edu.unifei.mestrado.commons.partition.index.CutIndexDB.remove(CutIndexDB.java:68)
	at br.edu.unifei.mestrado.commons.partition.AbstractPartition.clearInternalIndexes(AbstractPartition.java:311)
	at br.edu.unifei.mestrado.commons.partition.BestPartition.clear(BestPartition.java:57)
	at br.edu.unifei.mestrado.mn.TwoWayMultinivel.executeUncoarse(TwoWayMultinivel.java:246)
	at br.edu.unifei.mestrado.mn.TwoWayMultinivel.executeMultilevel(TwoWayMultinivel.java:286)
	at br.edu.unifei.mestrado.mn.MainMultiNivelNeo4J.main(MainMultiNivelNeo4J.java:29)
			 */
			
			part = uncoarseOneLevel();
			
			if(levelInfo.hasMoreLevels()) {
				levelInfo.getCurrentGraph().shutdown(true);
				levelInfo.removeGraph();
			}
		}
		
		logger.warn("Fim da fase de uncoarsening...");
		return part;
	}
	

	/**
	 * Execução geral do multinivel <br>
	 * 1) Prepare <br>
	 * 2) Coarse (contrai) <br>
	 * 3) Partitioning <br>
	 * 4) Uncoarse (expande)
	 * 
	 */
	public BestPartition executeMultilevel() {
		initView(getGraph(), AbstractPartition.TWO_WAY);
		GraphWrapper initialGraph = getGraph();

		preprocess(initialGraph);

		// fase de coarsening
		executeCoarsening();
		// fase de particionamento
		BestPartition part = executePartition(levelInfo.getCurrentGraph());
		logger.warn("Corte após particionamento: " + part.getCutWeight());

		updateView(levelInfo.getCurrentGraph(), part.getCutWeight());

		// part.calculateEdgeCut(levelInfo.getPreviousGraph().getAllEdgesStatic());
		// fase de uncoarsening
		part = executeUncoarse(part);
		return part;
	}

	@Override
	public void execute() {
		initView(getGraph(), AbstractPartition.TWO_WAY);

		try {
			long time = System.currentTimeMillis();
			executeMultilevel();
			time = System.currentTimeMillis() - time;
			logger.warn("Fim do 2-way Multinivel. Tempo gasto: " + time + " ms File: "
					+ getGraph().getGraphFileName());
		} catch (Throwable e) {
			logger.error("Erro executando Multinivel.", e);
		}
	}
}

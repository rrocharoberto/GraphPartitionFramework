package br.edu.unifei.mestrado.kl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.unifei.mestrado.commons.algo.AlgorithmObject;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.GraphWrapper;
import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.TransactionControl;
import br.edu.unifei.mestrado.commons.graph.TransactionInterface;
import br.edu.unifei.mestrado.commons.partition.AbstractPartition;
import br.edu.unifei.mestrado.commons.partition.BestPartition;
import br.edu.unifei.mestrado.commons.view.ViewListener;
import br.edu.unifei.mestrado.kl.util.DArray;
import br.edu.unifei.mestrado.kl.util.GainList;
import br.edu.unifei.mestrado.kl.util.KLPartition;
import br.edu.unifei.mestrado.kl.util.NodeDiff;
import br.edu.unifei.mestrado.kl.util.NodePair;

public class KL extends AlgorithmObject {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Number of iteration retries if there was no exchange in the iteration (G <= 0) 
	 */
	private static int MAX_NUMBER_OF_RETRIES = 5;
	
	/**
	 * Number of nodes with greatest D to calculate the gain.
	 * 
	 * Recomendado pelo artigo KL.
	 */
	private static int MAX_NUNBER_WITH_GREAT_D = 3;

	protected KLPartition basePartition;
	
	protected DArray dValues;

	public KL(GraphWrapper graph) {
		super(graph);
		this.basePartition = new KLPartition(graph.getLevel(), 
				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
	}

	public KL(GraphWrapper graph, ViewListener view) {
		super(graph, view);
		this.basePartition = new KLPartition(graph.getLevel(), 
				graph.getCurrentPartitionIndex(), graph.getCurrentCutIndex());
	}

	public KL(GraphWrapper graph, KLPartition partition) {
		super(graph);
		this.basePartition = partition;
	}

	/**
	 * Retorna uma aresta wrapper para a aresta verdadeira entre va e vb. Retornar null se não exisistir a
	 * aresta verdadeira.
	 * 
	 * @param va
	 * @param vb
	 * @return
	 */
	public EdgeWrapper getArestaLinking(long va, long vb) {
		return getGraph().getEdgeLinking(va, vb);
	}

	/**
	 * Calcula o custo.
	 * UTIL: calcula o custo baseado nas arestas dos vertices
	 * UTIL: para KL, calcula de todos os vertices de cada partição
	 * UTIL: para BKL, calcula somente dos vertices da fronteira de cada partição
	 */
	protected void calculateCost() {
		long delta = System.currentTimeMillis();

		TransactionControl transaction = new TransactionControl(getGraph());
		try {
			transaction.beginTransaction();
			
			//TODO_OK: verificar se dá para tirar esse partition.getAllNodes() daqui
			for (NodeWrapper node : basePartition.getAllNodesSet1()) {
				
				//nao precisa mais desse if, pois o referenceNode não está indexado
	//			if (!node.hasProperty(GraphProperties.ID)) { continue; }
				calculateDForNode(node);
				transaction.intermediateCommit();
			}
			for (NodeWrapper node : basePartition.getAllNodesSet2()) {
				calculateDForNode(node);				
				transaction.intermediateCommit();
			}
		} catch (Throwable e) {
			logger.error("Erro no banco calculando D.", e);
		} finally {
			transaction.commit();
		}
		// logger.warn("E: ");
		// Util.mostrarArray(custo.getExtCost(), grafo.getVertexCount());
		// logger.warn("I: ");
		// Util.mostrarArray(custo.getIntCost(), grafo.getVertexCount());
		logger.debug("D: {}", dValues);
		delta = System.currentTimeMillis() - delta;
		logger.debug("Tempo gasto para calcular o custo inicial:" + delta + " ms");
	}

	/**
	 * Calcula o valor de D para o vertice.
	 * 
	 * @param node
	 */
	protected void calculateDForNode(NodeWrapper node) {
		int intCost = 0;
		int extCost = 0;
		for (EdgeWrapper edge : node.getEdgesStatic()) {
			NodeWrapper other = edge.getOtherNode(node);
			// UTIL: se a partição for igual, então é custo interno, senão é custo externo
			if (node.getPartition() == other.getPartition()) {
				// calcula o peso entre elementos do mesmo grupo
				intCost += edge.getWeight();
			} else {
				// calcula o peso entre elementos de grupos diferentes
				extCost += edge.getWeight();
			}
		}
		node.setD(extCost - intCost); //TODO_OK: fazer commit no metodo que chama esse para salvar o D no banco
		dValues.addNode(node.getId(), node.getPartition(), node.getD());
	}

	/*
	 * Recalcula o custo.
	 */
	protected void recalculateCost(NodePair pair) {
		NodeDiff ai = pair.getI();
		NodeDiff bi = pair.getJ();

		TransactionControl transaction = new TransactionControl(getGraph());
		try {
			transaction.beginTransaction();
			// UTIL: só recalcula o D para os vertices que tem arestas para ai ou bi
			// Os outros ganhos não precisam ser recalculados
			updateDOfNeighbors(ai);
			updateDOfNeighbors(bi);
		} catch (Throwable e) {
			logger.error("Erro no banco recalculando D.", e);
		} finally {
			transaction.commit();
		}
		logger.debug("D recalculados: {}", dValues);
	}

	protected void updateDOfNeighbors(NodeDiff baseNodeDiff) {
		NodeWrapper baseNode = getGraph().getNode(baseNodeDiff.getNodeId());
		Iterable<EdgeWrapper> edges = baseNode.getEdgesStatic();
		int basePartition = baseNode.getPartition();
		for (EdgeWrapper edge : edges) {
			NodeWrapper other = edge.getOtherNode(baseNode);
			// se o vertice ainda não foi trocado
			if (!other.isLocked()) {// TODO: verificar pq o other pode não estar no corte, no caso do bkl
				int D = 0;
				int otherPartition = other.getPartition();
				if (otherPartition == basePartition) {
					// soma o custo, pois a aresta ai/bi vai sair da partição
					D = other.getD() + 2 * edge.getWeight();
				} else {
					// subtrai o custo, pois a aresta ai/bi vai entrar na partição
					D = other.getD() - 2 * edge.getWeight();
				}

				// UTIL: precisa remover, alterar o D, e inserir novamente, pra funcionar corretamente o treeSet
				dValues.removeNode(other.getId(), otherPartition, other.getD());
//				dValues.updateDValueForVertex(other, otherPartition);
				other.setD(D);
				dValues.addNode(other.getId(), otherPartition, D);
			}
		}
	}

	/*
	 * Calcula o ganho.
	 */
	protected GainList calculateGain() {
		GainList ganho = new GainList();

		// UTIL: tem que calcular o ganho, mesmo se os vertices não forem adjacentes
		// então tem que pegar todos por todos dos vertices
		// só as arestas não serve, pq as arestas pegam só vertices adjacentes.

		int count = 0;//just for log
		long delta = System.currentTimeMillis();
		
		int ia = 0;

		for (NodeDiff va : dValues.getSet1()) {
			int ib = 0;
			for (NodeDiff vb : dValues.getSet2()) {
				// UTIL: o DArray já está separado por particao e tem só os vertices não usados
				EdgeWrapper aresta = getArestaLinking(va.getNodeId(), vb.getNodeId()); // TODO: UTIL: isso está deixando muito
				// lento
				calculateGainForNodes(ganho, va, vb, aresta);

				count++;
				
				ib++;
				if(ib >= MAX_NUNBER_WITH_GREAT_D) {
					break;
				}
			}
			ia++;
			if(ia >= MAX_NUNBER_WITH_GREAT_D) {
				break;
			}
		}
		delta = System.currentTimeMillis() - delta;

		logger.debug("Tempo gasto para calcular " + ganho.getSize() + " ganhos: " + delta + " ms | count: "
				+ count);

		return ganho;
	}

	/**
	 * Calcula o ganho relativo a troca de va com vb. A peso da aresta é usada no calculo do ganho. <br>
	 * 
	 * Quando chama esse método, a partição de va é diferente de vb
	 * 
	 * @param gain
	 * @param va
	 * @param vb
	 * @param edge
	 */
	protected void calculateGainForNodes(GainList gain, NodeDiff va, NodeDiff vb,
			EdgeWrapper edge) {
		int costFromIToJ;
		if (edge != null) {
			costFromIToJ = edge.getWeight();
		} else {
			costFromIToJ = 0;
		}
		int gainValue = va.getD() + vb.getD() - 2 * costFromIToJ;
		if (gainValue < 0) {
			if (!acceptNegativeGain()) {// se for BKL, não pode incluir ganho negativo
				return;
			}
		}
		// UTIL: sempre salva o va no ganho, como sendo o vertice da particao 1 e vb da particao 2
		if (va.getPartition() == AbstractPartition.PART_1) {
			gain.addPair(va, vb, gainValue);
		} else {
			gain.addPair(vb, va, gainValue);
		}
	}

	/**
	 * Método usado para indicar se é KL ou BKL.
	 * 
	 * O BKL precisa reimplementar esse método retornando false;
	 * 
	 * @return
	 */
	protected boolean acceptNegativeGain() {
		return true;
	}

	/**
	 * Escolhe o melhor par (maior ganho).
	 * 
	 * @param gains
	 * @return
	 */
	protected NodePair choosePair(GainList gains) {

		gains.printGains(20);

		// se não existir o melhor, lança GainZeroException
		NodePair pair = gains.getBestPair();
		if(pair == null) {
			logger.debug("Não encontrou mais ganhos. Terminar a iteracao.");
			return null;
		}
		if(pair.getActualGain() == 0) {
			logger.debug("Encontrou par com ganho zero. Terminar a iteracao.");
			return null;
		}
		
		//UTIL: não pode fazer esse filtro aqui, pois um nó com ganho zero, pode afetar outros ganhos
//		if (pair.getGain() == 0) { //o ganho nunca é menor que zero.
//			throw new GainZeroException("Encontrou par com ganho ZERO. Terminar a iteracao.");
//		}

		if(logger.isDebugEnabled()) {
			logger.debug("Par escolhido: {}", pair.toHumanString());
		}

		TransactionControl transaction = new TransactionControl(getGraph());
		try{
			transaction.beginTransaction();
			// marca os elementos para não serem usados em futuros processamentos
			getGraph().getNode(pair.getI().getNodeId()).lock();
			getGraph().getNode(pair.getJ().getNodeId()).lock();
		} finally {
			transaction.commit();
		}
		
		//remove os vértices de dValues
		dValues.removeNode(pair.getI());
		dValues.removeNode(pair.getJ());
		return pair;
	}

	/*
	 * Executa a troca, se existir.
	 */
	protected int performExchange(List<NodePair> pairs) {
		int finalK = 0;
		int partialK = 0;

		int maxGain = 0;
		int partialGain = 0;

		for (NodePair pair : pairs) {
			partialGain += pair.getActualGain();
			partialK++;

			// UTIL: se o ganho total for MAIOR que algum ganho total anterior
			if (partialGain > maxGain) {
				// armazena somente até as trocas para o melhor ganho
				finalK = partialK;
				maxGain = partialGain;
			}
		}
		int cutWeight = -1;
		if (finalK > 0) {
			basePartition.exchangePairs(pairs.subList(0, finalK), getGraph());
			basePartition.printSets();
			
			//UTIL: calcula o corte de arestas no final, depois de ter trocado os vértices
			cutWeight = basePartition.calculateEdgeCut(getGraph().getAllEdgesStatic());
		}

		//just for logging
		if (cutWeight == -1) {
			logger.warn("Qtd de trocas: " + finalK + " ganho total: " + maxGain
					+ " No exchanges made.");
		} else {
			logger.warn("Qtd de trocas: " + finalK + " ganho total: " + maxGain
					+ " cutWeight: " + cutWeight);
		}
		return cutWeight;
	}

	/**
	 * Iteração KL.
	 * 
	 * @param itNumber
	 *            Número da iteração.
	 * @param partition
	 *            Partição usada para fazer as trocas necessarias
	 * @return O valor do corte.
	 */
	protected int klIteration(int itNumber) {
		
		//variaveis de contabilidade
		long delta = System.currentTimeMillis();
		long tGanhoI = 0, tGanho = 0, tChoose = 0, tRecalc = 0;
		long dl = 0;
		int countChoosen = 0;
		int qtdNodesLocked = 0;
		//variaveis de contabilidade

		List<NodePair> list = new ArrayList<NodePair>();
		dValues = new DArray();

		logger.warn("Inicio da iteracao KL " + itNumber);
		logger.debug("Initial partitions:");
		basePartition.printSets();
		
		logger.debug("1 - calcular o custo");
		calculateCost();
		
		logger.debug("2 - calcular o ganho");
		dl = System.currentTimeMillis();
		GainList ganhos = calculateGain();
		dl = System.currentTimeMillis() - dl;
		tGanhoI += dl;

		updateView(getGraph(), basePartition.getCutWeight());
		
		dl = System.currentTimeMillis();
		NodePair choosen = choosePair(ganhos);
		dl = System.currentTimeMillis() - dl;
		while (choosen != null) {
			countChoosen++;
			repaint(choosen.getI().getNodeId());
			repaint(choosen.getJ().getNodeId());
			tChoose += dl;

			qtdNodesLocked += 2;
			
			logger.debug("Tamanho da lista gain: " + ganhos.getSize() + " qtdNodesLocked: " + qtdNodesLocked + " curGain: " + choosen.getActualGain());

			// UTIL: cria um novo par, para desacoplar os nos escolhidos dos originais, que serao modificados
			// no decorrer do processamento.
			NodePair newPair = new NodePair(choosen);
			
			logger.debug("3 - armazenar o maior ganho (melhor par)");
			list.add(newPair);
			
			logger.debug("4 - atualizar os valores de D");
			dl = System.currentTimeMillis();
			recalculateCost(choosen);
			dl = System.currentTimeMillis() - dl;
			tRecalc += dl;

			logger.debug("2 - calcular o ganho");
			dl = System.currentTimeMillis();
			ganhos = calculateGain();
			dl = System.currentTimeMillis() - dl;
			tGanho += dl;

			repaint();
			
			dl = System.currentTimeMillis();
			choosen = choosePair(ganhos);
			dl = System.currentTimeMillis() - dl;
		}
		qtdNodesLocked = countChoosen * 2;

		int cutWeight = performExchange(list);
		delta = System.currentTimeMillis() - delta;

		repaint();
		
		logger.debug("Tempos: tGanhoI: " + tGanhoI + " tGanho: " + tGanho + " tChoose: " + tChoose
				+ " tRecalc: " + tRecalc + " countChoosen: " + countChoosen + " qtdNodesLocked: " + qtdNodesLocked);
		logger.warn("Fim da iteracao KL {}. Tempo total gasto: {} ms.", itNumber, delta);

		return cutWeight;
	}

	/*
	 * Metodo KL.
	 */
	public BestPartition executeKL() {
		initView(getGraph(), AbstractPartition.TWO_WAY);
		
		long delta = System.currentTimeMillis();
//		basePartition.initialFixedBalancedPartition(getGraph());
		basePartition.initialArbitraryPartition((int)System.currentTimeMillis(), getGraph());
		delta = System.currentTimeMillis() - delta;
		repaint();
		logger.debug("Tempo gasto no initialPartition : " + delta + " ms");

		int bestCut = basePartition.getCutWeight();

		BestPartition bestPartition = basePartition.createBestPartition(getGraph(), getGraph());

		int it = 0;
		int bestIt = 0;
		int retriesNewArbritraryPartition = 0;//número de tentativas para gerar uma nova partição inicial
		int retriesBestCut = 0; //número de tentativas de melhor corte para uma mesma partição inicial
		
		do {
			it++;

			getGraph().unlockAllNodes();
			repaint();

			int cut = klIteration(it);

			// se não houve trocas
			if (cut == -1) {
				logger.warn("Melhor corte: " + bestCut + " Iteração " + bestIt + " retries: " + retriesNewArbritraryPartition);
				retriesNewArbritraryPartition++;
				if(retriesNewArbritraryPartition < MAX_NUMBER_OF_RETRIES) {//se não chegou à última tentativa (para não desperdiçar processamento)
					// tenta outra partição arbitrária.
					basePartition.initialArbitraryPartition((int)System.currentTimeMillis(), getGraph());
					retriesBestCut = 0;
				}
			} else {
				// usa a mesma partição atual, como sendo a partição inicial do passo seguinte
				if (cut < bestCut) {
					// armazena o melhor resultado
					bestIt = it;
					
					//TODO_OK: acho que é melhor usar a instancia do indice existente para o best e
					//criar um novo índice para a partição seguinte: VERIRICAR! -> implementado
					
					///TODO:Verificar se precisa ou não do clear aqui //bestPartition.clear();
					bestPartition = basePartition.createBestPartition(getGraph(), getGraph());
					
					//teste
					int cutWeight = basePartition.calculateEdgeCut(getGraph().getAllEdgesStatic());
					System.out.println("cutWeight: " + cutWeight);
					bestCut = cut;
				} else {
					//UTIL: condição onde houve trocas, mas o cut atual não é melhor que o bestCut  
					//parece inconsistente, mas isso ocorre após a tentativa de uma novo particionamento arbitrário.
					retriesBestCut++;
					if(retriesBestCut > MAX_NUMBER_OF_RETRIES) {//se alcançou as tentativas
						retriesNewArbritraryPartition++;
						if(retriesNewArbritraryPartition < MAX_NUMBER_OF_RETRIES) {//se não chegou à última tentativa(para não desperdiçar processamento)
							// tenta outra partição arbitrária.
							basePartition.initialArbitraryPartition((int)System.currentTimeMillis(), getGraph());
						}
					}
				}
				logger.warn("Melhor corte KL: " + bestCut + " It: " + bestIt 
						+ " retries: " + retriesNewArbritraryPartition + " rtBest: " + retriesBestCut);
			}
		} while(retriesNewArbritraryPartition <= MAX_NUMBER_OF_RETRIES);
		
		//UTIL: depois que termina, atualiza a partição de cada node do grafo
		System.out.println("bestCut: " + bestCut);
		bestPartition.printSets();
		refreshNodePartiton(bestPartition, getGraph());
		int cutWeight = bestPartition.calculateEdgeCut(getGraph().getAllEdgesStatic());
		System.out.println("cutWeight: " + cutWeight);
		return bestPartition;
	}
	
	private void refreshNodePartiton(BestPartition bestPartition, TransactionInterface tIf) {
		TransactionControl tc = new TransactionControl(tIf);
		tc.beginTransaction();
		for (int k = AbstractPartition.FIRST_PART; k <= bestPartition.getK(); k++) {
			for (NodeWrapper node : bestPartition.queryNodesFromSet(k)) {
				node.setPartition(k);
				tc.intermediateCommit();
			}
		}
		tc.commit();
	}

	@Override
	public void execute() {
		try {
			long time = System.currentTimeMillis();
			BestPartition result = executeKL();
			updateView(getGraph(), result.getCutWeight());

			time = System.currentTimeMillis() - time;
			logger.warn("Fim do KL. Tempo gasto: " + time + " ms File: " + getGraph().getGraphFileName());
			result.printSets();
		} catch (Throwable e) {
			logger.error("Erro executando KL.", e);
		}
	}
	
}

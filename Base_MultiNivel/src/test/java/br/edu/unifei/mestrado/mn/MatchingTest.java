package br.edu.unifei.mestrado.mn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import br.edu.unifei.mestrado.commons.graph.NodeWrapper;
import br.edu.unifei.mestrado.commons.graph.EdgeWrapper;
import br.edu.unifei.mestrado.commons.graph.mem.GraphMem;

public class MatchingTest {

	/*
	 Grafo de teste:
	 
	 9
0 1
0 4
1 2
1 5
2 3
2 6
3 6
3 7
4 5

	 */
	private Matching mat = new Matching();
	
	@Test
	public void testCoarseVertex() {
		GraphMem gold = new GraphMem("../GraphGenerator/fixed/teste_6_2.txt");		
		GraphMem gnew = new GraphMem("../GraphGenerator/fixed/empty_graph.txt");

		{
			NodeWrapper v1 = gold.getNode(1);
			NodeWrapper v2 = gold.getNode(2);

			mat.coarseNodes(gnew, v1, v2);
			
			NodeWrapper coarsed0 = gnew.getNode(1);
			assertEquals(2, coarsed0.getWeight());
			
			assertFalse(gnew.getAllEdges().iterator().hasNext());
		}
		{
			NodeWrapper v5 = gold.getNode(5);
			NodeWrapper v6 = gold.getNode(6);

			mat.coarseNodes(gnew, v5, v6);
			
			NodeWrapper coarsed0 = gnew.getNode(1);
			assertEquals(2, coarsed0.getWeight());
			
			NodeWrapper coarsed4 = gnew.getNode(5);
			assertEquals(2, coarsed4.getWeight());
			
			Iterator<EdgeWrapper> iter = gnew.getAllEdges().iterator();
			assertTrue(iter.hasNext());
			
			EdgeWrapper edge = iter.next();
			
			assertEquals(2, edge.getWeight());
		}
		{
			NodeWrapper v3 = gold.getNode(3);
			NodeWrapper v4 = gold.getNode(4);

			mat.coarseNodes(gnew, v3, v4);
			
			NodeWrapper coarsed0 = gnew.getNode(1);
			assertEquals(2, coarsed0.getWeight());
			
			NodeWrapper coarsed4 = gnew.getNode(5);
			assertEquals(2, coarsed4.getWeight());
			
			NodeWrapper coarsed2 = gnew.getNode(3);
			assertEquals(2, coarsed2.getWeight());

			Iterator<EdgeWrapper> iter = gnew.getAllEdges().iterator();
			assertTrue(iter.hasNext());
			
			EdgeWrapper edge = iter.next();//aresta E2
			assertEquals(2, edge.getId());
			assertEquals(2, edge.getWeight());

			assertTrue(iter.hasNext());
			
			edge = iter.next(); //aresta E3
			assertEquals(3, edge.getId());
			assertEquals(1, edge.getWeight());
		}
	}

	@Test
	public void testProcessRemainingVertices() {
//		testCoarseVertex();
		GraphMem gold = new GraphMem("../GraphGenerator/fixed/teste_6_2.txt");
		GraphMem gnew = new GraphMem("../GraphGenerator/fixed/empty_graph.txt");
		
		Map<NodeWrapper, Boolean> nodes = new HashMap<NodeWrapper, Boolean>();
		nodes.put(gold.getNode(1), true);
		nodes.put(gold.getNode(2), true);
		nodes.put(gold.getNode(3), true);
		nodes.put(gold.getNode(4), true);
		nodes.put(gold.getNode(5), true);
		nodes.put(gold.getNode(6), true);
		nodes.put(gold.getNode(7), false);
		nodes.put(gold.getNode(8), false);
		mat.processRemainingNodes(nodes, gnew);
		
		NodeWrapper v7 = gnew.getNode(7);
		assertEquals(1, v7.getWeight());
		
		NodeWrapper v8 = gnew.getNode(8);
		assertEquals(1, v8.getWeight());
		
		Iterator<EdgeWrapper> iter = gnew.getAllEdges().iterator();
		assertTrue(iter.hasNext());
		
		EdgeWrapper edge = iter.next();//aresta E1
		assertEquals(1, edge.getId());
		assertEquals(2, edge.getWeight());

		assertTrue(iter.hasNext());
		
		edge = iter.next(); //aresta E2
		assertEquals(2, edge.getId());
		assertEquals(1, edge.getWeight());

		assertTrue(iter.hasNext());
		edge = iter.next(); //aresta E5
		assertEquals(5, edge.getId());
		assertEquals(2, edge.getWeight());

		assertTrue(iter.hasNext());
		edge = iter.next(); //aresta E7
		assertEquals(7, edge.getId());
		assertEquals(1, edge.getWeight());
	}
}

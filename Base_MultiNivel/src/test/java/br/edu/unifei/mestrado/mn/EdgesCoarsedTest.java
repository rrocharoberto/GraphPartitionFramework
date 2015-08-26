package br.edu.unifei.mestrado.mn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import br.edu.unifei.mestrado.commons.mn.EdgesCoarsed;
import br.edu.unifei.mestrado.commons.mn.TempEdge;

public class EdgesCoarsedTest {

	private int id = 0;
	
	@Test
	public void testEmpty() {
		EdgesCoarsed ec = new EdgesCoarsed();
		Iterator<TempEdge> it = ec.iterator();
		assertFalse(it.hasNext());
		
		ec.addEdge(1, 2, createEdge(1, 2));
		it = ec.iterator();
		assertTrue(it.hasNext());
		
		ec.clear();
		
		it = ec.iterator();
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testAddEdge() {
		EdgesCoarsed ec = new EdgesCoarsed();
		long nodeA = 1;
		long nodeB = 2;
		Iterator<TempEdge> it;
		TempEdge tmp;
		{	
			ec.addEdge(nodeA, nodeB, createEdge(nodeA, nodeB));
			
			it = ec.iterator();
			assertTrue(it.hasNext());
			
			tmp = it.next(); //aresta 0
			assertEquals(0, tmp.getId());
			assertEquals(1, tmp.getWeight());
			
			ec.addEdge(nodeA, nodeB, createEdge(nodeA, nodeB)); //nova aresta paralela
			
			it = ec.iterator();
			tmp = it.next();
			assertEquals(0, tmp.getId());
			assertEquals(2, tmp.getWeight());//peso 2
		}
		//outro par de vertices
		nodeA = 1;
		nodeB = 3;
		
		{	
			ec.addEdge(nodeA, nodeB, createEdge(nodeA, nodeB));
			
			it = ec.iterator();
			assertTrue(it.hasNext());
			tmp = it.next(); //aresta 0
			assertEquals(0, tmp.getId());
			assertEquals(2, tmp.getWeight());
			
			assertTrue(it.hasNext());
			tmp = it.next(); //aresta 1
			assertEquals(2, tmp.getId());
			assertEquals(1, tmp.getWeight());

			ec.addEdge(nodeA, nodeB, createEdge(nodeA, nodeB)); //nova aresta paralela
			
			it = ec.iterator();

			assertTrue(it.hasNext());
			tmp = it.next(); //aresta 0
			assertEquals(0, tmp.getId());
			assertEquals(2, tmp.getWeight());
			
			assertTrue(it.hasNext());
			tmp = it.next(); //aresta 1
			assertEquals(2, tmp.getId());
			assertEquals(2, tmp.getWeight());
		}
		
	}

	private TempEdge createEdge(long startNode, long endNode){
		TempEdge newEdge = new TempEdge(id++, 1, startNode, endNode);
		return newEdge;

	}
}

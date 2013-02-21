package com.diycomputerscience.minesweeper;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SquareTest {
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	private SquareDo square;

	@Before	
	public void setUp() throws Exception {
		this.square = new SquareDo();
	}

	@After
	public void tearDown() throws Exception {
		this.square = null;
	}

	@Test
	public void testInitialSquareState() throws Exception {
		assertEquals(SquareDo.SquareState.COVERED, this.square.getState());
	}
	
	@Test
	public void testUncoverCoveredSquare() throws Exception {
		this.square.uncover();
		assertEquals(SquareDo.SquareState.UNCOVERED, this.square.getState());
	}

	@Test
	public void testUncoverUncoveredSquare() throws Exception {
		this.square.uncover();
		this.square.uncover();
		assertEquals(SquareDo.SquareState.UNCOVERED, this.square.getState());
	}
	
	@Test
	public void testMarkCoveredSquare() throws Exception {
		this.square.mark();
		assertEquals(SquareDo.SquareState.MARKED, this.square.getState());
	}
	
	@Test
	public void testMarkUncoveredSquare() throws Exception {
		this.square.uncover();
		this.square.mark();
		assertEquals(SquareDo.SquareState.UNCOVERED, this.square.getState());
	}
	
	@Test
	public void testUncoverMarkedSquareWhichIsNotAMine() throws Exception {
		this.square.mark();
		this.square.uncover();		
		assertEquals(SquareDo.SquareState.MARKED, this.square.getState());
	}
	
	@Test
	public void testUncoverMarkedSquareWhichIsAAMine() throws Exception {
		this.square.setMine(true);
		this.square.mark();
		this.square.uncover();		
		assertEquals(SquareDo.SquareState.MARKED, this.square.getState());
	}
	
	@Test
	public void testMarkMarkedSquare() throws Exception {
		this.square.mark();
		this.square.mark();
		assertEquals(SquareDo.SquareState.COVERED, this.square.getState());
	}
	
	@Test(expected=UncoveredMineException.class)
	public void testUncoverMine() throws Exception {
		this.square.setMine(true);
		this.square.uncover();
	}
	
	@Test
	public void testNullCheckInCopyConstructor() throws Exception {
		expectedEx.expect(NullPointerException.class);
		expectedEx.expectMessage("square cannot be null");
		SquareDo square = new SquareDo(null);
	}
}

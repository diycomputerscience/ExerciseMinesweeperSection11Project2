package com.diycomputerscience.minesweeper;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BoardTest {

	private BoardDo board;
	private MineInitializationStrategy mineInitializationStrategy;
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		this.mineInitializationStrategy = EasyMock.createMock(MineInitializationStrategy.class);
		Point boardSize = new Point(BoardDo.MAX_ROWS, BoardDo.MAX_ROWS);
		expect(this.mineInitializationStrategy.mines(boardSize)).andReturn(MockMineUtils.mines(boardSize));
		replay(this.mineInitializationStrategy);
		
		this.board = new BoardDo(mineInitializationStrategy);
	}

	@After
	public void tearDown() throws Exception {
		this.board = null;
	}
		
	@Test
	public void testMineInitializationStrategyNullCheckInConstructor() {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("mineInitializationStrategy cannot be null");
		BoardDo board = new BoardDo(null);
	}
	
	@Test
	public void testSquaresNotNull() {
		SquareDo squares[][] = this.board.getSquares();		
		for(SquareDo squareRow[] : squares) {			
			for(SquareDo aSquare : squareRow) {
				assertNotNull(aSquare);
			}
		}
	}
	
	@Test
	public void testAtleastOneSquareShouldBeAMine() throws Exception {
		boolean mineFound = false;
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				if(square.isMine()) {
					// this code can be refactored to avoid unnecassary looping if a mine is found once
					// however we have kept it as is for the sake of simplicity
					mineFound = true;
				}
			}
		}
		// test will fail if at least one mine has not been found
		assertTrue(mineFound);
	}
	
	@Test
	public void testUncoverSquaresWhichAreNotMines() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				if(!square.isMine()) {
					// this should uncover the Square object we are holding
					this.board.uncover(new Point(row, col));
					SquareDo squareFromBoard = getSquareUsingReflection(this.board, new Point(row, col));
					assertEquals(SquareDo.SquareState.UNCOVERED, squareFromBoard.getState());
				}
			}
		}
	}
	
	@Test
	public void testUncoverSquaresWhichAreMines() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				if(square.isMine()) {
					try {
						// expect an exception
						this.board.uncover(new Point(row, col));
						// control should never come here, because invoking the line above should throw an Exception
						fail("Exception was not thrown when a mine was uncovered");
					} catch(UncoveredMineException eme) {
						// this is what we expect... so the code passes
					}
				}
			}
		}
	}
	
	@Test
	public void testUncoverSquaresWhichAreAlreadyUncovered() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				if(!square.isMine()) {
					this.board.uncover(new Point(row, col));
					assertEquals(SquareDo.SquareState.UNCOVERED, 
								 getSquareUsingReflection(this.board, new Point(row, col)).getState());
					// uncover the same square and verify that it's state is still UNCOVERED
					this.board.uncover(new Point(row, col));
					assertEquals(SquareDo.SquareState.UNCOVERED, 
								 getSquareUsingReflection(this.board, new Point(row, col)).getState());
				}
			}
		}
	}
	
	@Test
	public void testMarkSquare() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				this.board.mark(new Point(row, col));
				assertEquals(SquareDo.SquareState.MARKED, 
							 getSquareUsingReflection(this.board, new Point(row, col)).getState());				
			}
		}
	}
	
	@Test
	public void testMarkSquaresWhichAreAlreadyMarked() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				this.board.mark(new Point(row, col));
				assertEquals(SquareDo.SquareState.MARKED, 
							 getSquareUsingReflection(this.board, new Point(row, col)).getState());
				// mark the square again and verify that the state is changed to COVERED
				this.board.mark(new Point(row, col));
				assertEquals(SquareDo.SquareState.COVERED, square.getState());
			}
		}
	}

	@Test
	public void testMarkSquaresWhichAreUncovered() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				if(!square.isMine()) {
					// this should uncover the Square object we are holding
					this.board.uncover(new Point(row, col));
					assertEquals(SquareDo.SquareState.UNCOVERED, getSquareUsingReflection(this.board, new Point(row, col)).getState());
					// marking an uncovered Square should have no effect
					this.board.uncover(new Point(row, col));
					assertEquals(SquareDo.SquareState.UNCOVERED, getSquareUsingReflection(this.board, new Point(row, col)).getState());
				}
			}
		}
	}
	
	@Test
	public void testUncoverASquareWhichIsAlreadyMarked() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<squares.length; row++) {
			SquareDo squareRow[] = squares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				this.board.mark(new Point(row, col));
				assertEquals(SquareDo.SquareState.MARKED, getSquareUsingReflection(this.board, new Point(row, col)).getState());
				// uncovering a marked square should have no effect
				this.board.uncover(new Point(row, col));
				assertEquals(SquareDo.SquareState.MARKED, getSquareUsingReflection(this.board, new Point(row, col)).getState());
			}
		}
	}
	
	@Test
	public void testSquareCount() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		for(int row=0; row<BoardDo.MAX_ROWS; row++) {
			for(int col=0; col<BoardDo.MAX_COLS; col++) {
				assertEquals(MockMineUtils.getSquareCount(new Point(row, col)),
							 squares[row][col].getCount());
			}
		}
	}
	
	// Defensive coding tests
	
	@Test
	public void testSquaresGridSize() throws Exception {
		SquareDo squares[][] = this.board.getSquares();
		assertEquals(BoardDo.MAX_ROWS, squares.length);
		for(SquareDo squareRow[] : squares) {
			assertEquals(BoardDo.MAX_COLS, squareRow.length);
		}
	}
	
	@Test
	public void testDefensiveCopyingSetSquares() throws Exception {
		// create squares which we will set
		SquareDo squares[][] = new SquareDo[BoardDo.MAX_ROWS][BoardDo.MAX_COLS];
		for(int row=0; row<BoardDo.MAX_ROWS; row++) {
			for(int col=0; col<BoardDo.MAX_COLS; col++) {
				squares[row][col] = new SquareDo();
			}
		}
		// set them
		this.board.setSquares(squares);
		
		//change an original square and verify that the change is not reflected in the corresponding square in the board
		squares[0][0].setMine(true);
		
		SquareDo squareInBoard = getSquareUsingReflection(this.board, new Point(0, 0));
		assertFalse(squareInBoard.isMine());
	}
	
	@Test
	public void testDefensiveCopyingGetSquares() throws Exception {
		SquareDo squaresFromGetSquares[][] = this.board.getSquares();
		// we know from HardcodedMineInitializationStrategy that Point 0,0 is a mine
		
		squaresFromGetSquares[0][0].setMine(false); // changing to not a mine
		
		// verify that the change is not reflected in the Board
		SquareDo squareInBoard = getSquareUsingReflection(this.board, new Point(0, 0));
		assertTrue(squareInBoard.isMine());
	}
	
	@Test
	public void testDefensiveCopyFromGetSquaresIsAccurate() throws Exception {
		SquareDo squaresFromGetSquares[][] = this.board.getSquares();
		
		// verify the squares we obtained are accurate		
		List<Point> mines = MockMineUtils.mines(new Point(BoardDo.MAX_ROWS,BoardDo.MAX_COLS));
		for(int row=0; row<squaresFromGetSquares.length; row++) {
			SquareDo squareRow[] = squaresFromGetSquares[row];
			for(int col=0; col<squareRow.length; col++) {
				SquareDo square = squareRow[col];
				Point squarePoint = new Point(row, col);
				assertEquals(MockMineUtils.getSquareCount(squarePoint), square.getCount());
				if(mines.contains(squarePoint)) {
					assertTrue(square.isMine());
				} else {
					assertFalse(square.isMine());
				}
			}
		}
		
		
	}
	
	
	
	
	private SquareDo getSquareUsingReflection(BoardDo board, Point p) 
			throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field boardFieldSquare = BoardDo.class.getDeclaredField("squares");
		boardFieldSquare.setAccessible(true);
		SquareDo squares[][] = (SquareDo[][])boardFieldSquare.get(board);
		return squares[p.row][p.col];
	}
}

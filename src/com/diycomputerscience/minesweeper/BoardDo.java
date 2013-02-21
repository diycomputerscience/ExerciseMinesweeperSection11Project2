package com.diycomputerscience.minesweeper;

import java.util.List;

import org.apache.log4j.Logger;

import com.diycomputerscience.minesweeper.utils.MinesweeperUtils;

public class BoardDo {
	
	public static final int MAX_ROWS = 6;
	public static final int MAX_COLS = 6;
	
	private SquareDo squares[][];
	
	private Logger logger = Logger.getLogger(BoardDo.class);
	
	public BoardDo() {
		
	}
	
	public BoardDo(MineInitializationStrategy mineInitializationStrategy) {
		if(mineInitializationStrategy == null) {
			throw new NullPointerException("mineInitializationStrategy cannot be null");
		}
		logger.info("Initializing Board");
		this.squares = new SquareDo[MAX_ROWS][MAX_COLS];
		// intitialize squares
		for(int row=0; row<MAX_ROWS; row++) {
			for(int col=0; col<MAX_COLS; col++) { 				
				squares[row][col] = new SquareDo();
			}
		}
		
		//TODO: Use the mineInitializationStrategy to set mines in required squares
		List<Point> mines = mineInitializationStrategy.mines(new Point(BoardDo.MAX_ROWS, BoardDo.MAX_COLS));
		for(Point mine : mines) {
			this.squares[mine.row][mine.col].setMine(true);
		}
		
		logger.debug("Completed setting mines");
		
		computeCounts();
	}
	
	/**
	 * This method uncovers the specified Square
	 * @param point The Point representing the location to uncover
	 * @throws UncoveredMineException if the specified Square is a mine
	 */
	public void uncover(Point point) throws UncoveredMineException {
		logger.debug("Uncovering " + point);
		this.squares[point.row][point.col].uncover();
	}
	
	/**
	 * This method marks the specified Square
	 * @param point The point of the specified square
	 */
	public void mark(Point point) {
		logger.debug("marking " + point);
		this.squares[point.row][point.col].mark();
	}
	
	public void setSquares(SquareDo squares[][]) {
		SquareDo clonedSquares[][] = new SquareDo[squares.length][];
		for(int row=0; row<squares.length; row++) {
			clonedSquares[row] = new SquareDo[squares[row].length];
			for(int col=0; col<squares[row].length; col++) {
				clonedSquares[row][col] = new SquareDo(squares[row][col]);
			}
		}
		this.squares = clonedSquares;
	}
	
	public SquareDo[][] getSquares() {
		SquareDo clonedSquares[][] = new SquareDo[this.squares.length][];
		for(int row=0; row<this.squares.length; row++) {
			clonedSquares[row] = new SquareDo[this.squares[row].length];
			for(int col=0; col<this.squares[row].length; col++) {
				clonedSquares[row][col] = new SquareDo(this.squares[row][col]);
			}
		}
		return clonedSquares;
	}
	
	public void computeCounts() {
		// determine counts of all squares that are not mines
		for(int row=0; row<MAX_ROWS; row++) {
			for(int col=0; col<MAX_COLS; col++) {
				if(!squares[row][col].isMine()) {
					List<Point> neighbours = MinesweeperUtils.computeNeibhbours(new Point(row, col));
					int count=0;
					for(Point neighbour : neighbours) {
						if(squares[neighbour.row][neighbour.col].isMine()) {
							count++;
						}
					}
					squares[row][col].setCount(count);
				}				
			}
		}
		
		logger.debug("Completed square counts");
	}

}

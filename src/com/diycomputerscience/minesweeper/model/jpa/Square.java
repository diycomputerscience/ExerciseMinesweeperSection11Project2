package com.diycomputerscience.minesweeper.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class Square {

	private long id; // primary key
	private int row;
	private int col;
	private boolean mine;
	private int mcount;
	private SquareState state;	
	
	public Square() {
		
	}
	
	public Square(int row,
			      int col,
			      boolean mine,
			      int mcount,
			      SquareState state) {
		
		this.row = row;
		this.col = col;
		this.mine = mine;
		this.mcount = mcount;
		this.state = state;
	}

	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public boolean isMine() {
		return mine;
	}

	public void setMine(boolean mine) {
		this.mine = mine;
	}

	public int getMcount() {
		return mcount;
	}

	public void setMcount(int mcount) {
		this.mcount = mcount;
	}

	public SquareState getState() {
		return state;
	}

	public void setState(SquareState state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return "(" + this.row + ", " + this.col + ") " + 
	           this.isMine() + " " + 
			   this.mcount + " " + 
	           this.state;
	}
}

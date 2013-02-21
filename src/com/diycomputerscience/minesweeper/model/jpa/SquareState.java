package com.diycomputerscience.minesweeper.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SquareState {
	
	private long id;
	private String state;
	
	public SquareState() {
		
	}
	
	public SquareState(String state) {
		this.state = state;
	}
	
	public SquareState(com.diycomputerscience.minesweeper.SquareDo.SquareState state) {
		this.state = state.toString();
	}

	@Id		
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@Column(nullable=false)
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return this.state;
	}
}

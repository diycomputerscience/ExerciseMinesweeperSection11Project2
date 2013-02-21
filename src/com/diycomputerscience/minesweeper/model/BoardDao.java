package com.diycomputerscience.minesweeper.model;

import java.sql.Connection;

import com.diycomputerscience.minesweeper.BoardDo;

public interface BoardDao {

	public BoardDo load(Connection conn) throws PersistenceException;
	public void save(Connection conn, BoardDo board) throws PersistenceException;
	public void delete(Connection conn) throws PersistenceException;
	
}

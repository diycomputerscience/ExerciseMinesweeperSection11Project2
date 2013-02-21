package com.diycomputerscience.minesweeper.model.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.diycomputerscience.minesweeper.BoardDo;
import com.diycomputerscience.minesweeper.SquareDo;
import com.diycomputerscience.minesweeper.model.BoardDao;
import com.diycomputerscience.minesweeper.model.PersistenceException;

public class BoardDaoJDBC implements BoardDao {

	private static Logger logger = Logger.getLogger(BoardDaoJDBC.class);
	
	@Override
	public BoardDo load(Connection conn) throws PersistenceException {
		BoardDo board = null;
		SquareDo squares[][] = new SquareDo[BoardDo.MAX_ROWS][BoardDo.MAX_COLS];
		
		try {
				// create inner join
				String query = "SELECT b.row, b.col, b.is_mine, b.status_id, ss.status FROM BOARD as b INNER JOIN SQUARE_STATUS as ss ON b.status_id = ss.id;";
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					int row = rs.getInt("row");
					int col = rs.getInt("col");
					boolean isMine = rs.getBoolean("is_mine");
					String status = rs.getString("status");
					SquareDo square = new SquareDo();
					square.setMine(isMine);
					square.setState(SquareDo.SquareState.valueOf(status));
					squares[row][col] = square;
				}
				
				board = new BoardDo();
				board.setSquares(squares);
				board.computeCounts();
				return board;
			} catch(SQLException sqle) {
				throw new PersistenceException("Could not load Board from database", sqle);
			}		
	}

	@Override
	public void save(Connection conn, BoardDo board) throws PersistenceException {
		try {
			String sql = "INSERT INTO BOARD (row, col, is_mine, status_id) VALUES(?, ?, ?, (select id from SQUARE_STATUS where status=?));";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			SquareDo squares[][] = board.getSquares();
			for(int row=0; row<squares.length; row++) {
				for(int col=0; col<squares[row].length; col++) {
					SquareDo square = squares[row][col];					
					pStmt.setInt(1, row);
					pStmt.setInt(2, col);
					pStmt.setBoolean(3, square.isMine());					
					pStmt.setString(4, square.getState().toString());
					pStmt.executeUpdate();
				}
			}
		} catch(SQLException sqle) {
			throw new PersistenceException("Could not save Board", sqle);
		}
	}

	@Override
	public void delete(Connection conn) throws PersistenceException {
		String deleteSql = "DELETE FROM BOARD;";
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(deleteSql);
		} catch(SQLException sqle) {
			throw new PersistenceException("Could not delete all rows from Board", sqle);
		}		
	}

}

package com.diycomputerscience.minesweeper.model.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.diycomputerscience.minesweeper.SquareDo;

public class DBInit {

	public static boolean schemaExists(Connection conn) throws SQLException {
		DatabaseMetaData dbMeta = conn.getMetaData();
		
		ResultSet rs = dbMeta.getTables(null, null, "BOARD", null);
		if(!rs.next()) {
			return false;
		}
		
		rs = dbMeta.getTables(null, null, "SQUARE_STATUS", null);
		if(!rs.next()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates the following 2 tables
	 * 
	 * TableName: SQUARE_STATUS
	 * ****************************************
	 * * id     * INT PRIMARY KEY             *
	 * ****************************************
	 * * status * VARCHAR(128) UNIQUE NOT NULL*
	 * ****************************************
	 * 
	 * TableName: BOARD
	 * ***************************************************
	 * * row       * INT NOT NULL                        *
	 * ***************************************************
	 * * col       * INT NOT NULL                        *
	 * ***************************************************
	 * * is_mine   * BOOLEAN NOT NULL                    *
	 * ***************************************************
	 * * status_id * INT NOT NULL                        *
	 * ***************************************************
	 * * PRIMARY KEY (row, col)                          *
	 * * FOREIGN KEY status_id to the SQUARE_STATUS table*
	 * ***************************************************
	 * 
	 */	
	public static boolean buildSchema(Connection conn) throws SQLException {
		String createSquareStatusMaster = "CREATE TABLE SQUARE_STATUS (" +
				                          	"id INTEGER PRIMARY KEY," +
				                          	"status VARCHAR(128) UNIQUE NOT NULL" +
				                          ");";
		// 					 				"id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
		String createBoardSql = "CREATE TABLE BOARD (" +
					 				"row INTEGER NOT NULL," +
					 				"col INTEGER NOT NULL," +
					 				"is_mine BOOLEAN NOT NULL," +
					 				"status_id INTEGER NOT NULL," +
					 					"PRIMARY KEY(row, col)," +
					 					"CONSTRAINT status_id_fk FOREIGN KEY (status_id) REFERENCES SQUARE_STATUS(id)" + 
					 			");";
		
		boolean success = false;
		Statement stmt = conn.createStatement();
		success = stmt.execute(createSquareStatusMaster);
		success = stmt.execute(createBoardSql);		
		
		return success;
	}
	
	/**
	 * Adds data to the SQUARE_STATUS table
	 * ************************************
	 * * id    * status                   *
	 * ************************************
	 * * 1     * COVERED                  *
	 * ************************************
	 * * 1     * UNCOVERED                *
	 * ************************************
	 * * 1     * MARKED                   *
	 * ************************************
	 * @param conn
	 * @throws SQLException
	 */	
	public static void populateSquareStatus(Connection conn) throws SQLException {
		int pkCount = 1;
		PreparedStatement pStmt = conn.prepareStatement("INSERT INTO SQUARE_STATUS(id, status) VALUES(?, ?);");
		
		for(SquareDo.SquareState squareState : SquareDo.SquareState.values()) {
			pStmt.setInt(1, pkCount++);
			pStmt.setString(2, squareState.toString());
			pStmt.executeUpdate();
		}
	}
}

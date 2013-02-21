package com.diycomputerscience.minesweeper.model.jpa;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.diycomputerscience.minesweeper.BoardDo;
import com.diycomputerscience.minesweeper.ConfigurationException;
import com.diycomputerscience.minesweeper.MockMineUtils;
import com.diycomputerscience.minesweeper.Point;
import com.diycomputerscience.minesweeper.SquareDo;

public class JPAPersistenceStrategyTest {

	private JPAPersistenceStrategy persistenceStrategy;
	
	private static EntityManagerFactory emf;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		emf = Persistence.createEntityManagerFactory("MS-TEST");
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		emf.close();
	}
	
	@Before
	public void setUp() throws Exception {		
		Properties props = new Properties();
		props.setProperty(JPAPersistenceStrategy.PERSISTENCE_UNIT_NAME, "MS-TEST");
		this.persistenceStrategy = new JPAPersistenceStrategy();
		this.persistenceStrategy.configure(props);
	}

	@After
	public void tearDown() throws Exception {
		if(this.persistenceStrategy != null) {
			this.persistenceStrategy.close();
		}		
	}

	@Test(expected=NullPointerException.class)
	public void testConfigureNullProperties() throws Exception {
		JPAPersistenceStrategy jpaps = new JPAPersistenceStrategy();
		jpaps.configure(null);
	}
	
	@Test(expected=ConfigurationException.class)
	public void testConfigureWithoutPUName() throws Exception {
		JPAPersistenceStrategy jpaps = new JPAPersistenceStrategy();
		jpaps.configure(new Properties());
	}
	
	@Test(expected=NullPointerException.class)
	public void testsaveWithNullBoard() throws Exception {
		this.persistenceStrategy.save(null);
	}
	
	@Test
	public void testSave() throws Exception {
		BoardDo board = MockMineUtils.buildBoardFromLayout(MockMineUtils.expectedLinesInSavedBoard);
		
		this.persistenceStrategy.save(board);
		
		// verify
		List<Square> retreivedSquares = null;		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Query squaresQuery = em.createQuery("from Square", Square.class);
		retreivedSquares = squaresQuery.getResultList();
		em.getTransaction().commit();
		
		assertEquals(36, retreivedSquares.size());
		for(Square square : retreivedSquares) {
			Point point = new Point(square.getRow(), square.getCol());
			SquareDo expectedSquare = MockMineUtils.getSquareFromExpectedData(point);
			assertEquals("Failed isMine test for Point " + point, expectedSquare.isMine(), square.isMine());
			//assertEquals("Failed count test for Point " + point, expectedSquare.getCount(), square.getMcount());
			assertEquals("Failed state test for Point " + point, expectedSquare.getState().toString(), square.getState().getState());
		}
	}
	
	@Test
	public void testLoad() throws Exception {
		EntityManager em = emf.createEntityManager();
		
		List<SquareState> squareStates = null;		
		em.getTransaction().begin();
		Query allSquareStatesQuery = em.createQuery("from SquareState", SquareState.class);
		squareStates = allSquareStatesQuery.getResultList();
		em.getTransaction().commit();
		
		// save a Board into the database
		BoardDo originalBoard = MockMineUtils.buildBoardFromLayout(MockMineUtils.expectedLinesInSavedBoard);		
		em.getTransaction().begin();
		
		SquareDo squares[][] = originalBoard.getSquares();
		for(int row=0; row<squares.length; row++) {
			for(int col=0; col<squares[row].length; col++) {
				SquareState squareState = null;
				for(SquareState state : squareStates) {
					if(state.getState().equals(squares[row][col].getState().toString())) {
						squareState = state;
						break;
					}
				}
				Square square = new Square(row, col, squares[row][col].isMine(), squares[row][col].getCount(), squareState);
				em.persist(square);
			}
		}
		
		em.getTransaction().commit();
		
		BoardDo retrievedBoard = this.persistenceStrategy.load();
		Assert.assertNotNull(retrievedBoard);
		
		// verify all the squares
		SquareDo retrievedSquares[][] = retrievedBoard.getSquares();
		for(int row=0; row<retrievedSquares.length; row++) {
			for(int col=0; col<retrievedSquares[row].length; col++) {
				assertEquals("Failed for " + row + " " + col, 
						     originalBoard.getSquares()[row][col], 
						     retrievedSquares[row][col]);
			}
		}
	}

}

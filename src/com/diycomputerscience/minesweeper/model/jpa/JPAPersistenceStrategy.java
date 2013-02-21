package com.diycomputerscience.minesweeper.model.jpa;

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.diycomputerscience.minesweeper.BoardDo;
import com.diycomputerscience.minesweeper.ConfigurationException;
import com.diycomputerscience.minesweeper.SquareDo;
import com.diycomputerscience.minesweeper.model.BoardDao;
import com.diycomputerscience.minesweeper.model.PersistenceException;
import com.diycomputerscience.minesweeper.model.PersistenceStrategy;

public class JPAPersistenceStrategy implements PersistenceStrategy {
		
	private EntityManagerFactory emFactory;
	
	public static final String PERSISTENCE_UNIT_NAME="persistence.unit.name";
	
	public JPAPersistenceStrategy() {
		
	}
	
	@Override
	public void configure(Properties properties) throws ConfigurationException {
		if(properties == null) {
			throw new NullPointerException("properties cannot be null");
		}
		
		String persistenceUnitName = properties.getProperty(PERSISTENCE_UNIT_NAME);
		if(persistenceUnitName == null) {
			throw new ConfigurationException("Property not found '" + PERSISTENCE_UNIT_NAME + "'");
		}
		
		this.emFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		
		populateMasters();
	}
	
	/**
	 * Save the squares in BoardDo to the database using the {@link Square} 
	 * Entity object. Entity design can be inferred from the properties  in
	 * Square.
	 * 
	 * @param board the Board object whose squares have to be saved
	 * 
	 * @throws PersistenceException If the data could not be saved
	 */
	@Override
	public void save(BoardDo board) throws PersistenceException {
		
	}

	/**
	 * Read the data for Square objects that are stored in the database and
	 * create a Board object from the saved data.
	 * 
	 * @return The Board object created from the saved data
	 * 
	 * @throws PersistenceException If the data could not be read
	 */
	@Override
	public BoardDo load() throws PersistenceException {
		return null;
	}
	
	public final void close() {
		if(this.emFactory != null) {
			this.emFactory.close();
		}		
	}
	
	private void populateMasters() {
		boolean populate = false;
		EntityManager em = this.emFactory.createEntityManager();
		em.getTransaction().begin();
		Query fetchSquareStatesQuery = em.createQuery("from SquareState");
		if(fetchSquareStatesQuery.getResultList().size() == 0) {
			populate = true;
		}
		em.getTransaction().commit();
		
		if(populate) {
			em.getTransaction().begin();
			for(SquareDo.SquareState state : SquareDo.SquareState.values()) {
				SquareState ss = new SquareState(state);
				em.persist(ss);
			}
			em.getTransaction().commit();
		}		
	}
}

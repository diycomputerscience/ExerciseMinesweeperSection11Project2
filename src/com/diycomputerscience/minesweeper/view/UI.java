package com.diycomputerscience.minesweeper.view;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

import com.diycomputerscience.minesweeper.BoardDo;
import com.diycomputerscience.minesweeper.ConfigurationException;
import com.diycomputerscience.minesweeper.RandomMineInitializationStrategy;
import com.diycomputerscience.minesweeper.SquareDo;
import com.diycomputerscience.minesweeper.UncoveredMineException;
import com.diycomputerscience.minesweeper.model.PersistenceException;
import com.diycomputerscience.minesweeper.model.PersistenceStrategy;

public class UI extends JFrame {
	
	private BoardDo board;
	private OptionPane optionPane;
	private PersistenceStrategy peristenceStrategy;
	private JPanel panel;
	
	private ResourceBundle resourceBundle;
	public static final Properties configProperties = new Properties();	
	
	private static final Logger logger = Logger.getLogger(UI.class);
	private static final String DEFAULT_PERSISTENCE_STRATEGY = "com.diycomputerscience.minesweeper.DevNullPersistenceStrategy";
		
	public UI(BoardDo board, OptionPane optionPane, PersistenceStrategy persistenceStrategy) {
		try {
			this.resourceBundle = ResourceBundle.getBundle("MessageBundle");
		} catch(MissingResourceException mre) {
			logger.warn("Could not locate MessageBundle file", mre);
		}
		
		// set this.board to the injected Board
		this.board = board;
		this.optionPane = optionPane;
		this.peristenceStrategy = persistenceStrategy;
		
		// Set the title to "Minesweeper"
		this.setTitle(this.resourceBundle.getString("title"));
				
		this.panel = new JPanel();
		
		// Set the name of the panel to "MainPanel" 
		panel.setName("MainPanel");
		
		// Set the layout of panel to GridLayout. Be sure to give it correct dimensions
		panel.setLayout(new GridLayout(BoardDo.MAX_ROWS, BoardDo.MAX_COLS));
		
		// add squares to the panel
		this.layoutSquares(panel);
		// add panel to the content pane
		this.getContentPane().add(this.panel);
		
		// set the menu bar
		this.setJMenuBar(buildMenuBar());
		
		// validate components
		//this.validate();
	}
	
	public void load(BoardDo board) {
		logger.info("Loading board into the UI");
		this.board = board;
		
		this.getContentPane().removeAll();
		this.invalidate();
		
		this.panel = new JPanel();		
		// Set the name of the panel to "MainPanel" 
		panel.setName("MainPanel");		
		// Set the layout of panel to GridLayout. Be sure to give it correct dimensions
		panel.setLayout(new GridLayout(BoardDo.MAX_ROWS, BoardDo.MAX_COLS));
		
		// add squares to the panel
		this.layoutSquares(panel);
		// add panel to the content pane
		
		this.getContentPane().add(this.panel);
		this.validate();		
	}
	
	private void layoutSquares(JPanel panel) {		
		final SquareDo squares[][] = this.board.getSquares();
					
		for(int row=0; row<BoardDo.MAX_ROWS; row++) {
			for(int col=0; col<BoardDo.MAX_COLS; col++) {
				final JButton squareUI = new JButton();
				squareUI.setName(row+","+col);
				final int theRow = row;
				final int theCol = col;
				squareUI.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent me) {						
						// invoke the appropriate logic to affect this action (left or right mouse click)
						if(SwingUtilities.isLeftMouseButton(me)) {
							try {
								UI.this.board.uncover(new com.diycomputerscience.minesweeper.Point(theRow, theCol));												
							} catch(UncoveredMineException ume) {
								logger.info("Game lost");
								squareUI.setBackground(Color.RED);
								String gameOverTitle = resourceBundle.getString("gameover.dialogue.msg.title");
								String gameOverMsg = resourceBundle.getString("gameover.dialogue.msg");
								int answer = optionPane.userConfirmation(UI.this, gameOverMsg, gameOverTitle, JOptionPane.YES_NO_OPTION);
								if(answer == JOptionPane.YES_OPTION) {
									logger.debug("Creating new board");
									BoardDo board = new  BoardDo(new RandomMineInitializationStrategy());
									load(board);
								} else {
									logger.info("Exiting game...");
									UI.this.dispose();
								}
							}
						} else if(SwingUtilities.isRightMouseButton(me)) {
							UI.this.board.mark(new com.diycomputerscience.minesweeper.Point(theRow, theCol));
						}
						// display the new state of the square
						updateSquareUIDisplay(squareUI, UI.this.board.getSquares()[theRow][theCol]);						
					}
				});
				updateSquareUIDisplay(squareUI, UI.this.board.getSquares()[theRow][theCol]);
				panel.add(squareUI);
			}
		}
	}
	
	private void updateSquareUIDisplay(JButton squareUI, SquareDo square) {
		if(square.getState().equals(SquareDo.SquareState.UNCOVERED)) {
			if(square.isMine()) {
				squareUI.setBackground(ColorUIResource.RED);
			} else {
				squareUI.setText(String.valueOf(square.getCount()));
			}							
		} else if(square.getState().equals(SquareDo.SquareState.MARKED)) {
			squareUI.setText("");
			squareUI.setBackground(ColorUIResource.MAGENTA);
		} else if(square.getState().equals(SquareDo.SquareState.COVERED)) {
			squareUI.setText("");
			squareUI.setBackground(new ColorUIResource(238, 238, 238));
		}
	}
	
	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setName("menubar");
		
		JMenu file = new JMenu(resourceBundle.getString("menu.file"));
		file.setName("file");
		JMenuItem fileSave = new JMenuItem(resourceBundle.getString("menuitem.save"));
		fileSave.setName("file-save");
		fileSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					UI.this.peristenceStrategy.save(UI.this.board);
				} catch(PersistenceException pe) {
					logger.error("Could not save the Board", pe);					
				}
			}		
		});
		JMenuItem fileLoad = new JMenuItem(resourceBundle.getString("menuitem.load"));
		fileLoad.setName("file-load");
		fileLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					UI.this.load(UI.this.board = UI.this.peristenceStrategy.load());
				} catch(PersistenceException pe) {
					//TODO: error dialogue
					//TODO: This button should be enabled only if a previously saved state exists
					logger.error("Could not load the Board", pe);
				}
			}			
		});
		JMenuItem close = new JMenuItem(resourceBundle.getString("menuitem.close"));
		close.setName("file-close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				System.exit(0);
			}		
		});
		file.add(fileSave);
		file.add(fileLoad);
		file.add(close);
		menuBar.add(file);
		
		JMenu help = new JMenu(resourceBundle.getString("menu.help"));
		help.setName("help");
		JMenuItem helpAbout = new JMenuItem(resourceBundle.getString("menuitem.about"));
		helpAbout.setName("help-about");
		help.add(helpAbout);
		menuBar.add(help);
		
		return menuBar;
	}

	
	public static UI build(BoardDo board, OptionPane optionPane, PersistenceStrategy persistenceStrategy) {
		UI ui = new UI(board, optionPane, persistenceStrategy);
		ui.setSize(300, 400);
		ui.setVisible(true);
		ui.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		return ui;
	}

	public static void main(String[] args) {
		logger.info("Starting the game");
		InputStream configIS = null;
		try {
			configIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");			
			configProperties.load(configIS);
			final PersistenceStrategy persistenceStrategy = buildPersistenceStrategy();
			
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					build(new BoardDo(new RandomMineInitializationStrategy()), 
									new SwingOptionPane(), 
									persistenceStrategy);
				}
				
			});
			
		} catch(IOException ioe) {
			logger.error("Quitting: Could not load configuration properties... ", ioe);
		}
	}
	
	/**
	 * Read the property called 'persistence.strategy' from the attribute configProperties
	 * This property specified the fully qualified name of the class which is to be used
	 * for PersistenceStrategy. This method instantiates this class, and configures it. 
	 * 
	 * @return An instance of a subclass of PersistenceStrategy, as specified by the 
	 * value of the 'persistence.strategy' property, or an instance of DevNullPersistenceStrategy
	 * if the properties file does not contain a value for the key mentioned above, or for whatever
	 * reason the PersistenceStrategy class could not be instantiated
	 */
	private static PersistenceStrategy buildPersistenceStrategy() {
		PersistenceStrategy persistenceStrategy = null;
		
		String persistenceStrategyClazz = configProperties.getProperty("persistence.strategy");
		if(persistenceStrategyClazz == null) {
			logger.warn("Could not find property for 'persistence.strategy'...");
			persistenceStrategyClazz = DEFAULT_PERSISTENCE_STRATEGY;
		}			
		logger.info("Using persistence strategy '" + persistenceStrategyClazz + "'");										
		
		try {
			persistenceStrategy = instantiatePersistenceStrategyOrNull(persistenceStrategyClazz);							
			if(persistenceStrategy == null) {
				logger.info("Instantiating default persistence strategy");
				// We are assuming that the DEFAULT_PERSISTENCE_STRATEGY will always be instantiated without any problems !
				persistenceStrategy = instantiatePersistenceStrategyOrNull(DEFAULT_PERSISTENCE_STRATEGY);			
			}			
			persistenceStrategy.configure(configProperties);
		} catch(ConfigurationException e) {
			
		}
		return persistenceStrategy;
	}
	
	private static PersistenceStrategy instantiatePersistenceStrategyOrNull(String clazz) {
		PersistenceStrategy persistenceStrategy = null;
		
		try {
			persistenceStrategy = (PersistenceStrategy)Class.forName(clazz).newInstance();
		} catch (Exception e) {
			logger.warn("Could not instantiate PersistenceStrategy '" + clazz + "'", e);
		}
		
		return persistenceStrategy;
	}
}

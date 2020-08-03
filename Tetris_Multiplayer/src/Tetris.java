import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * The Tetris class is responsible for handling much of the game logic and reading user input.
 * @author Pedro Alves
 *
 */
public class Tetris extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final long FRAME_TIME = 20L;
	private static final int TYPE_COUNT = TileType.values().length;
	private static BoardPanel board;
	private static SidePanel side;
	private static boolean isPaused;
	private static boolean isNewGame;
	private static boolean isGameOver;
	private static int level;
	private static int score;
	private static Random random;
	private static Clock logicTimer;
	private static TileType currentType;
	private static TileType nextType;
	private static TileType nextTypetemp; // = null;
	private static int currentCol;
	private static int currentRow;
	private static int currentRotation;
	private static int dropCooldown;
	private static float gameSpeed;
	private static String difficulty = "Easy"; 
	private static int newLevel;
	private static int lines;
	private static int cleared;
	private static boolean login = false;
	public static boolean flagConnect = false;
	public static Socket cliente;
	private static String playerName;
	static DataPlayer dataPlayer = new DataPlayer();
	private static int count = 0;
	private static final TileType[] TILE_TYPES = new TileType[]{
			TileType.TypeI, TileType.TypeJ, TileType.TypeL, TileType.TypeO, TileType.TypeS, TileType.TypeT, TileType.TypeZ
	};

	// Creates a new Tetris instance. Sets up the window's properties, the menu bar and adds a controller listener.
	public Tetris (){

		setTitle("Tetris by Pedro Alves");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);		

		// Initialize the MenuBar instance
		JMenuBar menuBar = new JMenuBar();
		JMenu menu1 = new JMenu("Game");
		menuBar.add(menu1);
		JMenuItem Login = new JMenuItem("Login", KeyEvent.VK_N);
		JMenuItem Connect = new JMenuItem("Connect", KeyEvent.VK_N);
		JMenuItem disconnect = new JMenuItem("Disconnect", KeyEvent.VK_N);		
		JMenuItem newGame = new JMenuItem("New game", KeyEvent.VK_N);
		JMenuItem pause = new JMenuItem("Pause",KeyEvent.VK_P);
		JMenu hardship = new JMenu("Difficulty level");
		JMenuItem easy = new JMenuItem("Easy");
		JMenuItem intermediate = new JMenuItem("Intermediate");
		JMenuItem hard = new JMenuItem("Hard");
		JMenuItem ranking = new JMenuItem("Ranking",KeyEvent.VK_R);
		JMenuItem exit = new JMenuItem("Exit now",KeyEvent.VK_ESCAPE);
		JMenu menu2 = new JMenu("Help");
		menuBar.add(menu2);
		JMenuItem music = new JMenuItem("Music On/Off");
		JMenuItem instructions = new JMenuItem("Instructions",KeyEvent.VK_I);
		JMenuItem about = new JMenuItem("About",KeyEvent.VK_A);

		Login.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(login == true) {
					JOptionPane.showMessageDialog(null, "You are already logged in.", "Tetris", JOptionPane.INFORMATION_MESSAGE);
				}else {
					if(flagConnect == false) {
						playerName = JOptionPane.showInputDialog(null, "Insert a player name:", "Tetris", JOptionPane.INFORMATION_MESSAGE);
					
						while(playerName.isBlank()) {
							playerName = JOptionPane.showInputDialog(null, "Please insert a valid name:", "Tetris", JOptionPane.INFORMATION_MESSAGE);
						}
						login = true;
					} 
				}
			}
		});
		menu1.add(Login);

		Connect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(login == true) {
					if(flagConnect == false) {
						DataPlayer Player= new DataPlayer(playerName,difficulty);
						try {
							cliente=new Socket("localhost",6666);
							ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
							saida.writeObject("Connect");
							saida.writeObject(Player.getNome());
							saida.writeObject(Player.getDificuldade());
	
							flagConnect = true;
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null,"Error Connecting");
							flagConnect = false;
						}
					}else {
						JOptionPane.showMessageDialog(null,"Already connected");
					}

				} else {
					JOptionPane.showMessageDialog(null,"To play online please login first.", "Tetris", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		menu1.add(Connect);
		
		disconnect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{	
				if(flagConnect == false) {
					JOptionPane.showMessageDialog(null, "You are already disconnected.", "Tetris", JOptionPane.INFORMATION_MESSAGE);
				} else {
					int ans = JOptionPane.showConfirmDialog(null, "Are you sure you want to disconnect?\nThis will reset the current game", "Tetris", JOptionPane.INFORMATION_MESSAGE);
					if(ans==1 || ans==2) { // if No/Cancel
						return;
					}else if(ans==0) { // if Yes
						DataPlayer Player = new DataPlayer(playerName,difficulty);
						try {
							cliente=new Socket("localhost",6666);
							ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
							saida.writeObject("Exit");
							saida.writeObject(playerName);
							saida.writeObject(difficulty);
							if(cliente!=null) {
								try {
									cliente.close();
								}catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							flagConnect = false;
							login = false;
							isNewGame = true;
							isPaused = false;
							renderGame();
							setTitle("Tetris by Pedro Alves");
							JOptionPane.showMessageDialog(null, "You are now disconnected from the server");	
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		menu1.add(disconnect);
		
		newGame.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(flagConnect == true && login == true) {
					isNewGame = true;
					isPaused = false;
					repaint();
				}
				if(isPaused) {
					isPaused = !isPaused;;
				}
				resetGame();
			}
		});
		menu1.add(newGame);		

		//Another way to pause the game
		pause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				if(!isGameOver && !isNewGame) {
					isPaused = !isPaused;
					logicTimer.setPaused(isPaused);
				}
			}
		});
		menu1.add(pause);	

		easy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				if(flagConnect == true) {
					if(isNewGame == true) {
						difficulty = "Easy";
						repaint();
					} else {
						JOptionPane.showMessageDialog(null, "To play with a different difficulty please disconnect and login again.", "Tetris", JOptionPane.INFORMATION_MESSAGE);
					}
				}else if(flagConnect == false) {
					if(isNewGame == true) {
						difficulty = "Easy";
						repaint();
					} else {
						Object[] options = { "Yes", "No" };
						int n = JOptionPane.showOptionDialog(new JFrame(),
								"This will reset your current game.\nDo you wish to continue?\n","Tetris",
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
								options, options[1]);
						if(n == JOptionPane.OK_OPTION){ // Afirmative
							difficulty = "Easy";
							if(isNewGame || isGameOver) {
								resetGame();
							}
							if(isPaused) {
								isPaused = !isPaused;
								logicTimer.setPaused(isPaused);
								resetGame();
							}else if(!isPaused) {
								resetGame();
							}					
						}
						if(n == JOptionPane.NO_OPTION){ // negative
						}
						if(n == JOptionPane.CLOSED_OPTION){ // closed the dialog
						}
					}
				}
			}
		});		
		hardship.add(easy);

		intermediate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				if(flagConnect == true) {
					if(isNewGame == true) {
						difficulty = "Intermediate";
						isNewGame = true;
						isPaused = false;
						renderGame();
					} else {
						JOptionPane.showMessageDialog(null, "To play with a different difficulty please disconnect and login again.", "Tetris", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				if(flagConnect == false) {
					if(isNewGame == true) {
						difficulty = "Intermediate";
						isNewGame = true;
						isPaused = false;
						renderGame();
					} else {	
						Object[] options = { "Yes", "No" };
					    int n = JOptionPane.showOptionDialog(new JFrame(),
					            "This will reset your current game.\nDo you wish to continue?\n","Tetris",
					            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					            options, options[1]);
					    if(n == JOptionPane.OK_OPTION){ // Afirmative
					    		difficulty = "Intermediate";		    		
					    		if(isNewGame || isGameOver) {
					    			resetGame();
					    		}		 
					    		if(isPaused) {
					    			isPaused = !isPaused;
									logicTimer.setPaused(isPaused);
									resetGame();						
								} else if(!isPaused) {
									resetGame();
								}
						 }
						if(n == JOptionPane.NO_OPTION){ // negative
						}
						if(n == JOptionPane.CLOSED_OPTION){ // closed the dialog
						}
					}						
					}
			}
		});
		hardship.add(intermediate);		

		hard.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				if(flagConnect == true) {
					if(isNewGame == true) {
						difficulty = "Hard";					
						isNewGame = true;
						isPaused = false;
						renderGame();			
					} else {
						JOptionPane.showMessageDialog(null, "To play with a different difficulty please disconnect and login again.", "Tetris", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				if(flagConnect == false) {
					if(isNewGame == true) {
						difficulty = "Hard";					
						isNewGame = true;
						isPaused = false;
						renderGame();
					} else {			
						Object[] options = { "Yes", "No" };
					    int n = JOptionPane.showOptionDialog(new JFrame(),
					            "This will reset your current game.\nDo you wish to continue?\n","Tetris",
					            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					            options, options[1]);
					    if(n == JOptionPane.OK_OPTION){ // Afirmative
					    		difficulty = "Hard";
					    		if(isNewGame || isGameOver) {
					    			resetGame();
					    		}
					    		if(isPaused) {
					    			isPaused = !isPaused;
									logicTimer.setPaused(isPaused);
								resetGame();
					    		} else if(!isPaused) {
									resetGame();
								}
					    }
					    if(n == JOptionPane.NO_OPTION){ // negative
					    }
					    if(n == JOptionPane.CLOSED_OPTION){ // closed the dialog
					    }
					}					
				}	
			}
		});
		hardship.add(hard);
		menu1.add(hardship);
		
		ranking.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(flagConnect == true) {
					try {
						cliente=new Socket("localhost",6666);
						ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
						saida.writeObject("Ranking");
						ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
						StringBuilder SB=(StringBuilder)in.readObject();
						JOptionPane.showMessageDialog(null, SB.toString(), "Tetris", JOptionPane.INFORMATION_MESSAGE);
						cliente.close();
					} catch (IOException | ClassNotFoundException  e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage());
					}
				} else {
					HighScore[] h=HighScore.getHighScores();
					JFrame ranking = new JFrame("Ranking");
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}catch (Exception ex){
						ex.printStackTrace();
					}
					if(isNewGame) {
						DefaultTableModel model = new DefaultTableModel();
						model.addColumn("Position");
						model.addColumn("Name");
						model.addColumn("Score");
						model.addColumn("Lines");
						model.addColumn("Level");
						model.addColumn("Difficulty");
						JTable table = new JTable(model);
						for(int i=0; i<h.length; i++) {
							if(h[i].getScore()>0) {
								model.addRow(new Object[]{i+1 ,h[i].getName(), h[i].getScore(), h[i].getLines(), h[i].getLevel(), h[i].getDiff()});
							}
						}
						table.setBackground(Color.black);
						table.setForeground(Color.green);
						table.setGridColor(Color.green);
						table.setRowHeight(40);
						table.setBounds(30,40,200,100);    
						table.setFont(new Font("Tahoma",Font.PLAIN, 11));
						JScrollPane sp = new JScrollPane(table); 
						sp.setSize(200, 200);
						ranking.add(sp);  
						ranking.setSize(200,200);
						ranking.setResizable(false);
						ranking.pack();
						ranking.setLocationRelativeTo(null);
						ranking.setVisible(true);
					}else if(!isNewGame) {
						if(isPaused) {
							DefaultTableModel model = new DefaultTableModel();
							model.addColumn("Position");
							model.addColumn("Name");
							model.addColumn("Score");
							model.addColumn("Lines");
							model.addColumn("Level");
							model.addColumn("Difficulty");
							JTable table = new JTable(model);
							for(int i=0; i<h.length; i++) {
								if(h[i].getScore()>0) {
									model.addRow(new Object[]{i+1 ,h[i].getName(), h[i].getScore(), h[i].getLines(), h[i].getLevel(), h[i].getDiff()});
								}
							}
							table.setBackground(Color.black);
							table.setForeground(Color.green);
							table.setGridColor(Color.green);
							table.setRowHeight(40);
							table.setBounds(30,40,200,100);    
							table.setFont(new Font("Tahoma",Font.PLAIN, 11));
							JScrollPane sp = new JScrollPane(table); 
							sp.setSize(200, 200);
							ranking.add(sp);  
							ranking.setSize(200,200);
							ranking.setResizable(false);
							ranking.pack();
							ranking.setLocationRelativeTo(null);
							ranking.setVisible(true);
						}else if(!isPaused) {
							isPaused = !isPaused;
							logicTimer.setPaused(isPaused);
							DefaultTableModel model = new DefaultTableModel();
							model.addColumn("Position");
							model.addColumn("Name");
							model.addColumn("Score");
							model.addColumn("Lines");
							model.addColumn("Level");
							model.addColumn("Difficulty");
							JTable table = new JTable(model);
							for(int i=0; i<h.length; i++) {
								if(h[i].getScore()>0) {
									model.addRow(new Object[]{i+1 ,h[i].getName(), h[i].getScore(), h[i].getLines(), h[i].getLevel(), h[i].getDiff()});
								}
							}
							table.setBackground(Color.black);
							table.setForeground(Color.green);
							table.setGridColor(Color.green);
							table.setRowHeight(40);
							table.setBounds(30,40,200,100);    
							table.setFont(new Font("Tahoma",Font.PLAIN, 11));
							JScrollPane sp = new JScrollPane(table); 
							sp.setSize(200, 200);
							ranking.add(sp);  
							ranking.setSize(200,200);
							ranking.setResizable(false);
							ranking.pack();
							ranking.setLocationRelativeTo(null);
							ranking.setVisible(true);
						}
					}
				}
			}
		});
		menu1.add(ranking);		

		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				System.exit(0);
			}
		});
		menu1.add(exit);
		
		music.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filepath = "Music\\audio\\Tetris_song.wav.wav";
				MusicPlayer musicObject = new MusicPlayer(filepath);
			}
		});
		menu2.add(music);

		instructions.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				String instr = "The goal of Tetris is to eliminate \nas many lines as possible before\n the Tetrominoes reach the top.\n\n"
						+ "To play online first login wih a \nusername then press connect \non the menu bar item. Upon each \nplay a login should be chosen.\n\n"
						+ "Controls:\n\u2190        - Move Left\n\u2192        - Move Right\n\u2193         - Drop\n" +
						"C        - Rotate AntiClockwise\nV        - Rotate Clockwise\nP        - Pause\nEsc    - Quit";
				if(isNewGame) {
					JOptionPane.showMessageDialog(null, instr,"Instructions", JOptionPane.OK_OPTION, new ImageIcon());
				}else if(!isNewGame) {
					if(isPaused) {
						JOptionPane.showMessageDialog(null, instr,"Instructions", JOptionPane.OK_OPTION, new ImageIcon());
					}else if(!isPaused) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
						JOptionPane.showMessageDialog(null, instr,"Instructions", JOptionPane.OK_OPTION, new ImageIcon());
					}
				}
			}});
		menu2.add(instructions);

		about.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JLabel label = new JLabel("<html><center>Tetris made by Pedro Alves<br>MEEC - Telecomunicações<br>SADIT, 2020<html>");
				label.setHorizontalAlignment(SwingConstants.CENTER);
				if(isNewGame) {
					JOptionPane.showMessageDialog(null, label, "About", JOptionPane.INFORMATION_MESSAGE);
				}else if(!isNewGame) {
					if(isPaused) {
						JOptionPane.showMessageDialog(null, label, "About", JOptionPane.INFORMATION_MESSAGE);		
					} else if (!isPaused) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
						JOptionPane.showMessageDialog(null, label, "About", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		menu2.add(about);
		this.setJMenuBar(menuBar);

		// Initialize the BoardPanel and SidePanel instances	 
		Tetris.board = new BoardPanel(this);
		Tetris.side = new SidePanel(this);
		
		// Add the BoardPanel and SidePanel instances to the window.
		add(board, BorderLayout.CENTER);
		add(side, BorderLayout.EAST);

		// Adds a custom anonymous KeyListener to the frame.
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				
				// Drop - When pressed, checks to see that the game is not paused and that there is no drop cooldown, then set the logic timer to run at a speed of 25 cycles per second.		 
				case KeyEvent.VK_DOWN:
					if(!isPaused && dropCooldown == 0) {
						logicTimer.setCyclesPerSecond(25.0f);
					}
					break;

					// Move Left - When pressed, checks to see that the game is not paused and that the position to the left of the current position is valid. 
					// If so, decrements the current column by 1.
				case KeyEvent.VK_LEFT:
					if(!isPaused) {
						if(flagConnect) {
							if(board.isValidAndEmpty(currentType, currentCol - 1, currentRow, currentRotation)) {
								DataPlayer Player = new DataPlayer(playerName,difficulty);
								try {
									cliente = new Socket("localhost", 6666);
									ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
									saida.writeObject("Validate");
									saida.writeObject(Player.getNome()); 
									String CurrentType = currentType.toString();
									saida.writeObject(CurrentType);
									saida.writeObject(currentCol - 1);
									saida.writeObject(currentRow);
									saida.writeObject(currentRotation);
									saida.writeObject("V");
									saida.flush();
									ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
									String ans;
									ans = (String) input.readObject();
									if(ans.equals("Valid move")) {
										currentCol--;
									}else {
									}
									saida.close();
									cliente.close();
								} catch(IOException | ClassNotFoundException ey) {
									ey.printStackTrace();
								}
							}
						}else {
							if(board.isValidAndEmpty(currentType, currentCol - 1, currentRow, currentRotation)) {
								currentCol--;
							}
						}
					}				
					break;					

					// Move Right - When pressed, checks to see that the game is not paused and that the position to the right of the current position is valid. 
					// If so, increments the current column by 1.
				case KeyEvent.VK_RIGHT:			
					if(!isPaused) {		
						if(flagConnect) {
							if(board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {
								DataPlayer Player = new DataPlayer(playerName,difficulty);
								try {
									cliente = new Socket("localhost", 6666);
									ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
									saida.writeObject("Validate");
									saida.writeObject(Player.getNome()); 
									String CurrentType = currentType.toString();
									saida.writeObject(CurrentType);
									saida.writeObject(currentCol + 1);
									saida.writeObject(currentRow);
									saida.writeObject(currentRotation);
									saida.writeObject("V");
									saida.flush();
									ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
									String ans;
									ans = (String) input.readObject();
									if(ans.equals("Valid move")) {
										currentCol++;
									}
									saida.close();
									cliente.close();
								} catch(IOException | ClassNotFoundException ed) {
									ed.printStackTrace();
								}
							}
						}else {
							if(board.isValidAndEmpty(currentType, currentCol + 1, currentRow, currentRotation)) {
								currentCol++;
							}
						}
					}
					break;

					// Rotate Anticlockwise - When pressed, checks to see that the game is not paused and then attempt to rotate the piece anticlockwise. 
				case KeyEvent.VK_C:
					if(!isPaused) {
						rotatePiece((currentRotation == 0) ? 3 : currentRotation - 1);
					}
					break;

				// Rotate Clockwise - When pressed, check to see that the game is not paused and then attempt to rotate the piece clockwise. 
				case KeyEvent.VK_V:
					if(!isPaused) {
						rotatePiece((currentRotation == 3) ? 0 : currentRotation + 1);
					}
					break;

				case KeyEvent.VK_P:
					if(!isGameOver && !isNewGame) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
					}
					break;

				case KeyEvent.VK_ESCAPE:
					if(flagConnect == true) {
						try {
							cliente=new Socket("localhost",6666);
							ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
							saida.writeObject("Exit");
							saida.writeObject(playerName);
							saida.writeObject(difficulty);
							saida.flush();
							if(cliente!=null) {
								try {
									cliente.close();
								}catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						System.exit(0);	
					}else {
						if(isNewGame) {
							int ans = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?\n", "Tetris", JOptionPane.INFORMATION_MESSAGE);
							if(ans==1 || ans==2) {
								return;
							}else if(ans==0) {
								System.exit(0);
							}
						}else if(!isNewGame) {
							if(isPaused) {
								int ans = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?\n", "Tetris", JOptionPane.INFORMATION_MESSAGE);
								if(ans==1 || ans==2) {
									return;
								}else if(ans==0) {
									System.exit(0);						
								}
							} else if(!isPaused) {
								isPaused = !isPaused;
								logicTimer.setPaused(isPaused);
								int ans = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?\n", "Tetris", JOptionPane.INFORMATION_MESSAGE);
								if(ans==1 || ans==2) {
									isPaused = !isPaused;
									logicTimer.setPaused(isPaused);
									return;
								}else if(ans==0) {
									System.exit(0);						
								}
								break;
							}
						}
					}
				}
			}
		});

		// Resize the frame to hold the BoardPanel and SidePanel instances, center the window on the screen, and show it to the user.
		pack();
		setLocationRelativeTo(null);
		setVisible(true);		
	}


	// Starts the game running. Initializes everything and enters the game loop.
	static void startGame() {
		// Initialize our random number generator, difficulty, logic timer, and new game variables.
		Tetris.random = new Random();
		Tetris.isNewGame = true;
		// Set the game difficulty
		if(Tetris.difficulty.equals("Easy")) {
			Tetris.gameSpeed=1.0f;
		}else if(Tetris.difficulty.equals("Intermediate")) {
			Tetris.gameSpeed=1.25f;
		}else if(Tetris.difficulty.equals("Hard")) {
			Tetris.gameSpeed=1.5f;
		}
		// Sets the game level, sets the cleared lines and new level variables
		Tetris.level=1;
		Tetris.cleared=0;
		Tetris.newLevel=0;
		// Setup the timer to keep the game from running before the user presses enter to start it.
		Tetris.logicTimer = new Clock(gameSpeed);
		logicTimer.setPaused(true);
		while(true) {
			//Get the time that the frame started.
			long start = System.nanoTime();
			//Update the logic timer.
			logicTimer.update();
			// If a cycle has elapsed on the timer, we can update the game and move our current piece down.
			if(logicTimer.hasElapsedCycle()) {
				updateGame();
			}
			//Decrement the drop cool down if necessary.
			if(dropCooldown > 0) {
				dropCooldown--;
			}
			//Display the window to the user.
			renderGame();
			// Sleep to cap the framerate.			
			long delta = (System.nanoTime() - start) / 1000000L; // delta in miliseconds
			if(delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);	// sleeps the difference between the fps and the time for the game to process (delta)
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Updates the game and handles the bulk of it's logic.
	private static void updateGame() {
		String ans = "";
		if(flagConnect == true) {
				if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
					DataPlayer Player = new DataPlayer(playerName,difficulty);
					try {
						cliente = new Socket("localhost", 6666);
						ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
						saida.writeObject("Validate");
						saida.writeObject(Player.getNome()); 
						String CurrentType = currentType.toString();
						saida.writeObject(CurrentType);
						saida.writeObject(currentCol);
						saida.writeObject(currentRow + 1);
						saida.writeObject(currentRotation);
						saida.writeObject("V");
						saida.flush();
						ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());	
						ans = (String) input.readObject();
						if(ans.equals("Valid move")) {
							//Increment the current row if it's safe to do so.
							currentRow++;
							board.repaint();
						}	
						saida.close();
						cliente.close();
					}catch(IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}else {
					try {
						cliente = new Socket("localhost", 6666);
						ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
						saida.writeObject("Validate");
						saida.writeObject(playerName); 
						String CurrentType = currentType.toString();
						saida.writeObject(CurrentType);
						saida.writeObject(currentCol);
						saida.writeObject(currentRow + 1);
						saida.writeObject(currentRotation);
						saida.writeObject("I");
						saida.flush();
						ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
						ans = (String) input.readObject();
						if(ans.equals("Invalid move")) {
							board.addPiece(currentType, currentCol, currentRow, currentRotation);
							cleared = board.checkLines();
							if(cleared > 0) {
								lines += cleared;
								score += 50 << cleared;	// left bit shift - add the number of zeros on the right to the binary version of the number on the right
								// score = score + 50 << cleared;
							}
							newLevel+=cleared;
							gameSpeed += 0.035f;
							logicTimer.setCyclesPerSecond(gameSpeed);
							logicTimer.reset();
							dropCooldown = 25;
							if(newLevel<10) {
								newLevel+=cleared;
							}else if(newLevel>=10) {
								level+=1;
								newLevel=0;
								cleared=0;
							}
							spawnPiece();
						}					
						saida.close();
						cliente.close();
					}catch(IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
		} else { // flagConnect == false
			if(board.isValidAndEmpty(currentType, currentCol, currentRow + 1, currentRotation)) {
				//Increment the current row if it's safe to do so.
				currentRow++;
			} else {
				board.addPiece(currentType, currentCol, currentRow, currentRotation);
				cleared = board.checkLines();
				if(cleared > 0) {
					lines += cleared;
					score += 50 << cleared;	// left bit shift - add the number of zeros on the right to the binary version of the number on the right
					// score = score + 50 << cleared;
				}
				newLevel+=cleared;
				gameSpeed += 0.035f;
				logicTimer.setCyclesPerSecond(gameSpeed);
				logicTimer.reset();
				dropCooldown = 25;
				if(newLevel<10) {
					newLevel+=cleared;
				}else if(newLevel>=10) {
					level+=1;
					newLevel=0;
					cleared=0;
				}
				spawnPiece();
			}		
		}
	}

	// Forces the BoardPanel and SidePanel to repaint.
	private static void renderGame() {
		board.repaint();
		side.repaint();
	}
	
	// Resets the game variables to their default values at the start of a new game.
	// @throws ClassNotFoundException 
	public void resetGame() {
		if(flagConnect) {
			DataPlayer Player = new DataPlayer(playerName,difficulty);
			try {
				cliente = new Socket("localhost", 6666);
				ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
				saida.writeObject("Start");
				saida.writeObject(Player.getNome());
				saida.writeObject(Player.getDificuldade());
				saida.flush();	
				ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
				String typeS = (String) input.readObject();
				level = (int) input.readObject();
				score = (int) input.readObject();
				lines = (int) input.readObject();
				newLevel = (int) input.readObject();
				cleared = (int) input.readObject();
				isNewGame = (boolean) input.readObject();
				isGameOver = (boolean) input.readObject();		
				saida.close();
				cliente.close();
				for (int i = 0; i < TILE_TYPES.length && nextTypetemp == null; i++) {
					if (typeS.equals(TILE_TYPES[i].toString())) {
						nextTypetemp = TILE_TYPES[i];
					}
				}
				if(Tetris.difficulty.equals("Easy")) {
					Tetris.gameSpeed=1.0f;
				}else if(Tetris.difficulty.equals("Intermediate")) {
					Tetris.gameSpeed=1.25f;
				}else if(Tetris.difficulty.equals("Hard")) {
					Tetris.gameSpeed=1.5f;
				}
				board.clear();
				logicTimer.reset();
				logicTimer.setCyclesPerSecond(gameSpeed);
				spawnPiece();		
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}	
			count++;
		}else {	
			Tetris.level = 1;
			Tetris.score = 0;	
			Tetris.lines = 0;
			Tetris.newLevel = 0;
			Tetris.cleared = 0;
			if(Tetris.difficulty.equals("Easy")) {
				Tetris.gameSpeed=1.0f;
			}else if(Tetris.difficulty.equals("Intermediate")) {
				Tetris.gameSpeed=1.25f;
			}else if(Tetris.difficulty.equals("Hard")) {
				Tetris.gameSpeed=1.5f;
			}
			Tetris.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
			Tetris.isNewGame = false;
			Tetris.isGameOver = false;		
			board.clear();
			logicTimer.reset();
			logicTimer.setCyclesPerSecond(gameSpeed);	
			spawnPiece();
			
		}
		
	}

	// Spawns a new piece and resets our piece's variables to their default values.
	public static void spawnPiece() {
		
		if(flagConnect == true) {
			if(count == 0) {
				Tetris.currentType = nextTypetemp;
				Tetris.currentCol = currentType.getSpawnColumn();
				Tetris.currentRow = currentType.getSpawnRow();
				Tetris.currentRotation = 0;
				Tetris.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
			}else {
				Tetris.currentType = nextType;
				Tetris.currentCol = currentType.getSpawnColumn();
				Tetris.currentRow = currentType.getSpawnRow();
				Tetris.currentRotation = 0;
				Tetris.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
			}		
			
			if(!board.isValidAndEmpty(currentType, currentCol, currentRow, currentRotation)) {
				
				DataPlayer Player = new DataPlayer(playerName,difficulty);
				try {
					cliente = new Socket("localhost", 6666);
					ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
					saida.writeObject("Validate");
					saida.writeObject(Player.getNome()); 
					String CurrentType = currentType.toString();
					saida.writeObject(CurrentType);
					saida.writeObject(currentCol);
					saida.writeObject(currentRow + 1);
					saida.writeObject(currentRotation);
					saida.writeObject("I");
					saida.flush();
					ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
					String ans;
					ans = (String) input.readObject();
					saida.close();
					cliente.close();
					if(ans.equals("Invalid move")) {

						lose();
					}		
				} catch(IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}else {

			// Poll the last piece and reset the position and rotation to their default variables. 
			// Then pick the next piece to use.
			Tetris.currentType = nextType;
			Tetris.currentCol = currentType.getSpawnColumn();
			Tetris.currentRow = currentType.getSpawnRow();
			Tetris.currentRotation = 0;
			Tetris.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];

			// If the spawn point is invalid, pauses the game and flags that the game was lost because it means that the pieces on the board have gotten too high.
			// If the game has ended checks to see if it was a high score
			if(!board.isValidAndEmpty(currentType, currentCol, currentRow, currentRotation)) {
				lose();	
			}
		}
		
	}

	public static void lose() 
	{
		String info = "";
		Tetris.isGameOver = true;
		logicTimer.setPaused(isPaused);
		if(flagConnect == false){ // flagConnect = false
			Tetris.isGameOver = true;
			logicTimer.setPaused(isPaused);
			// If a high score was achieved, asks the player for a name and saves the high score
			// If a high score wasn't achieved, encouraging message pops up
			if (score>HighScore.getHighScores()[9].getScore())
			{

				info="You got a high score!\n<br>Please enter you name.\n<br>(Note: Only 10 characters will be saved)";
				JLabel label = new JLabel("<html><center>GAME OVER\n<br>" + info);
				label.setHorizontalAlignment(SwingConstants.CENTER);

				String name=JOptionPane.showInputDialog(null, label,"Tetris", JOptionPane.INFORMATION_MESSAGE);
				if (name!=null) {
					HighScore.addHighScore(new HighScore(score,level,lines,(name.length()>10)?name.substring(0, 10):name,(difficulty.length()>12)?difficulty.substring(0, 12):difficulty));
				}
			}else {
				info="You didn't get a high score:( \n<br>Keep trying you will get it next time!";
				JLabel label = new JLabel("<html><center>GAME OVER\n<br>" + info);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				JOptionPane.showMessageDialog(null, label, "Tetris", JOptionPane.PLAIN_MESSAGE);
			}

			if (JOptionPane.showConfirmDialog(null, "Do you want to play again?",
					"Tetris", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
				Tetris.score=0;
				Tetris.level=0;
				Tetris.lines=0;
				startGame();
			}else
			{
				System.exit(0);
			}

		} else if(flagConnect == true) {
			
			DataPlayer Player = new DataPlayer(playerName,difficulty);
			Player.setScore(score);
			try {
				cliente = new Socket("localhost", 6666);
				ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
				saida.writeObject("gameOver");
				saida.writeObject(Player.getNome());
				saida.writeObject(Player.getDificuldade());
				saida.writeObject(Player.getScore());
				saida.flush();
				ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
				int finalScore = (int) input.readObject();
				info="Congratulations!! Your score is: " + finalScore + "\n<br>The scores can be viewed on the ranking menu item.\n<br>";
				JLabel label = new JLabel("<html><center>GAME OVER\n<br>" + info);
				label.setHorizontalAlignment(SwingConstants.CENTER);	
				JOptionPane.showMessageDialog(null, label, "Tetris", JOptionPane.PLAIN_MESSAGE);
				saida.close();
				cliente.close();
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			isNewGame = true;
			isPaused = false;
			renderGame();

			if (JOptionPane.showConfirmDialog(null, "\nDo you want to play again?\nYou will be disconnected and have to insert a new login.",
					"Tetris", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
				try {
					cliente=new Socket("localhost",6666);
					ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
					saida.writeObject("Exit");
					saida.writeObject(Player.getNome());
					saida.writeObject(Player.getDificuldade());
					if(cliente!=null) {
						try {
							cliente.close();
						}catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					flagConnect = false;
					login = false;
					isNewGame = true;
					isPaused = false;
					isGameOver = false;
					renderGame();
					startGame();
					JOptionPane.showMessageDialog(null, "You are now disconnected from the server");	
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				System.exit(0);
			}
		}
	}

	private void rotatePiece(int newRotation) {
		
		int newColumn = currentCol;
		int newRow = currentRow;
		int left = currentType.getLeftInset(newRotation);
		int right = currentType.getRightInset(newRotation);
		int top = currentType.getTopInset(newRotation);
		int bottom = currentType.getBottomInset(newRotation);
		if(currentCol < -left) {
			newColumn -= currentCol - left;
		} else if(currentCol + currentType.getDimension() - right >= BoardPanel.COL_COUNT) {
			newColumn -= (currentCol + currentType.getDimension() - right) - BoardPanel.COL_COUNT + 1;
		}
		if(currentRow < -top) {
			newRow -= currentRow - top;
		} else if(currentRow + currentType.getDimension() - bottom >= BoardPanel.ROW_COUNT) {
			newRow -= (currentRow + currentType.getDimension() - bottom) - BoardPanel.ROW_COUNT + 1;
		}
		
		if(flagConnect == true) {
			if(board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) {
				try {
					cliente = new Socket("localhost", 6666);
					ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
					saida.writeObject("Validate");
					saida.writeObject(playerName); 
					String CurrentType = currentType.toString();
					saida.writeObject(CurrentType);
					saida.writeObject(newColumn);
					saida.writeObject(newRow);
					saida.writeObject(newRotation);
					saida.writeObject("V");
					saida.flush();
					ObjectInputStream input = new ObjectInputStream(cliente.getInputStream());
					String ans;
					ans = (String) input.readObject();
					if(ans.equals("Valid move")) {
						currentRotation = newRotation;
						currentRow = newRow;
						currentCol = newColumn;
					}
					saida.close();
					cliente.close();
				} catch(IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		} else {
			if(board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) {
				currentRotation = newRotation;
				currentRow = newRow;
				currentCol = newColumn;
			}
		}
	}

	public boolean isPaused() {
		return isPaused;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public boolean isNewGame() {
		return isNewGame;
	}

	public int getScore() {
		return score;
	}

	public int getLevel() {
		return level;
	}

	public String getDiff(){
		return difficulty;
	}

	public int getLines() {
		return lines;
	}

	public TileType getPieceType() {
		return currentType;
	}

	public TileType getNextPieceType() {
		return nextType;
	}

	public int getPieceCol() {
		return currentCol;
	}

	public int getPieceRow() {
		return currentRow;
	}

	public int getPieceRotation() {
		return currentRotation;
	}

	public static void close() {
		System.out.println(1);
		System.exit(0);

	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public boolean getStatus() {
		return flagConnect;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e){
			e.printStackTrace();
		}
		Tetris tetris = new Tetris();
		tetris.startGame();
	}

}



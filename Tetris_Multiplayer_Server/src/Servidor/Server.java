
package Servidor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


import Servidor.TileType;




public class Server implements Runnable {
	
	File currentDir = new File (".");
	String basePath = currentDir.getAbsolutePath();
	String pathLog = basePath + "/playersLogger.txt";
	String pathLogScore = basePath + "/scoreLogger.txt";
	static String date;

	private final static Logger logr = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME ); // console logger

    private static void setupLogger() {
    	
        LogManager.getLogManager().reset(); // delete root handlers
        logr.setLevel(Level.ALL);
        
        try {
            FileHandler fh = new FileHandler("moveLogger.log"/*, true*/);
            fh.setLevel(Level.ALL);
            logr.addHandler(fh);
        } catch (java.io.IOException e) {            
            // don't stop the program but log out to console
            logr.log(Level.ALL, "File logger not working.", e);
        }

    }
	
	public static final String HighScore = null;
	public Socket server = null;

	
	private static final TileType[] TILE_TYPES = new TileType[]{
	        TileType.TypeI, TileType.TypeJ, TileType.TypeL, TileType.TypeO, TileType.TypeS, TileType.TypeT, TileType.TypeZ
	    };

	private int lines, score, newLevel, level;

	private static TileType type;

	private static int cleared;

	private static int currentRow;

	public TileType[][] tiles;
	 
	public static final int COL_COUNT = 10;

	public static final int ROW_COUNT = 22;

	public Server(Socket socket){

		this.server = socket;

	}
	
	public static void main (String[] args) throws IOException, ClassNotFoundException{
		Calendar calendar = Calendar.getInstance(); // Returns instance with current date and time set
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		date = formatter.format(calendar.getTime());
		setupLogger();

		
		int port = 6666;

		try(ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server is listening on port: " + port + "\n\n");

			while(true) {
				Socket socket = serverSocket.accept();
				System.out.println("----------------------------------------------------------------------------------------------------");
				System.out.println("Communication accepted: " + socket.getInetAddress());
				Server server = new Server(socket);
				Thread t = new Thread(server);				
				t.start();
			}

		}catch(Exception e) {
			System.out.println(e.getMessage());
			//logr.log(Level.SEVERE, "A error occurred: ", e);
			System.exit(1);
		}
	}

	ServerProtocol Comunicao = new ServerProtocol();
	DataPlayer Player=new DataPlayer();

	@Override
	public void run() {
		
		FileWriter myWriter;
       

		ObjectInputStream input;
		
		try {
			input = new ObjectInputStream(server.getInputStream());

			String Msg = (String) input.readObject();
			System.out.println(Msg);

			if (Msg.equals("Connect")) {
				String player = (String) input.readObject();
				String difficulty = (String) input.readObject();
				System.out.print("Name: " + player + " + Difficulty: " + difficulty + "\n");
				Player = Comunicao.MsgConnect(server, input);
				logr.info("Name: " + player + " + Difficulty: " + difficulty);

				try {
					myWriter = new FileWriter(pathLog, true); // true = since creation
					myWriter.write("[NewGame] " + player + " started a new game at " + date + "\n");
					myWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					StringWriter errors = new StringWriter();
                    e1.printStackTrace(new PrintWriter(errors));
				}

				return;
			}
			
			if (Msg.equals("Start")) {
				String player = (String) input.readObject();
				String difficulty = (String) input.readObject();
				Comunicao.MsgStart(server, player, difficulty);
				return;
			}

			if (Msg.equals("Exit")) {
				Player=Comunicao.MsgExit(server, input);
				System.out.println(Player.getAll() + "\nPlayer disconnected\n");
				logr.info(Player.getAll() + "\nPlayer disconnected\n");
				return;
			}

			if (Msg.equals("Ranking")) {
				int Dificuldade=0;
				Comunicao.MsgRanking(server, Dificuldade, logr);
			}

			if(Msg.equals("Validate")) {
				String id = (String) input.readObject();
				String typeS = (String) input.readObject();
				int x = (int) input.readObject();
				int y = (int) input.readObject();
				int rotation = (int) input.readObject();
				String vd = (String) input.readObject();
				System.out.println("Player: " + id + "\nType: " + typeS + "\nX: " + x + "\nY: " + y + "\nRotation: " + rotation);
				logr.info("Request: Player: " + id + " Type: " + typeS + " X: " + x + " Y: " + y + " Rotation: " + rotation);

				if(vd.equals("V")) {
					System.out.println("Valid move");
					logr.info("Valid move");
					ObjectOutputStream saida = new ObjectOutputStream(server.getOutputStream());
					saida.writeObject("Valid move");
				}else if(vd.equals("I")) {
					System.out.println("Invalid move");
					logr.info("Invalid move");
					ObjectOutputStream saida = new ObjectOutputStream(server.getOutputStream());
					saida.writeObject("Invalid move");
				}
			}
			
			if(Msg.equals("gameOver")) {
				Player.setNome((String) input.readObject());
				Player.setDificuldade((String) input.readObject());
				Player.setScore((int) input.readObject());
				System.out.println(Player.getNome() + " + " + Player.getDificuldade() + " + " + Player.getScore());
				logr.info("Game over: " + Player.getNome() + " + " + Player.getDificuldade() + " + " + Player.getScore());
				List<DataPlayer> Players = new ArrayList<DataPlayer>();
				InputOutputData InputOutput = new InputOutputData();
				if (InputOutput.ReadData("Ficheiro") == null) {
					System.out.println("No players");
					Players.add(Player);
					InputOutput.WriteData(Players, "Ficheiro");
					StringBuilder SB = new StringBuilder();
					for (DataPlayer dataPlayer : Players) {
						SB.append(dataPlayer.getScore() + "\n");
					}
					return;
				}
				else {
					int fscore = Player.getScore();
					Players = InputOutput.ReadData("Ficheiro");
					Players.add(Player);
					InputOutput.WriteData(Players, "Ficheiro");
					ObjectOutputStream saida = new ObjectOutputStream(server.getOutputStream());
					saida.writeObject(fscore);
					
					try {
						myWriter = new FileWriter(pathLogScore, true);
						myWriter.write("[GameOver] " + Player.getNome() + " finished a new game at " + date + " with score: " + fscore + "\n");
						myWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						StringWriter errors = new StringWriter();
	                    e1.printStackTrace(new PrintWriter(errors));
					}
				}
				
			}
		}catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			logr.log(Level.SEVERE, "A error occurred: ", e);

		}
	}
}

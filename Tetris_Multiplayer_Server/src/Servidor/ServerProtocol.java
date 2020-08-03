
package Servidor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import Servidor.TileType;



// Server side protocol

public class ServerProtocol implements Serializable{

	DataPlayer dataPlayer;// = null;

	private static final TileType[] TILE_TYPES = new TileType[]{
			TileType.TypeI, TileType.TypeJ, TileType.TypeL, TileType.TypeO, TileType.TypeS, TileType.TypeT, TileType.TypeZ
	};

	public static TileType[][] tiles;

	private static TileType type;

	private static int cleared;

	private static int currentRow;

	private static String player;

	private static int newLevel;

	private static int lines;

	private static int level;

	private static int score;

	private static boolean isNewGame;

	private static boolean isGameOver;

	private static TileType nextType;

	private static final int TYPE_COUNT = TileType.values().length;

	private static Random random;

	public static final int NBEST_GAMES_SHOW=10;

	private static int currentCol;

	private static TileType currentType;

	private static int currentRotation;

	private static String difficulty = "Easy"; // default

	public ServerProtocol(){

	}

	public DataPlayer MsgConnect(Socket Cliente, ObjectInputStream entrada) {
		try {
			DataPlayer Player = new DataPlayer();

			Player.setNome((String) entrada.readObject());
			Player.setDificuldade((String) entrada.readObject());
			Player.setScore(0);
			System.out.println(Player.getAll());
			return Player;

		} catch (IOException | ClassNotFoundException ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}


	public void MsgStart(Socket cliente, String player, String difficulty) {

		try {
			this.random = new Random();
			this.level = 1;
			this.score = 0;	
			this.lines = 0;
			this.newLevel = 0;
			this.cleared = 0;
			this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
			this.isNewGame = false;
			this.isGameOver = false;		
			//board.clear();
			tiles = new TileType[22][10];
			for(int i = 0; i < 22; i++) {
				for(int j = 0; j < 10; j++) {
					tiles[i][j] = null;
				}
			}

			ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
			saida.writeObject(nextType.toString());
			saida.writeObject(level);
			saida.writeObject(score);
			saida.writeObject(lines);
			saida.writeObject(newLevel);
			saida.writeObject(cleared);
			saida.writeObject(isNewGame);
			saida.writeObject(isGameOver);

		}catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}


	public DataPlayer MsgExit(Socket cliente, ObjectInputStream entrada) {

		try {
			DataPlayer Player = new DataPlayer();

			Player.setNome((String) entrada.readObject());
			Player.setDificuldade((String) entrada.readObject());

			return Player;

		} catch (IOException | ClassNotFoundException ex) {
			System.out.println(ex.getMessage());
		}
		return null;
	}



	//@SuppressWarnings("static-access")
	public void MsgRanking(Socket cliente, int dificuldade, Logger logr) {

		List<DataPlayer> Players= new ArrayList<DataPlayer>();
		InputOutputData DataIO= new InputOutputData();

		try {

			Players = DataIO.ReadData("Ficheiro");

			if(Players.size() == 0) {
				JOptionPane.showMessageDialog(null, "No players");
				return;
			}

			// Sort by the top scores
			Collections.sort(Players, new SortScore());		

			List<DataPlayer> listOrdered = new ArrayList<DataPlayer>();
			int i=0;
			List<String> bestPlayers = new ArrayList<String>();

			for (DataPlayer resultado : Players) {
				// avoid various scores from the same player
				if(bestPlayers.contains(resultado.getNome()))
					continue;

				bestPlayers.add(resultado.getNome());

				listOrdered.add(resultado);
				i++;
				if(i == ServerProtocol.NBEST_GAMES_SHOW) // saves the 10 best scores
					break;
			}

			StringBuilder SB = new StringBuilder();

			for (DataPlayer dataPlayer : listOrdered) {
				SB.append("Name: "+ dataPlayer.getNome() + " \t");
				SB.append("Difficulty: "+ dataPlayer.getDificuldade() + " \t");
				SB.append("Score: "+ dataPlayer.getScore() + " \n");
			}

			System.out.println(SB.toString());

			ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
			saida.writeObject(SB);
			logr.info(SB.toString());

			return;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

	}



}

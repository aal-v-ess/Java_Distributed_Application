import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/*
 * The class that contains information about high scores and facilitates writing them to a .dat file 
 * @author Pedro Alves
 *
 */


public class HighScore implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int level;
	private int score;
	private int lines;
	private String difficulty;
	private String name;
	
	
	//The constructor
	public HighScore(int s, int l, int k, String n, String d/*, String d2*/)
	{
		score=s;
		setLevel(l);
		setLines(k);
		setName(n);
		setDiff(d);

	}

	/**
	 * Sets the difficulty
	 * @param difficulty
	 */
	public void setDiff(String difficulty) {
		this.difficulty=difficulty;
	}
	
	/**
	 * Returns the difficulty
	 * @return
	 */
	public String getDiff(){
		return difficulty;
	}
	
	/**
	 * Sets the number of lines
	 * @param lines
	 */
	public void setLines(int lines) {
		this.lines=lines;
	}
	
	/**
	 * Returns the number of lines
	 * @return
	 */
	public int getLines() {
		return lines;
	}
	
	
	/**
	 * Sets the score
	 * @param score
	 */
	public void setScore(int score)
	{
		this.score=score;
	}
	/**
	 * Returns the score
	 * @return
	 */
	public float getScore()
	{
		return score;
	}
	
	/**
	 * Sets the level
	 * @param level
	 */
	public void setLevel(int level) 
	{
		this.level = level;
	}
	
	/**
	 * Returns the level
	 * @return
	 */
	public int getLevel() 
	{
		return level;
	}

	/**
	 * Sets the name
	 * @param name
	 */
	public void setName(String name) 
	{
		this.name = name;
	}

	/**
	 * Returns the name
	 * @return
	 */
	public String getName() 
	{
		return name;
	}
	

	
	/**
	 * Decides whether this HighScore is greater than, less than, or equal to the argument
	 */
	
	public int compareTo(HighScore h)
	{
		return new Integer(this.score).compareTo(h.score);
	}
	
	
	/**
	 * This is called when there is an empty file in order prevent exceptions
	 */
	
	private static void initializeFile()
	{
		
		HighScore[] h={new HighScore(0,0,0," "," "),new HighScore(0,0,0," "," "),new HighScore(0,0,0," "," "),
				new HighScore(0,0,0," "," "),new HighScore(0,0,0," "," "),new HighScore(0,0,0," "," "),
				new HighScore(0,0,0," "," "),new HighScore(0,0,0," "," "),new HighScore(0,0,0," "," "),
				new HighScore(0,0,0," "," ")};
		
		try 
		{
			System.out.println("File created: HighScores.dat");
			ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream("HighScores.dat"));
			o.writeObject(h);
			o.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
		catch (IOException e) {e.printStackTrace();}
	}
	
	
	/**
	 * Reads the .dat file and returns the constants
	 * @return
	 */
	
	public static HighScore[] getHighScores()
	{
		if (!new File("HighScores.dat").exists())
			initializeFile();
		try 
		{
			ObjectInputStream o=new ObjectInputStream(new FileInputStream("HighScores.dat"));
			HighScore[] h=(HighScore[]) o.readObject();
			
			
			o.close();
			
			
			return h;
			
		} catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}
		return null;
	}
	
	/**
	 * Adds a new HighScore to the .dat file and maintains the proper order
	 * @param h
	 */
	public static void addHighScore(HighScore h)
	{
		HighScore[] highScores=getHighScores();
		highScores[highScores.length-1]=h;
		for (int i=highScores.length-2; i>=0; i--)
		{
			if (highScores[i+1].compareTo(highScores[i])>0)
			{
				HighScore temp=highScores[i];
				highScores[i]=highScores[i+1];
				highScores[i+1]=temp;
			}
		}
		try 
		{
			ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream("HighScores.dat"));
			o.writeObject(highScores);
			o.close();
		} catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	
}


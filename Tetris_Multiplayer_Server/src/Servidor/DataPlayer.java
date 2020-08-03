
package Servidor;



import java.io.Serializable;

public class DataPlayer implements Serializable{


	private static final long serialVersionUID = -2403621704120145428L;
	
	//private static final long serialVersionUID = 1L;
	private String Nome;
	private String Dificuldade;
	private int Score;
	
	//default
	public DataPlayer(){
		
	}

	//com atributos
	public DataPlayer(String _Nome,String  _Dificuldade){
		Nome=_Nome;
		Dificuldade=_Dificuldade;
		Score=0;
	}
	public int getScore() {
		return Score;
	}
	public void setScore(int score) {
		Score = score;
	}
	public String getNome() {
		return Nome;
	}
	public void setNome(String nome) {
		Nome = nome;
	}
	public String getDificuldade() {
		return Dificuldade;
	}
	public void setDificuldade(String dificuldade) {
		Dificuldade = dificuldade;
	}

	public String getAll() {
		StringBuilder SB= new StringBuilder();
		SB.append("Name: " + Nome + " + Difficulty: " + Dificuldade );
		return SB.toString();
	}
}
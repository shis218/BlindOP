package com.example.HerokuBlind;

public class Rota {
	private String nome;
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	private int numeroSequencia;
	private String Direcao;
	private float metros;
	private float passos;
	
	
	public int getNumeroSequencia() {
		return numeroSequencia;
	}
	public String getDirecao() {
		return Direcao;
	}
	public float getMetros() {
		return metros;
	}
	public float getPassos() {
		return passos;
	}
	public void setNumeroSequencia(int numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}
	public void setDirecao(String direcao) {
		Direcao = direcao;
	}
	public void setMetros(float metros) {
		this.metros = metros;
	}
	public void setPassos(float passos) {
		this.passos = passos;
	}
	
	
	
	
	
	
}
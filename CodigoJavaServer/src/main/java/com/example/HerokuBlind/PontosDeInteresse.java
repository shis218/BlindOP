package com.example.HerokuBlind;
/**
 * Classe de Modelo para Pontos de Interesse, contendo seu nome, a coordenada X e a coordenada Y
 * @author Henrique
 *
 */

public class PontosDeInteresse {
String nomeinteresse;
public String getNomeinteresse() {
	return nomeinteresse;
}
public String getCordX() {
	return cordX;
}
public String getCordY() {
	return cordY;
}
public void setNomeinteresse(String nomeinteresse) {
	this.nomeinteresse = nomeinteresse;
}
public void setCordX(String cordX) {
	this.cordX = cordX;
}
public void setCordY(String cordY) {
	this.cordY = cordY;
}
String cordX;
String cordY;
}

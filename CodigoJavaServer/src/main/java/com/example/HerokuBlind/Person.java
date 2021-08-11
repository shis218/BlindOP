package com.example.HerokuBlind;
/**
 * classe modelo de pessoas, tendo getId, nome e chave. 
 * @author Henrique
 *
 */
public class Person {

	  private String id;

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public String getNome() {
	        return nome;
	    }

	    public void setNome(String nome) {
	        this.nome = nome;
	    }

	    public String getKey() {
	        return key;
	    }

	    public void setKey(String key) {
	        this.key = key;
	    }

	    private String nome;
	    private String key;
	    @Override
	    public String toString() {
	        return "First Name: " + getNome()
	                + "\nkey: " +  getKey();
	    }

	}

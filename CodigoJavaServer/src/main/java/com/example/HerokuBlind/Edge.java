package com.example.HerokuBlind;
/**
 * Classe que guarda informação de arestas, tendo um vertice de inicio e um vertice alvo, alem de seu peso. 
 * Seu uso principal é para computar o algoritmo Dijkstra e seu uso padrão é utilizando o construtor com as informações de peso, inicio, fim e então passando a classe para outros lugares.
 * @author Artem Lovan 
 */
public class Edge {
    private double weight;
    private Vertex startVertex;
    private Vertex targetVertex;

    public Edge(float weight, Vertex startVertex, Vertex targetVertex) {
        this.weight = weight;
        this.startVertex = startVertex;
        this.targetVertex = targetVertex;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Vertex getStartVertex() {
        return startVertex;
    }

    public void setStartVertex(Vertex startVertex) {
        this.startVertex = startVertex;
    }

    public Vertex getTargetVertex() {
        return targetVertex;
    }

    public void setTargetVertex(Vertex targetVertex) {
        this.targetVertex = targetVertex;
    }
}
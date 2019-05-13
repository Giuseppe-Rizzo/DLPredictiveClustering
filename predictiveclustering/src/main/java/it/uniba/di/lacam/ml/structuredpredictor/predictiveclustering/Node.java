package it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering;

/**
 * A class for representing a node of the tree
 * @author Utente
 *
 */
public class Node<E,Model> {

	private E  t;
	private Model model;
	PredictiveTree pos;
	public E getT() {
		return t;
	}

	public Model getModel() {
		return model;
	}

	PredictiveTree neg;
	
	public Node(E test, Model model) {
		this.t= test;
		this.model=model;
	}

	
}

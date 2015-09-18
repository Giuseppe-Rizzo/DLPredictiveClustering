package predictiveclustering;

/**
 * A class for representing a node of the tree
 * @author Utente
 *
 */
public class Node<E,Model> {

	private E  t;
	private Model model;
	PredictiveTree pos;
	PredictiveTree neg;
	
	public Node(E test, Model model) {
		this.t= test;
		this.model=model;
	}

	
}

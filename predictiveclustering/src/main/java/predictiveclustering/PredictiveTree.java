package predictiveclustering;

public class PredictiveTree<E,Model> {
	private Node<E,Model> root;


	public PredictiveTree() {
	}
	
	public void setRoot(E test, Model model){
		
		root= new Node(test, model);
		
	}
	
	public Node<E,Model> getRoot(){
		
		return root;
		
	}
	public void setPosTree(PredictiveTree tree){
		
		root.pos= tree;
	}
	
	public void setNegTree(PredictiveTree tree){
		
		root.neg= tree;
	}
	
 public PredictiveTree getPosSubTree(){
		
		return root.pos;
	}
	
public PredictiveTree getNegSubTree(){
		
		return root.pos;
	}
}

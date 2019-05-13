package it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering;
/**
 * Main procedure
 * @author Giuseppe Rizzo
 *
 */
public class DLPredictiveClusteringEvaluation {

	public DLPredictiveClusteringEvaluation() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
	Evaluation  ev = new  Evaluation("C:/Users/Giuseppe/Documents/ontos/ontos4ArtificialLP/biopax.owl");	
	ev.bootstrap(Integer.parseInt("5"));
	

	}

}

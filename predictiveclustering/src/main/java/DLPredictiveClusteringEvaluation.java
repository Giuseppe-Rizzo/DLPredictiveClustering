import predictiveclustering.Evaluation;

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
	Evaluation  ev = new  Evaluation(args[0]);	
	ev.bootstrap(Integer.parseInt(args[1]));
	

	}

}

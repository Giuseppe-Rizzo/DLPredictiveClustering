package predictiveclustering;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;











import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

import predictiveclustering.utils.Couple;
import predictiveclustering.utils.Npla;
import predictiveclustering.utils.Split;

public class PredictiveClusterInducer<K,E> {
	private PelletReasoner reasoner;
	
	private static Logger logger = LoggerFactory.getLogger(PredictiveClusterInducer.class);

	public PredictiveClusterInducer() {
		
	}

	
	public PredictiveClusterInducer(PelletReasoner r) {
		reasoner=r;
	}

	
	
	public PredictiveTree<OWLClassExpression,Model<K,E>> induceTree(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs) {		
		Double prPos =0.5;
		Double prNeg=0.5;
		logger.info("Learning problem\t p:"+posExs.size()+"\t n:"+negExs.size()+"\t u:"+undExs.size()+"\t prPos:"+prPos+"\t prNeg:"+prNeg+"\n");
//		//ArrayList<OWLIndividual> truePos= posExs;
//		//ArrayList<OWLIndividual> trueNeg= negExs;
		int depth=0;
		 DLTreesRefinementOperator dlTreesRefinementOperator =  new DLTreesRefinementOperator(reasoner,8);
//       
		// set the heuristic for tree induction
		TreeInductionHeuristics heuristic= new TreeInductionHeuristics();
		heuristic.setReasoner(reasoner);
		
		
		Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> examples = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExs, negExs, undExs,depth, prPos , prNeg);
		PredictiveTree<OWLClassExpression,Model<K,E>> tree = new PredictiveTree<OWLClassExpression,Model<K,E>>(); // new (sub)tree
		Stack<Couple<PredictiveTree<OWLClassExpression,Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>> stack= new Stack<Couple<PredictiveTree<OWLClassExpression,Model<K,E>>,Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>>();
		Couple<PredictiveTree<OWLClassExpression,Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> toInduce= new Couple<PredictiveTree<OWLClassExpression,Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);
//
		while (!stack.isEmpty()){
			
			Couple<PredictiveTree<OWLClassExpression, Model<K,E>>, Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double>> pop = stack.pop();
			PredictiveTree<OWLClassExpression, Model<K,E>> currentTree = pop.getFirstElement();
			// generate the candidate concepts
			
			if (!stopcondition(currentTree)){
			Set<OWLClassExpression> refinements = dlTreesRefinementOperator.refine(null,posExs, negExs); // neg exs will be empty
			// for each candidate computes the local models if it exists
			OWLClassExpression[] ref= refinements.toArray(new OWLClassExpression[refinements.size()]);
			OWLClassExpression bestDescription= heuristic.selectBestConcept(ref, posExs, negExs, undExs, 0, 0);
			
			SortedSet<OWLIndividual> negExsF = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> undExsT = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> undExsF = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> posExsF = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();
			
			SortedSet<OWLIndividual> negExsT = new TreeSet<OWLIndividual>();
			Split.split(bestDescription, reasoner.getManager().getOWLDataFactory(), reasoner, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF); 
			
			}
			else{
				logger.info("not implemented yet!");
				
			}
			
			
		}

		return null; //tree;

	}


	private boolean stopcondition(PredictiveTree<OWLClassExpression, Model<K,E>> currentTree) {
		// TODO Auto-generated method stub
		return false;
	}

}

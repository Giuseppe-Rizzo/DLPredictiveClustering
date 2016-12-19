package predictiveclustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;





import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

import predictiveclustering.utils.Couple;
import predictiveclustering.utils.Npla;
import predictiveclustering.utils.Split;

/**
 * Implementation of the procedure for growing and for making prediction through Predictive Clustering Trees
 * @author Giuseppe Rizzo
 *
 * @param <K> the objects to be predicted
 * @param <E> the models 
 */
public class PredictiveClusterInducer<K,E> {
	private PelletReasoner reasoner;
	private PredictiveTree<K,E> tree;
	private int MAXDEPTH= 5; 
	private static boolean regressionTask= true;

	private static Logger logger = LoggerFactory.getLogger(PredictiveClusterInducer.class);

	public PredictiveClusterInducer() {

	}


	public PredictiveClusterInducer(PelletReasoner r) {
		reasoner=r;
	}


/**
 * Procedure for growing the tree
 * @param posExs
 * @param negExs
 * @param undExs
 * @param collection
 * @param queries
 * @return
 */
	public <K,E> PredictiveTree<OWLClassExpression,Model<K,E>> induceTree(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, Collection collection, Set<OWLDataProperty> queries) {		
		// K: data properties
logger.info("Learning start");
		Double prPos =0.5;
		Double prNeg=0.5;
		// combination of the models of the training individuals
		ArrayList<Model> models= new ArrayList<Model>(collection) ;
		//computePriors(posExs, negExs, undExs, models);

		Model[] m= models.toArray(new Model[models.size()]);
		// model for each element
		Model priorModel1= ModelUtils.combineModels(m);  //computeModel(posExs, negExs, undExs, models, regressionTask);
		
		
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

		while (!stack.isEmpty()){

	
			Couple<PredictiveTree<OWLClassExpression, Model<K,E>>, Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double>> pop = stack.pop();
			PredictiveTree<OWLClassExpression, Model<K,E>> currentTree = pop.getFirstElement();
			// generate the candidate concepts
			Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> secondElement = pop.getSecondElement();
			depth=secondElement.getFourth();
			posExs=secondElement.getFirst();
			negExs=secondElement.getSecond();
			undExs=secondElement.getThird();

			//			
			//			
			if ((posExs.size()==0) && (negExs.size()==0) && (undExs.size()==0)){
				
				currentTree.setRoot(null,priorModel1);
				System.out.println("Leaf (prior): "+ priorModel1);
				

			}else if (depth>MAXDEPTH){
				Model localModel2= computeModel(posExs, negExs, undExs, models, regressionTask);
				currentTree.setRoot(null, localModel2);
				System.out.println("Leaf: "+ localModel2);
				

			}
			else{
				Set<OWLClassExpression> refinements = dlTreesRefinementOperator.refine(null,posExs, negExs); // neg exs will be empty					
				// for each candidate computes the local models if it exists
				OWLClassExpression[] ref= refinements.toArray(new OWLClassExpression[refinements.size()]);
				OWLClassExpression bestDescription= heuristic.selectBestConceptRMSE(ref, posExs, negExs, undExs,models, 0, 0, queries);
				Model localModel2= computeModel(posExs, negExs, undExs, models, regressionTask);
				SortedSet<OWLIndividual> negExsF = new TreeSet<OWLIndividual>();
				SortedSet<OWLIndividual> undExsT = new TreeSet<OWLIndividual>();
				SortedSet<OWLIndividual> undExsF = new TreeSet<OWLIndividual>();
				SortedSet<OWLIndividual> posExsF = new TreeSet<OWLIndividual>();
				SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();
				SortedSet<OWLIndividual> negExsT = new TreeSet<OWLIndividual>();
				Split.split(bestDescription, reasoner.getManager().getOWLDataFactory(), reasoner, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF); 

				// set the root
				currentTree.setRoot(bestDescription, localModel2);
				System.out.println("Installed description: "+currentTree.getRoot().getT());
				PredictiveTree<OWLClassExpression, Model<K,E>> posTree= new PredictiveTree<OWLClassExpression, Model<K,E>>();
				PredictiveTree<OWLClassExpression, Model<K,E>> negTree= new PredictiveTree<OWLClassExpression, Model<K,E>>(); // recursive calls simulation
				currentTree.setPosTree(posTree);
				currentTree.setNegTree(negTree);
				Npla<SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, SortedSet<OWLIndividual>, Integer, Double, Double> npla1 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsT, negExsT, undExsT, (depth+1), 0.0, 0.0);
				Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double> npla2 = new Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>(posExsF, negExsF, undExsF, (depth+1), 0.0, 0.0);
				Couple<PredictiveTree<OWLClassExpression, Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> pos= new Couple<PredictiveTree<OWLClassExpression, Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
				pos.setFirstElement(posTree);
				pos.setSecondElement(npla1);

				// negative branch
				Couple<PredictiveTree<OWLClassExpression, Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>> neg= new Couple<PredictiveTree<OWLClassExpression, Model<K,E>>,Npla<SortedSet<OWLIndividual>,SortedSet<OWLIndividual>,SortedSet<OWLIndividual>, Integer, Double, Double>>();
				neg.setFirstElement(negTree);
				neg.setSecondElement(npla2);
				stack.push(neg);
				stack.push(pos);
			}


		}

		return tree;

	}


	/**
	 * The model to assign to the leaf
	 * @param posExs
	 * @param negExs
	 * @param undExs
	 * @param models
	 * @param regressionTask
	 * @return
	 */
	private  Model computeModel(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs, ArrayList<Model> models, boolean regressionTask) {
		for (OWLIndividual owlIndividual : posExs) {
			Model<?, ?> model = ModelUtils.getModels(owlIndividual);
			models.add(model);
		}

		for (OWLIndividual owlIndividual : negExs) {
			Model<Object, Object> model = ModelUtils.getModels(owlIndividual);
			models.add(model);
		}

		for (OWLIndividual owlIndividual : undExs) {
			Model<?, ?> model = ModelUtils.getModels(owlIndividual);
			models.add(model);
		}
		// model.getClass();
		Model[] modelsArray= models.toArray(new Model[models.size()]);
		//Model model = models.get(0);
		if (this.regressionTask)
			return ModelUtils.combineModels(modelsArray);
		
		return null;
	}


	private boolean stopcondition(PredictiveTree<OWLClassExpression, Model<K,E>> currentTree) {
		// TODO Auto-generated method stub
		return false;
	}


	/**
	 * Predict the approximate filler for the test individual
	 * @param indTestEx
	 * @param tree
	 * @param dataFactory
	 * @return the prediction
	 */
	public  Model classifyExample(OWLIndividual indTestEx, PredictiveTree tree, OWLDataFactory dataFactory) {


		Stack<PredictiveTree<E, Model>> stack= new Stack<PredictiveTree<E, Model>>();
		//OWLDataFactory dataFactory = kb.getDataFactory();
		stack.add(tree);
		Model result=null;
		boolean stop=false;
		while(!stack.isEmpty() && !stop){
			PredictiveTree currentTree= stack.pop();
		
			OWLClassExpression rootClass = (OWLClassExpression)currentTree.getRoot().getT();

			//			System.out.println("Root class: "+ rootClass);
			if (rootClass==null){
				stop=true;
				result=  (Model)(currentTree.getRoot()).getModel();

			}
			else {

				OWLClassAssertionAxiom owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(rootClass, indTestEx);
				OWLClassAssertionAxiom negOwlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(dataFactory.getOWLObjectComplementOf(rootClass), indTestEx);
				if (reasoner.isEntailed(owlClassAssertionAxiom))
					stack.push(currentTree.getPosSubTree());
				else //if (reasoner.isEntailed(negOwlClassAssertionAxiom))
					stack.push(currentTree.getNegSubTree());
			//	else {
				//	stop=true;
				//	result=  (Model)(currentTree.getRoot()).getModel();

				//}
			}

		}
		

	return result;

}
}

package predictiveclustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import com.hp.hpl.jena.reasoner.Reasoner;

public class Evaluation {
	KnowledgeBase kb;
	OWLIndividual[] allExamples;
	private Random generator;
	private Set<OWLDataProperty> queries;
	Map<OWLIndividual, Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues; // it contains prediction related to the data properties
	Map trainingPredictions= new HashMap();


	public Evaluation() {
		//initialization KB
		this.kb=new KnowledgeBase();
		this.kb.initKB();
		// collecting property values;
		generator= new Random(2);
		dataPropertyValues = kb.getDataPropertyValues();
		// initialization of properties
		Set<OWLOntology> ontologies = kb.getManager().getOntologies();
  	   for (OWLOntology owlOntology : ontologies) {
  		   System.out.println("Ontology: "+owlOntology);
  		   queries = owlOntology.getDataPropertiesInSignature();

		
		allExamples=kb.getAllExamples();
	     for (OWLIndividual ind: allExamples){
	    	 Model<OWLDataPropertyExpression, Double> model= new Model<OWLDataPropertyExpression, Double>(); 
	    	 // access to the pair (property, value)  and conversion from the literal to double
	    	 for (OWLDataProperty query : queries) {
	    		 Set<OWLLiteral> dataPropertyValues2 = ind.getDataPropertyValues(query, owlOntology);
	    		 	// conversion step
	    		 // number of role <=1
	    		 for (OWLLiteral owlLiteral : dataPropertyValues2) {
	    		System.out.println("Literal: "+ owlLiteral);
	    			 //if (owlLiteral.isDouble()){
	    			 double v = owlLiteral.parseDouble();
	    			 System.out.println("-->"+v);
	    			 model.setValues(query, v);
	    			 //}
	    			 //else if (owlLiteral.isFloat()){
		    			// double v = owlLiteral.parseFloat();
		    			 //System.out.println("-->"+v);
		    			 //model.setValues(query, v);
		    			 //}
	    			 //else if (owlLiteral.isInteger()){
		    			// double v = 0.0+(owlLiteral.parseInteger());
		    			 //System.out.println("--->"+v);
		    			 //model.setValues(query, v);
		    		//}
	    		 }
	    		 ModelUtils.setModels(ind, model); // add to set of predictions
	    		 
	    	 } 		 
	    	 
			}
			
	     
	     
	     } 
	    	 
	
		
		
		//System.out.println("No individual"+allExamples.length);
	}

	public  void bootstrap( int nFolds) throws Exception {
		System.out.println(nFolds+"-fold BOOTSTRAP Experiment on ontology: ");	

		//Class<?> classifierClass =ClassLoader.getSystemClassLoader().loadClass(className);



		// main loop on the folds
		int[] ntestExs = new int[nFolds];
		for (int f=0; f< nFolds; f++) {			

			System.out.print("\n\nFold #"+f);
			System.out.println(" **************************************************************************************************");

			Set<OWLIndividual> trainingExsSet = new HashSet<OWLIndividual>();

			Set<OWLIndividual> testingExsSet =  new HashSet<OWLIndividual>();
			for (int r=0; r<allExamples.length; r++) {
				OWLIndividual e = allExamples[generator.nextInt(allExamples.length)];
				trainingExsSet.add(e);
				//System.out.println(r+"--"+ModelUtils.getModels(e));
				trainingPredictions.put(e, ModelUtils.getModels(e));
				
			}
			//
			for (int r=0; r<allExamples.length; r++) {
				if (! trainingExsSet.contains(allExamples[r])) 
					testingExsSet.add(allExamples[r]);
			}			
			
		 System.out.println("Training set: "+ trainingExsSet);
		 System.out.println("Testing set: "+ testingExsSet);
			
		PredictiveClusterInducer inducer= new PredictiveClusterInducer(kb.getReasoner());
		// a different configuration can be made for other tasks like the role prediction
		SortedSet<OWLIndividual> posExs= new TreeSet<OWLIndividual>(trainingExsSet);
		SortedSet<OWLIndividual> negExs= new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> undExs= new TreeSet<OWLIndividual>();
		
		boolean consistent = kb.getReasoner().isConsistent();
		
		//System.out.println(dataPropertyValues);
		
		
		System.out.println(trainingPredictions.values());
		
		
		inducer.induceTree(posExs, negExs, undExs, trainingPredictions.values());
		 
		 
		 //			// splitting in growing and pruning set (70-30 ratio)
			//
			//			Integer[] trainingExs = new Integer[0];
			//			Integer[] testExs = new Integer[0];
			//			trainingExs = trainingExsSet.toArray(trainingExs);
			//			testExs = testingExsSet.toArray(testExs);
			//			//			pruningSet=pruningExsSet.toArray(pruningSet);
			//			ntestExs[f] = testExs.length;
			//			//			System.setOut(new PrintStream("C:/Users/Utente/Documents/biopax.txt"));
			//
			//			// training phase: using all examples but those in the f-th partition
			//			System.out.println("Training is starting...");
			//
			//
			//			
			//					int[][] results= kb.getClassMembershipResult();
			//			
			//		// training
			//
			//			// store model complexity evaluation
			//			
			//
			//			}
			//			System.out.println("End of Training.\n\n");
			//

		} // for f - fold look



	} // bootstrap DLDT induction	

}

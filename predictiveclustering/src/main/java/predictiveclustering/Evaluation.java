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

import predictiveclustering.utils.MathUtils;

import com.hp.hpl.jena.reasoner.Reasoner;

public class Evaluation {
	KnowledgeBase kb;
	OWLIndividual[] allExamples;
	private Random generator;
	private Set<OWLDataProperty> queries;
	Map<OWLIndividual, Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues; // it contains prediction related to the data properties
	Map trainingPredictions= new HashMap();
	Collection<Model> overallModels; 


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
				System.out.println("New Individuals: "+ind);
				Model<OWLDataPropertyExpression, Double> model= new Model<OWLDataPropertyExpression, Double>(); 
				// access to the pair (property, value)  and conversion from the literal to double
				Double[] individual= new Double[queries.size()];
				int n=0; 
				for (OWLDataProperty query : queries) {
					System.out.println("Query: "+ query);
					Set<OWLLiteral> dataPropertyValues2 = ind.getDataPropertyValues(query, owlOntology);
					// conversion step
					// number of role <=1
					for (OWLLiteral owlLiteral : dataPropertyValues2) {
						//System.out.println("Literal: "+ owlLiteral);
						//if (owlLiteral.isDouble()){
						double v = owlLiteral.parseDouble();
						//System.out.println("-->"+v);
						model.setValues(query, v);
						System.out.println("Literal: "+ v);
						System.out.println("Model: "+model);
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
					if (dataPropertyValues2.isEmpty()){
						System.out.println("Literal: "+ null);
					model.setValues(query, null);
					System.out.println("Model: "+model);
					}
					 // add to set of predictions
				} 		 

				
				ModelUtils.setModels(ind, model);

			}
			System.out.println();
			

		} 


		//Collection<Model> values = ModelUtils.getModels().values();
		//Model[] valuesA= values.toArray( new Model[values.size()]);
		overallModels= ModelUtils.getModels().values();
		System.out.println( "OVERALL MODELS");
		for (Model m : overallModels) {
			System.out.println(m);
		}

		//System.out.println("No individual"+allExamples.length);
	}

	public  void bootstrap( int nFolds) throws Exception {
		System.out.println(nFolds+"-fold BOOTSTRAP Experiment on ontology: ");	

		double[] result= new double[nFolds]; //for each fold
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
			
			Double avgModel=0.00;
			int notnull=0;
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


			//System.out.println(trainingPredictions.values());


			PredictiveTree induceTree = inducer.induceTree(posExs, negExs, undExs, overallModels);

			//initialization mapping between individuals and prediction with respect to
			Map<OWLDataPropertyExpression,Map<OWLIndividual,Double>> differences=new HashMap<OWLDataPropertyExpression,Map<OWLIndividual,Double>>();
			for (OWLDataProperty prop : queries) {
				System.out.println("Prop: "+prop);
				Map<OWLIndividual, Double> tobeAdded= new HashMap<OWLIndividual, Double>();
				differences.put(prop, tobeAdded);


			}
			for (OWLIndividual owlIndividual : testingExsSet) {


				Model classifyExample = inducer.classifyExample(owlIndividual, induceTree, kb.getDataFactory());
				Model known = ModelUtils.getModels(owlIndividual);
				System.out.println("Classifying individuals: "+owlIndividual+ " values:  Predicted"+classifyExample+" Original"+known+ "-"+known.getkeys().isEmpty());

				//Map<OWLDataPropertyExpression, Double> tobeAdded= HashMap<OWLDataPropertyExpression, Double>();

				for (OWLDataProperty query : queries) {
					Map v= differences.get(query);
					Double value2 = (Double)known.getValue(query); 
					Double value = (Double)classifyExample.getValue(query);
					System.out.println(" Predicted"+value +"  Original: "+ value2);
					if ((value2!=null)){
						v.put(owlIndividual, value-value2); // memorize the difference between the predicted value and the original one; 
						//System.out.println("To be Added: "+tobeAdded);
					}

				}	
			}
			System.out.println("Differences: "+differences);
			//			

			double num=0;
			//			// compute the average w.r.t. a query

			//System.out.println("print for each query");
			Double[] avgQueries= new Double[queries.size()];
			int i=0;
			for (OWLDataProperty query : queries) {
				Double sum= 0.0;
				Map<OWLIndividual,Double> addend = differences.get(query); // memorize the difference between the predicted value and the original one; 

				//System.out.println(query+" Addend "+addend);
				Set<OWLIndividual> keySet = addend.keySet();
				for (OWLIndividual key : keySet) {
					Double double1 = addend.get(key);
					sum+=(double1*double1);

				}
				if (keySet.size()>0)
					sum/=keySet.size();
				else 
					sum= null;

				avgQueries[i]= sum;
				//System.out.println("Query"+avgQueries[i] +"--"+Math.sqrt(avgQueries[i]));
				//if (addend!=null){
				//sum+= addend*addend; // replace withs the square
				i++;
			}

			
			
			for (int j = 0; j < avgQueries.length; j++) {
				if (avgQueries[j]!=null){
					System.out.println("Avg Model"+avgQueries[j]);
					avgModel+=avgQueries[j];
					notnull++;	
				}

			} 
		
		System.out.println("************* Results for the fold"+f+"***************");
		double a = avgModel /notnull;
		result[f]=Math.sqrt(a);
		System.out.println("aRMME: "+ result[f]);

		}
		
		System.out.println("************* Results of the experiments ***************");
		
		for (int f = 1; f <= nFolds; f++) {
		System.out.println("Fold "+f+":   "+result[f-1]);	
		}
		
		System.out.println("********* OVERALL RESULTS********************");
		System.out.println("Average: "+ MathUtils.avg(result));
		System.out.println("Std Dev."+ MathUtils.stdDeviation(result));
		
		//double a = avgModel /notnull;
		//result[f]=a;
		//System.out.println("aRMME: "+ Math.sqrt(a));

		
		
	} // for f - fold look



} // bootstrap DLDT induction	



package it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWL2Datatype;


import it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering.utils.MathUtils;

public class Evaluation {
	KnowledgeBase kb;
	OWLIndividual[] allExamples;
	private Random generator;
	private Set<OWLDataProperty> queries;
	Map<OWLIndividual, Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues; // it contains prediction related to the data properties
	Map groundtruth= new HashMap();
	Map<OWLIndividual,Model> overallModels; 


	public Evaluation(String file) {
		//initialization KB
		this.kb=new KnowledgeBase();
		kb.setUrlOwlFile(file);
		this.kb.initKB();
		// collecting property values;
		generator= new Random(2);
		queries= new HashSet<OWLDataProperty>();
		dataPropertyValues = kb.getDataPropertyValues();
		// initialization of properties
		Set<OWLOntology> ontologies = kb.getManager().getOntologies();
		for (OWLOntology onto : ontologies) {

			Set<OWLDataProperty> queries2 = onto.getDataPropertiesInSignature();
			for(OWLDataProperty q: queries2) {
				

				

				Iterator<OWLDataRange> iterator = EntitySearcher.getRanges(q, onto).iterator();
				boolean numeric = false;
				while (iterator.hasNext() && !numeric) {
					OWLDataRange r= iterator.next();
					if (r.isOWLDatatype()) {
						numeric=
								r.asOWLDatatype().isFloat()||r.asOWLDatatype().isDouble()||r.asOWLDatatype().isInteger();
						//System.out.println(r.asOWLDatatype()+" " +numeric);
					}
				}
				if (numeric)
					queries.add(q);

			}





			System.out.println("size: "+ queries.size());



			allExamples=kb.getAllExamples();
			// add a way to handle the individuals with null values

			// for all individuals
			for (OWLIndividual ind: allExamples){

				if (ind.isNamed()){
					//				System.out.println("New Individuals: "+ind);
					Model<OWLDataPropertyExpression, Double> model= new Model<OWLDataPropertyExpression, Double>(); 
					//				// access to the pair (property, value)  and conversion from the literal to double
					// for each query
					for (OWLDataProperty d: queries) {
						Set<OWLLiteral> dataPropertyValues2 = kb.getReasoner().getDataPropertyValues(ind.asOWLNamedIndividual(),d);
						//System.out.println("DP values "+ dataPropertyValues2.size());
						// parse the literal

						if (dataPropertyValues2.isEmpty())
							model.setValues(d, null);
						for(OWLLiteral l:dataPropertyValues2) {
							if (l.isDouble()) {
								double v = l.parseDouble();
								//System.out.println("-->"+v);
								model.setValues(d, v);
							}else
								if (l.isFloat()) {
									float v = l.parseFloat();
									//System.out.println("-->"+v);
									model.setValues(d, (double)v);
								}else
									if (l.isInteger()) {
										float v = l.parseInteger();
										//System.out.println("-->"+v);
										model.setValues(d, (double)v);
									}



						}



					}
					//System.out.println(ind+"  <"+ model+">");
					ModelUtils.setModels(ind, model);
					// store the n-pla <ind, ((p1,v1), ..., (pn,vn))

				}




			}	

			//System.out.println(ModelUtils.getModels());
			Collection<Model> modelvalues= ModelUtils.getModels().values();
			//System.out.println(modelvalues);


			for (OWLDataProperty p: queries) {
				double avg=0;
				double n=0;
				for (Model m: modelvalues) {

					if (m.getValue(p)!=null) {
						System.out.println(m.getValue(p));
						avg+=(Double)m.getValue(p);
						n++;
					}
				}
				avg= n==0?0:avg/n;
				System.out.println("Avg:"+p+"  "+ avg);

				for (OWLIndividual ind: allExamples){
					Model<OWLDataPropertyExpression, Double> models = ModelUtils.getModels(ind);
					models.setValues(p, avg); // replace with average value
				}





			}


		}
		overallModels=ModelUtils.getModels();

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
				Model<Object, Object> models = ModelUtils.getModels(e);
				System.out.println("-->"+models);
				groundtruth.put(e, models);
				// TODO replace the null values for all properties with the average values

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
			//
			boolean consistent = kb.getReasoner().isConsistent();

			//System.out.println(dataPropertyValues);


			//System.out.println(trainingPredictions.values());


			PredictiveTree induceTree = inducer.induceTree(posExs, negExs, undExs, overallModels, queries);

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



package it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import openllet.owlapi.*;
import it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering.utils.Split;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;


//import evaluation.Parameters;
/**
 * The heuristics for inducing the predictive clustering trees
 * @author Giuseppe Rizzo
 *
 */
public class TreeInductionHeuristics {

	private OWLReasoner reasoner;
	private OWLDataFactory dataFactory= new OWLDataFactoryImpl();
	private static Logger logger= LoggerFactory.getLogger(TreeInductionHeuristics.class);

	protected static final int UNCERTAIN_INSTANCE_CHECK_UNC = 8;

	protected static final int NEGATIVE_INSTANCE_CHECK_UNC = 7;

	protected static final int POSITIVE_INSTANCE_CHECK_UNC = 6;

	protected static final int UNCERTAIN_INSTANCE_CHECK_FALSE = 5;

	protected static final int NEGATIVE_INSTANCE_CHECK_FALSE = 4;

	protected static final int POSITIVE_INSTANCE_CHECK_FALSE = 3;

	protected static final int UNCERTAIN_INSTANCE_CHECK_TRUE = 2;

	protected static final int NEGATIVE_INSTANCE_CHECK_TRUE = 1;

	protected static final int POSITIVE_INSTANCE_CHECK_TRUE = 0;

	public TreeInductionHeuristics() {

	}






	public OWLReasoner getReasoner() {
		return reasoner;
	}



	public void setReasoner(OWLReasoner reasoner2) {
		this.reasoner = reasoner2;
		//this.problem=problem; //learning problem 	
	}


	public void init(){

	}


	public OWLClassExpression selectBestConcept(OWLClassExpression[] concepts, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs, double prPos, double prNeg) {


		int[] counts;

		int bestConceptIndex = 0;

		counts = getSplitCounts(concepts[0], posExs, negExs, undExs);
		//logger.debug("%4s\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t p:%d n:%d u:%d\t ", 
		//	"#"+0, counts[0], counts[1], counts[2], counts[3], counts[4], counts[5], counts[6], counts[7], counts[8]);
		logger.debug("#"+ 0+"  "+concepts[0]+"\t p:"+counts[0]+"n:"+counts[1]+"u:"+counts[2] +"\t p:"+counts[3] +" n:"+counts[4] +" u:"+ counts[5]+"\t p:"+counts[6] +" n:"+counts[7] +" u:"+counts[8] +"\t \n");
		double bestGain = gain(counts, prPos, prNeg);

		//System.out.printf("%+10e\n",bestGain);

		//System.out.println(concepts[0]);

		for (int c=1; c<concepts.length; c++) {

			counts = getSplitCounts(concepts[c], posExs, negExs, undExs);
			logger.debug("#"+c+"   "+concepts[c]+"   p: "+counts[0]+"n:"+counts[1]+"u:"+counts[2] +"\t p:"+counts[3] +" n:"+counts[4] +" u:"+ counts[5]+"\t p:"+counts[6] +" n:"+counts[7] +" u:"+counts[8] +"\t \n");

			double thisGain = gain(counts, prPos, prNeg);
			//logger.debug(thisGain+"\n");
			//logger.debug(concepts[c].toString());
			if(thisGain < bestGain) {
				bestConceptIndex = c;
				bestGain = thisGain;
			}
		}

		System.out.printf("best gain: "+ bestGain+" \t split "+ concepts[bestConceptIndex]+"\n");
		return concepts[bestConceptIndex];
	}

	
	/* Gain in terms of gini?*/
	private double gain(int[] counts, double prPos, double prNeg) {

		double sizeT = counts[POSITIVE_INSTANCE_CHECK_TRUE] + counts[POSITIVE_INSTANCE_CHECK_FALSE];
		double sizeF = counts[NEGATIVE_INSTANCE_CHECK_TRUE] + counts[NEGATIVE_INSTANCE_CHECK_FALSE];
		double sizeU = counts[POSITIVE_INSTANCE_CHECK_UNC] + counts[NEGATIVE_INSTANCE_CHECK_UNC ] + counts[UNCERTAIN_INSTANCE_CHECK_TRUE] + counts[UNCERTAIN_INSTANCE_CHECK_FALSE];
		double sum = sizeT+sizeF+sizeU; 

		double startImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_TRUE]+counts[POSITIVE_INSTANCE_CHECK_FALSE], counts[NEGATIVE_INSTANCE_CHECK_TRUE]+counts[NEGATIVE_INSTANCE_CHECK_FALSE], prPos, prNeg);
		double tImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_TRUE], counts[NEGATIVE_INSTANCE_CHECK_TRUE], prPos, prNeg);
		double fImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_FALSE], counts[NEGATIVE_INSTANCE_CHECK_FALSE], prPos, prNeg);
		double uImpurity = gini(counts[POSITIVE_INSTANCE_CHECK_UNC]+counts[UNCERTAIN_INSTANCE_CHECK_TRUE], counts[NEGATIVE_INSTANCE_CHECK_UNC]+counts[UNCERTAIN_INSTANCE_CHECK_FALSE] , prPos, prNeg);		

		return (startImpurity - (sizeT/sum)*tImpurity - (sizeF/sum)*fImpurity - -(sizeU/sum)*uImpurity);
	}


	static double gini(double numPos, double numNeg, double prPos,
			double prNeg) {

		double sum = numPos+numNeg;
		int M=3;

		double p1 = (numPos*M*prPos)/(sum+M); //m-estimate probability
		double p2 = (numNeg* M*prNeg)/(sum+M);

		return (1.0-p1*p1-p2*p2);
		//		return (1-Math.pow(p1,2)-Math.pow(p2,2))/2;
	}


	private int[] getSplitCounts(OWLClassExpression concept, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs) {

		int[] counts = new int[9];
		SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negExsT = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> undExsT = new TreeSet<OWLIndividual>();

		SortedSet<OWLIndividual> posExsF = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negExsF = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> undExsF = new TreeSet<OWLIndividual>();

		SortedSet<OWLIndividual> posExsU = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negExsU = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> undExsU = new TreeSet<OWLIndividual>();

		Split.splitGroup(concept,dataFactory, reasoner,posExs,posExsT,posExsF,posExsU);
		Split.splitGroup(concept,dataFactory, reasoner,negExs,negExsT,negExsF,negExsU);
		Split.splitGroup(concept,dataFactory, reasoner, undExs,undExsT,undExsF,undExsU);	

		counts[0] = posExsT.size(); 
		counts[1] = negExsT.size(); 
		counts[2] = undExsT.size(); 
		counts[3] = posExsF.size(); 
		counts[4] = negExsF.size();
		counts[5] = undExsF.size();
		counts[6] = posExsU.size(); 
		counts[7] = negExsU.size();
		counts[8] = undExsU.size();
		//		for(int i=0; i<counts.length;i++)
		//			System.out.println(counts[i]);

		return counts;

	}

	protected void split(OWLClassExpression concept, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs,
			SortedSet<OWLIndividual> posExsT, SortedSet<OWLIndividual> negExsT, SortedSet<OWLIndividual> undExsT, SortedSet<OWLIndividual> posExsF, SortedSet<OWLIndividual> negExsF,
			SortedSet<OWLIndividual> undExsF) {

		SortedSet<OWLIndividual> posExsU = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negExsU = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> undExsU = new TreeSet<OWLIndividual>();

		Split.splitGroup(concept,dataFactory, reasoner,posExs,posExsT,posExsF,posExsU);
		Split.splitGroup(concept,dataFactory, reasoner,negExs,negExsT,negExsF,negExsU);
		Split.splitGroup(concept,dataFactory, reasoner, undExs,undExsT,undExsF,undExsU);	

	}





 /**
  * Select the best concept according to the minimization of the RMSE
  * @param ref
  * @param posExs
  * @param negExs
  * @param undExs
  * @param models
  * @param i
  * @param j
 * @param queries 
  * @return
  */
	public OWLClassExpression selectBestConceptRMSE(OWLClassExpression[] ref, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, ArrayList<Model> models, int i, int j, Set<OWLDataProperty> queries) {
		// TODO Auto-generated method stub
		
		int bestConceptIndex = 0;
		double bestgain=Double.MIN_VALUE;
		System.out.println("Size "+ref.length);
		logger.info(("Model: "+ models.size()));
		double initialRmse= rmse(posExs, negExs,undExs, models, queries);
		
		for (int k=1; k< ref.length; k++){
			SortedSet<OWLIndividual> negExsT = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> undExsT = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> posExsT = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> posExsF= new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> undExsF = new TreeSet<OWLIndividual>();
			SortedSet<OWLIndividual> negExsF = new TreeSet<OWLIndividual>();
			// find a split w.r.t. the new refinement
			split(ref[k], posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
			 double positiveRmse= ((posExsT.size())+ (negExsT.size())+(undExsT.size())) *(rmse(posExsT, negExsT,undExsT, models, queries))/ ((posExs.size())+ (negExs.size())+(undExs.size()));
			 double negativeRmse= ((posExsF.size())+ (negExsF.size())+(undExsF.size())) *(rmse(posExsF, negExsF,undExsF, models, queries))/ ((posExs.size())+ (negExs.size())+(undExs.size()));
			
			 double gain= initialRmse- positiveRmse-negativeRmse;  // gain 
			 
			 if (gain>bestgain){
				
				 bestgain= gain;
				 bestConceptIndex= k;
			 }
			
			
		}
		
		 return ref[bestConceptIndex]; // return the best concept description
		
		
		
	}





/**
 * Compute the RMSE for the set of positive, negative and possibly uncertain instances
 * @param posExs
 * @param negExs
 * @param undExs
 * @param models
 * @param queries 
 * @return
 */
	private double rmse(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs,
			SortedSet<OWLIndividual> undExs, ArrayList<Model> models, Set<OWLDataProperty> queries) {
		// TODO Auto-generated method stub
		ArrayList<Model> m= new ArrayList<Model>(); // initialize the model
		for (OWLIndividual pE : posExs) {
			Model  models2 = ModelUtils.getModels(pE); // get the model for the current training individual
			m.add(models2); // a model is composed by pairs (prop, value);
		}
		//System.out.println("M +"+ m);
		
		// after the models have been collected, standardize and compute the RMSE
		HashMap<OWLDataProperty,Double> v= new HashMap<OWLDataProperty, Double>();
		
		double msePos= computeMSE(m, queries); 
		m= new ArrayList<Model>();
		
		// do the same for negative example
		for (OWLIndividual pE : negExs) {
			Model  models2 = ModelUtils.getModels(pE); // get the model for the current training individual
			m.add(models2); // a model is composed by pairs (prop, value);
		}
		logger.info(m.toString());
		double mseNeg= computeMSE(m, queries);
		
		// do the same for uncertain example
		m= new ArrayList<Model>();
				for (OWLIndividual pE : undExs) {
					Model  models2 = ModelUtils.getModels(pE); // get the model for the current training individual
					m.add(models2); // a model is composed by pairs (prop, value);
				}
		  
		
		double mseUnd= computeMSE(m, queries);
		 return msePos+mseNeg+mseUnd;
		// do the same for positive and uncertain-membership instances
		
		
		//return 0;
	}





 /**
  * Compute the MSE for each data property
  * @param m, the models
  * @param queries
  * @return
  */
	private double computeMSE(ArrayList<Model> m, Set<OWLDataProperty> queries) {
		double mse=0.0d; //mse error
		Set<OWLDataProperty> keySet = queries; // get the key set for obtaining query properties
		for (OWLDataProperty owlDataProperty : keySet) {
			ArrayList<Double> values= new ArrayList<Double>(); // collect the values for finding average, max and min in order to perform standardization
			ArrayList<Double> standardizedValues= new ArrayList<Double>();
			Double max= Double.MIN_VALUE;
			Double min= Double.MAX_VALUE;
			for (Model model : m) {
				//System.out.println("looooooooooool"+model.toString());
				Double value = (Double)model.getValue(owlDataProperty);
				values.add(value); // add the value to the list
				if (value < min)
					min= value;
				if (value > max)
					max= value;
			}
			
			//Double max = Collections.max(values); // max and min for standardization
			//Double min= Collections.min(values);
			Double avg= 0.0d;
			for (Double double1 : values) {
				double stddouble= (double1 - min)/(max-min); //standardization  min max 
				standardizedValues.add(stddouble); // add standardized value
				avg+=(stddouble/values.size()); 
			} 
		     // compute the normalized rmse for the value rmse
			
			for (Double double1 : standardizedValues) {
				mse+= ((double1-avg)*(double1-avg)); // 
			}
			 mse/= standardizedValues.size();
			
		}
		return mse;
	}

	
	
	



}

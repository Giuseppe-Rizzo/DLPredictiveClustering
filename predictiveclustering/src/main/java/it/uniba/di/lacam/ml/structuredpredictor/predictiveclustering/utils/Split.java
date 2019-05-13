package it.uniba.di.lacam.ml.structuredpredictor.predictiveclustering.utils;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;



import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.hp.hpl.jena.reasoner.Reasoner;


/**
 * A class for splitting sets of individuals
 * @author Giuseppe Rizzo
 *
 */
public class Split {

	public static void split(OWLClassExpression concept, OWLDataFactory  df, PelletReasoner  reasoner, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs,
			SortedSet<OWLIndividual> posExsT, SortedSet<OWLIndividual> negExsT, SortedSet<OWLIndividual> undExsT, SortedSet<OWLIndividual> posExsF, SortedSet<OWLIndividual> negExsF,
			SortedSet<OWLIndividual> undExsF) {

		SortedSet<OWLIndividual> posExsU = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> negExsU = new TreeSet<OWLIndividual>();
		SortedSet<OWLIndividual> undExsU = new TreeSet<OWLIndividual>();

		splitGroup(concept,df, reasoner, posExs,posExsT,posExsF,posExsU);
		splitGroup(concept,df, reasoner, negExs,negExsT,negExsF,negExsU);
		splitGroup(concept,df, reasoner, undExs,undExsT,undExsF,undExsU);	

	}

	public static void splitGroup(OWLClassExpression concept, OWLDataFactory  dataFactory, PelletReasoner  reasoner, SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> posExsT,
			SortedSet<OWLIndividual> posExsF, SortedSet<OWLIndividual> posExsU) {
		OWLClassExpression negConcept = dataFactory.getOWLObjectComplementOf(concept);

		for ( OWLIndividual individual :posExs ){//int e=0; e<nodeExamples.size(); e++) {
			
//			int exIndex = nodeExamples.get(e);
			OWLClassAssertionAxiom owlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(concept, individual);
			OWLClassAssertionAxiom negOwlClassAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(negConcept, individual);
			if (reasoner.isEntailed(owlClassAssertionAxiom))
				posExsT.add(individual);
			else  //if (reasoner.isEntailed(negOwlClassAssertionAxiom))
				posExsF.add(individual);
			//else
				//posExsU.add(individual);		
		}	

	}
	
	public static void splitting(OWLDataFactory df, PelletReasoner reasoner, OWLIndividual[] trainingExs, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs, SortedSet<OWLIndividual> undExs, OWLClassExpression classToDescribe2, boolean binaryClassification) {

		for (int e=0; e<trainingExs.length; e++){
			
			OWLClassAssertionAxiom owlClassAssertionAxiom = df.getOWLClassAssertionAxiom(classToDescribe2, trainingExs[e]);
			if (reasoner.isEntailed(owlClassAssertionAxiom))
				posExs.add(trainingExs[e]);
			else if (!binaryClassification){
				OWLObjectComplementOf owlObjectComplementOf = df.getOWLObjectComplementOf(classToDescribe2);
				OWLClassAssertionAxiom negOwlClassAssertionAxiom = df.getOWLClassAssertionAxiom(owlObjectComplementOf, trainingExs[e]);
				if (reasoner.isEntailed(negOwlClassAssertionAxiom))
					negExs.add(trainingExs[e]);
				else
					undExs.add(trainingExs[e]);
				
			}
			else
				negExs.add(trainingExs[e]);
				
			
		}
		
	}


	
	
	
	
	
	
	
	
	
	
}

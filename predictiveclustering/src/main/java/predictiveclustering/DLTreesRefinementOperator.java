package predictiveclustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;







import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.hp.hpl.jena.reasoner.Reasoner;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
//import evaluation.Parameters;
//import knowledgeBasesHandler.KnowledgeBase;

/**
 * The refinement operator
 * @author Giuseppe Rizzo
 *
 */

public class DLTreesRefinementOperator{

	private static Logger logger= LoggerFactory.getLogger(DLTreesRefinementOperator.class);

	//KnowledgeBase kb;
	private static final double d = 0.5;
	private ArrayList<OWLClass> allConcepts;
	private ArrayList<OWLObjectProperty> allRoles;
	private Random generator;
	
	//private OWLClassExpression expressio
	private PelletReasoner r;
	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	private int beam;

	private Set<OWLClassExpression> refinements;

	//	
	//
	//	
	
	public DLTreesRefinementOperator() {

		generator= new Random(2);
	}


	public DLTreesRefinementOperator (PelletReasoner reasoner, int beam) {
		super();
		// TODO Auto-generated constructor stub
		r=reasoner;
		//System.out.println("is Reasoner null? "+reasoner==null);
		allConcepts=new ArrayList<OWLClass>();
		Set<OWLOntology> ontologies = reasoner.getManager().getOntologies();
		for (OWLOntology owlOntology : ontologies) {
			
			allConcepts.addAll(owlOntology.getClassesInSignature());
		}
		
		//System.out.println("all+ Concepts: "+allConcepts.size());
		allRoles= new ArrayList<OWLObjectProperty>();
		for (OWLOntology owlOntology : ontologies) {
			
			allRoles.addAll(owlOntology.getObjectPropertiesInSignature());
			
		}
		
		this.beam=beam; // set the maximum number of candidates that can be generated
		
		generator= new Random(2);

	}








	public ArrayList<OWLClass> getAllConcepts() {
		return allConcepts;
	}


	public void setAllConcepts(ArrayList<OWLClass> allConcepts) {
		this.allConcepts = allConcepts;
	}


	public ArrayList<OWLObjectProperty> getAllRoles() {
		return allRoles;
	}


	public void setAllRoles(ArrayList<OWLObjectProperty> allRoles) {
		this.allRoles = allRoles;
	}


	/**
	 * Random concept generation
	 * @return 
	 */
	public OWLClassExpression getRandomConcept() {

		OWLClassExpression newConcept = null;
		
		//System.out.println("*********"+ generator);
			// case A:  ALC and more expressive ontologies
			do {

				//System.out.println("No of classes: "+allConcepts.isEmpty());
				newConcept = allConcepts.get(generator.nextInt(allConcepts.size()));
				if (generator.nextDouble() < d) {
					OWLClassExpression newConceptBase =     getRandomConcept();
					if (generator.nextDouble() < d) {
						if (allRoles.size()>0){ // for tackling the absence of roles
						if (generator.nextDouble() <d) { // new role restriction
							OWLObjectProperty role = allRoles.get(generator.nextInt(allRoles.size()));
							//					OWLDescription roleRange = (OWLDescription) role.getRange
							if (generator.nextDouble() < d)
								newConcept = dataFactory.getOWLObjectAllValuesFrom(role, newConceptBase);
							else
								newConcept = dataFactory.getOWLObjectSomeValuesFrom(role, newConceptBase);
						}
						else					
							newConcept = dataFactory.getOWLObjectComplementOf(newConceptBase);
						}
						else					
								newConcept = dataFactory.getOWLObjectComplementOf(newConceptBase);
					}
				}

			} while (!(r.isSatisfiable(newConcept))); //not only a satisfiable concept but also with some instances in the Abox

		
				//System.out.println("*********");
		return newConcept;				
	}

	public SortedSet<OWLClassExpression>generateNewConcepts(SortedSet<OWLIndividual> posExs, SortedSet<OWLIndividual> negExs, boolean seed) {

		logger.info("Generating node concepts ");
		TreeSet<OWLClassExpression> rConcepts = new TreeSet<OWLClassExpression>();
		System.out.println("Generating node concepts ");
		OWLClassExpression newConcept=null;
		boolean emptyIntersection;
		for (int c=0; c<beam; c++) {

			do {
				emptyIntersection =  false;
				//System.out.println("Before the try");
				//					try{
				System.out.println("---------->");
				newConcept = getRandomConcept();
				System.out.println(c+"-  New Concept: "+newConcept);
				NodeSet<OWLNamedIndividual> individuals;

				individuals = (r.getInstances(newConcept, false));
				Iterator<OWLNamedIndividual> instIterator = individuals.getFlattened().iterator();
				while (emptyIntersection && instIterator.hasNext()) {
					Node<OWLNamedIndividual> nextInd = (Node<OWLNamedIndividual>) instIterator.next();
					int index = -1;
					ArrayList<OWLIndividual> individuals2 = new ArrayList<OWLIndividual>(individuals.getFlattened());
					for (int i=0; index<0 && i<individuals2.size(); ++i)
						if (nextInd.equals(individuals2)) index = i;
					if (posExs.contains(index))
						emptyIntersection = false;
					else if (negExs.contains(index))
						emptyIntersection = false;
				}

			} while (emptyIntersection);
			//if (newConcept !=null){
			System.out.println(newConcept==null);
			rConcepts.add(newConcept);
			//}

		}
		System.out.println();

		logger.debug(""+rConcepts.size());
		return rConcepts;
	}







	//	
	public void setReasoner(PelletReasoner reasoner) {
		// TODO Auto-generated method stub
		this.r= reasoner;
		allConcepts=new ArrayList<OWLClass>();
		Set<OWLOntology> ontologies = reasoner.getManager().getOntologies();
		for (OWLOntology owlOntology : ontologies) {
			
			allConcepts.addAll(owlOntology.getClassesInSignature());
		}
		
		//System.out.println("all+ Concepts: "+allConcepts.size());
		allRoles= new ArrayList<OWLObjectProperty>();
		for (OWLOntology owlOntology : ontologies) {
			
			allRoles.addAll(owlOntology.getObjectPropertiesInSignature());
			
		}

	}







	public Set<OWLClassExpression> refine(OWLClassExpression definition, SortedSet<OWLIndividual> posExs,
			SortedSet<OWLIndividual> negExs) {
		
			return (generateNewConcepts(posExs, negExs, false));
		
	}





	public void setBeam(int i) {
		// TODO Auto-generated method stub
		beam=i;

	}





	public int getBeam() {
		return beam;
	}






}

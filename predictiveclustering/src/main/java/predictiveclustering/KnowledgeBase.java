package predictiveclustering;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;

public class KnowledgeBase {

	private OWLOntologyManager manager;
	private String urlOwlFile = "C:/Users/Utente/Desktop/MDM0.732.owl";
	private OWLDataFactory dataFactory;
	private PelletReasoner reasoner;
	private OWLClass[] allConcepts;
	private OWLObjectProperty[] allRoles;
	private OWLIndividual[] allExamples;
	private Map<OWLIndividual,Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues = new HashMap<OWLIndividual, Map<OWLDataPropertyExpression,Set<OWLLiteral>>>();



	public OWLOntologyManager getManager() {
		return manager;
	}



	public void setManager(OWLOntologyManager manager) {
		this.manager = manager;
	}



	public String getUrlOwlFile() {
		return urlOwlFile;
	}



	public void setUrlOwlFile(String urlOwlFile) {
		this.urlOwlFile = urlOwlFile;
	}



	public OWLDataFactory getDataFactory() {
		return dataFactory;
	}



	public void setDataFactory(OWLDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}



	public PelletReasoner getReasoner() {
		return reasoner;
	}



	public void setReasoner(PelletReasoner reasoner) {
		this.reasoner = reasoner;
	}



	public OWLClass[] getAllConcepts() {
		return allConcepts;
	}



	public void setAllConcepts(OWLClass[] allConcepts) {
		this.allConcepts = allConcepts;
	}



	public OWLObjectProperty[] getAllRoles() {
		return allRoles;
	}



	public void setAllRoles(OWLObjectProperty[] allRoles) {
		this.allRoles = allRoles;
	}



	public OWLIndividual[] getAllExamples() {
		return allExamples;
	}



	public void setAllExamples(OWLIndividual[] allExamples) {
		this.allExamples = allExamples;
	}



	public Map<OWLIndividual,Map<OWLDataPropertyExpression, Set<OWLLiteral>>> getDataPropertyValues() {
		return dataPropertyValues;
	}



	public void setDataPropertyValues(Map<OWLIndividual,Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues) {
		this.dataPropertyValues = dataPropertyValues;
	}



	public KnowledgeBase() {
		// TODO Auto-generated constructor stub
	}

	
	
	public   OWLOntology initKB() {

		manager = OWLManager.createOWLOntologyManager();        

		// read the file
		URI fileURI = URI.create(urlOwlFile);
		dataFactory = manager.getOWLDataFactory();
		OWLOntology ontology = null;
		//IRI.create(fileURI);
		try {
			//SimpleIRIMapper mapper = new SimpleIRIMapper(IRI.create("http://semantic-mediawiki.org/swivt/1.0"),IRI.create("file:///C:/Users/Utente/Documents/Dataset/Dottorato/10.owl"));
			//			manager.addURIMapper();
			//manager.addIRIMapper(mapper);
			ontology = manager.loadOntologyFromOntologyDocument(new File(urlOwlFile));
			//			OWLImportsDeclaration importDeclaraton = dataFactory.getOWLImportsDeclarationAxiom(ontology, URI.create("file:///C:/Users/Utente/Documents/Dataset/10.owl"));
			//		   manager.makeLoadImportRequest(importDeclaraton);


		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}


		reasoner = new PelletReasoner(ontology, BufferingMode.NON_BUFFERING);
		

//		reasoner.getKB().realize();
		System.out.println("\nClasses\n-------");
		Set<OWLClass> classList = ontology.getClassesInSignature();
		allConcepts = new OWLClass[classList.size()];
		int c=0;
		for(OWLClass cls : classList) {
			if (!cls.isOWLNothing() && !cls.isAnonymous()) {
				allConcepts[c++] = cls;
				System.out.println(c +" - "+cls);
			}	        		
		}
		System.out.println("---------------------------- "+c);

		System.out.println("\nProperties\n-------");
		Set<OWLObjectProperty> propList = ontology.getObjectPropertiesInSignature();
		allRoles = new OWLObjectProperty[propList.size()];
		int op=0;
		for(OWLObjectProperty prop : propList) {
			if (!prop.isAnonymous()) {
				allRoles[op++] = prop;
				System.out.println(prop);
			}	        		
		}
		System.out.println("---------------------------- "+op);
		
		
		System.out.println("\nIndividuals\n-----------");
		Set<OWLNamedIndividual> indList = ontology.getIndividualsInSignature();
		allExamples = new OWLIndividual[indList.size()];
		int i=0;
		for(OWLNamedIndividual ind : indList) {
			allExamples[i++] = ind;   
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataPropertyValues = ind.getDataPropertyValues(ontology);
			this.dataPropertyValues.put(ind, dataPropertyValues);
			//Collection<Set<OWLLiteral>> values = dataPropertyValues.values();
//			for (Set<OWLLiteral> set : values) {
//				for (OWLLiteral owlLiteral : set) {
//					System.out.println(Double.parseDouble(owlLiteral.getLiteral()));
//				}
//				
//			}
//			System.out.println(values);
//			System.out.println();
		}
		System.out.println("---------------------------- "+i);
		
		
		

		System.out.println("\nKB loaded. \n");	
		return ontology;		

	}

}

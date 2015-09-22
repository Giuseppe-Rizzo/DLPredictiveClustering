package predictiveclustering;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

import jjtraveler.SuccessCounter;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

public class KnowledgeBaseTest {

	@Test
	public void test() {
		KnowledgeBase kb= new KnowledgeBase();
		kb.initKB();
		Map<OWLIndividual,Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues = kb.getDataPropertyValues();
	
		System.out.println(dataPropertyValues);
	}

}

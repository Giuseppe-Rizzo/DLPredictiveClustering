package predictiveclustering;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLIndividual;

public class ModelTest {

	@Test
	public void test() {
Model<OWLIndividual,Double> model= new Model<OWLIndividual, Double>(); 
		

		//boolean m=(== );
		System.out.println("---Z"+ (model.getClass()== Model.class));
		Assert.assertEquals(model.getClass(), Model.class);
	}

}

package predictiveclustering;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

public class PredictiveTreeTest {

	@Test
	public void test() {
		Model<Integer> m= new Model<Integer>();
		PredictiveTree<String,Model<Integer>> tree= new PredictiveTree<String, Model<Integer>>();
		Assert.assertEquals((tree==null), false);
		tree.setRoot("Ciao", m);
		Assert.assertEquals(tree.getRoot().getT(), "Ciao");
		
		
		
	}

}

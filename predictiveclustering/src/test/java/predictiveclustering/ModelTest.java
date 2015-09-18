package predictiveclustering;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

	@Test
	public void test() {
	
		
		Model<Integer> l1 = new Model<Integer>();
		Assert.assertEquals(new ArrayList<Integer>(), l1.getValues());
		ArrayList<Integer> v= new ArrayList<Integer>();
		v.add(1);
		v.add(2);
		v.add(3);
		l1.setValues(v);
		Assert.assertEquals(v,l1.getValues());		
		
	}

}

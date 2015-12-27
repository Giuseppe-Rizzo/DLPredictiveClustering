package predictiveclustering;

import static org.junit.Assert.*;

import org.junit.Test;

public class EvaluationTest {

	@Test
	public void test() throws Exception {
		Evaluation eval= new Evaluation();
		eval.bootstrap(2);
	}

}

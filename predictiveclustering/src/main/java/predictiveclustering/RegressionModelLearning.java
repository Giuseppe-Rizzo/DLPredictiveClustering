package predictiveclustering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

public class RegressionModelLearning {

	HashMap<OWLIndividual,Model<OWLDataPropertyExpression, Double>> mapping; //= new HashMap<OWLIndividual, Model<Double>>(); 


	public RegressionModelLearning() {
		mapping= new HashMap<OWLIndividual, Model<OWLDataPropertyExpression,Double>>();
		//initialization	
	}




	public void associatesLocalModels(KnowledgeBase kb, OWLDataPropertyExpression...queries){
		Map<OWLIndividual,Map<OWLDataPropertyExpression, Set<OWLLiteral>>> dataPropertyValues = kb.getDataPropertyValues();

		Set<OWLIndividual> keySet = dataPropertyValues.keySet();

		for (OWLIndividual owlIndividual : keySet) {
			Model<OWLDataPropertyExpression,Double> model=  new Model<OWLDataPropertyExpression,Double>();
			Map<OWLDataPropertyExpression, Set<OWLLiteral>> map = dataPropertyValues.get(owlIndividual);
			for (OWLDataPropertyExpression owlDataPropertyExpression : queries) {
				Set<OWLLiteral> values = map.get(owlDataPropertyExpression);
				for (OWLLiteral owlLiteral : values) {
					double literal=0;
					if (owlLiteral.isDouble()||owlLiteral.isFloat()||owlLiteral.isInteger()){

						literal= owlLiteral.parseDouble();

					}		

				}
			}
		}  



	}



}

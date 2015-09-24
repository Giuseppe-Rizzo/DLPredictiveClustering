package predictiveclustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public class ModelUtils<K,E> {
	//<owl individual, <property, filler>
	private static Map<OWLIndividual, Model> models = new HashMap<OWLIndividual, Model>(); // it contains associated to the individuals
	
	public static <K,E> Model<K,E> getModels(OWLIndividual ind){
		
		return models.get(ind);
		
	}
	
	public static <K,E> void setModels(OWLIndividual ind, Model<K,E> model){
		
		models.put(ind, model); // add the pair (key value)
		
	}
	
	/**
	 * Combination for multi-target regression
	 * @param m1 the list of models that will be combined
	 * @return
	 */
	public static Model<OWLDataPropertyExpression, Double> combineModels(Model<OWLDataPropertyExpression,Double>... m1){
		Model<OWLDataPropertyExpression,Double> result= new Model<OWLDataPropertyExpression,Double>();
		Set<OWLDataPropertyExpression> getkeys = m1[0].getkeys();
		for (OWLDataPropertyExpression owlDataPropertyExpression : getkeys) {
			int n=0;
			double avg=0;
			for (int i = 0; i < m1.length; i++) {
				Double value = m1[i].getValue(owlDataPropertyExpression);
				if (value!=null){ // the individual with null value are not considered in the computation of the model
					avg+=value;
					n++;
					
				}
			}
		
		
			avg/=n;
			 result.setValues(owlDataPropertyExpression,avg);
		}
			 
		 return result;
		
		
	} 

}

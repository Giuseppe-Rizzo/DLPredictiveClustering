package predictiveclustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLIndividual;

public class ModelUtils<E> {
	
	private static Map<OWLIndividual, Model> models = new HashMap<OWLIndividual, Model>(); // it contains associated to the individuals
	
	public static <E> Model<E> getModels(OWLIndividual ind){
		
		return models.get(ind);
		
	}
	
	public static <E> void setModels(OWLIndividual ind, Model<E> model){
		
		models.put(ind, model); // add the pair (key value)
		
	}
	
	/**
	 * Combination for multi-target regression
	 * @param m1 the list of models that will be combined
	 * @return
	 */
	public static Model<Double> combineModels(Model<Double>... m1){
		ArrayList<Double> values = (ArrayList<Double>) m1[0].getValues().clone();
		for (int j= 1; j<m1.length; j++){
			ArrayList<Double> currentValues=(ArrayList<Double>) m1[j].getValues();
			for (int i = 0; i < values.size(); i++) {
				values.add(values.get(i)+currentValues.get(i));
			}
		}
		
		// average computation
		for (int j= 0; j<m1.length; j++){
			values.set(j, (values.get(j)/m1.length));
			
		}
		
		 Model<Double> result= new Model<Double>();
		 result.setValues(values);
		 
		 return result;
		
		
	} 
	

	
	
	
}

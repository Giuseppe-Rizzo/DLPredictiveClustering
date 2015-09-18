package predictiveclustering;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Model<E> {
	 private List<E> values; 
	

	public Model() {
		// TODO Auto-generated constructor stub
		values= new ArrayList<E>();
	}


	public ArrayList<E> getValues() {
		return (ArrayList<E>) values;
	}


	public void setValues(ArrayList<E> values) {
		this.values = values;
	}
	
	
	

	

}

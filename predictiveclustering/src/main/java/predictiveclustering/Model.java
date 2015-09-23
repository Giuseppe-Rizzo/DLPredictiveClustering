package predictiveclustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Model<P,E> {
 private HashMap<P,E> mapping;
	
	public Model() {
		// TODO Auto-generated constructor stub
		mapping= new HashMap<P,E>();
	}

	public Set<P> getkeys() {
		return mapping.keySet();
	}
	
	public E getValue(P key){
		
	return	mapping.get(key);
	}


	public void setValues(P key, E value) {
		mapping.put(key, value);
	}	
	
	public String toString(){
		
		return mapping.toString();
	}
	
	

	

}

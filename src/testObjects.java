import java.util.LinkedList;

import utilities.Place;

public class testObjects {

	public static final LinkedList<Place> PLACELIST1(){
		LinkedList<Place> list = new LinkedList<Place>();
		list.add(HANNOVER());
		return list;
	}
	
	public static final LinkedList<Place> PLACELIST2(){
		LinkedList<Place> list = new LinkedList<Place>();
		list.add(GOETTINGEN());
		return list;
	}
	
	public static final LinkedList<Place> PLACELIST_3(){
		LinkedList<Place> list = new LinkedList<Place>();
		list.add(HANNOVER());
		list.add(BERLIN());
		list.add(PADDERBORN());
		return list;
	}
	
	public static final Place HANNOVER(){
		Place place = new Place();
		
		place.setCity("Hannover");
		place.setCountry("Germany");
		place.setId("HAN");
		
		return place;
	}
	
	public static final Place GOETTINGEN(){
		Place place = new Place();
		
		place.setCity("Göttingen");
		place.setCountry("Germany");
		place.setId("GÖT");
		
		return place;
	}
	
	public static final Place BERLIN(){
		Place place = new Place();
		
		place.setCity("Berlin");
		place.setCountry("Germany");
		place.setId("BER");
		
		return place;
	}
	
	public static final Place PADDERBORN(){
		Place place = new Place();
		
		place.setCity("Padderborn");
		place.setCountry("Germany");
		place.setId("PAD");
		
		return place;
	}
}

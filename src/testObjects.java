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
	
	public static final LinkedList<Place> PLACELIST_USA(){
		LinkedList<Place> list = new LinkedList<Place>();
		list.add(SEATTLE_1());
		list.add(SEATTLE_2());
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
	
	public static final Place ROSDORF(){
		Place place = new Place();
		
		place.setCity("Rosdorf");
		place.setCountry("Germany");
		place.setId("ROS");
		
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
		
		
		place.setHousenumber("100");
		place.setStreet("Warburger Str.");
		place.setCity("Padderborn");
		place.setCountry("Germany");
		place.setId("PAD");
		
		return place;
	}
	
	
	public static final Place SEATTLE_1(){
		Place place = new Place();
		
		
		place.setHousenumber("400");
		place.setStreet("Pine St");
		place.setCity("Seattle");
		place.setCountry("USA");
		place.setId("SET");
		
		return place;
	}
	
	
	public static final Place SEATTLE_2(){
		Place place = new Place();
		
		
		place.setHousenumber("222");
		place.setStreet("Pike St");
		place.setCity("Seattle");
		place.setCountry("USA");
		place.setId("SET");
		
		return place;
	}
}

package pathCalculation;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jdom2.JDOMException;

import api.utilities.GoogleMaps;
import database.ConnectedHotspots;
import pathCalculation.hotspotPath.HotspotSearch;
import pathCalculation.recursiveBreadthFirst.RecursiveBreadthSearch;
import utilities.Connection;
import utilities.Place;
import utilities.Request;

public class RequestHandler {

	
	public RequestHandler(){
		
	}
	
	@SuppressWarnings("null")
	public LinkedBlockingQueue<Connection> solveRequest(Request request){
		LinkedBlockingQueue<Connection> connectionList = new LinkedBlockingQueue<Connection>();
		
		//If Method is GoogleMapsDistance uses Google Maps Distance Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDistance")){
			GoogleMapsDistance distance = new GoogleMapsDistance();
			connectionList.addAll(distance.getConnectionList(request));
		}
		
		//If Method is GoogleMapsDirecton uses Google Maps Direction Service only
		if(request.getMethode().equalsIgnoreCase("GoogleMapsDirection")){
			GoogleMapsDirection direction = new GoogleMapsDirection();
			connectionList.addAll(direction.getConnectionList(request));
		}
		
		
		if(request.getMethode().equalsIgnoreCase("SkyscannerCacheOnly")){
			try {
				SkyscannerCache skyCache = new SkyscannerCache();
				return skyCache.getConnectionList(request, connectionList);
			} catch (IllegalStateException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		if(request.getMethode().equalsIgnoreCase("eStreamingCacheOnly")){
			try {
				EStreamingCache eStreaming = new EStreamingCache();
				return eStreaming.getConnectionList(request, connectionList);
			} catch (IllegalStateException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		//uses eStreaming
		if(request.getMethode().equalsIgnoreCase("FFC")){
			try {
				FindFirstConnection ffc = new FindFirstConnection();
				return ffc.getConnectionList(request, connectionList);
			} catch (IllegalStateException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}

		if(request.getMethode().equalsIgnoreCase("OutboundConnections")){
			try {
				OutboundConnections outboundConnections = new OutboundConnections();
				return outboundConnections.getConnectionList(request, connectionList);
			} catch (IllegalStateException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		//searches all direct connections in database
		if(request.getMethode().equalsIgnoreCase("Database")){
			try {
				RequestDatabase requestDatabase = new RequestDatabase();
				return requestDatabase.getConnectionList(request);
			} catch (IllegalStateException e) {
				System.out.println("FAIL");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("FAIL");
				e.printStackTrace();
			}
		}
		
		//Breadth search
		if(request.getMethode().equalsIgnoreCase("BFS")){
			try {
				BreadthFirstSearch breadthFirstSearch = new BreadthFirstSearch();
				return breadthFirstSearch.getConnectionList(request);
			} catch (IllegalStateException e) {
				System.out.println("BFS failed: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("BFS failed: " + e.toString());
				e.printStackTrace();
			}
		}
		
		if(request.getMethode().equalsIgnoreCase("Hotspots")){
			try {
				Hotspot hotspot = new Hotspot();
				return hotspot.getConnectionList(request);
			} catch (IllegalStateException e) {
				System.out.println("Hotspot failed: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Hotspot failed: " + e.toString());
				e.printStackTrace();
			}
		}
		
		if(request.getMethode().equalsIgnoreCase("HotspotPath")){
			try {
				HotspotSearch hotspot = new HotspotSearch();
				return hotspot.getHotspotPath(request);
			} catch (IllegalStateException e) {
				System.out.println("Hotspot Search failed: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Hotspot Search failed: " + e.toString());
				e.printStackTrace();
			}
		}
		
		
		if(request.getMethode().equalsIgnoreCase("RecursiveBFS")){
			try {
				RecursiveBreadthSearch breadthSearch = new RecursiveBreadthSearch();
				return breadthSearch.getReqursivePath(request);
			} catch (IllegalStateException e) {
				System.out.println("RecursiveBFS failed: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("RecursiveBFS failed: " + e.toString());
				e.printStackTrace();
			}
		}
		
		if(request.getMethode().equalsIgnoreCase("FullSearch")){
			try {
				HotspotSearch hotspot = new HotspotSearch();
				RecursiveBreadthSearch breadthSearch = new RecursiveBreadthSearch();
				LinkedBlockingQueue<Connection> preConnectionList = hotspot.getHotspotPath(request);
				return breadthSearch.getReqursivePath(request, preConnectionList);
			} catch (IllegalStateException e) {
				System.out.println("Full Search failed: " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Full Search failed: " + e.toString());
				e.printStackTrace();
			}
		}
		
		
		return connectionList;
	}
}


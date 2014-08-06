/* Code for COMP261 Assignment
 * Name:difu chen
 * Usercode:chendifu
 * ID:300252166

 */

import java.awt.Graphics;
import java.awt.Point;
import java.util.*;



/** Node   */

public class Node{

    private int id;  
    private Location loc;  // coordinates of the intersection
    private List<Segment> outNeighbours = new ArrayList<Segment>(2);
    private List<Segment> inNeighbours = new ArrayList<Segment>(2);
    public boolean visited = false;
    public int depth =0;
    
    /** Construct a new Node object */
    public Node(int id, Location l){
	this.id = id;
	loc = l;
    }

    /** Construct a new Node object from a line in the data file*/
    public Node(String line){
	String[] values = line.split("\t");
	id = Integer.parseInt(values[0]);
	double lat = Double.parseDouble(values[1]);
	double lon = Double.parseDouble(values[2]);
	loc = Location.newFromLatLon(lat, lon);
	//System.out.printf("Created Node %6d %s%n", id, loc);
    }
    

    public int getID(){
	return id;
    }
    public Location getLoc(){
	return this.loc;
    }

    public void addInSegment(Segment seg){
	inNeighbours.add(seg);
    }	
    public void addOutSegment(Segment seg){
	outNeighbours.add(seg);
    }	

    public List<Segment> getOutNeighbours(){
	return outNeighbours;
    }	
    
    public List<Segment> getInNeighbours(){
	return inNeighbours;
    }	
    
    public boolean closeTo(Location place, double dist){
	return loc.closeTo(place, dist);
    }

    public double distanceTo(Location place){
	return loc.distanceTo(place);
    }

    public void draw(Graphics g, Location origin, double scale){
	Point p = loc.getPoint(origin, scale);
	g.fillRect(p.x+AucklandMapper.dX, p.y+AucklandMapper.dY, 2, 2);
    }
    
    public String toString(){
	StringBuilder b = new StringBuilder(String.format("Intersection ID: %d: at %s; \nRoads:\n", id, loc));
	Set<String> roadNames = new HashSet<String>();
	for (Segment neigh : inNeighbours){
	    roadNames.add(neigh.getRoad().getFullName());
	}
	for (Segment neigh : outNeighbours){
	    roadNames.add(neigh.getRoad().getFullName());
	}
	for (String name : roadNames){
	    b.append(name).append("\n");
	}
	return b.toString();
    }



}

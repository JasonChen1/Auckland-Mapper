/* Code for COMP261 Assignment
 * Name:difu chen
 * Usercode:chendifu
 * ID:300252166

 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** RoadMap: The list of the roads and the graph of the road network    */

public class RoadGraph{

    double westBoundary = Double.POSITIVE_INFINITY;
    double eastBoundary = Double.NEGATIVE_INFINITY;
    double southBoundary = Double.POSITIVE_INFINITY;
    double northBoundary = Double.NEGATIVE_INFINITY;


    //the map containing the graph of nodes (and roadsegments), indexed by the nodeID
    Map<Integer,Node> nodes = new HashMap<Integer,Node>();

    //the map of roads, indexed by the roadID
    Map<Integer,Road> roads = new HashMap<Integer,Road>();;

    //the map of roads, indexed by name
    Map<String,Set<Road>> roadsByName = new HashMap<String,Set<Road>>();;

    ArrayList<String> roadNames = new ArrayList<String>();

  //ArrayList for drawing polygons
  	private ArrayList<Location>  coordinates;//store the polygon's location
  	private ArrayList<Polygon> polygon;//store the objects of polygon
  	private ArrayList<ArrayList<Location>> polyData;//store all the list of location
  	private boolean drawPoly = false;
  	
  	// A*
  	private Comparator<distanceObj> compare  = new DisComparator();
  	public distanceObj goalObj;
  	private Map<String,Double> roadName;
  	
  	//articulation point
  	private Set<Node> artPoints = new HashSet<Node>();
  	public boolean reset= false;
  	
    /** Construct a new RoadMap object */
    public RoadGraph(){
    }

    public String loadData(String dataDirectory){
	// Read roads into roads array.
	// Read the nodes into the roadGraph array.
	// Read each road segment
	//   put the segment into the neighbours of the startNode
	//   If the road of the segment is not one way,
	//   also construct the reversed segment and put it into
	//   the neighbours of the endNode
	// Work out the boundaries of the region.
	String report= "";
	System.out.println("Loading roads...");
	loadRoads(dataDirectory);
	report += String.format("Loaded %,d roads, with %,d distinct road names%n",
			  roads.entrySet().size(), roadNames.size());
	System.out.println("Loading intersections...");
	loadNodes(dataDirectory);
	report += String.format("Loaded %,d intersections%n", nodes.entrySet().size());
	System.out.println("Loading road segments...");
	loadSegments(dataDirectory);
	report += String.format("Loaded %,d road segments%n", numSegments());
	loadPolygon(dataDirectory);
	return report;
    }

    public void loadRoads(String dataDirectory){
	File roadFile = new File(dataDirectory+"roadID-roadInfo.tab");
	if ( !roadFile.exists() ){
	    System.out.println("roadID-roadInfo.tab not found");
	    return;
	}
	BufferedReader data;
	try{
	    data = new BufferedReader(new FileReader(roadFile));
	    data.readLine(); //throw away header line.
	    while (true){
		String line = data.readLine();
		if (line==null) {break;}
		Road road = new Road(line);
		roads.put(road.getID(), road);
		String fullName = road.getFullName();
		roadNames.add(fullName);
		Set<Road> rds = roadsByName.get(fullName);
		if (rds==null){
		    rds = new HashSet<Road>(4);
		    roadsByName.put(fullName, rds);
		}
		rds.add(road);
	    }
	} catch(IOException e){System.out.println("Failed to open roadID-roadInfo.tab: " + e);}
    }

    public void loadNodes(String dataDirectory){
	File nodeFile = new File(dataDirectory+"nodeID-lat-lon.tab");
	if ( !nodeFile.exists() ){
	    System.out.println("nodeID-lat-lon.tab not found");
	    return;
	}
	BufferedReader data;
	try{
	    data = new BufferedReader(new FileReader(nodeFile));
	    while (true){
		String line = data.readLine();
		if (line==null) { break; }
		Node node = new Node(line);
		nodes.put(node.getID(), node);
	    }
	} catch(IOException e){System.out.println("Failed to open roadID-roadInfo.tab: " + e);}
    }

    public void loadSegments(String dataDirectory){
	File segFile = new File(dataDirectory+"roadSeg-roadID-length-nodeID-nodeID-coords.tab");
	if ( !segFile.exists() ){
	    System.out.println("roadSeg-roadID-length-nodeID-nodeID-coords.tab not found");
	    return;
	}
	BufferedReader data;
	try{
	    data = new BufferedReader(new  FileReader(segFile));
	    data.readLine();  // get rid of headers
	    while (true){
		String line = data.readLine();
		if (line==null) { break; }
		Segment seg = new Segment(line, roads, nodes);
		//System.out.println(seg);
		Node node1 = seg.getStartNode();
		Node node2 = seg.getEndNode();
		node1.addOutSegment(seg);
		node2.addInSegment(seg);
		Road road = seg.getRoad();
		road.addSegment(seg);
		if (!road.isOneWay()){
		    Segment revSeg = seg.reverse();
		    node2.addOutSegment(revSeg);
		    node1.addInSegment(revSeg);
		}
	    }
	    
	    
	} catch(IOException e){System.out.println("Failed to open roadID-roadInfo.tab: " + e);}
    }

    public void loadPolygon(String dataDirectory){
		File file = new File(dataDirectory+"polygon-shapes.mp");
		if (file.exists()){
			drawPoly = true;
			BufferedReader data;
			try {
			polyData = new ArrayList<ArrayList<Location>>();// Every time the method is called make a new arrayList
			polygon = new ArrayList<Polygon>();
			data = new BufferedReader(new FileReader(file));
			String type = null;
			String label = null;
			int endLevel = -1;
			int cityIdx = -1;
			String line ;

			while((line= data.readLine())!=null){
				String[] values = line.split("=");
				if(values[0].equals("[POLYGON]")){
				}
				if(values[0].equals("Type")){
					type = values[1];
				}
				if(values[0].equals("Label")){
					label = values[1];
				}
				if(values[0].equals("EndLevel")){
					endLevel = Integer.parseInt(values[1]);
				}
				if(values[0].equals("CityIdx")){
					cityIdx = Integer.parseInt(values[1]);
				}
				if(values[0].startsWith("Data")){
					coordinates = new ArrayList<Location>();
					String loc = values[1].replaceAll("[()]","");
					String[] val = loc.split(",");
					for(int i = 0; i<val.length; i+=2){
					double x = Double.parseDouble(val[i]);
					double y = Double.parseDouble(val[i+1]);
					coordinates.add(Location.newFromLatLon(x, y));
					//System.out.println(x+"x  : y"+y);
					}
					polyData.add(coordinates);
				}
				if(values[0].equals("END")){
				}
				polygon.add(new Polygon(type, label, endLevel, cityIdx, polyData));
		}

			data.close();
			} catch (Exception ex) {
				System.out.println("Error, Can't access file");
				ex.printStackTrace();//find error
		}
	}else{drawPoly = false;}
	}

    public double[] getBoundaries(){
	double west = Double.POSITIVE_INFINITY;
	double east = Double.NEGATIVE_INFINITY;
	double south = Double.POSITIVE_INFINITY;
	double north = Double.NEGATIVE_INFINITY;

	for (Node node : nodes.values()){
	    Location loc = node.getLoc();
	    if (loc.x < west) {west = loc.x;}
	    if (loc.x > east) {east = loc.x;}
	    if (loc.y < south) {south = loc.y;}
	    if (loc.y > north) {north = loc.y;}
	}
	return new double[]{west, east, south, north};
    }

    public void checkNodes(){
	for (Node node : nodes.values()){
	    if (node.getOutNeighbours().isEmpty()&& node.getInNeighbours().isEmpty()){
		System.out.println("Orphan: "+node);
	    }
	}
    }

    public int numSegments(){
	int ans = 0;
	for (Node node : nodes.values()){
	    ans += node.getOutNeighbours().size();
	}
	return ans;
    }

	public Road getRoadInfo(String name){//when the user type in the name then it gets all the infos about that name
		if(name !=null){
		for(Road r: roads.values()){//go through the list of roads
			if(r.getName().equals(name)){//find the name that matches with the userInput once find it return
				return r;
			}
		}
		}
		return null;
	}

    public void redraw(Graphics g, Location origin, double scale){
	//System.out.printf("Drawing road graph. at (%.2f, %.2f) @ %.3f%n", origX, origY, scale);
    	//draw polygon
    	if(drawPoly){
	for(ArrayList<Location> loc: polyData){
			int index = 0;
			int xArray[] = new int[loc.size()];
			int yArray[] = new int[loc.size()];
			for(Location l: loc){
			Point point = l.getPoint(origin, scale);
			xArray[index] = point.x+AucklandMapper.dX;
			yArray[index] = point.y+AucklandMapper.dY;
			index++;
		}
			g.setColor (new Color(0,191,255));
			g.fillPolygon(xArray, yArray, index);
	}
    	}
// draw nodes segments
    g.setColor(Color.red);
	for(Node node : nodes.values()){
	    node.draw(g, origin, scale);
	}
	
	g.setColor(Color.black);
	for (Node node : nodes.values()){
	    for (Segment seg : node.getOutNeighbours()){
		seg.draw(g, origin, scale);
	    }
	}

	//draw path	
	if(goalObj!=null){
		roadName = new HashMap<String,Double>();
		Node goal = goalObj.getNode();
		Point p1 = goal.getLoc().getPoint(origin, scale);
		distanceObj from = goalObj.getFrom();
		while(from!=null){	
			for(Node n: nodes.values()){
				for(Segment seg: n.getOutNeighbours()){
				if(seg.getStartNode().getID()==goal.getID()
						||seg.getEndNode().getID()==goal.getID()){
					roadName.put(seg.getRoad().getName(),seg.getLength());	
				}
				if(seg.getStartNode().getID()==from.getNode().getID()
						||seg.getEndNode().getID()==from.getNode().getID()){
					roadName.put(seg.getRoad().getName(), seg.getLength());
					//System.out.println(seg.getLength());
				}	
				}
			}
			Point p2 = from.getNode().getLoc().getPoint(origin, scale);
			g.setColor(Color.green);
			g.drawLine(p1.x+AucklandMapper.dX, p1.y+AucklandMapper.dY, p2.x+AucklandMapper.dX, p2.y+AucklandMapper.dY);
			p1=p2;
			from = from.getFrom();
		}
		double totalDis = 0;
		AucklandMapper.textOutput.setText("");
		for(Map.Entry<String, Double> info: roadName.entrySet()){
			totalDis += info.getValue();
		AucklandMapper.textOutput.append("\n"+info.getKey()+": "+info.getValue().toString().substring(0, 5)+"km");
		}
		String total = ""+totalDis;
		AucklandMapper.textOutput.append("\n\nTotal distance = "+total.substring(0, 5)+"km");
	}
		if(reset == true){
			if(!artPoints.isEmpty()){
				g.setColor(Color.cyan);
				for(Node n: artPoints){
					n.draw(g, origin, scale);
				}
			}
		}
    }


    public Node findNode(Point point, Location origin, double scale){
	Point p = new Point();
	p.x = point.x-= AucklandMapper.dX;
	p.y = point.y-= AucklandMapper.dY;
	Location mousePlace = Location.newFromPoint(point, origin, scale);
	Node closestNode = null;
	double mindist = Double.POSITIVE_INFINITY;
	for (Node node : nodes.values()){
	    double dist = node.distanceTo(mousePlace);
	    if (dist<mindist){
		mindist = dist;
		closestNode = node;
	    }
	}
	return closestNode;
    }
      

    //---------------------------------------------->
    
    public void aStarSearch (Node start, Node goal){
      	//priority queue for search
      	PriorityQueue<distanceObj> search = new PriorityQueue<distanceObj>(10, compare);
      	for(Node n: nodes.values()){    
      		n.visited = false;	
      	}
      	distanceObj pathFrom = null;
      	search.offer(new distanceObj(start,pathFrom,0,estimate(start,goal)));
      	//estimate is the distance from the nodes to the goal e.g straight line
      	while(!search.isEmpty()){
      		distanceObj obj= search.poll();
      		Node node = obj.getNode();
      		if(node.visited==false){
      			node.visited=true;
      			pathFrom = obj;
      			if(node.equals(goal)){
      				//draws it ->call draw method
      				goalObj = obj;
      				return;
      			}
      			
      			List<Segment> seg = new ArrayList<Segment>();
      			seg = node.getOutNeighbours();      	     			
      			for(Segment s: seg){
      				Node nb = null;//Neighbor node
      				if(s.getStartNode().equals(node)){
      					if(s.getEndNode()!=null){
      						nb = s.getEndNode();
      					}	
      				}
      				else if(s.getEndNode().equals(node)){
      					if(s.getStartNode()!=null){
      						nb = s.getStartNode();
      					}     					
      				}     				   				
      				
      				if(nb!=null && !nb.visited){ 
      					double disToNb = obj.getDistanceHere()+s.getLength();
      					double totalestimates = disToNb + estimate(nb, goal);
      					search.offer(new distanceObj(nb, obj, disToNb, totalestimates));
      				}
      			}     			
      			
      		}
      	}      	      	      	
    }
    

    public double estimate(Node n1, Node n2){
    	double dis = n1.getLoc().distanceTo(n2.getLoc());
    	return dis;
    }
      
    //------------------------------------------------->

    public void artPoint(Node start){
    	artPoints = new HashSet<Node>();
    	for(Node n: nodes.values()){
    		n.depth =  Integer.MAX_VALUE;       		
    	} 
    	
    	start.depth=0;
    	int numSubtree=0;
    	//get the neighbor of the node
    	List<Segment> neigh = new ArrayList<Segment>();
    	neigh = start.getOutNeighbours(); 
    	
    	
    	for(Segment s: neigh){
    		if(s.getEndNode().depth==Integer.MAX_VALUE){   			
    			reArtPts(s.getEndNode(),1,start);
    			numSubtree++;
    		}
    	}
    	
    	if(numSubtree>1){
    		artPoints.add(start);  
    	}     
    }
           
    
   private int reArtPts(Node node, int depth, Node from) {
		node.depth = depth;
		int reachBack = depth;
		//get the neighbor of the node
		List<Segment> neigh = new ArrayList<Segment>();
    	neigh = node.getOutNeighbours();
    	
		for(Segment s:neigh){
			if( s.getEndNode()!=from){
				 if(s.getEndNode().depth< Integer.MAX_VALUE){					
					reachBack = Math.min(s.getEndNode().depth, reachBack);
				}
				else{	
					int childReach = reArtPts(s.getEndNode(), depth+1, node);
					reachBack= Math.min(childReach, reachBack);
					if(childReach>=depth){
						artPoints.add(node);	
					}
				}
				
			}			
		}

		return reachBack;
	}


    
    /** Returns a set of full road names that match the query.
     *  If the query matches a full road name exactly, then it returns just that name*/
    
    public Set<String> lookupName(String query){
	Set<String> ans = new HashSet<String>(10);
	if (query==null) return null;
	query = query.toLowerCase();
	for (String name : roadNames){
	    if (name.equals(query)){  // this is the right answer
		ans.clear();
		ans.add(name);
		return ans;
	    }
	    if (name.startsWith(query)){ // it is an option
		ans.add(name);
	    }
	}
	return ans;
    }

    
    /** Get the Road objects associated with a (full) road name */
    
    public Set<Road> getRoadsByName(String fullname){
	return roadsByName.get(fullname);
    }
    

   
    /** Return a list of all the segments belonging to the road with the
     given (full) name. */
    
    public List<Segment> getRoadSegments(String fullname){
	Set<Road> rds = roadsByName.get(fullname);
	if (rds==null) { return null; }
	System.out.println("Found "+rds.size()+" road objects: "+rds.iterator().next());
	List<Segment> ans = new ArrayList<Segment>();
	for (Road road : rds){
	    ans.addAll(road.getSegments());
	}
	return ans;
    }

    public static void main(String[] arguments){
	AucklandMapper.main(arguments);
    }


}

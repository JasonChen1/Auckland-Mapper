/* Code for COMP261 Assignment
 * Name:difu chen
 * Usercode:chendifu
 * ID:300252166

 */

// call repaint() on this object to invoke the drawing.

import java.awt.Graphics;
import java.awt.Point;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.*;
import java.io.*;


public class AucklandMapper{

    private JFrame frame;
    private JComponent drawing; 
    public static JTextArea textOutput;
    private JTextField nameEntry;
    private int windowSize = 800;
    private JPanel bg = new JPanel(new BorderLayout());
    
    
    private RoadGraph roadGraph;

    private Node selectedNode;  // the currently selected node
    private List<Segment> selectedSegments;  // the currently selected road or path

    private boolean loaded = false;

    // Dimensions for drawing
    double westBoundary ;
    double eastBoundary ;
    double southBoundary;
    double northBoundary;
    Location origin;
    double scale;
    private int clickX;//x value when the mouse click on the panel
	private int clickY;//y value when the mouse click on the panel
	private int dragX ;//x value when then mouse release on the panel
	private int dragY ;//y value when the mouse release on the panel
	public static int dX;
	public static int dY;
	
	//Astar search variables
	private Node start= null;
	private Node goal= null;
	private boolean path=false;
	
	//Articulation point variables
	private Node begin=null;
	private boolean findArt= false;
	
	
    public AucklandMapper(String dataDir){
	setupInterface();
	roadGraph = new RoadGraph();

	textOutput.setText("Loading data...");
	while (dataDir==null){dataDir=getDataDir();}
	textOutput.append("Loading from "+dataDir+"\n");
	textOutput.append(roadGraph.loadData(dataDir));
	setupScaling();
	loaded = true;
	drawing.repaint();
    }

    private class DirectoryFileFilter extends FileFilter{
	public boolean accept(File f) {return f.isDirectory();}
	public String getDescription(){return "Directories only";}
    }

    private String getDataDir(){
	JFileChooser fc = new JFileChooser();
	fc.setFileFilter(new DirectoryFileFilter());
	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	if (fc.showOpenDialog(frame)!=JFileChooser.APPROVE_OPTION){return null;}
	return fc.getSelectedFile().getPath()+File.separator;
    }


	private void setupScaling(){
	    double[] b = roadGraph.getBoundaries();
	    westBoundary = b[0];
	    eastBoundary = b[1];
	    southBoundary = b[2];
	    northBoundary = b[3];
	    resetOrigin();
	    /*
	      System.out.printf("Boundaries: w %.2f, e %.2f, s %.2f, n %.2f%n",
	      b[0], b[1], b[2], b[3]);
	      System.out.printf("Scaling from %s @ %.5f,%n", origin, scale);
	    */
	}

	@SuppressWarnings("serial")
	private void setupInterface(){
	    // Set up a window .
	    frame = new JFrame("Graphics Example");
	    frame.setSize(windowSize, windowSize);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    // Set up a JComponent in the window that we can draw on
	    // When the JComponent tries to paint itself, it will call the redraw method
	    //  in this PathDrawer class, passing a Graphics object to it.
	    // The redraw method can draw whatever it wants on the Graphics object.
	    // We can ask the JComponent to paint itself by calling drawing.repaint() 
	    //  Note that this merely requests that the drawing is repainted; it won't
	    //  necessarily do it immediately.
	    frame.add(bg,BorderLayout.CENTER);
	    bg.setBackground(Color.WHITE);
	    drawing = new JComponent(){
		    protected void paintComponent(Graphics g){redraw(g);}
		};
	    bg.add(drawing, BorderLayout.CENTER);
	    
	    drawing.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
					drawing.repaint();
					scale -= e.getWheelRotation();
					if(scale <1){
						scale = 1;
					}					
			}
		});
	    
	    // Setup a text area for output
	    textOutput = new JTextArea(10, 100);
	    textOutput.setEditable(false);
	    JScrollPane textSP = new JScrollPane(textOutput);
	    frame.add(textSP, BorderLayout.SOUTH);

	    //Set up a panel for some buttons.
	    //To get nicer layout, we would need a LayoutManager on the panel.
	    JPanel panel = new JPanel();
	    frame.add(panel, BorderLayout.NORTH);

	    //Add a text label to the panel.
	    
	    JButton lButton = new JButton("LoadFile");	    
	    panel.add(lButton);
	    
	    lButton.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent ev){
		    	
		    roadGraph = new RoadGraph();
		    String data = getDataDir();
		    roadGraph.loadData(data);
		    setupScaling();
		    path = false;
		    start = null;
			goal = null;
			findArt = false;
			textOutput.setText("");//reset text area
		    drawing.repaint();
		    
		    }});
	   
	    JButton pathButton = new JButton("Path Draw");
	    panel.add(pathButton);
	    
	    //------------------------------------------------------>
	    pathButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//reset start node and goal node
				drawing.repaint();
				roadGraph.goalObj=null;
				path = true;
				start = null;
				goal = null;
				findArt = false;
				roadGraph.reset = false;
				selectedNode = null;
				textOutput.setText("");
			}
		});	    
	    //------------------------------------------------>	
	   
	    JButton artPoint = new JButton("Find Articulation Point");
	    panel.add(artPoint);
	    
	    artPoint.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {		
				roadGraph.reset = true;
				findArt=true;
				roadGraph.goalObj=null;
				path = false;
				start = null;
				goal = null;	
				selectedNode = null;
				textOutput.setText("");
				drawing.repaint();
			}
		});
	    
	    drawing.addMouseListener(new MouseAdapter() {		
			
			@Override
			public void mousePressed(MouseEvent e) {
				dragX = e.getX();
				dragY= e.getY();
				
				drawing.repaint();
			}	
				
		});
				
		drawing.addMouseMotionListener(new MouseAdapter() {			
			
			@Override
			public void mouseDragged(MouseEvent e) {
				clickX = dragX;
				clickY = dragY;
				//get the value when the mouse release
				dragX = e.getX();
				dragY = e.getY();		
				//the different between the mouse pressed and release x and y
				dX += dragX - clickX;
				dY += dragY - clickY;				
				drawing.repaint();
			}			
		});
		panel.add(new JLabel("Type road name: "));
	    nameEntry = new JTextField(15);
	    panel.add(nameEntry);
	    nameEntry.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			lookupName(nameEntry.getText());
			drawing.repaint();
		    }});
	    
	    JButton button = new JButton();
	    button = new JButton("Quit");
	    panel.add(button);
	    button.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent ev){System.exit(0);}});

	    // Add a mouselistener to the drawing JComponent to respond to mouse clicks.
	    drawing.addMouseListener(new MouseAdapter(){
		    public void mouseReleased(MouseEvent e){
			selectedNode = findNode(e.getPoint());	
			drawing.repaint();
			
			//-------------------------------->
			if(start!=null && goal!=null  && goal!=start){
				//System.out.println(start+" "+goal);
				roadGraph.aStarSearch(start, goal);					
			}
			if( goal==start && start!=null && goal !=null){
				textOutput.append("Can not go to the same intersection\n");
			}
			//-------------------------------->
			
			if(begin!=null){
				roadGraph.artPoint(begin);
			}
			
			if (selectedNode!=null){
			    textOutput.setText(selectedNode.toString());
			}	
			}});	    
	    
	    // Once it is all set up, make the interface visible
	    frame.setVisible(true);

	}

	//set origin and scale for the whole map
	private void resetOrigin(){
	    origin = new Location(westBoundary, northBoundary);
	    scale = Math.min(windowSize/(eastBoundary-westBoundary),
			     windowSize/(northBoundary-southBoundary));
	    
	}
    
	//Find the place that the mouse was clicked on (if any)
	private Node findNode(Point mouse){	
		Node node = null;
		//----------------------->			
		if(path==true){
			if(start!=null){
				goal = roadGraph.findNode(mouse,origin,scale);
			}
			if(goal==null){
				start = roadGraph.findNode(mouse,origin,scale);	
			}				
		}
		//----------------------->	
		if(findArt== true){			
			begin = roadGraph.findNode(mouse, origin, scale);
			}
		
		if(path == false){					
	    return roadGraph.findNode(mouse, origin, scale);  
	  }

		return node;
	}


	private void lookupName(String query){
	    List<String> names = new ArrayList<String>(roadGraph.lookupName(query));
	    if (names.isEmpty()){
		selectedSegments=null;
		textOutput.setText("Not found");
	    }
	    else if (names.size()==1){
		String fullName =names.get(0);
		nameEntry.setText(fullName);
		Road r = roadGraph.getRoadInfo(query);
		String oneway, speed=null, roadClass=null,car= null, peo = null,bic=null;//variables
		if(r!=null){
			if(!r.isOneWay()){ oneway = "both directions allowed";}
			else{  oneway ="one way road, direction from beginning to end"; }
			
			if(r.getSpeed()==0){speed = "5km/h";}
			else if(r.getSpeed()==1){speed = "20km/h";}
			else if(r.getSpeed()==2){speed = "40km/h";}
			else if(r.getSpeed()==3){speed = "60km/h";}
			else if(r.getSpeed()==4){speed = "80km/h";}
			else if(r.getSpeed()==5){speed = "100km/h";}
			else if(r.getSpeed()==6){speed = "110km/h";}
			else if(r.getSpeed()==7){speed = "no limit";}
			
			if(r.getRoadclass()==0){roadClass ="Residential";}
			else if(r.getRoadclass()==1){roadClass ="Collector";}
			else if(r.getRoadclass()==2){roadClass ="Arterial";}
			else if(r.getRoadclass()==3){roadClass ="Principal HW";}
			else if(r.getRoadclass()==4){roadClass ="Major HW ";}
			
			if(!r.isNotForCars()){
				car = "OK for this category of traffic";
			}else{car = "Not useable by this category of traffic";}
			
			if(!r.isNotForPedestrians()){
				peo = "OK for this category of traffic";
			}else{peo = "Not useable by this category of traffic";}
			
			if(!r.isNotForBicycles() ){
				bic = "OK for this category of traffic";
			}else{bic = "Not useable by this category of traffic";}
			
			textOutput.setText("ID: "+r.getID()+"	Road: "+ r.getFullName()+"\nOneway/Not: "+oneway+
					"\nSpeed limit: "+speed+"\nRoadClass: "+roadClass+"\nCar: "+
					car+"\nPeople: "+peo+"\nBicycle: "+bic+"\n--------------------\n");
			}
			
		
		//textOutput.setText("Found\n");
		selectedSegments=roadGraph.getRoadSegments(fullName);
	    }
	    else{
		selectedSegments=null;
		String prefix = maxCommonPrefix(query, names);
		nameEntry.setText(prefix);
		textOutput.setText("Options: ");
		for (int i = 0; i<10&&i<names.size(); i++){
		    textOutput.append(names.get(i));textOutput.append(", ");
		}
		if (names.size()>10){textOutput.append("...\n");}
		else { textOutput.append("\n"); }
	    }
	    

	}

    private String maxCommonPrefix(String query, List<String>names){
	String ans = query;
	for (int i = query.length(); ; i++){
	    if (names.get(0).length()<i) return ans;
	    String cand = names.get(0).substring(0,i);
	    for (String name : names){
		if (name.length()<i) return ans;
		if (name.charAt(i-1)!=cand.charAt(i-1)) return ans;
	    }
	    ans = cand;
	}
    }
	


	//The redraw method that will be called from the drawing JComponent and will
	//draw the map at the current scale and shift.
	public void redraw(Graphics g){
	    if (roadGraph!= null && loaded ){
		roadGraph.redraw(g, origin, scale);
		if (selectedNode!=null) {
		    g.setColor(Color.GREEN);
		    selectedNode.draw(g, origin, scale);}
		//----------------------->	
		if(start!=null){
			g.setColor(Color.GREEN);
			start.draw(g, origin, scale);
			
		}
		if(goal != null){
			g.setColor(Color.blue);
			goal.draw(g, origin, scale);
		}	
		//----------------------->	

		if (selectedSegments!=null){
		    g.setColor(Color.GREEN);
		    for (Segment seg : selectedSegments){
			seg.draw(g, origin, scale);
		    }
		}
	    }
	}


	@SuppressWarnings("unused")
	public static void main(String[] arguments){
		AucklandMapper obj;
		if (arguments.length>0){		
		obj= new AucklandMapper(arguments[0]);
	    }
	    else{
		obj = new AucklandMapper(null);
	    }
	}	


    }

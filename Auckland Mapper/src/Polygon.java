/* Code for COMP261 Assignment
 * Name:difu chen
 * Usercode:chendifu
 * ID:300252166

 */

import java.util.ArrayList;


public class Polygon {

	private String type;
	private int endLevel;
	private int cityIdx;
	ArrayList<ArrayList<Location>> data;
	private String label;
	
	public Polygon(String type, String label, int endLevel,int cityIdx, ArrayList<ArrayList<Location>> data){
		this.type = type;
		this.label = label;
		this.endLevel = endLevel;
		this.cityIdx = cityIdx;
		this.data = data;		
	}
	
	
	public String getType(){
		return type;	
	}

	public String getLabel(){
		return label;
	}
	
	public int getEndLevel(){
		return endLevel;
	}
	
	public int getCityIdx (){
		return cityIdx;
	}
	
	public ArrayList<ArrayList<Location>> getData(){
		return data;
	}


}

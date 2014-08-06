/* Code for COMP261 Assignment
 * Name:difu chen
 * Usercode:chendifu
 * ID:300252166

 */
public class distanceObj {

	private double distanceHere;
	private double distanceToGoal;
	private Node node;
	private distanceObj from;//Neighbor
	
	
	public distanceObj( Node node, distanceObj from,double disToHere, double disToGoal){
		this.distanceHere = disToHere;
		this.distanceToGoal = disToGoal;
		this.node = node;
		this.from = from;
		
	}

	
	public double getDistanceHere() {
		return distanceHere;
	}


	public void setDistanceHere(double distanceHere) {
		this.distanceHere = distanceHere;
	}


	public double getDistanceToGoal() {
		return distanceToGoal;
	}


	public void setDistanceToGoal(double distanceToGoal) {
		this.distanceToGoal = distanceToGoal;
	}


	public Node getNode() {
		return node;
	}


	public void setNode(Node node) {
		this.node = node;
	}


	public distanceObj getFrom() {
		return from;
	}


	public void setFrom(distanceObj from) {
		this.from = from;
	}
	
	
	
}

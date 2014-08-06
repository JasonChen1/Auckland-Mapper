/* Code for COMP261 Assignment
 * Name:difu chen
 * Usercode:chendifu
 * ID:300252166

 */

import java.util.Comparator;


public class DisComparator implements Comparator<distanceObj>{

	@Override
	public int compare(distanceObj o1, distanceObj o2) {
		
		if(o1.getDistanceToGoal()-o2.getDistanceToGoal()<0){
			return -1;		
		}
		else if(o1.getDistanceToGoal()-o2.getDistanceToGoal()>0){
			return 1;
			
		}		
			return 0;				
	}	
	
}

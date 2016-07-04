import java.util.ArrayList;

/**
 * A class that represents the ActivityZone. It contains information such as the start, the end, the threshold for the discretization of the activity and the usual consumption patterns in the activity zone. 
 * @author pant
 *
 */
public class ActivityZone {
	int center;
	int start,end;
	int aggStart,aggEnd;
	ArrayList<Double> thresholds;
	ArrayList<Double> values;
	ArrayList<Pattern> patterns;
	int maxStart;
	
	public ActivityZone(int start,int end, int center){
		
		this.start=start;
		this.end=end;
		this.center=center;
		patterns = new ArrayList<Pattern>();
		thresholds = new ArrayList<Double>();
		values = new ArrayList<Double>();
	
	}
	
	public void testAdd(ArrayList<ActivityZone> zones,Pattern p){
		if(p.isIn(zones,this))
			patterns.add(p);
	}
	
	
	public double[] meanConsumption(){
		double[] hours = new double[25];
		int offset;
		
		for(Pattern p:patterns){
			offset = center-p.getCenter();
			for(int i=0;i<p.values.size();i++){
				if(i+p.start+offset > 24 || i+p.start+offset < 1 )
					break;
				hours[i+p.start+offset] += p.values.get(i);
			}
		}
		
		for(int i=0;i<hours.length;i++)
			hours[i]/=patterns.size();

		return hours;
	}
	
	public Integer discretize(Double cons){
		for(int i=0;i<thresholds.size();i++)
			if(cons<=thresholds.get(i))
				return i;
		return -1;
	}

}

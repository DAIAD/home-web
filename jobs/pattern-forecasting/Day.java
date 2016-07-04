import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class represents the water consumption of a day
 * @author pant
 *
 */
public class Day {
	Calendar cal;
	ArrayList<Pattern> patterns;
	ArrayList<DataPair> data;
	double sigFactor=0.08;
	
	public Day(){}
	
	/**
	 * Initialize a day
	 * @param data		A list of DataPair objects containing the water consumption for each hour of the day
	 * @param patterns	A list of Pattern objects the describe the water consumption in the activity zones of the day
	 */
	public Day(ArrayList<DataPair> data, ArrayList<Pattern> patterns){
		this.patterns=patterns;
		cal = data.get(0).cal;
		this.data=data;
	}
	
	public Day(ArrayList<Pattern> patterns){
		this.patterns=patterns;
	}
	
	public ArrayList<Pattern> significantPatterns(double sigFactor){	
		ArrayList<Pattern> significant = new ArrayList<Pattern>();
		double dailyConsumption = 0;
		for(DataPair dp:data)
			dailyConsumption+=dp.consumption;
		
		for(Pattern pat:patterns)
			if(pat.totalConsumption() > sigFactor*dailyConsumption)
				significant.add(pat);
		 return significant;
	}
		
	public double totalConsumption(){
		double sum=0;
		for(DataPair dp:data)
			sum+=dp.consumption;
		return sum;
	}
	
	public DayReducedRepresentation aggregateDay(ArrayList<ActivityZone> zones){
		DayReducedRepresentation drr = new DayReducedRepresentation();
		drr.cal = cal;
		drr.totalCons = totalConsumption();
		double sum;
		for(int z=0;z<zones.size();z++){
			
			sum=0.0;
			for(int i=zones.get(z).aggStart;i<zones.get(z).aggEnd;i++)
				sum+=data.get(i).consumption;
			drr.volumes.add(new SinglePrediction(sum,1));
			drr.zones.add(new SinglePrediction(zones.get(z).discretize(sum),1));
			
		}
		return drr;
	}
	
}

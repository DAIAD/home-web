import java.util.ArrayList;
import java.util.Calendar;


public class Pattern {
	Calendar startDate;
	ArrayList<Double> values;
		
	int start;
	int end;
	
	public Pattern(Calendar cal){
		values = new ArrayList<Double>();
		startDate = cal;
		start = Preprocessor.getCalTime(cal);
		end=-1;
	}
	
	public Pattern(int start){
		values = new ArrayList<Double>();
		this.start = start;
		end=-1;
	}
	
	public void addValue(Double val){
		values.add(val);
		if (end==-1)
			end=start;
		else
			end++;
	}
	
	public double getValue(int i){
		return values.get(i);
	}
	
	public Pattern getSubPattern(int start,int end){
		
		//Calendar bufCal = (Calendar) startDate.clone();
		//bufCal.add(Calendar.HOUR, start);
		
		Pattern pat = new Pattern(this.start+start);
		for(int i=start;i<end;i++)
			pat.addValue(values.get(i));
		
		return pat;
		
	}
	
	public double totalConsumption(){
		double sum=0;
		for(Double d:values)
			sum+=d;
		return sum;
	}
	
	public int getCenter(){
		double ind=0;
		double sum=0;
		for(int i=start;i<=end;i++){
			ind+=i*values.get(i-start);
			sum+=values.get(i-start);
		}
		return (int)Math.round(ind/sum);
	}
	
	public boolean isIn(ArrayList<ActivityZone> zones, ActivityZone zone){
		
		double zInter = intersection(zone);
		double maxInter = 1;
		double tempInter;
		
		for(ActivityZone z:zones){
			tempInter=intersection(z);
			if(tempInter>maxInter)
				maxInter=tempInter;
		}
		
		if (zInter==maxInter && zInter>0)
			return true;
		else
			return false;
	}
	
	public double intersection(ActivityZone z){
		
		if (z==null)
			return -1;
		
		double sum=0;
		
		for(int i=start;i<=end;i++)
			if ( i>=z.start && i<=z.end )
					sum+=values.get(i-start);
		return sum;
		
	}
	
	
}

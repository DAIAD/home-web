import java.util.ArrayList;
import java.util.Calendar;


public class DayReducedRepresentation {

	ArrayList<SinglePrediction> zones ;
	ArrayList<SinglePrediction> times;
	ArrayList<SinglePrediction> volumes;
	Calendar cal;
	double totalCons;
	
	
	public DayReducedRepresentation(){
		zones   = new ArrayList<SinglePrediction>();
		times   = new ArrayList<SinglePrediction>();
		volumes = new ArrayList<SinglePrediction>();
	}
	
	public double[] produceTimeSeries(ArrayList<Day> days){
		double[] hours = new double[25];
		for(int i=0;i<zones.size();i++)
			if(zones.get(i).value == 1)
				hours = add(hours,typicalPattern(days,times.get(i).value, volumes.get(i).value));
		return hours;
	}
	
	public ArrayList<Pattern> selectPatterns(ArrayList<Day> days, double time, double cons){
		ArrayList<Pattern> selected = new ArrayList<Pattern>();
		for(Day d:days)
			for(Pattern p:d.patterns)
				if(p.getCenter() == (int)time && Math.abs(p.totalConsumption()-cons)<10 )
					selected.add(p);
		return selected;
	}
	
	public double[] typicalPattern(ArrayList<Day> days,double time, double cons){
		
		double[] typicalPat = new double[25];
		ArrayList<Pattern> similPats = selectPatterns(days,time,cons);
		
		if(similPats.size()==0)
			return typicalPat;
		
		for(Pattern p:similPats)
			for(int i=0;i<p.values.size();i++)
				typicalPat[p.start+i]+=p.values.get(i);
		
		for(int i=0;i<25;i++)
			typicalPat[i]/=similPats.size();
	
		return typicalPat;
	
	}
	
	public double[] add(double[] a,double[] b){
		double[] c = new double[a.length];
		for(int i=0;i<a.length;i++)
			c[i]=a[i]+b[i];
		return c;
	}
}

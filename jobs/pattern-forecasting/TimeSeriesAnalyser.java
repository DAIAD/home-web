import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


/**
 * This class contains the methods that perform the pattern recognition functionality. Mainly it identifies the activity zones and their characteristics
 * @author pant
 *
 */
public class TimeSeriesAnalyser {
	
	TsaParameters params;
	
	public TimeSeriesAnalyser(TsaParameters params){
		this.params=params;
	}
	
	public int firstMondayIndex(ArrayList<DataPair> data){
		
		int index = 0;
		while(data.get(index).cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
			index++;
		return index;
		
	}
	
	public double mean(ArrayList<DataPair> data){
		
		double sum=0;
		for(DataPair dp:data)
			sum+=dp.consumption;
		return sum/data.size();
			
	}
	
	public int dstOffset(Calendar cal){
		if (cal.get(Calendar.MONTH)==Calendar.MARCH && cal.get(Calendar.DAY_OF_MONTH)==30)
			return -1;
		else if (cal.get(Calendar.MONTH)==Calendar.OCTOBER && cal.get(Calendar.DAY_OF_MONTH)==27) 
			return 1;
		else
			return 0;
	}
	
	public boolean isWorkday(Calendar cal){
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY )
			return false;
		else
			return true;
	}
	
	public ArrayList<ArrayList<DataPair>> getDays(ArrayList<DataPair> data, int typeOfDay){
		
		ArrayList<ArrayList<DataPair>> days = new ArrayList<ArrayList<DataPair>>();
		int index = firstMondayIndex(data);
		for(int i = index;i<data.size()-24;i+=24+dstOffset(data.get(i).cal)){
			
			if(dstOffset(data.get(i).cal)!=0)
				continue;
			
			if(typeOfDay == 0)
				days.add(new ArrayList<DataPair>(data.subList(i, i+24)));
			else if(typeOfDay == 1 && isWorkday(data.get(i).cal))
				days.add(new ArrayList<DataPair>(data.subList(i, i+24)));
			else if(typeOfDay == 2 && !isWorkday(data.get(i).cal))
				days.add(new ArrayList<DataPair>(data.subList(i, i+24)));		
		
		}
		return days;
	
	}
	
	public ArrayList<Pattern> extractPatterns(ArrayList<DataPair> day){
		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		Pattern pat=null;
		double thres = mean(day)*params.extractPatterns;
		boolean active = false;
		
		for(int i=0;i<24;i++){
			
			if (day.get(i).consumption>thres && !active){
				active=true;
				pat = new Pattern(day.get(i).cal);
			}
			
			else if(day.get(i).consumption<thres && active){
				active=false;
				patterns.add(pat);
			}
			
			if(active)
				pat.addValue(day.get(i).consumption);
			
		}	
		
		if (active)
			patterns.add(pat);
		
		return refinePatterns(patterns);
	
	}
	
	public ArrayList<Pattern> extractPatterns(double[] day){
		ArrayList<Pattern> patterns = new ArrayList<Pattern>();
		Pattern pat=null;
		double thres = mean(day)*params.extractPatterns;
		boolean active = false;
		
		for(int i=1;i<25;i++){
			
			if (day[i]>thres && !active){
				active=true;
				pat = new Pattern(i);
			}
			
			else if(day[i]<thres && active){
				active=false;
				patterns.add(pat);
			}
			
			if(active)
				pat.addValue(day[i]);
			
		}	
		
		if (active)
			patterns.add(pat);
		
		return refinePatterns(patterns);
	
	}
	
	public ArrayList<Pattern> refinePatterns(ArrayList<Pattern> patterns){
		
		ArrayList<Pattern> initial = patterns;
		ArrayList<Pattern> refined = new ArrayList<Pattern>();
		
		while(true){
			
			for(Pattern pat:initial)
				breakAndAdd(refined,pat);
			
			if(refined.size()==initial.size())
				break;
			else{
				initial = refined;
				refined = new ArrayList<Pattern>();
			}
		}
		
		return refined;
	
	}
	
	public void breakAndAdd(ArrayList<Pattern> patterns, Pattern pat ){

		int breakPoint = getBreakPoint(pat);
		
		if ( breakPoint==-1)
			patterns.add(pat);
		else{
			patterns.add(pat.getSubPattern(0, breakPoint));
			patterns.add(pat.getSubPattern(breakPoint, pat.values.size()));
		}
	
	}
	
	public int previousMaximum(Pattern pat,int cur){
		
		int index=0;
		double max = Double.NEGATIVE_INFINITY;
		
		for(int i=cur-1;i>=0;i--)
			if(pat.getValue(i)>max){
				max=pat.getValue(i);
				index=i;
			}
		
		return index;
	
	}
	
	public int previousBoundedMinimum(Pattern pat, int cur, int limit){
		
		int index=0;
		double min= Double.POSITIVE_INFINITY;
		
		for(int i=cur-1;i>=limit;i--){
			if(pat.getValue(i) < min){
				min=pat.getValue(i);
				index=i;
			}
		}
		
		return index;
	
	}
	
	public int getBreakPoint(Pattern pat) {
		int minIndex,maxIndex;
		
		for(int i=0;i<pat.values.size();i++){
			
			maxIndex = previousMaximum(pat,i);
			minIndex = previousBoundedMinimum(pat,i,maxIndex);
			
			if(pat.values.get(minIndex)<params.getBreakPoint*pat.values.get(maxIndex) && pat.values.get(minIndex)<params.getBreakPoint*pat.getValue(i) && pat.values.get(minIndex)>0 && pat.values.get(maxIndex)>0 )
				return minIndex;
				
		}
		
		return -1;
	}
	
	public ArrayList<Day> getDailyPatterns(ArrayList<ArrayList<DataPair>> days){
		ArrayList<Day> dailyPatterns =  new ArrayList<Day>();
		for(ArrayList<DataPair> day : days)
			dailyPatterns.add(new Day(day,extractPatterns(day)));
		return dailyPatterns;		
	}
	
	public double[] activityPerHour(ArrayList<Day> days){
		double[] hours = new double[25];
		for(Day d:days)
			for(Pattern p:d.significantPatterns(params.significantPatterns))
				for(int i=p.start;i<=p.end;i++)
					hours[i]++;

		return hours;
	}
	
	public double[] patternsPerDay(ArrayList<Day> days){
		
		int max=0;
		for(Day d:days)
			if(d.significantPatterns(params.significantPatterns).size()>max)
				max = d.significantPatterns(params.significantPatterns).size();
		
		double[] numbers = new double[max+1];
		
		for(Day d:days)
			numbers[d.significantPatterns(params.significantPatterns).size()]++;
	//	for(int i=0;i<numbers.length;i++)
	//		numbers[i]/=days.size();
		return numbers;
		
	}
	
	public double mean(double[] vec){
		double sum=0;
		for(Double d:vec)
			sum+=d;
		return sum/(vec.length-1);
	}
	
	public int numberOfPatterns(ArrayList<Day> days){
		
		double[] numberOfPattsDistr = patternsPerDay(days);
		int num=0;
		
		for(int i=0; i<numberOfPattsDistr.length; i++){
			if(numberOfPattsDistr[i] >= params.numberOfPatterns*days.size())
				num=i;
		}
		return num;
	}
	
	public int getMax(double[] peaksPerHour){
		int index=0;
		double max = Double.NEGATIVE_INFINITY;
		for(int i=0;i<peaksPerHour.length;i++){
			if (peaksPerHour[i]>max){
				max = peaksPerHour[i];
				index=i;
			}
		}
		return index;
	}
	
	public void clearPeak(double[] peaksPerHour,int start,int end){
		for(int i=start;i<=end;i++)
			peaksPerHour[i]=0;
	}
	
	public int[] findZone(double[] activityPerHour, int peak , double thresh){
		int[] bounds = {peak, peak, peak};
		
		bounds[2]=peak;
		
		for(int i=peak;i>0  && activityPerHour[i]>thresh; i--)
			bounds[0]=i;
		
		for(int i=peak;i<25 && activityPerHour[i]>thresh; i++)
			bounds[1]=i;
	
		return bounds;
	}
	
	public double[] centersPerHour(ArrayList<Day> days){
	
		double[] hours = new double[25];
		for(Day d:days)
			for(Pattern p:d.significantPatterns(params.significantPatterns))
				if(p.values.size()<params.centersPerHour)
					hours[p.getCenter()]++;
		return hours;
	
	}
	
	public ArrayList<ActivityZone> getActivityZones(ArrayList<Day> days,int symbols){
	
		ArrayList<ActivityZone> zones = new ArrayList<ActivityZone>();
		
		double[] centersPerHour = centersPerHour(days);
		int numberOfPats = numberOfPatterns(days);
		double thresh = params.findZone*mean(centersPerHour);
		
		int[] bounds;
		for(int i=0;i<numberOfPats;i++){
			bounds = findZone(centersPerHour,getMax(centersPerHour),thresh);
			zones.add(new ActivityZone(bounds[0],bounds[1],bounds[2]));
			clearPeak(centersPerHour,bounds[0],bounds[1]);
		}
		
		makePatternGroups(days,zones);
		findAggregationZones(days,zones);
		findThresholds(days,zones,symbols);
		return zones;

	}
	
	public int findClosestZone(Pattern p,ArrayList<ActivityZone> zones){
		
		double dist;
		double min = Double.POSITIVE_INFINITY;
		int index=0;
		for(int i=0;i<zones.size();i++){
			dist = Math.abs(p.start-zones.get(i).start);
			if(dist<min ){
				min=dist;
				index=i;
			}
		}
		return index;
	
	}

	
	public void makePatternGroups(ArrayList<Day> days, ArrayList<ActivityZone> zones){
		double[] intersections;
		for(Day d:days)
			for(Pattern p:d.significantPatterns(params.significantPatterns)){
				intersections = findIntersections(zones,p);
				testAndAdd(p,zones,intersections);
			}
					
					
	}
	
	private void testAndAdd(Pattern p, ArrayList<ActivityZone> zones, double[] intersections) {
		
		double max=Double.NEGATIVE_INFINITY,secondMax=Double.NEGATIVE_INFINITY;
		int maxInd=-1;
		
		for(int i=0;i<zones.size();i++){	
			if(intersections[i]>max){
				secondMax=max;
				max=intersections[i];
				maxInd=i;
			}
		}
		
		if(secondMax<max/params.testAndAdd)
			zones.get(maxInd).patterns.add(p);
		
	}

	public double[] findIntersections(ArrayList<ActivityZone> zones, Pattern p) {
		double[] intersections = new double[zones.size()];
		for(int i=0;i<zones.size();i++)
			intersections[i] = p.intersection(zones.get(i));
		return intersections;
	}

	public double[] plainMean(ArrayList<Day> days){
		double[] hours = new double[25];
		for(Day d:days){
			for(int i=1;i<25;i++)
				hours[i]+=d.data.get(i-1).consumption;
		}
		for(int i=0;i<25;i++)
			hours[i]/=days.size();
			
		return hours;
	}
	
	public ArrayList<ArrayList<DataPair>> vectorToDataPairs(ArrayList<double[]> daysVec, Calendar cal){
		
		ArrayList<ArrayList<DataPair>> daysDP = new ArrayList<ArrayList<DataPair>>();
		ArrayList<DataPair> day;
		for(double[] dv : daysVec ){
			day = new ArrayList<DataPair>();
			for(int i=0;i<24;i++){
				day.add( new DataPair( (Calendar)cal.clone(), dv[i+1] ));
				cal.add(Calendar.HOUR_OF_DAY, 1);
			}	
			daysDP.add(day);
			
		}
		
		return daysDP;
		
	}
	
	public ArrayList<DayReducedRepresentation> VecToDRRs( ArrayList<double[]> daysVec, ArrayList<ActivityZone> zones, Calendar cal){
	
		ArrayList<ArrayList<DataPair>> daysDP = vectorToDataPairs(daysVec, cal);
		ArrayList<DayReducedRepresentation> daysDRR = new ArrayList<DayReducedRepresentation>();
		for( ArrayList<DataPair> aldp:daysDP)
			daysDRR.add((new Day(aldp,extractPatterns(aldp)).aggregateDay(zones)));
		return daysDRR;

	}
	
	public static double[] DayToDoubleVec(Day d){
		double[] hours = new double[25];
		for(int i =1; i<25; i++)
			hours[i]=d.data.get(i-1).consumption;
		return hours;
	}
	
	public void countStartsEnds(ArrayList<ActivityZone> zones,int[][] starts,int[][] ends){
		
		for(int i=0;i<zones.size();i++){
			for(Pattern p:zones.get(i).patterns){
				starts[i][p.start]++;
				ends[i][p.end]++;
			}
		}
		
	}
	
	public void defineBounds(ArrayList<ActivityZone> zones, int[][] starts, int[][] ends){
		
		for(int i=0;i<zones.size();i++){
			zones.get(i).aggStart=getMax(starts[i]);
			zones.get(i).aggEnd=getMax(ends[i]);
			for(int j=zones.get(i).aggStart;j>=1;j--){
				if(hasFew(i,j,starts,zones) || collides(i,j,starts,ends,zones))
					break;
				zones.get(i).aggStart=j;
			}
			
			for(int j=zones.get(i).aggEnd;j<=24;j++){
				if(hasFew(i,j,ends,zones) || collides(i,j,ends,starts,zones))
					break;
				zones.get(i).aggEnd=j;
			}
		}
		
	}
	
	private int getMax(int[] a) {
		int index=0;
		int max = Integer.MIN_VALUE;
		
		for(int i=0;i<a.length;i++)
			if(a[i]>max){
				max=a[i];
				index=i;
			}
		return index;
	}

	private boolean collides(int i, int j, int[][] current,  int[][] others, ArrayList<ActivityZone> zones) {
		for(int k=0;k<zones.size();k++)
			if(k==i)
				continue;
			else
			if(current[i][j]<params.collides*others[k][j])
				return true;
		return false;
	}

	private boolean hasFew(int i, int j, int[][] current, ArrayList<ActivityZone> zones) {
		
		if(current[i][j]<params.hasFew*zones.get(i).patterns.size())
			return true;
		else
			return false;

	}

	public void findAggregationZones(ArrayList<Day> days,ArrayList<ActivityZone> zones){
		
		int[][] starts = new int[zones.size()][25];
		int[][] ends = new int[zones.size()][25];
		
		makePatternGroups(days,zones);
		countStartsEnds(zones,starts,ends);
		defineBounds(zones,starts,ends);
		
	}
	
	public void printDays(ArrayList<Day> days) throws FileNotFoundException{
	
		PrintWriter pw = new PrintWriter("/home/pant/Desktop/predicted_days/day");
		
		for(Day d:days){
			for(DataPair dp:d.data){
				pw.print(dp.consumption);
				pw.print(" ");
			}
			pw.println();
		}
		
		pw.close();
		
	}
	
	public int min(int a,int b){
		if (a<b)
			return a;
		else 
			return b;
	}
	
	public int max(int a,int b){
		if (a<b)
			return b;
		else 
			return a;
	}
	
	public double[] findDensities(ArrayList<Double> consumptions){
		
		int N = consumptions.size();
		int step = (int)(0.07*N);
		double[] densities = new double[N]; 
		int upper,lower;
		double number;
		
		for(int i=0;i<N;i++){
			
			upper  = min(i+step,N-1);
			lower  = max(i-step,0);
			number = upper - lower;
	
			densities[i] = number / (consumptions.get(upper) - consumptions.get(lower)+0.0001) ;
		
		}
		
		return densities; 
	
	}
	
	public int findMax(double[] densities,ArrayList<Double> consumptions){
		
		double max = Double.NEGATIVE_INFINITY;
		int index = 0;
		
		for (int i=0;i<densities.length;i++)
			if(densities[i]>max){
				max = densities[i];
				index=i;
			}
		
		return index;
	
	}
	
	public int findMin(double[] densities,int max){
		
		double min = Double.POSITIVE_INFINITY;
		int index = 0;
		
		for(int i=0;i<=max;i++)
			if(densities[i]<min){
				min = densities[i];
				index = i;
			}
		
		return index;
	
	}
	
	public ArrayList<Double> getZoneConsumptions( ArrayList<Day> days, ActivityZone z ){
		ArrayList<Double> consumptions = new ArrayList<Double>();
		double sum;
		for(Day d:days){
			sum=0;
			for(int i=z.aggStart;i<z.aggEnd;i++)
				sum+=d.data.get(i).consumption;
			consumptions.add(sum);
		}
		return consumptions;
	}
	
	public void findDensityThresholds(ActivityZone zone,ArrayList<Double> consumptions){
		
		Collections.sort(consumptions);
		double[] densities = findDensities(consumptions);
		int maxDensIndex = findMax(densities,consumptions);
		int boundIndex = findMin(densities,maxDensIndex);
		
		zone.thresholds.add(consumptions.get(boundIndex));
		zone.thresholds.add(Double.POSITIVE_INFINITY);
	
	}
	
	
	
	public void findSimpleThresholds(ActivityZone zone,ArrayList<Double> consumptions,int symbols){
		
		Collections.sort(consumptions);
		double range = consumptions.get(consumptions.size()-1);
		double step=range/(symbols-1);
		
		for(int i=0;i<symbols-1;i++)
			zone.thresholds.add(i*step);
		zone.thresholds.add(Double.POSITIVE_INFINITY);
		
	}
	
	public void findQuantileThresholds(ActivityZone zone,ArrayList<Double> consumptions,int symbols){
		
		Collections.sort(consumptions);
		int size = consumptions.size();
		int step=size/symbols;
		
		for(int i=0;i<symbols-1;i++){
			zone.thresholds.add(consumptions.get((i+1)*step));
			zone.values.add(levelMean(new ArrayList<Double>(consumptions.subList(i*step, i*step+step))));
		}
		zone.values.add(levelMean(new ArrayList<Double>(consumptions.subList(consumptions.size()-step, consumptions.size()))));
		zone.thresholds.add(Double.POSITIVE_INFINITY);
		
	}
	
	public void findThresholds(ArrayList<Day> days,ArrayList<ActivityZone> zones,int symbols){
		
		for(ActivityZone zone:zones)
			//findSimpleThresholds(zone,getZoneConsumptions(days,zone),symbols);
			//findQuantileThresholds(zone,getZoneConsumptions(days,zone),symbols);
			findDensityThresholds(zone,getZoneConsumptions(days,zone));
	}
	
	public double levelMean(ArrayList<Double> values){
		double sum=0;
		for(Double v:values)
			sum+=v;
		return sum/values.size();
	}
	
	
}

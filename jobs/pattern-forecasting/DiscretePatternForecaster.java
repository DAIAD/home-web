import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class that predicts whether there will be significant activity in each activity zone of the next day
 * @author pant
 *
 */
public class DiscretePatternForecaster {
	
	private GeneralSR[] gsrs;
	private ArrayList<ActivityZone> zones;
	private ArrayList<Double> thresholds;
	private int type;
	
	public DiscretePatternForecaster(int type){
		this.type=type;
	}
	
	private void makeKeyVals(ArrayList<DayReducedRepresentation> drrs, ArrayList<Integer[]> keys, ArrayList<Integer> vals, int zone){
	
		for(int i=0;i<drrs.size()-1;i++){
			keys.add(newKey(drrs.get(i)));
			vals.add((int) drrs.get(i+1).zones.get(zone).value);
		}
			
	}
	
	private Integer[] newKey(DayReducedRepresentation drr) {
	
		Integer[] key = new Integer[drr.zones.size()];
		for(int i=0;i<drr.zones.size();i++)
			key[i] = (int)drr.zones.get(i).value;
		return key;
	
	}
	
	private static ArrayList<Double> getZoneConsumptions( ArrayList<Day> days, ActivityZone z ){
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
	
	private static int min(int a,int b){
		if (a<b)
			return a;
		else 
			return b;
	}
	
	private static int max(int a,int b){
		if (a<b)
			return b;
		else 
			return a;
	}
	
	private static double[] findDensities(ArrayList<Double> consumptions){
		
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
	
	private static int findMax(double[] densities,ArrayList<Double> consumptions){
		
		double max = Double.NEGATIVE_INFINITY;
		int index = 0;
		
		for (int i=0;i<densities.length;i++)
			if(densities[i]>max){
				max = densities[i];
				index=i;
			}
		
		return index;
	
	}
	
	private static int findMin(double[] densities,int max){
		
		double min = Double.POSITIVE_INFINITY;
		int index = 0;
		
		for(int i=0;i<=max;i++)
			if(densities[i]<min){
				min = densities[i];
				index = i;
			}
		
		return index;
	
	}
	
	private Double findThreshold(ArrayList<Double> consumptions){
		
		Collections.sort(consumptions);
		double[] densities = findDensities(consumptions);
		int maxDensIndex = findMax(densities,consumptions);
		int boundIndex = findMin(densities,maxDensIndex);
		
		return consumptions.get(boundIndex);
	
	}
	
	private ArrayList<Double> findThresholds(ArrayList<Day> days, ArrayList<ActivityZone> zones){
		
		ArrayList<Double> thresholds = new ArrayList<Double>();
		for(int i=0;i<zones.size();i++)
			thresholds.add(findThreshold(getZoneConsumptions(days,zones.get(i))));
		return thresholds;
	
	}
	/**
	 * Function that trains the model. It discretizes the consumption for each activity zone into different classes and trains a model that predicts
	 * the class of the consumption in the next day's activity zones
	 * @param days	    A list of the days used for the training
	 * @param zones	    A list of AcivityZone objects for the activity zones of the days 
	 * @param symbols	Parameter that defines the number of different classes 
	 * @throws FileNotFoundException
	 */
	public void train(ArrayList<Day> days, ArrayList<ActivityZone> zones,int symbols) throws FileNotFoundException{
		
		init(zones,symbols);
		thresholds = findThresholds(days,zones);
		ArrayList<DayReducedRepresentation> drrs = aggregateDays(days,zones);
		
		ArrayList<Integer[]> keys; 
		ArrayList<Integer> values; 
		
		for(int i=0;i<zones.size();i++){
			keys = new ArrayList<Integer[]>();
			values = new ArrayList<Integer>();
			makeKeyVals(drrs,keys,values,i);
			gsrs[i].train(keys,values);
			//gsrs[i].dumpCounts(i);
		}
	
	}
	
	/**
	 * Function that predicts the activity zones of the next day
	 * @param prevDay A day object containing the consumption of the previous day 
	 * @return A DayReducedRepresentation object containing the predictions for the next day 
	 */
	public DayReducedRepresentation predict(Day prevDay){
		Integer[] key = newKey(prevDay.aggregateDay(zones));
		DayReducedRepresentation drr = new DayReducedRepresentation();
		
		for(int i=0;i<zones.size();i++){
			if(type==0)
				drr.zones.add(new SinglePrediction(gsrs[i].predict(key),1));
			if(type==1)
				drr.zones.add(new SinglePrediction(gsrs[i].independentPredict(key),1));
		}
		return drr;
	}

	/**
	 * Function that tests the model against a time series with known values and returns the error.
	 * @param days A list containing the days against which to test
	 * @return Returns a Results object with the predictions and the error.
	 */
	public Results test(ArrayList<Day> days){
		ArrayList<DayReducedRepresentation> drrs = new ArrayList<DayReducedRepresentation>();
		
		for(int i=0;i<days.size()-1;i++)
			drrs.add(predict(days.get(i)));
		
		return new Results(days,drrs,null,zones);
	}
	
	private void init(ArrayList<ActivityZone> zones,int symbols) {
		
		this.zones = zones;
		
		gsrs = new GeneralSR[zones.size()];
		for(int i=0;i<zones.size();i++)
			gsrs[i] = new GeneralSR(symbols);
		
	}

	private ArrayList<DayReducedRepresentation> aggregateDays( ArrayList<Day> days, ArrayList<ActivityZone> zones) {
		ArrayList<DayReducedRepresentation> drrs = new ArrayList<DayReducedRepresentation>();
		for(Day d:days)
			drrs.add(d.aggregateDay(zones));
		return drrs;
	}
	
}

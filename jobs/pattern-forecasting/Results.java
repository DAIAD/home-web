import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.dtw.DTWSimilarity;
import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;

/**
 * Class that contains the results of a forecasting algorithm and calculates the error.
 * @author pant
 *
 */
public class Results {

	/**
	 * The actual water consumption.
	 */
	public ArrayList<Day> days;
	/**
	 * The activity zones of the time series
	 */
	public ArrayList<ActivityZone> zones;
	/**
	 * The prediction for the activity zones
	 */
	public ArrayList<DayReducedRepresentation> predDRR;
	/**
	 * The prediction for the water consumption
	 */
	public ArrayList<double[]> predTS;
	
	/**
	 * An object that calculates the accuracy of the activity zone prediction
	 */
	HighLevelError hle;
	/**
	 *  A list containing the errors
	 */
	public ArrayList<Double> errors;
	/**
	 * A list containing the respective error names
	 */
	public ArrayList<String> errorNames;
	
	public Results(){
		
		errors = new ArrayList<Double>();
		errorNames = new ArrayList<String>();
	
	}
	
	/**
	 * This constructor initializes the respective fields and calculates the errors 
	 * @param days    Variable to initialize the respective object field
	 * @param predDRR Variable to initialize the respective object field
	 * @param predTS  Variable to initialize the respective object field
	 * @param zones   Variable to initialize the respective object field
	 */
	public Results(ArrayList<Day> days, ArrayList<DayReducedRepresentation> predDRR,ArrayList<double[]> predTS, ArrayList<ActivityZone> zones){
		
		this.days    = new ArrayList<Day>(days.subList(1, days.size()));
		this.predDRR = predDRR;
		this.predTS  = predTS;
		this.zones   = zones;
		
		errors     = new ArrayList<Double>();
		errorNames = new ArrayList<String>();
		
	//	test.close();
		calcAllErrors();	
					
	}
	
	/**
	 * Calculates all errors from the given object fields
	 */
	public void calcAllErrors() {
		
		if(predTS!=null){
			errors.add( NMAE() );
			errorNames.add("NMAE");
		
			errors.add( NMSE() );
			errorNames.add("NMSE");
		
			errors.add( DTWError() );
			errorNames.add("DTW");
		}
		
		if( predDRR!=null){
		
			hle = new HighLevelError(days,predDRR,zones);
		
			errors.add( hle.meanACC );
			errorNames.add("ACC");
		
		/*	errors.add( hle.consMAPE );
			errorNames.add("HLE-CONS-MAPE");
		
			errors.add( hle.consMAE );
			errorNames.add("HLE-CONS-MAE");
		
			errors.add( hle.timeMAE );
			errorNames.add("HLE-TIME-MAE");
		*/
		}
		
	}
	
	
	private double mean(ArrayList<Day> days){
		double m = 0;
		for(Day d:days)
			for(DataPair dp:d.data)
				m+=dp.consumption;
		return m/(24*days.size());
	}
	
	private double mean(Day d){
		double m = 0;
		for(DataPair dp:d.data)
				m+=dp.consumption;
		return m/24;
	}
	
	private double AEofDay(Day d, double[] hours){
		double err=0;
		for(int i=0;i<24;i++)
			err+=Math.abs(d.data.get(i).consumption-hours[i+1]);
		return err;
	}
	
	private double NMAE(){
		double err=0;
		for(int i=0;i<days.size();i++)
			err+=AEofDay(days.get(i),predTS.get(i));
		return err/(mean(days)*days.size()*24);
	}
	
	double DTWError(){
		return DTW.getWarpDistBetween(new TimeSeries(daysToInstance()),new TimeSeries(predToInstance()))/(mean(days)*days.size()*24);
	}
	
	private Instance daysToInstance() {
		double[] array = new double[days.size()*24];
		for(int i=0;i<days.size();i++)
			for(int j=0;j<24;j++)
				array[i*24+j]=days.get(i).data.get(j).consumption;
		Instance di = new DenseInstance(array);
		return di;
	}

	private Instance predToInstance() {
		double[] array = new double[days.size()*24];
		for(int i=0;i<predTS.size();i++)
			for(int j=0;j<24;j++)
				array[i*24+j]=predTS.get(i)[j];
		Instance di = new DenseInstance(array);
		return di;
	}

	private double SEofDay(Day d, double[] hours){
		double err=0;
		for(int i=0;i<24;i++)
			err+=Math.pow(d.data.get(i).consumption-hours[i+1],2);
		return err;
	}
	
	private double NMSE(){
		double err=0;
		for(int i=0;i<days.size();i++)
			err+=SEofDay(days.get(i),predTS.get(i));
		return err/(mean(days)*days.size()*24);
	}
	
	private int findMatch(Day d,double[] hours,int i, boolean[] taken, int step){
		double min = Double.POSITIVE_INFINITY;
		int minInd = -1;
		for(int j=i-step;j<=i+step;j++){
			
			if( j<0 || j>=24 )
				continue;
			
			if( Math.abs(hours[i+1]-d.data.get(j).consumption) < min && !taken[j] ){
				min = Math.abs(hours[j+1]-d.data.get(j).consumption);
				minInd = j;
			}
		}
		
		if(minInd>0)
			taken[minInd]=true;
		return minInd;
	}
	
	private double GTSEofDay(Day d, double[] hours, int step, double m){
		
		boolean[] taken = new boolean[24];
		double err=0;
		
		int matchInd;
		for(int i=0;i<24;i++){
			matchInd = findMatch(d,hours,i,taken,step);
			if(matchInd>0)
				err += Math.abs( hours[i+1]-d.data.get(matchInd).consumption );
			else
				err += Math.abs( hours[i+1] );
				
		}
		err/=24*m;
		return err;
		
	}
	
	private double GTSE( int step){
		
		double m = mean(days);
		double err=0;
		for(int i=0;i<days.size();i++)
			err+=GTSEofDay(days.get(i),predTS.get(i),step,m);
		return err/days.size();
		
	}
	
	public Results clone(){
		Results clone = new Results();
		clone.days=days;
		clone.zones=zones;
		clone.predDRR = cloneDRR();
		clone.predTS  = cloneTS();
		return clone;
	}

	private ArrayList<double[]> cloneTS() {
		ArrayList<double[]> tsClone = new ArrayList<double[]>();
		for(double[] d:predTS)
			tsClone.add(copy(d));
		return tsClone;
	}

	private double[] copy(double[] d) {
		double[] c = new double[d.length];
		for(int i=0;i<d.length;i++)
			c[i] = d[i];
		return c;
	}

	private ArrayList<DayReducedRepresentation> cloneDRR() {
		ArrayList<DayReducedRepresentation> drrClone = new ArrayList<DayReducedRepresentation>();
		for(DayReducedRepresentation drr:predDRR)
			drrClone.add(copy(drr));
		return drrClone;
	}

	private DayReducedRepresentation copy(DayReducedRepresentation drr) {
		DayReducedRepresentation drrCopy = new DayReducedRepresentation();
		for(int i=0;i<drr.zones.size();i++)
			drrCopy.zones.add(new SinglePrediction(drr.zones.get(i).value,1));
		return drrCopy;
	}
}

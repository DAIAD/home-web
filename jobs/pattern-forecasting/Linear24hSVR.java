import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Class that implements the Support Vector Regression model and provides forecasting functionality. 
 * It contains 24 separate SVR models, one for each hour of the day.   
 * @author pant
 *
 */
public class Linear24hSVR{

	/**
	 * The array containing the SVR models of each hour.
	 */
	public LinearSingleHourSVR[] svrs;
	private double scaleFactor;
	
	public Linear24hSVR(){
		svrs = new LinearSingleHourSVR[24];
	}
	
	private double findMax(ArrayList<Day> days){
		double max = Double.NEGATIVE_INFINITY;
		for(Day d:days)
			for(DataPair dp:d.data)
				if(dp.consumption>max)
					max=dp.consumption;
		return max;
	}
	
	private void scale(ArrayList<Day> days){
		for(Day d:days)
			for(DataPair dp:d.data)
				dp.consumption/=scaleFactor;
	}
	
	private void scale(Day d){
		for(DataPair dp:d.data)
			dp.consumption/=scaleFactor;
	}
	
	private void unScale(ArrayList<Day> days){
		for(Day d:days)
			for(DataPair dp:d.data)
				dp.consumption*=scaleFactor;
	}
	
	private void unScale(Day d){
			for(DataPair dp:d.data)
				dp.consumption*=scaleFactor;
	}
	
	private void unScale(double[] hours){
		for(int i=0;i<hours.length;i++)
			hours[i]*=scaleFactor;
	}
	
	/**
	 * Function that trains the SVR models. Receives the C and Epsilon parameters directly
	 * @param days The training set for the models. It contains the historical values. 
	 * @param start The index of the first day from the training set to be used.
	 * @param end	The index of the last day from the training set to be used.
	 * @param c		Parameter C of the SVR. Controls regularization. 
	 * @param e		Parameter Epsilon of the SVR. 
	 */
	public void train(ArrayList<Day> days,int start,int end, double c, double e){
		scaleFactor = findMax(days);
		scale(days);
		for(int i=0;i<24;i++)
			svrs[i] = new LinearSingleHourSVR(days, start, end, i, c, e);
		unScale(days);
	}
	
	private static ArrayList<Double> readParams(String id,String path) throws IOException{
		
		FileInputStream fstream = new FileInputStream(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line;
		
		while(true){
			
			line = br.readLine();
			if(line==null)
				break;
			
			if(line.split(" ")[0].equals(id)){
				br.close();
				return getParams(line);
			}
		
		}
		br.close();
		return null;
		
	}
	
	private static ArrayList<Double> getParams(String line) {
		ArrayList<Double> params = new ArrayList<Double>();
		String[] str = line.split(" ");
		Double buf = Double.parseDouble(str[1]);
		if (buf==0.0)
			buf+=0.0000001;
		params.add(buf);
		buf = Double.parseDouble(str[2]);
		if (buf==0.0)
			buf+=0.0000001;
		params.add(buf);
		return params;
	}

	/**
	 * Function that trains the SVR models. Reads the parameters C and Epsilon from a specified file.
	 * 
	 * @param days  The training set for the models. It contains the historical values. 
	 * @param start The index of the first day from the training set to be used.
	 * @param end	The index of the last day from the training set to be used.
	 * @param paramPath  The path of the parameter file. The parameter file must have the following columns: id		C	Epsilon
	 * @param id	The id of the time series 
	 * @throws IOException
	 */
	public void train(ArrayList<Day> days,int start,int end, String paramPath, String id) throws IOException{
		scaleFactor = findMax(days);
		scale(days);
		ArrayList<Double> params = readParams(id,paramPath);
		for(int i=0;i<24;i++)
			svrs[i] = new LinearSingleHourSVR(days, start, end, i, params.get(0), params.get(1));
		unScale(days);
	}

	/**
	 * Function that predicts the water consumption of the following day.
	 * @param days The historical values of consumption
	 * @param d	   Index of the current day
	 * @return	   Returns a vector of the hourly consumption for the 24 hours of the next day. At index i of the vector 
	 * is the consumption from hour i-1 to hour i  
	 */
	public double[] predict(ArrayList<Day> days, int d){
		
		double[] hours = new double[25];
			
		for(int i=0;i<24;i++){
			if(svrs[i]!=null)
				hours[i+1] = svrs[i].predict(days,d);
			else
				hours[i+1]=0;
		}
		
		unScale(hours);
		
		return hours;
	}
	
	/**
	 * Function that performs grid search for optimal parameters C and Epsilon.  
	 * @param days 		 The training set on which to perform the optimization
	 * @param trainStart The index of the first day from the training set to be used for training. 	
	 * @param trainEnd   The index of the last day from the training set to be used for training.
	 * @param valStart	 The index of the first day from the training set to be used for validation.
	 * @param valEnd	 The index of the first day from the training set to be used for validation.
	 * @param params	 An object of class TsaParameters
	 * @param zones		 A list of the Activity Zones of the training set
	 * @param pw		 The file on which to output the resulting parameters
	 * @param id		 An id for the time series
	 * @throws FileNotFoundException
	 */
	public void optimize(ArrayList<Day> days,int trainStart,int trainEnd,int valStart,int valEnd,TsaParameters params, ArrayList<ActivityZone> zones, PrintWriter pw, String id) throws FileNotFoundException{
		
		double min=Double.POSITIVE_INFINITY,minC = 0,minE = 0;
		Linear24hSVR lsvr;
		Results res;
		
		for(double c=Math.pow(10,-6);c<1001;c*=10){
			for(double e=Math.pow(10,-6);e<1001;e*=10){
				lsvr = new Linear24hSVR();
				lsvr.train(days,trainStart,trainEnd,c,e);
				res = lsvr.test(days, valStart, valEnd, params, zones);
				if (res.errors.get(0)<min){
					min = res.errors.get(0);
					minC=c;
					minE=e;
				}
			}
		}
		pw.print(id);
		pw.print(" ");
		pw.print(minC);
		pw.print(" ");
		pw.println(minE);
		pw.flush();
	}
	
	/**
	 * Function that tests the model against a time series with known values and returns the error.
	 * @param days  The available dataset
	 * @param start	Index for the starting day of the test
	 * @param end	Index for the ending day of the test
	 * @param params An TsaParameters object for the time series
	 * @param zones	A list of the Activity Zones of the dataset
	 * @return Returns an object of class Results containing the predictions and the errors.
	 * @throws FileNotFoundException
	 */
	public Results test(ArrayList<Day> days,int start,int end,TsaParameters params, ArrayList<ActivityZone> zones) throws FileNotFoundException{
		
		scale(days);
		ArrayList<double[]> pred = new ArrayList<double[]>();
		for(int i=start;i<end-1;i++)
			pred.add(predict(days,i));
		unScale(days);
		
		//PrintWriter test = new PrintWriter("/home/pant/Desktop/predicted_days/svr");
		TimeSeriesAnalyser tsa = new TimeSeriesAnalyser(params);
		return new Results(new ArrayList<Day>(days.subList(start, end)),tsa.VecToDRRs(pred, zones, days.get(1).cal),pred,zones);
	
	}
	
	
}

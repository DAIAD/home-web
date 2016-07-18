import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;


public class PatternForecaster {

	DiscretePatternForecaster dpf;
	DiscretePatternFilter fil;
	Linear24hSVR svr;
	
	String input_path;
	
		
	public void train(String input_path,double c,double e) throws IOException, NumberFormatException, ParseException{
		
		this.input_path=input_path;
		ArrayList<DataPair> ts;
		ArrayList<ArrayList<DataPair>> dailyData;
		ArrayList<Day> days;
		TsaParameters params = new TsaParameters();
		TimeSeriesAnalyser tsa = new TimeSeriesAnalyser(params);
		
		Preprocessor ps = new Preprocessor(input_path);
		ts = ps.getNextSeries();		
		dailyData = tsa.getDays(ts,0);
		days = tsa.getDailyPatterns(dailyData);
		ArrayList<ActivityZone> zones = tsa.getActivityZones(days,2);
		
		dpf = new DiscretePatternForecaster(1);
		dpf.train(days,zones,2);	
	
		svr = new Linear24hSVR();
		svr.train(days,0,days.size(),c,e);
		
		fil = new DiscretePatternFilter();
	
	}
	
	public void test(String output_path) throws IOException, NumberFormatException, ParseException{
		
		ArrayList<DataPair> ts;
		ArrayList<ArrayList<DataPair>> dailyData;
		ArrayList<Day> days;
		TsaParameters params = new TsaParameters();
		TimeSeriesAnalyser tsa = new TimeSeriesAnalyser(params);
		
		Preprocessor ps = new Preprocessor(input_path);
		ts = ps.getNextSeries();		
		dailyData = tsa.getDays(ts,0);
		days = tsa.getDailyPatterns(dailyData);
		ArrayList<ActivityZone> zones = tsa.getActivityZones(days,2);
	
		DayReducedRepresentation drr = dpf.predict(days.get(days.size()-1));
		double[] pred = svr.predict(days, days.size()-1);
		
		pred=fil.filter(drr, pred, zones);
		
		writePred(output_path,pred);
		
	}

	private void writePred(String output_path, double[] pred) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(output_path);
		for(double d:pred){
			pw.print(d);
			pw.print(" ");
		}
		pw.close();
	}
	
	
}

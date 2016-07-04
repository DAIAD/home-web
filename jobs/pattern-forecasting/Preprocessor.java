import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This class reads the water consumption time series from a csv file and performs basic preprocessing
 * @author pant
 *
 */
public class Preprocessor {

	
	BufferedReader br;
	
	ArrayList<DataPair> ts;
	ArrayList<CSVRecord> csv;
	String name;
	int index;
	boolean hasMore;
	
	public Preprocessor(String path) throws IOException{
		readCSV(path);
	}

	public void readCSV(String path) throws IOException{
		 Reader in = new FileReader(path);
		 CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withDelimiter(','));
		 csv = (ArrayList<CSVRecord>) parser.getRecords();
		 in.close();
		 index = 1;
		 hasMore = true;
	}
	
	public void reset(){
		index=1;
		hasMore=true;
	}
	
	public ArrayList<DataPair> getNextSeries() throws NumberFormatException, ParseException{
		ts = new ArrayList<DataPair>();
		int i;
		name = csv.get(index).get(0);
		
		ts.add(new DataPair(csv.get(index).get(1),(double)Double.parseDouble(csv.get(index).get(2)),(double)Integer.parseInt(csv.get(index).get(3))));
		
		for(i=index+1;i<csv.size();i++){
			if( index==csv.size()-1 || csv.get(i).get(0).equals(csv.get(i-1).get(0)))
				ts.add(new DataPair(csv.get(i).get(1),(double)Double.parseDouble(csv.get(i).get(2)),(double)Integer.parseInt(csv.get(i).get(3)) ));
			else
				break;
		}
		
		index=i;
		if(index==csv.size())
			hasMore=false;
		
		return ts;
	}
	
	public int locateDay(ArrayList<Day> year, String date) throws ParseException{
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		long targetTime = df.parse(date).getTime();
		
		for(int i=0;i<year.size();i++)
			if(year.get(i).cal.getTime().getTime() == targetTime)
				return i;
		return -1;
	}
	
	public Integer[] getSpring(ArrayList<Day> year) throws ParseException{
		
		Integer[] indices = new Integer[4];
		String train = "2014/03/03 01:00";
		String eval  = "2014/04/28 01:00";
		String test  = "2014/05/12 01:00";
		String end	 = "2014/05/26 01:00";
		
		indices[0] = locateDay(year,train);
		indices[1] = locateDay(year,eval);
		indices[2] = locateDay(year,test);
		indices[3] = locateDay(year,end);
		
		return indices;
		
	}
	
public Integer[] getAutumn(ArrayList<Day> year) throws ParseException{
		
		Integer[] indices = new Integer[4];
		String train = "2013/09/02 01:00";
		String eval  = "2013/10/28 01:00";
		String test  = "2013/11/11 01:00";
		String end	 = "2013/11/25 01:00";
		
		indices[0] = locateDay(year,train);
		indices[1] = locateDay(year,eval);
		indices[2] = locateDay(year,test);
		indices[3] = locateDay(year,end);
		
		return indices;
		
	}

	public Integer[] getRollingIndices(ArrayList<DataPair> year, int offset)
	{

	Integer[] indices = new Integer[4];
	indices[0] = (6*24+offset*7*24) % 8760;
	indices[1] = (indices[0] + 41*7*24) % 8760;
	indices[2] = (indices[1] + 5*7*24) % 8760;
	indices[3] = (indices[2] + 5*7*24) % 8760;
	
	return indices;
	
	}
	
	static double findMax(ArrayList<DataPair> original){
		double max = Double.NEGATIVE_INFINITY;
		
		for(DataPair dp:original)
			if (dp.consumption>max)
				max=dp.consumption;
		return max;
		
	}
	
	static double findMin(ArrayList<DataPair> original){
		double min = Double.POSITIVE_INFINITY;
		
		for(DataPair dp:original)
			if (dp.consumption<min)
				min=dp.consumption;
		return min;
		
	}
	
	static ArrayList<Double> scaleUp(ArrayList<Double> original,double a){
		
		ArrayList<Double> scaled = new ArrayList<Double>();
		for(Double d:original)
			scaled.add(d*a);
		return scaled;
		
	}
	
	static ArrayList<DataPair> scaleDown(ArrayList<DataPair> original,double a){
		
		ArrayList<DataPair> scaled = new ArrayList<DataPair>();
		for(DataPair dp:original)
			scaled.add(new DataPair(dp.cal,dp.consumption/a));
		return scaled;
	
	}
	
	ArrayList<DataPair> delta(ArrayList<DataPair> data) throws ParseException{
		
		ArrayList<DataPair> tData = new ArrayList<DataPair>(data.size()-1);
		for(int i=1;i<data.size();i++)
			tData.add(new DataPair(data.get(i).date, data.get(i).cal, data.get(i).consumption-data.get(i-1).consumption));
		return tData;
		
	}
	
	ArrayList<DataPair> reverse(ArrayList<DataPair> original){
		
		int len = original.size();
		ArrayList<DataPair> reverse = new ArrayList<DataPair>(len);
		for(int i=0;i<len;i++)
			reverse.add(original.get(len-i-1));
		return reverse;
	
	}
	
	double[] extractDoubles(ArrayList<DataPair> data){
		
		double[] buf = new double[data.size()];
		for(int i=0;i<buf.length;i++)
			buf[i]=data.get(i).consumption;
		return buf;
		
	}
	
	ArrayList<Double> extractALDouble(ArrayList<DataPair> data){
		
		ArrayList<Double> buf = new ArrayList<Double>(data.size());
		for(int i=0;i<data.size();i++)
			buf.add(data.get(i).consumption);
		return buf;
		
	}
	
	public ArrayList<DataPair> getSeries(String name) throws NumberFormatException, ParseException{
		reset();
		ArrayList<DataPair> ts;
		while(hasMore){
			ts = getNextSeries();
			if (name.equals(this.name))
				return ts;
		}
		return null;
	}
	
	public ArrayList<DataPair> getSeries(int number) throws NumberFormatException, ParseException{
		reset();
		ArrayList<DataPair> ts=null;
		int count=-1;
		
		while(count!=number && hasMore){
			ts = getNextSeries();
			if(isBroken(ts) || isCountry(ts))
				continue;
			count++;
		}
		
		if(count==number)
			return ts;
		else
			return null;
		
	}
	
	public boolean isBroken(ArrayList<DataPair> series){
		for(DataPair dp:series)
			if(dp.consumption<0 || dp.consumption>800)
				return true;
		if(series.size()>500*24)
			return true;
		return false;
	}
	
	public double sum(ArrayList<DataPair> year, int start, int end){
		double sum= 0;
		for (int i=start; i<end;i++)
			sum+=year.get(i).consumption;
		return sum;
	}
	
	public ArrayList<Double> getWeeks(ArrayList<DataPair> series){
		ArrayList<Double> weeks = new ArrayList<Double>();
		for(int i=0;i<series.size()-168;i+=168)
			weeks.add(sum(series,i,i+168));
		return weeks;	
	}
	
	public boolean isCountry(ArrayList<DataPair> year){
		ArrayList<Double> weeks = getWeeks(year);
		double count = 0;
		for(Double w:weeks)
			if (w==0)
				count++;
		if(count>0.5*year.size()/7)
			return true;
		else
			return false;
	}
	
	public static ArrayList<Double> getDoubles(ArrayList<DataPair> dataPairs,int start,int end){
		ArrayList<Double> doubles = new ArrayList<Double>(end-start);
		for(int i=start;i<end;i++)
			doubles.add(dataPairs.get(i).consumption);
		return doubles;
	}
	
	public static int getCalTime(Calendar cal){
		int time = cal.get(Calendar.HOUR_OF_DAY);
		if (time==0)
			time=24;
		return time;
				
	}
	
}

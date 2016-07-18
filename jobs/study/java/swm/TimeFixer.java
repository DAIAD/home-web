import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class TimeFixer {
	
	public static int measurementsDiff(ArrayList<DataPair> ts){
		int expMeas = (int) ((ts.get(ts.size()-1).cal.getTimeInMillis() - ts.get(0).cal.getTimeInMillis())/3600000);
		int actMeas = ts.size();
		
		return actMeas - expMeas;
	}

	static void  checkDates(String path, String[] ids,String out_path) throws NumberFormatException, IOException, ParseException{
		PrintWriter out = new PrintWriter(out_path);
		for(String id : ids){
			ArrayList<DataPair> ts = NewDataPreprocessor.reverse(NewDataPreprocessor.getField(path,id));
			//System.out.println(id + " : " + ts .get(0).date + " - " + ts.get(ts.size()-1).date);
			out.println(id + ":" +ts .get(0).date + " - " +ts.get(ts.size()-1).date);
			
		}
		
		out.close();
	}
	
	public static ArrayList<Integer> findErrors(ArrayList<DataPair> dp){
		
		Calendar refCal = (Calendar) dp.get(1).cal.clone();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		ArrayList<Long> errors = new ArrayList<Long>();
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		
		for(int i=1;i<dp.size();i++){
			if (Math.abs(dp.get(i).cal.getTimeInMillis() - refCal.getTimeInMillis()) > 1000*60*30){
				
			//	System.out.println(df.format(dp.get(i-1).cal.getTime()) + " - " + df.format(dp.get(i).cal.getTime()) + " --- " + df.format(refCal.getTime())+"  "+Double.toString(dp.get(i).consumption - dp.get(i-1).consumption ));
				
				indices.add(i);
				errors.add( ( dp.get(i).cal.getTimeInMillis() - refCal.getTimeInMillis() )/1000/60 );
				refCal = (Calendar) dp.get(i).cal.clone();
		
			}
			refCal.add(Calendar.HOUR_OF_DAY, 1);
		}
			
		return indices;
		
	}
	
	public static double mean(ArrayList<DataPair> ts){
		double sum=0.0;
		for (DataPair dp:ts)
			sum+=dp.consumption;
		return sum/ts.size();
	}
	
	public static double specificMean(ArrayList<DataPair> ts, ArrayList<Integer> indices){
		double sum = 0.0;
		for(Integer i : indices)
			sum+=ts.get(i).consumption;
		return sum/indices.size();
	}
	
	
	public static void checkErrors(String path, String[] ids,String out_path) throws NumberFormatException, IOException, ParseException{
		PrintWriter out = new PrintWriter(out_path);
		for(String id : ids){
			ArrayList<DataPair> ts = NewDataPreprocessor.delta(NewDataPreprocessor.reverse(NewDataPreprocessor.getField(path,id)));
			ArrayList<Integer> indices = findErrors(ts);
			//System.out.println(id + " : " +mean(ts) + " - " +specificMean(ts,indices));
			out.println(id + " : " +mean(ts) + " - " +specificMean(ts,indices));
			
		}
		
		out.close();
	}
	
	public static int firstIndex(Calendar cal,long zeroHour){
		return (int) ((cal.getTimeInMillis()-zeroHour)/60000);
	}
	
	public static int lastIndex(Calendar cal,long zeroHour){
		return (int) ((cal.getTimeInMillis()-zeroHour)/60000)-1;
	
	}
	
	public static double[] makeMinutes(ArrayList<DataPair> ts, long zeroHour){
		double[] minutes = new double[525600];
		int start,end;
		double cons=0.0;
		Calendar prev,cur;
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		for(int i=1;i<ts.size();i++){
			cur  = ts.get(i).cal;
			prev = ts.get(i-1).cal;
			start = firstIndex(prev,zeroHour);
			end = lastIndex(cur,zeroHour);
			cons = ts.get(i).consumption/(end-start);
			for(int j=start;j<end-1;j++)
				minutes[j]+=cons;
		}
		return minutes;
	}
	
	public static ArrayList<DataPair> makeTS(double[] minutes,long zeroHour){
		
		ArrayList<DataPair> fixed = new ArrayList<DataPair>();
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date(zeroHour));
		
		double sum;
		for(int i=0;i<minutes.length;i+=60){
			sum=0;
			for(int j=0;j<60;j++)
				sum+=minutes[i+j];
			fixed.add(new DataPair((Calendar)cal.clone(),sum));
			cal.add(Calendar.HOUR_OF_DAY, 1);
			
		}
		
		return fixed;
	}
	
	public static ArrayList<DataPair> fixTS1(ArrayList<DataPair> ts) throws ParseException{
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		long zeroHour = df.parse("2014/01/01 00:00").getTime();
		
		double[] minutes = makeMinutes(ts,zeroHour);
		ArrayList<DataPair> fixed = makeTS(minutes,zeroHour);
		
		return fixed;
	
	}
	
	public static ArrayList<DataPair> getInterval(ArrayList<DataPair> ts, long end,int index){
		ArrayList<DataPair> intrvl = new ArrayList<DataPair>();
		for(int i=index; i<ts.size() && ts.get(i).cal.getTimeInMillis() < end ;i++)
			intrvl.add(ts.get(i));
		return intrvl;
	}
	
	public static double sumConsumptions(ArrayList<DataPair> ts){
		double sum=0.0;
		for(DataPair dp:ts)
			sum+=dp.consumption;
		return sum;
	}
	
	public static ArrayList<DataPair> fixTS2(ArrayList<DataPair> ts) throws ParseException {
		ArrayList<DataPair> fixed = new ArrayList<DataPair>(8760);
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Calendar cal = new GregorianCalendar();
		cal.setTime(df.parse("2014/01/01 00:00"));
		int index=0;
		ArrayList<DataPair> intrvl;
		for(int i=0;i<8760;i++){
			cal.add(Calendar.HOUR_OF_DAY, 1);
			intrvl = getInterval( ts, cal.getTimeInMillis(),index );
			if(intrvl.size()==0){
				if (fixed.size()<24)
					fixed.add(new DataPair((Calendar)cal.clone(),0.0));
				else
					fixed.add(new DataPair((Calendar)cal.clone(),fixed.get(i-24).consumption));
			}
				
			else 
				fixed.add(new DataPair((Calendar)cal.clone(), sumConsumptions(intrvl) ));
			
			index+=intrvl.size();
		}
		
		return fixed;
	}

}

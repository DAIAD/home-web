import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class TsaParameters {
	
	double extractPatterns;
	double getBreakPoint;
	double centersPerHour;
	double significantPatterns;
	double numberOfPatterns;
	double findZone;
	double testAndAdd;
	double hasFew;
	double collides;
	
	public TsaParameters(){
		extractPatterns 	= 0.1;
		getBreakPoint   	= 0.5;
		centersPerHour  	= 8.0;
		significantPatterns = 0.08;
		numberOfPatterns	= 0;
		findZone			= 1.0;
		testAndAdd			= 10;
		hasFew				= 0.8;
		collides			= 8;
	}
	
	public TsaParameters(int id) throws IOException {
		readExtractPatterns(id);
		readGetBreakPoint(id);
		readCentersPerHour(id);
		readSignificantPatterns(id);
		readNumberOfPatterns(id);
		readFindZone(id);
		readTestAndAdd(id);
		readHasFew(id);
	}

	private static ArrayList<Double> parseLine(String line) {

		ArrayList<Double> values = new ArrayList<Double>();
		String[] args = line.split(" ");
		values.add(Double.parseDouble(args[0]));
		values.add(Double.parseDouble(args[2]));
		return values;

	}
	
	public Double getValue(FileInputStream fstream,int id) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		ArrayList<Double> values = new ArrayList<Double>();
		String line;
		double val=0;
		
		while(true){
			
			line = br.readLine();
			if(line==null)
				break;
			
			values = parseLine(line);
			if( values.get(0)==id ){
				val = values.get(1);
				break;
			}
		
		}
		
		br.close();
		return val;
	}
	
	public void readExtractPatterns(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/extractPatterns");
		extractPatterns = getValue(fstream,id);
		fstream.close();	
	}
	
	public void readGetBreakPoint(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/getBreakPoint");
		getBreakPoint = getValue(fstream,id);
		fstream.close();
	}
	
	public void readCentersPerHour(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/centersPerHour");
		centersPerHour = getValue(fstream,id);
		fstream.close();
	}
	
	public void readSignificantPatterns(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/significantPatterns");
		significantPatterns = getValue(fstream,id);
		fstream.close();
	}
	
	public void readNumberOfPatterns(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/numberOfPatterns");
		numberOfPatterns = getValue(fstream,id);
		fstream.close();
	}
	
	public void readFindZone(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/findZone");
		findZone = getValue(fstream,id);
		fstream.close();
	}
	
	public void readTestAndAdd(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/testAndAdd");
		testAndAdd = getValue(fstream,id);
		fstream.close();
	}
	
	public void readHasFew(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/hasFew");
		hasFew = getValue(fstream,id);
		fstream.close();
	}
	
	public void readCollides(int id) throws IOException{
		FileInputStream fstream = new FileInputStream("/home/pant/Desktop/predicted_days/tsa-parameters/collides");
		collides = getValue(fstream,id);
		fstream.close();
	}

}

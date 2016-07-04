import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.math.plot.Plot2DPanel;


public class NewDataPreprocessor {
	
	static ArrayList<CSVRecord> csv;
	static int index;
	
	static ArrayList<DataPair> getField(String path,String id) throws IOException, NumberFormatException, ParseException{
		Reader in = new FileReader(path);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		ArrayList<DataPair> data = new ArrayList<DataPair>();
	
		for (CSVRecord record : records){
			if (record.get(0).equals(id))
				data.add(new DataPair(record.get(1),(double)Integer.parseInt(record.get(2))));
			else if (data.size() > 0)
				break;

		}
			 
		in.close();
		return data;
		
	}
	
	public static Integer discretize(ArrayList<Range> ranges,double d){
		for (int i=0;i<ranges.size();i++)
			if(ranges.get(i).xLimits[1]>d)
				return i;
		return -1;
	}
	
	public static void readCSV(String path) throws IOException{
		 Reader in = new FileReader(path);
		 CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
		 csv = (ArrayList<CSVRecord>) parser.getRecords();
		 in.close();
		 index = 0;
	}

	
	public static ArrayList<DataPair> getNextSeries() throws NumberFormatException, ParseException{
		ArrayList<DataPair> ts = new ArrayList<DataPair>();
		if (index<csv.size()){
			ts.add(new DataPair(csv.get(index).get(1),(double)Integer.parseInt(csv.get(index).get(2))));
			index++;
			for(int i=index;i<csv.size();i++){
				if(csv.get(i).get(0).equals(csv.get(i-1).get(0)))
					ts.add(new DataPair(csv.get(i).get(1),(double)Integer.parseInt(csv.get(i).get(2))));
				else{
					index=i;
					break;
				}
			}
		}
		
		return ts;
	}
	
	public static ArrayList<Double> cluster(ArrayList<Double> data,int k){

		ArrayList<Point> points = new ArrayList<Point>(data.size());

		for (Double d:data)
			points.add(new Point(d));

		KMeansPlusPlusClusterer<Point> clusterer = new KMeansPlusPlusClusterer<Point>(k, 1000);
		List<CentroidCluster<Point>> clusterResults = clusterer.cluster(points);

		ArrayList<Double> centers = new ArrayList<Double>();

		for (CentroidCluster<Point> cc:clusterResults)
			centers.add(cc.getCenter().getPoint()[0]);

		return centers;
	}
	
	static ArrayList<Range> clusterRanges(ArrayList<Double> centers){

		Collections.sort(centers);
		
		ArrayList<Range> ranges = new ArrayList<Range>(centers.size());
		
		ranges.add( new Range(Double.NEGATIVE_INFINITY,(centers.get(0)+centers.get(1))/2));
		
		for (int i=1;i<centers.size()-1;i++)
			ranges.add(new Range( (centers.get(i-1)+centers.get(i) )/2 , (centers.get(i)+centers.get(i+1))/2 ) );
		
		ranges.add( new Range( (centers.get(centers.size()-2)+centers.get(centers.size()-1))/2, Double.POSITIVE_INFINITY));
		
		return ranges;
	}
	
	static ArrayList<Range> equalRanges(ArrayList<Double> data, int k){
		double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
		
		for(Double d:data){
			if(d<min)
				min=d;
			if(d>max)
				max=d;
		}
		
		double step = (max-min)/k;
		ArrayList<Range> ranges = new ArrayList<Range>();
		
		for(int i=0;i<k-1;i++){
			Range r = new Range();
			r.xLimits[0] = i*step;
			r.xLimits[1] = (i+1)*step;
			ranges.add(r);
		}
		Range r = new Range();
		r.xLimits[0] = (k-1)*step;
		r.xLimits[1] = Double.POSITIVE_INFINITY;
		ranges.add(r);
		return ranges;
		
	}

	static ArrayList<DataPair> sumSeries(String path,String[] ids) throws NumberFormatException, IOException, ParseException{
		
		ArrayList<Double> sum = new ArrayList<Double>();
		Calendar cal = null;
		for(int i =0; i<20000;i++)
			sum.add(0.0);
		
		ArrayList<DataPair> ts;
	
		for(String id:ids){
			ts = TimeFixer.fixTS1(delta(reverse(getField(path,id))));
			cal = (Calendar) ts.get(0).cal.clone();
			System.out.println(id);
			for(int i=0;i<ts.size();i++)
				sum.set(i ,sum.get(i)+ts.get(i).consumption);
		}
		
		ArrayList<DataPair> outTs = new ArrayList<DataPair>(8760);
		for(int i=0;i<8760;i++){
			outTs.add(new DataPair(cal,sum.get(i) ));
			cal = (Calendar) cal.clone();
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		PrintWriter pw = new PrintWriter("/home/pant/Desktop/aggregate");
		for(DataPair dp:outTs)
			pw.println(dp.consumption);
		pw.close();
		
		return outTs;
			
	}

	static ArrayList<DataPair> delta(ArrayList<DataPair> data) throws ParseException{
		
		ArrayList<DataPair> tData = new ArrayList<DataPair>(data.size()-1);
		for(int i=1;i<data.size();i++)
			tData.add(new DataPair(data.get(i).cal, data.get(i).consumption-data.get(i-1).consumption));
		return tData;
		
	}
	
	static ArrayList<DataPair> reverse(ArrayList<DataPair> original){
		
		int len = original.size();
		ArrayList<DataPair> reverse = new ArrayList<DataPair>(len);
		for(int i=0;i<len;i++)
			reverse.add(original.get(len-i-1));
		return reverse;
	
	}
	
	static double findMax(ArrayList<Double> original){
		double max = Double.NEGATIVE_INFINITY;
		
		for(Double d:original)
			if (d>max)
				max=d;
		return max;
		
	}
	
	static ArrayList<Double> normalize(ArrayList<Double> original){
		double max = findMax(original);
		ArrayList<Double> normalized = new ArrayList<Double>();
		for(Double d:original)
			normalized.add(new Double(d/max));
		return normalized;
	}
	
	static double[] extractDoubles(ArrayList<DataPair> data){
		
		double[] buf = new double[data.size()];
		for(int i=0;i<buf.length;i++)
			buf[i]=data.get(i).consumption;
		return buf;
		
	}
	
	static ArrayList<Double> extractALDouble(ArrayList<DataPair> data){
		
		ArrayList<Double> buf = new ArrayList<Double>(data.size());
		for(int i=0;i<data.size();i++)
			buf.add(data.get(i).consumption);
		return buf;
		
	}
	
	static void plotTS(ArrayList<DataPair> data,String name){
		double[] d = extractDoubles(data);
		Plot2DPanel plot = new Plot2DPanel();
		plot.addBarPlot(name, d);
		JFrame frame = new JFrame(name);
		frame.setContentPane(plot);
		frame.setSize(10240,768);
		frame.setVisible(true);
	}
	
	static double[] Dtod(ArrayList<Double> data){
		double[] buf = new double[data.size()];
		for(int i=0;i<data.size();i++)
			buf[i]=data.get(i);
		return buf;
	}
	
	static void plotD(ArrayList<Double> data,String name){
		double[] d = Dtod(data);
		Plot2DPanel plot = new Plot2DPanel();
		plot.addBarPlot(name, d);
		JFrame frame = new JFrame(name);
		frame.setContentPane(plot);
		frame.setSize(10240,768);
		frame.setVisible(true);
	}
	
	static void saveSinglePlot(ArrayList<DataPair> data,String name) throws IOException, InterruptedException{
		Plot2DPanel plot = new Plot2DPanel();
		double[] d = extractDoubles(data);
		plot.addBarPlot(name,d);
		JFrame frame = new JFrame(name);
		frame.setContentPane(plot);
		frame.setSize(1024,768);
		frame.setVisible(true);
		Thread.sleep(2000);
		BufferedImage image = new BufferedImage(1024,768,BufferedImage.TYPE_INT_RGB);
		frame.paint(image.getGraphics());
		ImageIO.write(image, "png", new File("/home/pant/Desktop/graphs/".concat(name).concat(".png")));
		frame.dispose();
	}
		
}

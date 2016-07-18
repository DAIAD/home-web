
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;


public class DataPreprocessor {


	static double parseNum(String s){
		double num = 0;

		try{
			num = (double)Integer.parseInt(s);
		}
		catch (Exception e){
			num = (double)Float.parseFloat(s);
		}
		finally{
			return num;
		}
	}

	static ArrayList<Double> getField(String path,int id,int field) throws IOException{
		Reader in = new FileReader(path);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		ArrayList<Double> data = new ArrayList<Double>();
		for (CSVRecord record : records)
			if ((int)parseNum(record.get(0))==id)
				data.add( parseNum(record.get(7)) );
		in.close();
		return data;
	}

	
	static ArrayList<Integer> discretize(ArrayList<Double> data, ArrayList<Range> ranges){
		ArrayList<Integer> sequence = new ArrayList<Integer>(data.size());
		for(Double d:data)
			for (int i=0;i<ranges.size();i++)
				if(ranges.get(i).xLimits[1]>d){
					sequence.add(i);
					break;
				}
		return sequence;
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
	
	static ArrayList<Range> equiprobableRanges(ArrayList<Double> data,int k){
		
		ArrayList<Range>  ranges = new ArrayList<Range>(k);
		ArrayList<Double> buf = new ArrayList<Double>(data);
		Collections.sort(buf);
		int l = buf.size();
		int step = (int) Math.floor((double)(l)/(double)k);
		for(int i=0;i<k-1;i++){
			Range r = new Range();
			r.xLimits[0]=buf.get(i*step);
			r.xLimits[1] = buf.get((i+1)*step);
			ranges.add(r);
		}
		Range r = new Range();
		r.xLimits[0]=buf.get((k-1)*step);
		r.xLimits[1]=Double.POSITIVE_INFINITY;
		ranges.add(r);
			
		return ranges;
				
	}
	
	static ArrayList<MultiPoint> getMultiFields(String path, int id,int[] fields) throws IOException{
		Reader in = new FileReader(path);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		ArrayList<MultiPoint> data = new ArrayList<MultiPoint>();
		for (CSVRecord record : records){
			if ((int)parseNum(record.get(0))==id){
				double[] vals = new double[fields.length];		
				for(int i=0; i<fields.length;i++)
					vals[i] = parseNum(record.get(fields[i]));
				data.add(new MultiPoint(vals));
			}
		}
		in.close();
		return data;
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
	
	static void normalizeMultiPoints(ArrayList<MultiPoint> points){
		int l = points.get(0).values.length;
		double[] maxes = new double[l];
		
		for (MultiPoint p:points)
			for ( int i=0;i<l;i++)
				if (p.values[i] > maxes[i])
					maxes[i] = p.values[i];
		
		for (MultiPoint p:points)
			for ( int i=0;i<l;i++)
				p.values[i] /= maxes[i];
		
		
	}
	
	static void scaleToOne(ArrayList<Double> data){
		double max=0;
		
		for(Double d:data)
			if (d>max)
				max=d;
		
		for(int i=0;i<data.size();i++)
			data.set(i,data.get(i)/max);

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
	
	
	public static ArrayList<MultiPoint> multiCluster(ArrayList<MultiPoint> points,int k){
		DataPreprocessor.normalizeMultiPoints(points);
		KMeansPlusPlusClusterer<MultiPoint> clusterer = new KMeansPlusPlusClusterer<MultiPoint>(k, 1000);
		List<CentroidCluster<MultiPoint>> clusterResults = clusterer.cluster(points);

		ArrayList<MultiPoint> centers = new ArrayList<MultiPoint>();

		for (CentroidCluster<MultiPoint> cc:clusterResults)
			centers.add(new MultiPoint(cc.getCenter().getPoint()));

		return centers;
		
	}
	
	static double evaluateClustering(ArrayList<Double> data, ArrayList<Integer> discrete, ArrayList<Double> centers){
		double[] D = new double[centers.size()];
		int[] n = new int[centers.size()];
		double W=0;
		
		for(int i=0;i<data.size();i++){
			D[discrete.get(i)]+=Math.abs(data.get(i)-centers.get(discrete.get(i)));
			n[discrete.get(i)]++;
		}
		
		for(int i=0;i<centers.size();i++)
			W+=D[i];
		
		return W;
	}
	
	static int findK(ArrayList<Double> data){
		int k,m=20;
		double[] scores = new double[m+1];
		double[] diff = new double[m+1];
		ArrayList<Double> centers;
		ArrayList<Range> ranges;
		ArrayList<Integer> discrete;
		
		if(data.size()<m)
			m=data.size();
		
		for(k=0;k<=m;k++){
			centers=cluster(data,k);
			ranges = clusterRanges(centers);
			discrete=discretize(data,ranges);
			scores[k]=evaluateClustering(data,discrete,centers);
		}
		
		for(int i=1;i<m;i++)
			diff[i]=scores[i-1]-scores[i];

		for(int i=1;i<m;i++)
			if(diff[i+1]<0.1*scores[0])
				return i;
		
		Plotter plot = new Plotter(800,600);
		plot.dPlot(diff);
		plot.dPlot(scores);
		
		return m;
	}
	
	public static ArrayList<Integer> multiDiscretize(ArrayList<MultiPoint> data,ArrayList<MultiPoint> centers){
		double min;
		int sym=-1;
		ArrayList<Integer> sequence = new ArrayList<Integer>(data.size());
		for(MultiPoint d:data){
			min=Double.POSITIVE_INFINITY;
			for(int i=0;i<centers.size();i++){
				if (d.eucDist(centers.get(i))<min){
					min = d.eucDist(centers.get(i));
					sym=i;
				}
			}
			sequence.add(sym);
		}
		return sequence;
	}

	
	public static double[] Dtod(ArrayList<Double> data){
		double[] buf = new double[data.size()];
		for(int i=0;i<data.size();i++)
			buf[i]=data.get(i);
		return buf;
	}
	
	public static AllData getData(String path,int id,int k,int discretization,boolean useMetadata,double ratio) throws IOException{
		
		ArrayList<Double> data = DataPreprocessor.getField(path,id,7);
		
		ArrayList<Range> ranges;
		ArrayList<Double> trainData = new ArrayList<Double>(data.subList(0, (int)(ratio*data.size())));
		if(discretization==0)
			ranges = DataPreprocessor.clusterRanges(DataPreprocessor.cluster(trainData, k));
		else if( discretization == 1)
			ranges = DataPreprocessor.equalRanges(trainData, k);
		else
			ranges = DataPreprocessor.equiprobableRanges(trainData, k);
		ArrayList<Integer> discrete = DataPreprocessor.discretize(data, ranges);
		
		ArrayList<MultiPoint> metadata;
		ArrayList<Integer> metaDiscrete;
		if ( useMetadata ){
			int[] fields = { 4,5,6,7,8,9 };
			metadata = DataPreprocessor.getMultiFields(path,id,fields);
			ArrayList<MultiPoint> trainMetadata = new ArrayList<MultiPoint> (metadata.subList(0,(int)(ratio*metadata.size())));
			try{
				metaDiscrete = DataPreprocessor.multiDiscretize(metadata, DataPreprocessor.multiCluster(trainMetadata, k));
				metadata = DataPreprocessor.getMultiFields(path,id,fields);
			}
			catch (Exception e){
				metadata = DataPreprocessor.getMultiFields(path,id,fields);
				metaDiscrete=discrete;
			}
		}
		else {
			int[] fields = { 7 };
			metadata = DataPreprocessor.getMultiFields(path,id,fields);
			metaDiscrete = discrete;
		}
		
		return new AllData(data,discrete,metadata,metaDiscrete,ranges);
	}
	
	public static ArrayList<Double> cloneALD(ArrayList<Double> original){
		ArrayList<Double> clone = new ArrayList<Double>(original.size());
		
		for (int i=0;i<original.size();i++)
			clone.add(new Double(original.get(i)));
		
		return clone;
		
	}
	
	public static ArrayList<MultiPoint> cloneALMP(ArrayList<MultiPoint> original){
		ArrayList<MultiPoint> clone = new ArrayList<MultiPoint>(original.size());
		
		for (int i=0;i<original.size();i++)
			clone.add(new MultiPoint(original.get(i).values));
		
		return clone;
		
	}
	
	public static AllData pretestData(AllData allData,int k,int discretization,boolean useMetadata,double ratio){
		
		ArrayList<Double> data = cloneALD(allData.data);
		ArrayList<MultiPoint> metadata = cloneALMP(allData.metadata);
		
		ArrayList<Double> pretestData = new ArrayList<Double>(data.subList(0, (int)(ratio*data.size())));
		ArrayList<MultiPoint> pretestMetadata = new ArrayList<MultiPoint>(metadata.subList(0, (int)(ratio*data.size())));
		ArrayList<Range> ranges;
		
		ArrayList<Double> trainData = new ArrayList<Double>(data.subList(0, (int)(ratio*ratio*data.size())));
		ArrayList<MultiPoint> trainMetadata=new ArrayList<MultiPoint>(metadata.subList(0, (int)(ratio*ratio*metadata.size())));
		
		if(discretization==0)
			ranges = DataPreprocessor.clusterRanges(DataPreprocessor.cluster(trainData, k));
		else if( discretization == 1)
			ranges = DataPreprocessor.equalRanges(trainData, k);
		else
			ranges = DataPreprocessor.equiprobableRanges(trainData, k);
		ArrayList<Integer> discrete = DataPreprocessor.discretize(pretestData, ranges);
		
		
		ArrayList<Integer> metaDiscrete;
		if ( useMetadata ){
			int[] fields = { 4,5,6,7,8,9 };
			try{
				metaDiscrete = DataPreprocessor.multiDiscretize(pretestMetadata, DataPreprocessor.multiCluster(trainMetadata, k));
				pretestMetadata = new ArrayList<MultiPoint>(allData.metadata.subList(0, (int)(ratio*allData.data.size())));
			}
			catch (Exception e){
				pretestMetadata = new ArrayList<MultiPoint>(allData.metadata.subList(0, (int)(ratio*allData.data.size())));
				metaDiscrete=discrete;
			}
		}
		else {
			int[] fields = { 7 };
			pretestMetadata = new ArrayList<MultiPoint>(allData.metadata.subList(0, (int)(ratio*allData.data.size())));;
			metaDiscrete = discrete;
		}
		
		return new AllData(pretestData,discrete,pretestMetadata,metaDiscrete,ranges);
		
		
	}
	
}


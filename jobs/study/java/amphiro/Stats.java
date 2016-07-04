import java.util.ArrayList;
import java.util.List;


public class Stats {
	
	static double[][] calcPMF(ArrayList<Double> data, int splits){
		double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
		
		for (Double d:data){
			if (d<min)
				min=d;
			if (d>max)
				max=d;
		}
		
		double range= max-min;
		double step = range/splits;
		
		double[] discrete = new double[data.size()];
		double[] lvls = new double[splits+1];
		double[] pmf = new double[splits+1];
		
		for(int i=0;i<=splits;i++)
			lvls[i]=min+i*step+step/2;
		
		for(int j=0;j<data.size();j++)
			for(int i = 0; i<=splits; i++)
				if (data.get(j)>=min+i*step && data.get(j)<min+(i+1)*step){
					pmf[i]++;
					discrete[j]=i;
				}
		
		for (int i=0;i<=splits;i++)
			pmf[i]/=data.size();
		
		
		double[][] results = new double[splits+1][2];
		results[1]=lvls;
		results[0]=pmf;
		
		return results;
		
	}
	
	static double mean(List<Double> data){
		double sum=0;
		for(Double d:data)
			sum+=d;
		return sum/data.size();
		
	}
	
	static double variance(List<Double> data){
		double m = mean(data);
		double var = 0;
		for(Double d:data)
			var+=Math.pow(d-m, 2);
		return var/data.size();
			
	}
	
	static double absMean(List<Double> data){
		double sum=0;
		for(Double d:data)
			sum+=Math.abs(d);
		return sum/data.size();
	}
	
	static double autocorrelation(ArrayList<Double> data,int b){
		
		double m = mean(data.subList(b, data.size()));
		double var = variance(data.subList(b, data.size()));
		ArrayList<Double> X = new ArrayList<Double>();
		
		for(int i=b;i<data.size();i++)
			X.add( (data.get(i)-m) * (data.get(i-b)-m) );
		
		return mean(X)/var;
			
	}
	
	

}

import org.apache.commons.math3.ml.clustering.Clusterable;


public class MultiPoint implements Clusterable {
	
	public double[] values;
	
	public MultiPoint(double[] vals){
		values = new double[vals.length];
		for(int i=0;i<vals.length;i++)
			values[i]=vals[i];
	}
	@Override
	public double[] getPoint() {
		// TODO Auto-generated method stub
		return values;
	}
	
	public double eucDist(MultiPoint b){
		
		double sum=0;
		for(int i=0;i<values.length;i++)
			sum+=Math.pow(values[i]-b.values[i],2);
		
		return Math.sqrt(sum);
	}

}

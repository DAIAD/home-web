import org.apache.commons.math3.ml.clustering.Clusterable;


public class Point implements Clusterable{
	
	double value;

	public Point(Double val){
		value = val;
	}
	@Override
	public double[] getPoint() {
		
		double[] buf = new double[1];
		buf[0]=value;
		return buf;
	}


}

import java.util.ArrayList;


public class Range {

	double[] yLimits;
	double[] xLimits;
	ArrayList<Double> xPoints;
	ArrayList<Double> yPoints;
	double pValue;
	
	public Range(){
		xLimits = new double[2];
		yLimits = new double[2];
	}
	
	public Range(ArrayList<Pair> pairs,int index,int K){
		
		xLimits = new double[2];
		xLimits[0]=pairs.get(index).x;
		xLimits[1]=pairs.get(index).x;
		
		yLimits = new double[2];
		yLimits[0]=pairs.get(index).y;
		yLimits[1]=pairs.get(index).y;
		
		xPoints = new ArrayList<Double>();
		yPoints = new ArrayList<Double>();
		
		for(int i=index;i<index+K;i++)
			addPoint(pairs.get(i).x,pairs.get(i).y);
		
		
	}
	
	public void addPoint(double x,double y){
		
		xPoints.add(x);
		yPoints.add(y);
		
		if (x<xLimits[0])
			xLimits[0]=x;
		if(x>xLimits[1])
			xLimits[1]=x;
		if (y<yLimits[0])
			yLimits[0]=y;
		if(y>yLimits[1])
			yLimits[1]=y;
		
	}
	
	public double calcPValue(ArrayList<Double> sortedPoints){
		
		int start=sortedPoints.indexOf(yLimits[0]);
		int end=sortedPoints.lastIndexOf(yLimits[1]);
		
		double prob = (double)(end-start+1)/(double)sortedPoints.size();
		
		pValue = prob*yPoints.size();
		
		return pValue;
		
	}
	
	boolean inRange(double a){
		if(a>=xLimits[0] && a<=xLimits[1])
			return true;
		else
			return false;
	}
	
	double length(){
		return xLimits[1]-xLimits[0];
	}
	
	public double jointPValue(ArrayList<Double> sortedPoints,Range other){
		
		return 0.0;
		
	}
	
	public Range(double a,double b){
		if (a<=b){
			xLimits = new double[2];
			xLimits[0]=a;
			xLimits[1]=b;
		}
		if (a>b){
			xLimits = new double[2];
			xLimits[0]=b;
			xLimits[1]=a;
		}
	
	}
	
}


public class Pair implements Comparable<Pair>{
	
	double x;
	double y;
	
	public Pair(double x, double y){
		this.x=x;
		this.y=y;
	}

	public int compareTo(Pair other) {
		return (int)Math.signum(x-other.x);
	}

}

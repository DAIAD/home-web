import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class Correlations {
	
	double[][] x;
	double[][] y;
	
	double[][] c;
	
	int offset;
	int featCount;
	int l,n;
	
	public Correlations(ArrayList<Day> days,int start,int end){
		
		init(start,end);
		
		buildMatrices(days,start,end);
	
		calcC();
		
	}
	
	public void init(int start, int end){
		
		offset = 4;
		l = end-offset-start;
		n = 24+7+31+24+24+24+24+1;
		x = new double[l][n];
		y = new double[l][24];
		c = new double[24][n];
		
	}
	
	public double mean(double[][] A, int j){
		double sum=0;
		for(int i=0;i<A.length;i++)
			sum+=A[i][j];
		return sum/(double)A.length;
	}
	
	public double var(double[][] A, int j){
		double m = mean(A, j);
		double v = 0;
		for(int i=0;i<A.length;i++)
			v+=Math.pow(A[i][j]-m,2);
		return v/((double)A.length-1.0);
	}
	
	public double singleCorrelation(int feat, int hour){
		
		double mY = mean(y, hour);
		double mX = mean(x, feat);
		double corr = 0;
		
		for(int i=0;i<l;i++)
			corr+= (x[i][feat]-mX)*(y[i][hour]-mY);
		
		corr/= ((double)(l-1)); 
		corr/= Math.sqrt(var(y,hour)*var(x,feat)) ;
		
		return corr;
		
	}
	
	private void calcC() {
		 for(int i=0;i<n;i++)
			 for(int j=0;j<24;j++)
				 c[j][i] = singleCorrelation(i,j);
	}
	
	public void printCorrelations() throws FileNotFoundException{
		PrintWriter pw = new PrintWriter("/home/pant/Desktop/predicted_days/correlations.csv");
		for(int i=0;i<n;i++){
			for(int j=0;j<24;j++){
				pw.print(c[i][j]);
				pw.print(" ");
			}
			pw.println();	
		}
		pw.close();
	}
	
	private void buildMatrices(ArrayList<Day> days, int start, int end) {
		for(int i=start;i<end-1-offset;i++){
			featCount=0;
			add24Hours(days,i);
			addDayOfWeek(days,i);
			addDayOfMonth(days,i);
			addMeanOfWeekHours(days,i);
			addMaxOfWeekHours(days,i);
			addMinOfWeekHours(days,i);
			addMeanOfHoursTotal(days,i);
			addMeanOfDays(days,i);
			addY(days,i);
		}
		
	}

	private void addY(ArrayList<Day> days, int i) {
		
		for(int j=0;j<24;j++)
			y[i][j] = days.get(i+offset+1).data.get(j).consumption;
		
	}

	private void addMeanOfDays(ArrayList<Day> days, int i) {
		x[i][featCount] = getMeanOfDays(days,i);
		featCount++;
	}

	public double meanOfDay(Day d){
		double sum=0;
		for(DataPair dp:d.data)
			sum+=dp.consumption;
		return sum/24;
	}
	
	public double getMeanOfDays(ArrayList<Day> days,int i){
		double sum = 0;
		for(int j=i+offset-4;j>=0;j-=5)
			sum+= meanOfDay(days.get(j));
		return sum/(1+Math.floor((i+offset-4)/5));
	}
	
	public double getMeanOfHour(ArrayList<Day> days, int i, int hour){
		double mean=0;
		for(int j=i+offset;j>=0;j--)
			mean+=days.get(j).data.get(hour).consumption;
		return mean/(i+offset+1);
	}
	
	private void addMeanOfHoursTotal(ArrayList<Day> days, int i) {
		for(int h=0;h<24;h++,featCount++)
			x[i][featCount] = getMeanOfHour(days,i,h);
	}

	private void addMinOfWeekHours(ArrayList<Day> days, int i) {
		double[] mins = minsOfWeek(days,i);
		for(int j=0;j<24;j++,featCount++)
			x[i][featCount] = mins[j];
	}

	private double[] minsOfWeek(ArrayList<Day> days, int i) {
		double[] mins = new double[24];
	
		for(int j=0;j<24;j++)
			mins[j] = Double.POSITIVE_INFINITY;
		
		for(int j=0;j<5;j++)
			for(int k=0;k<24;k++)
				if(days.get(i+offset-j).data.get(k).consumption<mins[k])
					mins[k] = days.get(i+offset-j).data.get(k).consumption;
		return mins;
	}

	private void addMaxOfWeekHours(ArrayList<Day> days, int i) {
		double[] maxes = maxesOfWeek(days,i);
		for(int j=0;j<24;j++,featCount++)
			x[i][featCount] = maxes[j];
	}
	
	private double[] maxesOfWeek(ArrayList<Day> days, int i) {
		double[] maxes = new double[24];
		for(int j=0;j<5;j++)
			for(int k=0;k<24;k++)
				if(days.get(i+offset-j).data.get(k).consumption>maxes[k])
					maxes[k] = days.get(i+offset-j).data.get(k).consumption;
		return maxes;
	}

	public double[] meansOfWeek(ArrayList<Day> days, int i){
		double[] means = new double[24];
		for(int j=0;j<5;j++)
			for(int k=0;k<24;k++)
				means[k]+=days.get(i+offset-j).data.get(k).consumption;
		for(int j=0;j<24;j++)
			means[j]/=5;
		return means;
	}
	
	private void addMeanOfWeekHours(ArrayList<Day> days, int i) {
		double[] means = meansOfWeek(days,i);
		for(int j=0;j<24;j++,featCount++)
			x[i][featCount] = means[j];
	}

	private void addDayOfMonth(ArrayList<Day> days, int i) {
		Day d = days.get(i+offset);
		for(int j=0;j<31;j++,featCount++)
			if ( j == d.cal.get(Calendar.DAY_OF_MONTH)-1)
				x[i][featCount] =1;
			else
				x[i][featCount] = 0;	
	}

	private void addDayOfWeek(ArrayList<Day> days, int i) {
		Day d = days.get(i+offset);
		for(int j=0;j<7;j++,featCount++)
			if ( j == d.cal.get(Calendar.DAY_OF_WEEK)-1)
				x[i][featCount] = 1;
			else
				x[i][featCount] = 0;
	}

	private void add24Hours(ArrayList<Day> days, int i) {
		for(int j=0;j<24;j++,featCount++)
			x[i][featCount] = days.get(i+offset).data.get(j).consumption;
	}
	
	
}

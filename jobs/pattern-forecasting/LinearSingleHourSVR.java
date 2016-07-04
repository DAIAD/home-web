import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import libsvm.svm_node;
import libsvm.svm_problem;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;


public class LinearSingleHourSVR {
	
	int featCount;
	Problem problem;
	int hour;
	int offset;
	Model model;
	
	public LinearSingleHourSVR(ArrayList<Day> days, int start, int end, int hour,double c,double e) {
		train(days,start,end,hour,c,e);
	}

	public void train(ArrayList<Day> days, int start,int end,int hour, double c, double e){
		
		this.offset = 1;
		this.hour=hour;
		
		problem = new Problem();
		problem.l =  end-start-offset-1; 
		problem.n =  24+7;//+31+24+24+24+2; 
		problem.x = new FeatureNode[problem.l][problem.n]; 
		problem.y = new double[problem.l];
		
		for(int i=start;i<end-1-offset;i++){
			featCount=0;
			add24Hours(days,i,problem.x[i]);
			addDayOfWeek(days,i,problem.x[i]);
		//	addDayOfMonth(days,i,problem.x[i]);
		//	addMeanOfWeekHours(days,i,problem.x[i]);
		//	addMaxOfWeekHours(days,i,problem.x[i]);
		//	addMinOfWeekHours(days,i,problem.x[i]);
		//	addMeanOfHour(days,i,problem.x[i]);
		//	addMeanOfDays(days,i,problem.x[i]);
			problem.y[i] = days.get(i+offset+1).data.get(hour).consumption;
		}
		
	//	printDataset();
	//	printTS(days);
		
		SolverType solver = SolverType.L2R_L1LOSS_SVR_DUAL; 
		
		Parameter parameter = new Parameter(solver,c,0.0001,e);
		model = Linear.train(problem, parameter);
		
	}

	private void printTS(ArrayList<Day> days) {
		try {
			PrintWriter pw = new PrintWriter("/home/pant/Desktop/ts");
			for(Day d:days){
				for(DataPair dp:d.data){
					pw.print(String.format("%.3f", dp.consumption));
					pw.print(" ");
				}
				pw.println();
			}
			pw.close();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	private void printDataset() {
		
		try {
			
			PrintWriter pw = new PrintWriter("/home/pant/Desktop/linSVR");
			for(int i=0;i<problem.l;i++){
				for(int j=0;j<problem.n;j++){
					pw.print(String.format("%.3f", problem.x[i][j].getValue()));
					pw.print(" ");
				}
				pw.print(" ");
				pw.println(String.format("%.3f", problem.y[i]));
			}
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	private void addMeanOfDays(ArrayList<Day> days, int i,Feature[] x) {
		x[featCount] = new FeatureNode(featCount+1,getMeanOfDays(days,i));
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
	
	public double getMeanOfHour(ArrayList<Day> days, int i){
		double mean=0;
		for(int j=i+offset;j>=0;j--)
			mean+=days.get(j).data.get(hour).consumption;
		return mean/(i+offset+1);
	}
	
	private void addMeanOfHour(ArrayList<Day> days, int i,Feature[] x) {
		x[featCount] = new FeatureNode(featCount+1,getMeanOfHour(days,i));
		featCount++;
	}

	private void addMinOfWeekHours(ArrayList<Day> days, int i,Feature[] x) {
		double[] mins = minsOfWeek(days,i);
		for(int j=0;j<24;j++,featCount++)
			x[featCount] = new FeatureNode(featCount+1,mins[j]);
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

	private void addMaxOfWeekHours(ArrayList<Day> days, int i,Feature[] x) {
		double[] maxes = maxesOfWeek(days,i);
		for(int j=0;j<24;j++,featCount++)
			x[featCount] = new FeatureNode(featCount+1,maxes[j]);
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
	
	private void addMeanOfWeekHours(ArrayList<Day> days, int i,Feature[] x) {
		double[] means = meansOfWeek(days,i);
		for(int j=0;j<24;j++,featCount++)
			x[featCount] = new FeatureNode(featCount+1,means[j]);
	}

	private void addDayOfMonth(ArrayList<Day> days, int i,Feature[] x) {
		Day d = days.get(i+offset);
		for(int j=0;j<31;j++,featCount++)
			if ( j == d.cal.get(Calendar.DAY_OF_MONTH)-1)
				x[featCount] = new FeatureNode(featCount+1,1);
			else
				x[featCount] = new FeatureNode(featCount+1,0);	
	}

	private void addDayOfWeek(ArrayList<Day> days, int i,Feature[] x) {
		Day d = days.get(i+offset);
		for(int j=0;j<7;j++,featCount++)
			if ( j == d.cal.get(Calendar.DAY_OF_WEEK)-1)
				x[featCount] = new FeatureNode(featCount+1,1);
			else
				x[featCount] = new FeatureNode(featCount+1,0);
	}

	private void add24Hours(ArrayList<Day> days, int i, Feature[] x) {
		for(int j=0;j<24;j++,featCount++)
			x[featCount] = new FeatureNode(featCount+1, days.get(i+offset).data.get(j).consumption);
	}
	
	double predict(ArrayList<Day> days, int i){
		i-=offset;
		Feature[] x = new Feature[problem.n];
		featCount=0;
		add24Hours(days,i,x);
		addDayOfWeek(days,i,x);
	//	addDayOfMonth(days,i,x);
	//	addMeanOfWeekHours(days,i,x);
	//	addMaxOfWeekHours(days,i,x);
	//	addMinOfWeekHours(days,i,x);
	//	addMeanOfHour(days,i,x);
	//	addMeanOfDays(days,i,x);
		return Linear.predict(model, x);	
	}

}

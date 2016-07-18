import java.util.ArrayList;
import java.util.List;


public class Results {
	
	ArrayList<Double>  predC;
	ArrayList<Integer> predD;
	ArrayList<Double>  errors;
	int testSize;
	int testStart;
	int symbols;
	
	double[]   interCounts;
	double[]   predCounts;
	double[]   origCounts;
	double[][] confMatrix;
	
	public Results(ArrayList<Double> predC,ArrayList<Integer> predD, int testStart, int symbols){
		
		this.predC=new ArrayList<Double>();
		this.predD=new ArrayList<Integer>();
		errors = new ArrayList<Double>();
		this.testStart=testStart;
		testSize=predC.size();
		
		for(int i=0;i<testStart;i++){
			this.predC.add(0.0);
			this.predD.add(0);
		}
		
		for(int i=0;i<testSize;i++){
			this.predC.add(predC.get(i));
			this.predD.add(predD.get(i));
		}
		
		this.symbols=symbols;
		interCounts = new double[symbols];
		predCounts = new double[symbols];
		origCounts = new double[symbols];
		confMatrix = new double[symbols][symbols];
	}
	
	public double mean(List<Double> data){
		double sum=0;
		
		for(Double d:data)
			sum+=d;
		
		return sum/data.size();
	}
	
	public double max(List<Double> data){
		double max=0;
		
		for (Double d:data)
			if (d>max)
				max=d;
		return max;
	}
	
	public void count(ArrayList<Integer> discrete){
		
		for(int i = testStart; i<discrete.size();i++){
			predCounts[predD.get(i)]++;
			origCounts[discrete.get(i)]++;
			if(predD.get(i) == discrete.get(i))
				interCounts[predD.get(i)]++;
		}
			
		
	}
	
	public double calcE4(ArrayList<Integer> discrete){
		
		double e = 0;
		double ei = 0;
		double ci = 0;
		count(discrete);
		
		for(int i=0;i<symbols;i++){
			ci = interCounts[i]/predCounts[i];
			if(Double.isNaN(ci))
				ci=0;
			ci-= origCounts[i]/testSize;
			ei = ci*origCounts[i]/testSize;
			e += ei;
		}
		
		return e;
		
	}
	
	public double F1(int s){
	
		double pr = precision(s);
		double re = recall(s);
		if (Double.isNaN(pr))
			pr=0.5;
		if (Double.isNaN(re))
			re=0.5;
		if (pr==0 && re==0)
			return 0;
		return 2*pr*re/(pr+re);
	
	}
	
	public double recall(int s){
		
		double tp = 0;
		double sum = 0;
		
		tp=confMatrix[s][s];
		
		for(int i=0;i<symbols;i++)
				sum += confMatrix[i][s];
		
		return tp/sum;
		
	}
	
	public double precision(int s){
		
		double tp = 0;
		double sum = 0;
		
		tp=confMatrix[s][s];
		
		for(int i=0;i<symbols;i++)
				sum += confMatrix[s][i];
		
		return tp/sum;
		
	}
	
	public double randPrecision(int s){
		double predPs=1;
		predPs/=symbols;
		double Pr;
		
		Pr=origCounts[s]/testSize * predPs  /  ( origCounts[s]/testSize * predPs  +  predPs * (1 - origCounts[s]/testSize));
		
		return Pr;
		
		
	}
	
	public double randRecall(int s){
		double predPs=1;
		predPs/=symbols;
		double Re;
		
		Re=origCounts[s]/testSize * predPs  /  ( origCounts[s]/testSize * predPs  +  (1-predPs) * origCounts[s]/testSize);
		
		return Re;
	}
	
	public double randF1(int s){
		double pr=randPrecision(s);
		double re=randRecall(s);
		
		if (Double.isNaN(pr))
			pr=0.5;
		if (Double.isNaN(re))
			re=0.5;
		if (pr==0 && re==0)
			return 0;
		
		return 2*pr*re/(pr+re);
	
	}
	
	public double randMeanF1(){
		double mean=0;
		for( int i=0;i<symbols;i++)
			mean+=origCounts[i]/testSize*randF1(i);
		return mean;
	}
	
	public void makeConfMatrix(ArrayList<Integer> discrete){
		
		try{
			for (int i = testStart;i<discrete.size();i++ ){
				confMatrix[predD.get(i)][discrete.get(i)]++;
				origCounts[discrete.get(i)]++;
			}
		}
		catch(Exception e){
			
		}
		
	}
	
	public double meanF1(){
		
		
		double M=0;
		for(int i=0;i<symbols;i++)
			M+=origCounts[i]/testSize*F1(i);
		return M;
		
	}
	
	public void calculateErrors(AllData allData){
		double err=0;
		double m = mean(allData.data.subList(testStart, allData.data.size())),p = mean(predC.subList(testStart, allData.data.size()));
		if (m==0)
			err=0;
		for(int i = testStart;i<allData.data.size();i++)
			err+=Math.abs(allData.data.get(i)-predC.get(i))/allData.data.get(i);
		err/=testSize;
	//	err/=m;
		errors.add(err);
		
		/*double mx = max(allData.data.subList(testStart, allData.data.size()));
		for(int i = testStart;i<allData.data.size();i++)
			err+=Math.log(Math.abs(allData.data.get(i)-predC.get(i)))-Math.log(mx);
		err/=testSize;
		errors.add(err);*/
		
		err=0;
		for(int i = testStart;i<allData.data.size();i++)
			err+=Math.pow(allData.data.get(i)-predC.get(i),2)/(p*m);
		err/=testSize;
		boolean check;
		if (err<0)
			check=true;
		errors.add(err);

		err=0;
		for(int i = testStart;i<allData.data.size();i++)
			err+=Math.pow(allData.data.get(i)-predC.get(i),2);
		//err/=Math.pow(max(allData.data.subList(testStart, allData.data.size())),2);
		err/=testSize;
		err=Math.sqrt(err);
		errors.add(err);
		
		err=0;
		for(int i = testStart;i<allData.discrete.size();i++)
			if (allData.discrete.get(i)==predD.get(i))
				err++;
		err/=testSize;
		errors.add(err);
	
		makeConfMatrix(allData.discrete);
		errors.add( meanF1() );
	//	errors.add( meanF1() / randMeanF1() );
	}
	
}

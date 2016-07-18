import java.util.ArrayList;
import java.util.Collections;


public class BaselineRegressor implements Regressor{
	
	double mean;
	double median;
	int prior;
	int size;
	int symbols;
	
	public void train(AllData allData,double ratio,int k,int b){
		
		this.size = allData.data.size();
		symbols=k;
		mean=0;
		int trainEnd = (int)(size*ratio);
		int[] syms = new int[k];
		for(int i=0;i<trainEnd;i++){
			mean+=allData.data.get(i);
			syms[allData.discrete.get(i)]++;
		}
		
		mean/=trainEnd-1;
		int max=0;
		for(int i=0;i<k;i++){
			if (syms[i]>max){
				max=syms[i];
				prior=i;
			}
		}
		
		ArrayList<Double> copy = DataPreprocessor.cloneALD(allData.data);
		Collections.sort(copy);
		median = copy.get( (int)(copy.size()/2) );
	}
	
	public Results test(AllData allData,double ratio,int b){
		int testStart=(int)(size*(1-ratio))+1;
		ArrayList<Double> predD = new ArrayList<Double>(size);
		ArrayList<Integer> predI = new ArrayList<Integer>(size);
		
		for(int i=testStart;i<size;i++){
			predD.add(median);
			predI.add(prior);
		}
		
		return new Results(predD,predI,testStart,symbols);
	}
}

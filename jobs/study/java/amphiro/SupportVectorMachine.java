import java.util.ArrayList;

import libsvm.LibSVM;
import libsvm.svm_parameter;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;




public class SupportVectorMachine implements Regressor{

	Dataset ds;
	LibSVM svm;
	int symbols;
	double[] symMeans;
	double[] symCounts;
	int maxSym;
	boolean svmTrained;
	
	public void train(AllData allData, double ratio, int k, int b) {
		int trainEnd = (int)(ratio*(allData.data.size()-b));
		symbols=k;
		ds = new DefaultDataset();
		for(int i=b;i<trainEnd;i++){
			double[] values = new double[b];
			for(int j=0;j<b;j++)
				values[j]=allData.data.get(i-j-1);
			ds.add( new DenseInstance(values,allData.discrete.get(i)) );
		}
		
		svm = new LibSVM();
		svm_parameter param = new svm_parameter();
	    param.probability = 1;
	    param.gamma = 0.5;
	    param.nu = 0.5;
	    param.C = 0.5;
	    param.svm_type = svm_parameter.C_SVC;
	    param.kernel_type = svm_parameter.POLY;
	    param.cache_size = 20000;
	    param.eps = 0.001;
		svm.setParameters(param);
		try{
			svm.buildClassifier(ds);
			svmTrained=true;
		}
		catch (Exception e){
			svmTrained=false;
		}
		
		calcSymMeans(allData,ratio,b);
			
	}

	public void calcSymMeans(AllData allData,double ratio,int b){
		symMeans=new double[symbols];
		symCounts=new double[symbols];
		
		int trainEnd = (int)(ratio*(allData.data.size()-b));
		for(int i=b;i<trainEnd;i++){
			symMeans[allData.discrete.get(i)]+=allData.data.get(i);
			symCounts[allData.discrete.get(i)]++;
		}
		
		double max=0;
		for(int i=0;i<symbols;i++){
			symMeans[i]/=symCounts[i];
			if(symCounts[i]>max){
				max=symCounts[i];
				maxSym=i;
			}
		}
		
	}
	
	public Results test(AllData allData, double ratio, int b) {
		
		ArrayList<Double> predC = new ArrayList<Double>();
		ArrayList<Integer> predD = new ArrayList<Integer>(); 
		
		int testStart = (int)((1-ratio)*(allData.discrete.size()-b));
		for(int i=b+testStart;i<allData.discrete.size();i++){
			double[] values = new double[b];
			for(int j=0;j<b;j++)
				values[j]=allData.data.get(i-j-1);
			Integer pred;
			if (svmTrained)
				pred = (Integer)svm.classify(new DenseInstance(values));
			else
				pred = maxSym;
			predD.add( pred );
			predC.add( symMeans[pred] );
			
		}
		
		return new Results(predC,predD,testStart+b,symbols);
	
	}

}

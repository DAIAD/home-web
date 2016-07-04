import java.util.ArrayList;

import libsvm.LibSVM;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import org.ejml.simple.SimpleMatrix;


public class SupportVectorRegression {
	
	svm_model mdl;
	int colCnt;
	int prev;
	int k;
	int discretization;
	boolean features;
	boolean previous;
	boolean disc;
	boolean cycles;
	double[] max;
	int volIndex;
	
	public void findMaxes(ArrayList<MultiPoint> data){
		int L = max.length;
		for(int i=0;i<L;i++)
			max[i] = Double.NEGATIVE_INFINITY;
		for(MultiPoint d:data)
			for(int l=0;l<L;l++)
			if (d.values[l]>max[l])
				max[l]=d.values[l];
	}
	
	public void addPrev(ArrayList<MultiPoint> data, svm_node[] buf,int i){
		int L = data.get(0).values.length;
		for(int j=0;j<prev;j++){
			for( int l = 0 ; l<L ; l++){
				svm_node node = new svm_node();
				node.index = colCnt+j;
				node.value = data.get(i-j-1).values[l];
				buf[colCnt+j*L+l]=node;
			}
		}
//		svm_node node = new svm_node();
//		node.index = colCnt+prev;
//		node.value = 1.0;
//		buf[colCnt+prev*L]=node;
		colCnt+=prev*L;
		
	}
		
	
	public svm_parameter setupSVM(){
		
		svm_parameter param = new svm_parameter();
	    param.probability = 0;
	    param.gamma = 0.5;
	  //param.nu = 0.5;
		param.C = 0.5;
	    param.svm_type = svm_parameter.EPSILON_SVR;
	    param.kernel_type = svm_parameter.POLY;
	    param.cache_size = 20000;
	    param.eps = 0.001;
		
	    return param;
	
	}
	
	public ArrayList<MultiPoint> scaleDown(ArrayList<MultiPoint> original){
		int L = original.get(0).values.length;
		double[] buf;
		ArrayList<MultiPoint> scaled = new ArrayList<MultiPoint>();
		for(MultiPoint mp:original){
			buf = new double[L];
			for(int i=0;i<L;i++){
				buf[i] = mp.values[i]/max[i];
			}
			scaled.add(new MultiPoint(buf));
		}
		return scaled;
	
	}
	
	public ArrayList<MultiPoint> scaleUp(ArrayList<MultiPoint> original){
		int L = original.get(0).values.length;
		double[] buf;
		ArrayList<MultiPoint> scaled = new ArrayList<MultiPoint>();
		for(MultiPoint mp:original){
			buf = new double[L];
			for(int i=0;i<L;i++){
				buf[i] = mp.values[i]*max[i];
			}
			scaled.add(new MultiPoint(buf));
		}
		return scaled;
	
	}
	
	
	
	public void train( ArrayList<MultiPoint> data, double ratio, int prev ){
		
		this.prev=prev;
		max = new double[data.get(0).values.length];
		if (data.get(0).values.length==1)
			volIndex=0;
		else
			volIndex=3;
		findMaxes(data);
		
		ArrayList<MultiPoint> sData = scaleDown(data);
		
		int trainEnd;
		int l;
		int start=prev; 
		int totalCols=0;
	
		
		totalCols+=prev*data.get(0).values.length;
		
		trainEnd = (int)(ratio*(data.size()-start));
		l = trainEnd-start;
		
		svm_problem trainSet = new svm_problem();
		trainSet.y = new double[l];
		trainSet.l = l;
		trainSet.x = new svm_node[l][totalCols];
		
		
		for(int i=start;i<trainEnd;i++){
			
			colCnt=0;
			svm_node[] buf = new svm_node[totalCols];	
			
			addPrev(sData,buf,i);
			
			trainSet.x[i-start] = buf;
			trainSet.y[i-start] = sData.get(i).values[volIndex];
		
		}
		
		svm_parameter param = setupSVM();
		mdl = svm.svm_train(trainSet, param); 
		
	}
	
	public Results test(ArrayList<MultiPoint> data,double ratio,ArrayList<Range> ranges){
		
		int l;
		int start=prev; 
		int totalCols=0;
		
		ArrayList<MultiPoint> sData = scaleDown(data);
		ArrayList<Double> predictions = new ArrayList<Double>();
		
		totalCols+=prev*data.get(0).values.length;
		
		int testStart = (int)((1-ratio)*(data.size()-start));
		l = data.size()-testStart;	
		
		for(int i=testStart;i<data.size();i++){
			
			colCnt=0;
			svm_node[] buf = new svm_node[totalCols];	
			
			addPrev(sData,buf,i);
				
			predictions.add(svm.svm_predict(mdl, buf)*max[volIndex]);
		
		}
		
		ArrayList<Integer> discPredictions = DataPreprocessor.discretize(predictions, ranges); 
		
		return new Results(predictions,discPredictions,testStart+start,ranges.size());
	
	}
}

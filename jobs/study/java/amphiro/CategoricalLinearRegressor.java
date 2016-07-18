import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;


public class CategoricalLinearRegressor implements Regressor{
	
	SimpleMatrix X;
	SimpleMatrix Y;
	SimpleMatrix b;
	double lamda = 0.1;
	
	public void train(AllData allData,double ratio,int k,int b){
		
		X = new SimpleMatrix(allData.data.size()-b,k*b+1);
		Y = new SimpleMatrix(allData.data.size()-b,1);
		
		int trainEnd = (int)(ratio*(allData.data.size()-b));
		
		for(int i=b;i<allData.data.size();i++){
			for(int j=0;j<b;j++){
				X.set(i-b,j*k+allData.metaDiscrete.get(i-j-1),1);
			}
			X.set(i-b,b*k,1);
			Y.set(i-b,allData.data.get(i));
		}
		SimpleMatrix X_t = X.extractMatrix(0, trainEnd, 0, X.numCols());
		SimpleMatrix Y_t = Y.extractMatrix(0, trainEnd, 0, Y.numCols());
		this.b=X_t.transpose().mult(X_t).plus( SimpleMatrix.identity(X_t.numCols()).scale(lamda) ).invert().mult(X_t.transpose()).mult(Y_t);
	}
	
public Results test(AllData allData,double ratio,int b){
		
		int testStart = (int)((1-ratio)*Y.numRows());
		
		SimpleMatrix X_t = X.extractMatrix(testStart, X.numRows(), 0, X.numCols());
		SimpleMatrix Y_t = X_t.mult(this.b);
		
		ArrayList<Double> predictionsD = new ArrayList<Double>();
		ArrayList<Integer> predictionsI = new ArrayList<Integer>();
		
		for (int i=0;i<Y_t.numRows();i++)
			predictionsD.add(Y_t.get(i));
		
		predictionsI = DataPreprocessor.discretize(predictionsD, allData.ranges);
		
		return new Results(predictionsD,predictionsI,testStart+b,allData.ranges.size());
		
		
	}
	
}

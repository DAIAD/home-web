import java.util.Comparator;


public class arrayComparator implements Comparator<Integer[]>{

	@Override
	public int compare(Integer[] arg0, Integer[] arg1) {
		
		for(int i=0;i<arg0.length;i++){
			if(arg0[i]>arg1[i])
				return-1; 
			if(arg0[i]<arg1[i])
				return 1;
		}
		
		return 0;
	}

}

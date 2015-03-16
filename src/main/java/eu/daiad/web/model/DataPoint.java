package eu.daiad.web.model;

import org.joda.time.DateTime;

public class DataPoint {

	public long showerId;
	
	public int showerTime;
	
    public float temperature = 0;
    
    public float volume = 0;

    public float energy = 0;
       
    public long timestamp;
    
	public int getDayOfMonth() {
		return new DateTime(this.timestamp).getDayOfMonth();
	}
    
}

package main;

import org.apfloat.Apfloat;


public class TaskResult{
	private Apfloat lastPartialTerm;
	private Apfloat sum;
	
    private long timeToComplete;
    private int numTerms;
    
    public TaskResult(Apfloat sum, Apfloat lastPartialTerm, int numTerms, long timeMilliseconds) {
        this.sum = sum;
        this.lastPartialTerm = lastPartialTerm;
        this.numTerms = numTerms;
        this.timeToComplete = timeMilliseconds;
    }
    
    public Apfloat getSum() {
        return sum;
    }

    public Apfloat getLastPartialTerm() {
        return lastPartialTerm;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public long getTimeToComplete() {
        return timeToComplete;
    }
}

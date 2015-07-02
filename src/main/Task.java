package main;

import java.util.Properties;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatContext;

// The basic logic behind calculating a sum of Rumanujan 
// Each process calculates a sum 
public class Task{
	private int id;
	private int startIndex, numTerms;

	public Task(int id, int startIndex, int numTerms) {
		this.id = id;
		this.startIndex = startIndex;
		this.numTerms = numTerms;
	}

	public TaskResult calculate(Rumanujan2Pi rSum) {

		if (!rSum.isQuiet()) {
			System.out.println("Thread " + id + " started / Start Index - "
					+ startIndex + " / Terms to compute - " + numTerms);
		}
		int progressStep = numTerms / 100;
		int currentProgress = 0;

		Apfloat lastPartialTerm = Apfloat.ONE;
		Apfloat sum = Apfloat.ZERO;
		long startTime = System.currentTimeMillis();

		ApfloatContext context = new ApfloatContext(new Properties());
		context.setNumberOfProcessors(1);
		ApfloatContext.setThreadContext(context);

		int endIndex = startIndex + numTerms;
		for (int i = startIndex; i < endIndex; i++) {
			sum = sum.add(rSum.calculateTerm(lastPartialTerm, i));
			lastPartialTerm = rSum.nextPartialTerm(lastPartialTerm, i + 1);

			currentProgress += progressStep;
		}

		long timeElapsed = System.currentTimeMillis() - startTime;
		if (!rSum.isQuiet()) {
			System.out.println("Thread " + id
					+ " finished / Time to complete: " + timeElapsed + "ms");
		}
		return new TaskResult(sum, lastPartialTerm, numTerms, timeElapsed);
	}

}

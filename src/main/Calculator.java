package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apfloat.Apfloat;

/* Main thread operator */
//Controls the threads, the desired calculator is split into

public class Calculator {
	private List<Task> tasks;
	private ExecutorService executor;
	private int numThreads;

	public Calculator(int numThreads) {
		this.numThreads = numThreads;
		tasks = new ArrayList<Task>(numThreads);
		this.executor = Executors.newFixedThreadPool(numThreads);
	}

	Calculator(int numThreads, int maxNumThreadsAtOnce) {
		tasks = new ArrayList<Task>(numThreads);
		this.executor = Executors.newFixedThreadPool(maxNumThreadsAtOnce);
	}

	public TaskResult calculate(Rumanujan2Pi sum, int numTerms)
			throws InterruptedException, ExecutionException {

		distributeTaskTerms(numTerms);
		final List<Future<TaskResult>> futureResults = new ArrayList<Future<TaskResult>>(
				tasks.size());

		for (int i = 0; i < tasks.size(); i++) {
			Task taskToCall = tasks.get(i);
			
			Callable<TaskResult> callableTask = new Callable<TaskResult>() {
				@Override
				public TaskResult call() throws Exception {
					return taskToCall.calculate(sum);
				}
			};

			futureResults.add(executor.submit(callableTask));

		}

		List<TaskResult> finalResults = waitAndGetAllResults(futureResults);

		return combineSums(finalResults);
	}

	private void distributeTaskTerms(int numTerms) {
		int numTermsLeft = numTerms;
		int termsAssigned = numTerms / numThreads;
		int startIndex = 0;

		for (int i = 0; i < numThreads - 1; i++) {
			tasks.add(new Task(i, startIndex, termsAssigned));
			startIndex += termsAssigned;
			numTermsLeft -= termsAssigned;
		}

		tasks.add(new Task(numThreads - 1, startIndex, numTermsLeft));
	}

	private List<TaskResult> waitAndGetAllResults(
			List<Future<TaskResult>> futureResults)
			throws InterruptedException, ExecutionException {

		List<TaskResult> results = new ArrayList<TaskResult>(
				futureResults.size());

		for (int i = 0; i < futureResults.size(); i++) {
			results.add(futureResults.get(i).get());
		}

		return results;
	}

	private TaskResult combineSums(List<TaskResult> results) {
		Apfloat sum = Apfloat.ZERO;
		Apfloat lastPartialTerm = Apfloat.ONE;
		int termCount = 0;

		for (TaskResult partialResult : results) {
			sum = sum.add(lastPartialTerm.multiply(partialResult.getSum()));

			lastPartialTerm = lastPartialTerm.multiply(partialResult
					.getLastPartialTerm());
			termCount += partialResult.getNumTerms();
		}

		return new TaskResult(sum, lastPartialTerm, termCount, 1);
	}

	public void shutdown() {
		executor.shutdown();
	}
}

package main.GUI;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatContext;

import main.Rumanujan2Pi;
import main.TaskResult;

public class MainFrame extends JFrame {

	private TextPanel textPanel;
	private DetailsPanel detailsPanel;

	private Rumanujan2Pi sum;
	private int numTerms;
	private int numThreads;
	private String outFile;

	private SwingCalculator calculator;
	static TaskResult taskResult;

	private JProgressBar[] progressBars;

	public MainFrame(int numTerms, int numThreads, String outFile) {
		super("Pi Calculator");

		setSize(720, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		// Set layout manager
		setLayout(new BorderLayout());

		// Create swing components
		textPanel = new TextPanel();

		this.numTerms = numTerms;
		this.numThreads = numThreads;
		this.outFile = outFile;

		detailsPanel = new DetailsPanel(numTerms, numThreads);
		
		detailsPanel.setDetailListener(new DetailListener() {
			public void detailEventOccured(DetailEvent e) {
				int[] details = e.getDetails();
				progressBars = e.getProgressBars();

				sum = new Rumanujan2Pi(details[0], false);

				if (details[0] <= 0 || details[1] <= 0) {
					textPanel
							.appendText("Please enter correct values for terms and threads\n");
				} else {
					calculator = new SwingCalculator(sum, details[0],
							details[1]);
					calculator.execute();

				}
			}
		});

		// Add swing components
		add(textPanel, BorderLayout.CENTER);
		add(detailsPanel, BorderLayout.WEST);
		
		startFirstCalculation();
	}

	private void startFirstCalculation() {
		progressBars = detailsPanel.getProgressBars();
		
		sum = new Rumanujan2Pi(numTerms, false);
		calculator = new SwingCalculator(sum, numTerms, numThreads);
		calculator.execute();
	}

	public class SwingTask {
		private int id;
		private int startIndex, numTerms;
		private float currentProgress;

		public SwingTask(int id, int startIndex, int numTerms) {
			this.id = id;
			this.startIndex = startIndex;
			this.numTerms = numTerms;
			currentProgress = 0.0f;
		}

		public TaskResult calculate(Rumanujan2Pi rSum) {

			if (!rSum.isQuiet()) {
				System.out.println("Thread " + id + " started / Start Index - "
						+ startIndex + " / Terms to compute - " + numTerms);
			}
			float progressStep = 100.f / numTerms;

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
				System.out
						.println("Thread " + id
								+ " finished / Time to complete: "
								+ timeElapsed + "ms");
			}
			return new TaskResult(sum, lastPartialTerm, numTerms, timeElapsed);
		}

		public float getProgress() {
			return currentProgress;
		}

	}

	public class SwingCalculator extends SwingWorker<TaskResult, String> {

		private List<SwingTask> tasks;
		private ExecutorService executor;
		private int numThreads;
		private int numTerms;
		private Rumanujan2Pi sum;

		public SwingCalculator(Rumanujan2Pi sum, int numTerms, int numThreads) {
			this.numThreads = numThreads;
			this.numTerms = numTerms;
			this.sum = sum;
			this.tasks = new ArrayList<SwingTask>(numThreads);
			this.executor = Executors.newFixedThreadPool(numThreads);
		}

		@Override
		protected TaskResult doInBackground() throws Exception {
			distributeTaskTerms(numTerms);
			final List<Future<TaskResult>> futureResults = new ArrayList<Future<TaskResult>>(
					tasks.size());
			long startTime, timeToCompute;

			publish("Starting....\n");
			publish("Terms: " + numTerms + " | Threads: " + numThreads + "\n");
			for (int i = 0; i < 8; i++) {
				System.out.println("Thread " + i + ": " + 0 + "%\n");
				progressBars[i].setValue(0);
			}

			startTime = System.currentTimeMillis();
			for (int i = 0; i < tasks.size(); i++) {
				SwingTask taskToCall = tasks.get(i);

				publish("Thread " + i + " started \n");
				Callable<TaskResult> callableTask = new Callable<TaskResult>() {
					@Override
					public TaskResult call() throws Exception {
						return taskToCall.calculate(sum);
					}
				};

				futureResults.add(executor.submit(callableTask));

			}
			int numCompleted = 0;
			int[] lastPrinted = new int[numThreads];
			boolean[] completed = new boolean[numThreads];
			for (int i = 0; i < numThreads; i++) {
				lastPrinted[i] = 0;
				completed[i] = false;
			}

			while (numCompleted < numThreads) {
				for (int i = 0; i < tasks.size(); i++) {
					int progress = Math.round(tasks.get(i).getProgress());
					if (progress > 99 && completed[i] == false && progress!= lastPrinted[i]) {
						numCompleted++;
						completed[i] = true;
						System.out.println("Thread " + i + ": " + progress
								+ "%\n");
						progressBars[i].setValue(Math.round(progress));
						lastPrinted[i] = progress;
					} else if (progress < 99 && progress!= lastPrinted[i]) {
						System.out.println("Thread " + i + ": " + progress
								+ "%\n");
						progressBars[i].setValue(Math.round(progress));
						lastPrinted[i] = progress;
					}
				}
			}

			for (int i = 0; i < tasks.size(); i++) {
				System.out.println("Thread " + i + ": " + 100 + "%\n");
				progressBars[i].setValue(100);
			}

			List<TaskResult> finalResults = waitAndGetAllResults(futureResults);
			executor.shutdown();

			timeToCompute = System.currentTimeMillis() - startTime;
			return combineSums(finalResults, timeToCompute);
		}

		@Override
		protected void done() {
			try {
				taskResult = get();
				Apfloat pi;
				long time = taskResult.getTimeToComplete();
				pi = sum.roundPiSum(taskResult);

				textPanel.appendText("Pi: " + pi.toString() + "\n");
				textPanel.appendText("Time to complete: "
						+ String.valueOf(time) + "ms\n");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void process(List<String> chunks) {
			for (int i = 0; i < chunks.size(); i++) {
				String text = chunks.get(i);
				textPanel.appendText(text);
			}

		}

		private void distributeTaskTerms(int numTerms) {
			int numTermsLeft = numTerms;
			int termsAssigned = numTerms / numThreads;
			int startIndex = 0;

			for (int i = 0; i < numThreads - 1; i++) {
				tasks.add(new SwingTask(i, startIndex, termsAssigned));
				startIndex += termsAssigned;
				numTermsLeft -= termsAssigned;
			}

			tasks.add(new SwingTask(numThreads - 1, startIndex, numTermsLeft));
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

		private TaskResult combineSums(List<TaskResult> results,
				long timeToCompute) {
			Apfloat sum = Apfloat.ZERO;
			Apfloat lastPartialTerm = Apfloat.ONE;
			int termCount = 0;

			for (TaskResult partialResult : results) {
				sum = sum.add(lastPartialTerm.multiply(partialResult.getSum()));

				lastPartialTerm = lastPartialTerm.multiply(partialResult
						.getLastPartialTerm());
				termCount += partialResult.getNumTerms();
			}

			return new TaskResult(sum, lastPartialTerm, termCount,
					timeToCompute);
		}
	}

}

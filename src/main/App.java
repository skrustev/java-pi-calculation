package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import main.Rumanujan2Pi;
import main.Calculator;
import main.CustomMath;
import main.Task;
import main.TaskResult;
import main.Task;

import javax.swing.SwingUtilities;

import main.GUI.MainFrame;

import org.apfloat.Apfloat;

public class App {

	public static boolean quiet = false;

	public static void main(String[] args) throws FileNotFoundException,
			InterruptedException, ExecutionException, RemoteException {

		int numThreads = 1, numTerms = 100;
		String outFile = "pi";

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("-p") || arg.equals("--terms")) {
				numTerms = Integer.parseInt(args[i + 1]);
				i++;
			} else if (arg.equals("-t") || arg.equals("--threads")) {
				numThreads = Integer.parseInt(args[i + 1]);
				i++;
			} else if (arg.equals("-o") || arg.equals("--out")) {
				outFile = args[i + 1];
				i++;
			} else if (arg.equals("-q") || arg.equals("--quiet")) {
				quiet = true;
			} else {
				System.err.println("Unknown option " + arg);
				System.exit(1);
			}
		}

		if (numTerms == 0) {
			System.err.println("Number of terms should be specified with -p");
			System.exit(1);
		}

		if (numThreads == 0) {
			numThreads = Runtime.getRuntime().availableProcessors();
			System.out.println("Using " + numThreads + " threads");
		}

		Rumanujan2Pi sum = new Rumanujan2Pi(numTerms, quiet);
		if (quiet) {
			CaltulatePi(sum, numTerms, numThreads, outFile);
		} else {
			final int numTermsF = numTerms;
			final int numThreadsF = numThreads;
			final String outFileF = outFile;
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new MainFrame(numTermsF, numThreadsF, outFileF);
				}
			});
		}

	}

	private static void CaltulatePi(Rumanujan2Pi sum, int numTerms,
			int numThreads, String outFile) throws FileNotFoundException,
			InterruptedException, ExecutionException, RemoteException {

		long startTime, timeToCompute;
		
		Calculator calculator = new Calculator(numThreads);
		TaskResult result;
		Apfloat pi;

		startTime = System.currentTimeMillis();
		result = calculator.calculate(sum, numTerms);
		pi = sum.roundPiSum(result);
		timeToCompute = System.currentTimeMillis() - startTime;
		calculator.shutdown();

		System.out.println("Time to complete: " + timeToCompute + "ms");

		PrintWriter file = new PrintWriter(new FileOutputStream(outFile, false));
		file.println(pi.toString());
		file.flush();
		file.close();

	}
}

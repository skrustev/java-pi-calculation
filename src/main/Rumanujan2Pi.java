package main;

import java.math.RoundingMode;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class Rumanujan2Pi{
	private static final int minPrecisionPerTerm = 6;

	private Apfloat a, b, c, c2, four, minusOne;
	private boolean quiet = false;

	public Rumanujan2Pi(int numTerms, boolean quiet) {
		int precision = numTerms * minPrecisionPerTerm;

		a = new Apfloat(1123, precision);
		b = new Apfloat(21460, precision);
		c = new Apfloat(882, precision);
		c2 = ApfloatMath.pow(c, 2);
		four = new Apfloat(4, precision);
		minusOne = new Apfloat(-1, precision);
		this.quiet = quiet;
	}
	
	public boolean isQuiet() {
		return quiet;
	}

	public Apfloat nextPartialTerm(Apfloat lastPartialTerm, int index) {
		Apfloat nextPartial = lastPartialTerm
				.multiply(minusOne)
				.multiply(CustomMath.fact2(4 * (index - 1) + 1, 4 * index))
				.divide(ApfloatMath.pow(four.multiply(new Apfloat(index)), 4)
						.multiply(c2));

		return nextPartial;
	}

	public Apfloat calculateTerm(Apfloat partialTerm, int index) {
		return partialTerm.multiply(a.add(b.multiply(new Apfloat(index))));
	}
	
	public Apfloat roundPiSum(TaskResult result) {
		Apfloat pi1 = finalizeSum(result.getSum());
		Apfloat pi2 = finalizeSum(result.getSum().add(calculateTerm(result.getLastPartialTerm(), result.getNumTerms())));
		long equalDigits = pi1.equalDigits(pi2);

        return ApfloatMath.round(pi1, equalDigits - 2, RoundingMode.FLOOR);
	}
	
	private Apfloat finalizeSum(Apfloat sum) {
		return Apfloat.ONE.divide(sum.divide(new Apfloat(4).multiply(new Apfloat(882))));
	}
}

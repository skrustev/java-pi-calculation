package main;
import org.apfloat.Apfloat;

public class CustomMath {
	public static Apfloat fact(int num) {
		if (num == 0) {
			return Apfloat.ONE;
		}

		return fact2(1, num);
	}

	public static Apfloat fact2(int from, int to) {
		if (from == to) {
			return new Apfloat(from);
		}

		int middle = (from + to) / 2;
		Apfloat a = fact2(from, middle);
		Apfloat b = fact2(middle + 1, to);

		return a.multiply(b);
	}

}

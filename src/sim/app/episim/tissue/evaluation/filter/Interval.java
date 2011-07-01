package sim.app.episim.tissue.evaluation.filter;

public class Interval {
	public Interval(boolean includeMin, double min, boolean includeMax,
			double max) {
		if (max >= min) {
			this.min = min;
			this.max = max;
		} else {
			this.min = max;
			this.max = min;
		}
		if (min == max) {
			this.includeMax = this.includeMin = true;

		} else {
			this.includeMax = includeMax;
			this.includeMin = includeMin;
		}
	}

	public Interval(double min, double max) {
		this(true, min, true, max);
	}

	public boolean touches(Interval int2) {
		return (this.includes(int2.min) || this.includes(int2.max));
	}

	public double length() {
		return max - min;
	}

	public boolean includes(double value) {
		return (overMin(value) && underMax(value));
	}

	public double compare(double value) {
		if (includes(value))
			return 0;
		else {
			return value - min;
		}

	}

	public Interval nextMatching(double value) {

		Interval i = new Interval(includeMin, min, includeMax, max);
		if (length() == 0 || (!includeMin && !includeMax))
			i = new Interval(true, value, true, value);
		while (!i.overMin(value)) {
			i = new Interval(includeMin, i.getMin() - i.length(), includeMax,
					i.getMax() - i.length());
		}
		while (!i.underMax(value)) {
			i = new Interval(includeMin, i.getMin() + i.length(), includeMax,
					i.getMax() + i.length());
		}
		return i;
	}

	public Interval nextInterval() {
		return new Interval(includeMin, max, includeMax, 2 * max - min);
	}

	public Interval previousInterval() {
		return new Interval(includeMin, 2 * min - max, includeMax, min);
	}

	public double getMean() {
		return (min + max) / 2.0d;
	}

	private boolean overMin(double value) {
		if (includeMin)
			return value >= min;
		else
			return value > min;
	}

	private boolean underMax(double value) {
		if (includeMax)
			return value <= max;
		else
			return value < max;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	private double min;
	private double max;
	private boolean includeMin;
	private boolean includeMax;

}

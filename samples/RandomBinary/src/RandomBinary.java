import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.*;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy.FuzzyForwardChecking2;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.InteractiveProblemFactory;

/**
 * ランダム二項問題の実装サンプルです．
 * @author Takuto Yanagida
 * @version 2012/11/22
 */
public class RandomBinary implements InteractiveProblemFactory {

	// ################################################################

	final static int COUNT = 10;  // 試行回数
	final static int VAR_NUM = 10;  // 変数の個数
	final static double DENSITY = 0.5;
	final static double AVE_TIGHTNESS = 0.5;

	static public void main(String args[]) {
		double sum_time = 0.0, sum_degree = 0.0;
		for(int i = 0; i < COUNT; ++i) {
			final RandomBinary rp = new RandomBinary(VAR_NUM, DENSITY, AVE_TIGHTNESS);
			final Problem p = rp.createProblem(new Problem());	
			final long t = System.currentTimeMillis();  // 計時開始

			final Solver s = new FuzzyForwardChecking2(p);
			s.setTargetRate(Solver.UNSPECIFIED);
			s.setTimeLimit(10000);
			s.solve();

			final double ct = System.currentTimeMillis() - t;  // 計時終了
			final double cd = p.worstSatisfactionDegree();
			System.out.println("Trial: " + (i + 1) + "   Time: " + ct + "   Degree: " + cd);
			sum_time += ct; sum_degree += cd;
		}
		System.out.println("Average Time: " + (sum_time / COUNT) + "   Average Degree: " + (sum_degree / COUNT));
	}

	// ################################################################

	static private class TableRelation extends FuzzyRelation {
		
		final private double[][] table_;
		
		public TableRelation(double[][] table) {
			table_ = table;
		}

		@Override
		public double satisfactionDegree(int value1, int value2) {
			return table_[value1][value2];
		}

	}
	static private final Random RAND = new Random();

	private int n_;  // 変数の個数
	private double den_;	
	private double t_;
	private int sig_;  // 各ドメインの要素数（本来は変数の個数と等しいらしい...[Bowen1996]）

	public RandomBinary() {
		// do nothing
	}

	public RandomBinary(final int varCount, final double density, final double aveTightness) {
		this(varCount, density, aveTightness, varCount);
	}

	public RandomBinary(final int varCount, final double density, final double aveTightness, final int domainSize) {
		n_ = varCount;
		den_ = density;
		t_ = aveTightness;
		sig_ = domainSize;
	}

	public int getVariableCount() {
		return n_;
	}
	
	public void setVariableCount(final int count) {
		n_ = count;
	}

	public double getDensity() {
		return den_;
	}
	
	public void setDensity(final double density) {
		den_ = density;
	}

	public double getAverageTightness() {
		return t_;
	}
	
	public void setAverageTightness(final double tightness) {
		t_ = tightness;
	}
	
	public int getDomainSize() {
		return sig_;
	}
	
	public void setDomainSize(final int size) {
		sig_ = size;
	}
	
	@Override
	public boolean showDialog(Object ownerWindow) {
		final Frame f = (ownerWindow instanceof Frame) ? (Frame)ownerWindow : null;
		final RandomBinaryDialog d = new RandomBinaryDialog(f, true);
		if(d.showDialog() == 1) {
			n_ = d.getVariableCount();
			den_ = d.getDensity();
			t_ = d.getAverageTightness();
			sig_ = d.getDomainSize();
			if(sig_ == 0) sig_ = n_;
			return true;
		}
		return false;
	}

	@Override
	public boolean isFuzzy() {
		return true;
	}

	@Override
	public Problem createProblem(final Problem p) {
		final int r = (int)(den_ * ((n_ * n_ - n_) / 2));
		final Variable vs[] = new Variable[n_];
		for(int i = 0; i < vs.length; ++i) {
			vs[i] = p.createVariable(p.createDomain(0, sig_ - 1), 0);
		}
		final List<Constraint> temp = new ArrayList<>();
		while(p.constraintSize() < r) {
			int i = RAND.nextInt(n_), j = RAND.nextInt(n_);
			if(i != j) {
				p.constraintsBetween(vs[i], vs[j], temp);
				if(temp.isEmpty()) {
					p.createConstraint(new TableRelation(getRelationTable()), vs[i], vs[j]);
				}
			}
		}
		return p;
	}

	private double[][] getRelationTable() {
		final double[][] table = new double[sig_][]; 
		for(int i = 0; i < sig_; ++i) {
			table[i] = new double[sig_];
		}
		for(int i = 0; i < sig_; ++i) {
			for(int j = 0; j < sig_; ++j) {
				final double q = (t_ == 0.0) ? Double.MAX_VALUE : (1.0 - t_) / t_;
				table[i][j] = Beta.random(1.0, q);
			}
		}
		return table;
	}

}

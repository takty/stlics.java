

import java.awt.Frame;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispRelation;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp.Breakout;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.InteractiveProblemFactory;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.ProblemFactory;

/**
 * Nクイーン問題の実装サンプルです．
 * @author Takuto Yanagida
 * @version 2012/11/22
 */
public class N_queens implements InteractiveProblemFactory {

	// ################################################################

	final static int COUNT = 10;  // 試行回数
	final static int QUEEN_NUM = 100;  // クイーン数

	static public void main(String args[]) {
		double sum_time = 0.0, sum_rate = 0.0;
		for(int i = 0; i < COUNT; ++i) {
			final N_queens nq = new N_queens(QUEEN_NUM);
			final CrispProblem p = (CrispProblem)nq.createProblem(new CrispProblem());
			long t = System.currentTimeMillis();  // 計時開始

			final Solver s = new Breakout(p);
			s.solve();

			final double ct = System.currentTimeMillis() - t;  // 計時終了
			final double cr = p.satisfiedConstraintRate();
			System.out.println("Time: " + ct + "   Rate: " + cr);
			nq.printResult(p);
			sum_time += ct; sum_rate += cr;
		}
		System.out.println("Average Time: " + (sum_time / COUNT) + "   Average Rate: " + (sum_rate / COUNT));
	}

	// ################################################################

	private int n_;

	static private class QueenRelation extends CrispRelation {
		private final int diff_;
		public QueenRelation(final int i, final int j) {diff_ = j - i;}
		@Override
		public boolean isSatisfied(final int v1, final int v2) {
			if((v1 != v2) && (v1 != v2 + diff_) && (v1 != v2 - diff_)) return true;
			return false;
		}
	}

	public N_queens() {
		// do nothing
	}

	public N_queens(final int queenSize) {
		n_ = queenSize;
	}
	
	public int getQueenSize() {
		return n_;
	}
	
	public void setQueenSize(final int n) {
		n_ = n;
	}
	
	@Override
	public boolean showDialog(Object ownerWindow) {
		final Frame f = (ownerWindow instanceof Frame) ? (Frame)ownerWindow : null;
		final N_queensDialog d = new N_queensDialog(f, true);
		if(d.showDialog() == 1) {
			n_ = d.getQueenSize();
			return true;
		}
		return false;
	}

	@Override
	public boolean isFuzzy() {
		return false;
	}

	@Override
	public Problem createProblem(final Problem p) {
		final Variable v[] = new Variable[n_];
		for(int i = 0; i < v.length; ++i) {
			v[i] = p.createVariable("Queen " + i, p.createDomain(1, n_), 1);
		}
		for(int i = 0; i < n_; ++i) {
			for(int j = i + 1; j < n_; ++j) {
				p.createConstraint(new QueenRelation(i, j), v[i], v[j]);
			}
		}
		return p;
	}

	public void printResult(final Problem p) {
		StringBuilder el = new StringBuilder();
		for(int i = 0; i < n_; ++i) el.append("- ");
		for(int i = 0; i < n_; ++i) {
			StringBuilder line = new StringBuilder(el);
			if(!p.variableAt(i).isEmpty()) line.setCharAt(((p.variableAt(i).value()) - 1) * 2, 'o');
			System.out.println(line);
		}
	}

}

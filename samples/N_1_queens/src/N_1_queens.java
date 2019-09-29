import java.awt.Frame;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.FuzzyRelation;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy.FuzzyForwardChecking2;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy.FuzzyForwardChecking2_v2;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.InteractiveProblemFactory;

/**
 * N-1クイーン問題の実装サンプルです．
 * これは，N個のクイーンを縦Nマス，横N-1マスの盤面にできるだけクイーン同士が取られないように配置する問題です．
 * もし，二つのクイーンが取られる位置関係にあるとき，その距離が遠ければ遠いほど充足度は高くなります．
 * @author Takuto Yanagida
 * @version 2012/11/26
 */
public class N_1_queens implements InteractiveProblemFactory {

	// ################################################################

	final static int COUNT = 1;  // 試行回数
	final static int QUEEN_NUM = 200;  // クイーン数

	static public void main(final String args[]) {
		double sum_time = 0.0, sum_degree = 0.0;
		for(int i = 0; i < COUNT; ++i) {
			final N_1_queens nq = new N_1_queens(QUEEN_NUM);
			final Problem p = nq.createProblem(new Problem());
			final long t = System.currentTimeMillis();  // 計時開始
			
			final Solver s = new FuzzyForwardChecking2_v2(p);
			s.setTargetRate(Solver.UNSPECIFIED);
			s.setTimeLimit(10000);
			s.solve();

			final double ct = System.currentTimeMillis() - t;  // 計時終了
			final double cd = p.worstSatisfactionDegree();
			System.out.println("Trial: " + (i + 1) + "   Time: " + ct + "   Degree: " + cd);
			nq.printResult(p);
			sum_time += ct; sum_degree += cd;
		}
		System.out.println("Average Time: " + (sum_time / COUNT) + "   Average Degree: " + (sum_degree / COUNT));
	}

	// ################################################################

	private int n_;

	public N_1_queens() {
		// do nothing
	}
	
	public N_1_queens(final int queenSize) {
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
		final N_1_queensDialog d = new N_1_queensDialog(f, true);
		if(d.showDialog() == 1) {
			n_ = d.getQueenSize();
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
		final Variable v[] = new Variable[n_];
		for(int i = 0; i < v.length; ++i) {
			v[i] = p.createVariable("Queen " + i, p.createDomain(1, n_ - 1), 1);
		}
		for(int i = 0; i < n_; ++i) {
			for(int j = i + 1; j < n_; ++j) {
				p.createConstraint(new QueenRelation(i, j), v[i], v[j]);
			}
		}
		return p;
	}

	private class QueenRelation extends FuzzyRelation {
		private final int i_, j_;
		public QueenRelation(final int i, final int j) {i_ = i; j_ = j;}
		@Override
		public double satisfactionDegree(final int v1, final int v2) {
			if((v1 != v2) && (v1 != v2 + (j_ - i_)) && (v1 != v2 - (j_ - i_))) return 1.0;
			return (j_ - i_ - 1.0) / (n_ - 1);
		}
	}

	public void printResult(final Problem p) {
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n_ - 1; ++i) sb.append("- ");
		for(int i = 0; i < n_; ++i) {
			final StringBuilder line = new StringBuilder(sb);
			if(!p.variableAt(i).isEmpty()) line.setCharAt(((p.variableAt(i).value()) - 1) * 2, 'o');
			System.out.println(line);
		}
	}

}

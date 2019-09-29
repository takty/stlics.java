package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import java.util.*;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractFuzzySolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.StochasticSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.Assignment;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * ファジィCSP用のブレイクアウト法によるソルバを実装したクラスです．
 * @author Takuto Yanagida
 * @version 2012/07/31
 */
public class FuzzyBreakout extends AbstractFuzzySolver implements StochasticSolver, Randomizable {

	private final double[] weights_;
	private boolean isRandomized_ = true;
	private double lastSolutionDeg_;

	public FuzzyBreakout(Problem p) {
		super(p, "Breakout for fuzzy CSPs");
		weights_ = new double[pro_.constraintSize()];
		Arrays.fill(weights_, 1);
	}

	private void findCandidates(Variable[] worstVariables, AssignmentList candidates) {
		double maxDiff = 0.0;
		for(Variable v: worstVariables) {
			final int v_val = v.value();  // 値を保存
			Constraint[] v_c = v.constraints();
			Domain v_d = v.domain();

			double nowVio = 0.0;
			for(Constraint c: v_c) {
				nowVio += (1.0 - c.satisfactionDegree()) * weights_[c.index()];
			}
			out: for(int i = 0; i < v_d.size(); ++i) {
				int d = v_d.at(i);
				if(v_val == d) continue;
				v.assign(d);
				double diff = nowVio;
				for(Constraint c: v_c) {
					diff -= (1.0 - c.satisfactionDegree()) * weights_[c.index()];
					if(diff < maxDiff) continue out;  // これまでの改善幅よりも少なくなったら次の変数を試す
				}
				if(diff > maxDiff) {  // これまでよりも改善する割り当てが見つかった
					maxDiff = diff;
					candidates.clear();
					candidates.add(v, d);
				} else if(maxDiff != 0.0) {  // これまでと同等の改善が可能な割り当てが見つかった
					candidates.add(v, d);
				}
			}
			v.assign(v_val);  // 値を復元
		}
	}

	private Variable[] listWorstVariables(List<Constraint> worstConstraints) {
		Set<Variable> wvs = new HashSet<>(pro_.variableSize());
		for(int i = 0; i < worstConstraints.size(); ++i) {
			Constraint c = worstConstraints.get(i);
			for(int j = 0; j < c.size(); ++j) wvs.add(c.at(j));
		}
		return wvs.toArray(new Variable[0]);
	}

	@Override
	protected boolean exec() {
		long endTime = (timeLimit_ == -1) ? Long.MAX_VALUE : (System.currentTimeMillis() + timeLimit_);
		int iterCount = 0;

		double deg = pro_.worstSatisfactionDegree();
		List<Constraint> vc = new ArrayList<>();
		AssignmentList candidates = new AssignmentList();

		AssignmentList sol = new AssignmentList();
		
		while(true) {
			double wsd = pro_.constraintsWithWorstSatisfactionDegree(vc);
			if(targetDeg_ != UNSPECIFIED && targetDeg_ <= wsd) return true;  // 違反度が指定より改善されたら成功
			if(iterLimit_ < iterCount++) break;  // 規定回数繰り返したら失敗
			if(endTime < System.currentTimeMillis()) break;  // 制限時間を超えたら失敗

			if(debug) debugStream.println("Worst satisfaction degree: " + wsd);
			
			if(lastSolutionDeg_ < wsd) {
				sol.set(pro_);
				lastSolutionDeg_ = wsd;
				if(foundSolution(sol, lastSolutionDeg_)) {  // フック呼び出し
					return true;
				}
			}

			findCandidates(listWorstVariables(vc), candidates);
			if(candidates.size() > 0) {
				Assignment e = isRandomized_ ? candidates.arbitraryAssignment() : candidates.get(0);
				e.apply();
				candidates.clear();
				if(debug) debugStream.println("\t" + e);
			} else {
				for(int i = 0; i < vc.size(); ++i) weights_[vc.get(i).index()]++;
				if(debug) debugStream.println("Breakout!");
			}
		}
		if(targetDeg_ == UNSPECIFIED && deg < pro_.worstSatisfactionDegree()) return true;
		return false;
	}

	protected boolean foundSolution(final AssignmentList solution, final double worstDegree) {return false;}

	/**
	 * アルゴリズムのランダム性を設定します．
	 * ランダム性を有効にすると局所解に陥る危険性が減りますが，解に再現性がなくなります．
	 * @param flag trueならランダム性が有効
	 */
	@Override
	public void setRandomized(boolean flag) {
		isRandomized_ = flag;
	}

}

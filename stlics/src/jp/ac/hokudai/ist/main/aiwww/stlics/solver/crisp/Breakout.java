package jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp;

import java.util.*;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractCrispSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.StochasticSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.Assignment;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * ブレイクアウト法によるソルバを実装したクラスです．
 * 最大CSPとして問題の解を求めます．
 * @author Takuto YANAGIDA
 * @version 2009/09/16
 */
public class Breakout extends AbstractCrispSolver implements StochasticSolver {

	private final double[] weights_;

	private int iterationLimit_ = Integer.MAX_VALUE;
	private int satisfiedSizeLimit_;
	private int timeLimit_ = -1;

	public Breakout(final CrispProblem p) {
		super(p);
		weights_ = new double[pro_.constraintSize()];
		Arrays.fill(weights_, 1);
		satisfiedSizeLimit_ = pro_.constraintSize();
	}

	private void findCandidates(final Variable[] violatingVariables, final AssignmentList candidates) {
		double maxDiff = 0.0;

		for(Variable v: violatingVariables) {
			final int v_val = v.value();  // 値を保存
			final Constraint[] v_c = v.constraints();
			final Domain v_d = v.domain();

			double nowVio = 0.0;
			for(Constraint c: v_c) {
				nowVio += (1 - c.isSatisfied()) * weights_[c.index()];
			}
			out: for(int i = 0; i < v_d.size(); ++i) {
				final int d = v_d.at(i);
				if(v_val == d) continue;
				v.assign(d);
				double diff = nowVio;
				for(Constraint c: v_c) {
					diff -= (1 - c.isSatisfied()) * weights_[c.index()];
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
	
	private Variable[] listViolatingVariables(final List<Constraint> violatingConstraints) {
		final Set<Variable> vvs = new HashSet<>(pro_.variableSize());
		for(int i = 0; i < violatingConstraints.size(); ++i) {
			final Constraint c = violatingConstraints.get(i);
			for(int j = 0; j < c.size(); ++j) vvs.add(c.at(j));
		}
		return vvs.toArray(new Variable[vvs.size()]);
	}

	@Override
	protected boolean exec() {
		final long time = System.currentTimeMillis() + timeLimit_;
		int iterationCount = 0;

		for(Variable v: pro_.variables()) {
			if(v.isEmpty()) v.assign(v.domain().at(0));
		}
		
		final List<Constraint> vc = new ArrayList<>();
		final AssignmentList candidates = new AssignmentList();
		while(true) {
			pro_.violatingConstraints(vc);
			if(satisfiedSizeLimit_ <= pro_.constraintSize() - vc.size()) return true;  // 違反率が指定より改善されたら成功
			if(iterationLimit_ < iterationCount++) return false;  // 規定回数繰り返したら失敗
			if(timeLimit_ != -1 && time < System.currentTimeMillis()) return false;  // 制限時間を超えたら失敗

			if(debug) debugStream.println(vc.size() + " violations");
			findCandidates(listViolatingVariables(vc), candidates);
			if(candidates.size() > 0) {
				final Assignment e = candidates.arbitraryAssignment();
				e.apply();
				candidates.clear();
				if(debug) debugStream.println("\t" + e);
			} else {
				for(int i = 0; i < vc.size(); ++i) weights_[vc.get(i).index()]++;
				if(debug) debugStream.println("breakout");
			}
		}
	}

	@Override
    public String name() {
        return "Breakout";
    }

	@Override
    public void setIterationLimit(final int count) {
		iterationLimit_ = (count == -1) ? Integer.MAX_VALUE : count;
	}

	@Override
    public void setTargetRate(final double rate) {
		satisfiedSizeLimit_ = (rate == -1) ? pro_.constraintSize() : (int)(pro_.constraintSize() * rate);
    }

	@Override
    public void setTimeLimit(final int msec) {
		timeLimit_ = msec;
    }

}

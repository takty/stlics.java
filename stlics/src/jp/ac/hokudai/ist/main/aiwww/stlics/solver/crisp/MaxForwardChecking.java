package jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractCrispSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.DomainPruner;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.SystematicSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.Assignment;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * 前方チェック法を実装したクラスです．
 * 最大CSPとして問題の解を求めます．
 * 枝刈としてドメインの要素をhideするので，変数ごとに独自のドメインを持つ必要があります．
 * @author Takuto Yanagida
 * @version 2010/10/17
 */
public class MaxForwardChecking extends AbstractCrispSolver implements SystematicSolver {

	private final Variable[] variables_;
	private final AssignmentList assignmentList_ = new AssignmentList();
	private int maxViolatedCount_;
	private int violatedCount_;
	private final Set<Constraint> checkedConstraints_ = new HashSet<>();
	private List<Constraint> constraints_ = new ArrayList<>();  // 再利用用

	private int iterationLimit_ = Integer.MAX_VALUE;
	private int satisfiedSizeLimit_;
	private int timeLimit_ = -1;
	private int iterationCount_;
	private long time_;

	public MaxForwardChecking(final CrispProblem p) {
		super(p);
		maxViolatedCount_ = pro_.constraints().size();
		variables_ = pro_.variables().toArray(new Variable[0]);
		for(Variable v: variables_) {
			v.solverObject = new DomainPruner(v.domain().size());
		}
		satisfiedSizeLimit_ = pro_.constraintSize();
	}

	private boolean branch(final int level, final int violatedCount) {
		if(iterationLimit_ < iterationCount_++) return false;  // 規定回数繰り返したら失敗
		if(timeLimit_ != -1 && time_ < System.currentTimeMillis()) return false;  // 制限時間を超えたら失敗

		if(level == pro_.variableSize()) {
			final int vcs = pro_.violatingConstraintSize();
			if(vcs < maxViolatedCount_) {
				maxViolatedCount_ = vcs;
				assignmentList_.clear();
				assignmentList_.add(pro_);
				if(debug) debugStream.println("    refreshed " + maxViolatedCount_);
				if(satisfiedSizeLimit_ <= pro_.constraintSize() - maxViolatedCount_) return true;  // 違反率が指定より改善されたら成功
			}
			return false;
		}
		final Variable vc = variables_[level];
		final Domain dom = vc.domain();
		final DomainPruner dc = (DomainPruner)vc.solverObject;
		for(int i = 0; i < dom.size(); ++i) {
			if(dc.isValueHidden(i)) continue;
			vc.assign(dom.at(i));
			violatedCount_ = violatedCount + getAdditionalViolationCount(level, vc);  // for max begin
			if(violatedCount_ > maxViolatedCount_) continue;  // for max end
			if(checkForward(level) && branch(level + 1, violatedCount_)) return true;
			for(Variable v: variables_) {
				((DomainPruner)v.solverObject).reveal(level);
			}
		}
		vc.clear();
		return false;
	}

	// 現在変数の割り当てから未来変数に割り当て可能かを調べる．
	private boolean checkForward(final int level) {
		final Variable vc = variables_[level];
		for(int i = level + 1; i < variables_.length; ++i) {
			final Variable future = variables_[i];
			pro_.constraintsBetween(vc, future, constraints_);
			for(int j = 0; j < constraints_.size(); ++j) {
				final Constraint c = constraints_.get(j);
				if(c.emptyVariableSize() != 1) continue;
				if(revise(future, c, level)) {
					if(((DomainPruner)future.solverObject).isEmpty()) return false;  // 未来変数の1つのドメインが空になると，失敗．
				}
			}
		}
		return true;
	}

	// 現在変数vcに値を設定したことによって増加した制約違反数を求める．
	private int getAdditionalViolationCount(final int level, final Variable vc) {
		int avc = 0;
		checkedConstraints_.clear();  // 再利用
		for(int i = 0; i < level; ++i) {
			pro_.constraintsBetween(vc, variables_[i], constraints_);
			for(int j = 0; j < constraints_.size(); ++j) {
				final Constraint c = constraints_.get(j);
				if(checkedConstraints_.contains(c)) continue;  // 多項制約では重複の恐れがあるため
				if(c.isSatisfied() == 0) ++avc;  // 充足でも未定義でもない
				checkedConstraints_.add(c);
			}
		}
		return avc;
	}

	// v1のドメインからv2と対応しない値を除去する．すなわち，v1をv2に整合させる．
	private boolean revise(final Variable v1, final Constraint c, final int level) {
		boolean deleted = false;
		final Domain dom = v1.domain();
		final DomainPruner dc = (DomainPruner)v1.solverObject;
		for(int i = 0; i < dom.size(); ++i) {
			if(dc.isValueHidden(i)) continue;
			v1.assign(dom.at(i));
			final int s = c.isSatisfied();
			if(s == 0 && violatedCount_ + 1 > maxViolatedCount_) {
				dc.hide(i, level);
				deleted = true;
			}
		}
		return deleted;
	}

	@Override
    protected boolean exec() {
    	time_ = System.currentTimeMillis() + timeLimit_;
    	iterationCount_ = 0;
    	
    	pro_.clearAllVariables();
    	final boolean r = branch(0, 0);
    	for(Assignment a: assignmentList_) {
    		a.apply();
		((DomainPruner)a.variable().solverObject).revealAll();
    	}
    	return r;
    }

	@Override
	public String name() {
		return "Forward checking for Max CSPs";
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

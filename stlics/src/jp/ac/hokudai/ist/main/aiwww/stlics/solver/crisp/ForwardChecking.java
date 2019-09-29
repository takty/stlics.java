package jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp;

import java.util.ArrayList;
import java.util.List;
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
 * オプションの指定により，最小ドメイン優先ヒューリスティック(MRV)を使用することもできます．
 * 全制約を満たす変数割り当てを探索し，見つからないときは失敗します．
 * 枝刈としてドメインの要素をhideするので，変数ごとに独自のドメインを持つ必要があります．
 * 多項制約を持つ問題に対しても，前方チェックを行います．
 * @author Takuto Yanagida
 * @version 2010/10/17
 */
public class ForwardChecking extends AbstractCrispSolver implements SystematicSolver {

	private final Variable[] variables_;
	private final AssignmentList assignmentList_ = new AssignmentList();
	private Constraint[][][] relatedConstraints_;  // 2変数間の制約をキャッシュするテーブル
	private boolean useMRV_ = false;

	private int iterationLimit_ = Integer.MAX_VALUE;
	private int timeLimit_ = -1;
	private int iterationCount_;
	private long time_;

	public ForwardChecking(final CrispProblem p) {
		super(p);
		variables_ = pro_.variables().toArray(new Variable[pro_.variableSize()]);
		for(Variable v: variables_) {
			v.solverObject = new DomainPruner(v.domain().size());
		}
		initializeRelatedConstraintTable();
	}

	// 2変数間の制約をキャッシュするテーブルを初期化する．
	private void initializeRelatedConstraintTable() {
		final List<Constraint> temp = new ArrayList<>();
		relatedConstraints_ = new Constraint[variables_.length][variables_.length][];
		for(int j = 0; j < variables_.length; ++j) {
			relatedConstraints_[j] = new Constraint[variables_.length][];
			for(int i = 0; i < variables_.length; i++) {
				if(i < j) {
					pro_.constraintsBetween(variables_[i], variables_[j], temp);
					relatedConstraints_[j][i] = temp.toArray(new Constraint[temp.size()]);
				}
			}
		}
	}

	// 2変数間の制約をキャッシュするテーブルをから制約の配列を取得する．
	private Constraint[] getConstraintsBetween(final int i, final int j) {
		if(i < j) return relatedConstraints_[j][i];
		return relatedConstraints_[i][j];
	}

	// 現在変数の割り当てから未来変数に割り当て可能かを調べる．
	private boolean checkForward(final int currentLevel, final int currentIndex) {
		for(Variable v_i: variables_) {
			if(!v_i.isEmpty()) continue;  // 過去変数か現在変数だったら
			final Domain d_i = v_i.domain();
			final DomainPruner dc_i = (DomainPruner)v_i.solverObject;
			final Constraint[] cs = getConstraintsBetween(currentIndex, v_i.index());
			for(Constraint c: cs) {
				if(c.emptyVariableSize() != 1) continue;
				for(int k = 0, n = d_i.size(); k < n; ++k) {
					if(dc_i.isValueHidden(k)) continue;
					v_i.assign(d_i.at(k));
					if(c.isSatisfied() == 0) {  // 違反(未定義でもない)のときはhide
						dc_i.hide(k, currentLevel);
					}
				}
				v_i.clear();
				if(dc_i.isEmpty()) return false;  // 未来変数の1つのドメインが空になると，失敗．
			}
		}
		return true;
	}

	// もっともドメインの小さな変数のインデックスを返す．
	private int indexOfVariableWithMRV() {
		int index = 0;
		int size = Integer.MAX_VALUE;
		for(int i = 0; i < variables_.length; ++i) {
			final Variable v = variables_[i];
			if(!v.isEmpty()) continue;
			final Domain d = v.domain();
			final DomainPruner dc = (DomainPruner)v.solverObject;
			final int s = d.size() - dc.hiddenSize();
			if(s < size) {
				size = s;
				index = i;
			}
		}
		return index;
	}

	// 探索を1変数ずつ行う．
	private boolean branch(final int currentLevel) {
		if(iterationLimit_ < iterationCount_++) return false;  // 規定回数繰り返したら失敗
		if(timeLimit_ != -1 && time_ < System.currentTimeMillis()) return false;  // 制限時間を超えたら失敗

		if(currentLevel == pro_.variableSize()) {
			assignmentList_.set(pro_);
			return true;
		}
		final int vc_index = useMRV_ ? indexOfVariableWithMRV() : currentLevel;
		final Variable vc = variables_[vc_index];
		final Domain d = vc.domain();
		final DomainPruner dc = (DomainPruner)vc.solverObject;
		for(int i = 0, n = d.size(); i < n; ++i) {
			if(dc.isValueHidden(i)) continue;
			vc.assign(d.at(i));
			if(checkForward(currentLevel, vc_index) && branch(currentLevel + 1)) return true;
			for(Variable v: variables_) {
				((DomainPruner)v.solverObject).reveal(currentLevel);
			}
		}
		vc.clear();
		return false;
	}

	// 探索を行う．
	@Override
	protected boolean exec() {
		time_ = System.currentTimeMillis() + timeLimit_;
		iterationCount_ = 0;

		pro_.clearAllVariables();
		final boolean r = branch(0);
		for(Assignment a: assignmentList_) {
			a.apply();
			((DomainPruner)a.variable().solverObject).revealAll();
		}
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name() {
		return "Forward checking";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIterationLimit(final int count) {
		iterationLimit_ = (count == -1) ? Integer.MAX_VALUE : count;
	}

	/**
	 * このメソッドによる設定は無効です．
	 */
	@Override
	public void setTargetRate(final double rate) {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeLimit(final int msec) {
		timeLimit_ = msec;
	}

	/**
	 * 最小ドメイン優先ヒューリスティック(MRV)を使用するかどうかを指定します．
	 * 問題によってはMRVの使用が処理時間を長くすることがあります．
	 * デフォルトではfalseです．
	 * @param flag MRVを使うかどうか．trueなら使う．falseなら使わない．
	 */
	public void setUsingMinimumRemainingValuesHeuristics(final boolean flag) {
		useMRV_ = flag;
	}

}

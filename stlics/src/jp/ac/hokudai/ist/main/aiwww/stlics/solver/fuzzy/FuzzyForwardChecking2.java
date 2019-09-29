package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import java.util.ArrayList;
import java.util.List;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractFuzzySolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.DomainPruner;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.SystematicSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * 2項制約のみを含むファジィCSP用の前方チェック法を実装したクラスです．
 * オプションの指定により，最小ドメイン優先ヒューリスティック(MRV)を使用することもできます．
 * @author Takuto Yanagida
 * @version 2012/07/31
 */
public class FuzzyForwardChecking2 extends AbstractFuzzySolver implements SystematicSolver {

	private static enum BranchCode {CONTINUE, TERMINATE}

	private final AssignmentList solution_ = new AssignmentList();
	private final Variable[] vars_;
	private Constraint[][][] relCons_;  // 2変数間の制約をキャッシュするテーブル
	private double solutionWorstDeg_ = 0.0;  // 既出解の充足度（これ未満の解を見つける必要なし）

	private int iterCount_;
	private long endTime_;
	private boolean useMRV_ = false;
	private double degInc_ = 0.0;

	/**
	 * ファジィ制約充足問題を指定してソルバを生成します．
	 * @param p ファジィ制約充足問題
	 */
	public FuzzyForwardChecking2(Problem p) {
		super(p, "Forward checking for binary fuzzy CSPs");
		vars_ = pro_.variables().toArray(new Variable[pro_.variableSize()]);
		initializeRelatedConstraintTable();
	}

	/**
	 * ファジィ制約充足問題と最悪充足度を指定してソルバを生成します．
	 * 最悪充足度に従って枝刈りを開始します．
	 * もし，最悪の充足度が予想できる場合は枝刈りの効率が上がり，解を速く得られるようになりますが，
	 * この値以下の解しか存在しないとき，アルゴリズムは解を見つけることなく停止します．
	 * @param p ファジィ制約充足問題
	 * @param worstSatisfactionDegree 最悪充足度
	 */
	public FuzzyForwardChecking2(Problem p, double worstSatisfactionDegree) {
		this(p);
		solutionWorstDeg_ = worstSatisfactionDegree;
	}

	// 2変数間の制約をキャッシュするテーブルを初期化する．
	private void initializeRelatedConstraintTable() {
		List<Constraint> temp = new ArrayList<>();
		relCons_ = new Constraint[vars_.length][vars_.length][];
		for(int j = 0; j < vars_.length; ++j) {
			relCons_[j] = new Constraint[vars_.length][];
			for(int i = 0; i < vars_.length; i++) {
				if(i < j) {
					pro_.constraintsBetween(vars_[i], vars_[j], temp);
					relCons_[j][i] = temp.toArray(new Constraint[0]);
				}
			}
		}
	}

	// 2変数間の制約をキャッシュするテーブルをから制約の配列を取得する．
	private Constraint[] getConstraintsBetween(int vi_index, int vj_index) {
		if(vi_index < vj_index) return relCons_[vj_index][vi_index];
		return relCons_[vi_index][vj_index];
	}

	// 現在変数と一つの未来変数との整合を調べ，整合しないドメインの要素を枝刈りする(制約のスコープの未割り当て変数が一つの場合)．
	private boolean checkForwardConsistency(int currentLevel, Variable vi, Constraint c) {
		Domain di = vi.domain();
		DomainPruner dci = (DomainPruner)vi.solverObject;
		for(int i = 0, n = di.size(); i < n; ++i) {
			if(dci.isValueHidden(i)) continue;
			vi.assign(di.at(i));
			if(c.satisfactionDegree() <= solutionWorstDeg_) {  // 「小なりイコール」のときは解とならない※
				dci.hide(i, currentLevel);  // ここで枝刈り!!
			}
		}
		vi.clear();
		return !dci.isEmpty();  // 未来変数viのドメインdiが空にならなければ成功．
	}

	// 現在変数の割り当てから未来変数に割り当て可能かを調べる．
	private boolean checkForward(final int currentLevel, final int currentIndex) {
		for(Variable v_i: vars_) {
			if(!v_i.isEmpty()) continue;  // 過去変数か現在変数だったら
			Constraint[] cs = getConstraintsBetween(currentIndex, v_i.index());
			for(Constraint c: cs) {
				if(c.size() == 2) {  // 2項制約だったら
					if(!checkForwardConsistency(currentLevel, v_i, c)) return false;
				}
			}
		}
		return true;
	}

	// もっともドメインの小さな変数のインデックスを返す．
	private int indexOfVariableWithMRV() {
		int index = 0;
		int size = Integer.MAX_VALUE;
		for(int i = 0; i < vars_.length; ++i) {
			final Variable v = vars_[i];
			if(!v.isEmpty()) continue;
			final Domain d = v.domain();
			final int s = d.size() - ((DomainPruner)v.solverObject).hiddenSize();
			if(s < size) {
				size = s;
				index = i;
			}
		}
		return index;
	}

	// 探索を1変数ずつ行う．
	private BranchCode branch(int currentLevel) {
//		System.out.println("currentLevel " + currentLevel);
		
		BranchCode bc = BranchCode.CONTINUE;
		final int vc_index = useMRV_ ? indexOfVariableWithMRV() : currentLevel;
		final Variable vc = vars_[vc_index];
		final Domain d = vc.domain();
		final DomainPruner dc = (DomainPruner)vc.solverObject;

		for(int i = 0, n = d.size(); i < n; ++i) {
			if(dc.isValueHidden(i)) continue;
			if(iterLimit_ < iterCount_++ || endTime_ < System.currentTimeMillis()) {
				bc = BranchCode.TERMINATE;  // 制限により探索終了
				break;
			}
			vc.assign(d.at(i));

			for(Variable v: vars_) ((DomainPruner)v.solverObject).reveal(currentLevel);
			if(!checkForward(currentLevel, vc_index)) continue;

			int nextLevel = currentLevel + 1;
			bc = (nextLevel == vars_.length - 1) ? branchLast(nextLevel) : branch(nextLevel);
			if(bc == BranchCode.TERMINATE) break;
		}
		if(bc == BranchCode.CONTINUE) {  // 親に戻って探索するときは，ここでの枝刈りを元に戻す．
			for(Variable v: vars_) ((DomainPruner)v.solverObject).reveal(currentLevel);
		}
		vc.clear();
		return bc;
	}

	// 探索を最後の変数で行う．
	private BranchCode branchLast(int currentLevel) {
//		System.out.println("currentLevel " + currentLevel);

		BranchCode bc = BranchCode.CONTINUE;
		final Variable vc = vars_[useMRV_ ? indexOfVariableWithMRV() : currentLevel];
		final Domain d = vc.domain();
		final DomainPruner dc = (DomainPruner)vc.solverObject;

		for(int i = 0, n = d.size(); i < n; ++i) {
			if(dc.isValueHidden(i)) continue;
			if(iterLimit_ < iterCount_++ || endTime_ < System.currentTimeMillis()) {
				bc = BranchCode.TERMINATE;  // 制限により探索終了
				break;
			}
			vc.assign(d.at(i));

			double deg = pro_.worstSatisfactionDegree();
			if(deg > solutionWorstDeg_) {  // 「大なり」のときは新たな解とする
				solutionWorstDeg_ = deg;
				solution_.set(pro_);
				bc = BranchCode.TERMINATE;
				if(targetDeg_ != UNSPECIFIED && targetDeg_ <= solutionWorstDeg_) break;  // 目標到達により探索終了
			}
		}
		vc.clear();
		return bc;
	}

	// 探索を行う．
	@Override
	protected boolean exec() {
		final long stime = System.currentTimeMillis();
		endTime_ = (timeLimit_ == -1) ? Long.MAX_VALUE : (stime + timeLimit_);
		iterCount_ = 0;

		for(Variable v: vars_) v.solverObject = new DomainPruner(v.domain().size());  // 枝刈り器の生成
		pro_.clearAllVariables();

		AssignmentList sol = new AssignmentList();

		boolean success = false;
		long ctime;
		while(!(iterLimit_ < iterCount_++ || endTime_ < (ctime = System.currentTimeMillis()))) {
			BranchCode bc = branch(0);
//			System.out.println("iterCount_ " + iterCount_);
//			if(solution_.isEmpty() || solution_.equals(sol) || bc == BranchCode.CONTINUE) break;  // 2つ目の条件を追加 2012/07/31
			if(solution_.isEmpty()) break;

			sol.set(solution_);
			solution_.clear();  // 次の探索で解が見つからなかった時，それを分かるようにするため，クリアしておく．
			
			if(debug) debugStream.println(String.format("\tFound a solution: %1.4f, time: %d", solutionWorstDeg_, ctime - stime));
			if(foundSolution(sol, solutionWorstDeg_)) {  // フック呼び出し
				success = true;
				break;
			}
			if(targetDeg_ == UNSPECIFIED) {  // 充足度指定なし
				success = true;
				if (solutionWorstDeg_ + degInc_ > 1.0) break;  // 追加 2013/09/06
				solutionWorstDeg_ += ((solutionWorstDeg_ + degInc_ > 1.0) ? 0.0 : degInc_);  // 制限内で次の解を探す
			} else if(targetDeg_ <= solutionWorstDeg_) {  // 充足度指定あり，かつそれを上回った
				success = true;
				break;
			}
			for(Variable v: vars_) ((DomainPruner)v.solverObject).revealAll();
		}
		if(success) sol.apply();
		for(Variable v: vars_) v.solverObject = null;  // 枝刈り器の削除
		return success;
	}

	protected boolean foundSolution(final AssignmentList solution, final double worstDegree) {return false;}
	
	/**
	 * ソルバの停止の条件となる達成目標として，制約充足度を設定します．
	 * 指定した充足度以上になると，ソルバは成功として停止します．
	 * デフォルト(未設定)はUNSPECIFIEDです．
	 * @param rate 割合．UNSPECIFIEDは未設定を表す．
	 */
	@Override
	public void setTargetRate(double rate) {
		targetDeg_ = rate;
		if(targetDeg_ == UNSPECIFIED) {
			solutionWorstDeg_ = 0.0;
		} else {
			// targetDegree_よりもわずかに小さいworstSatisfactionDegree_を求める
			double e = Double.MIN_NORMAL;
			solutionWorstDeg_ = targetDeg_ - e;
			while(solutionWorstDeg_ >= targetDeg_) {
				e *= 10;
				solutionWorstDeg_ = targetDeg_ - e;
			}
		}
	}

	/**
	 * 最小ドメイン優先ヒューリスティック(MRV)を使用するかどうかを指定します．
	 * 問題によってはMRVの使用が処理時間を長くすることがあります．
	 * デフォルトではfalseです．
	 * @param flag MRVを使うかどうか．trueなら使う．falseなら使わない．
	 */
	public void setUsingMinimumRemainingValuesHeuristics(boolean flag) {
		useMRV_ = flag;
	}

	/**
	 * 解が見つかり，探索を継続する場合，最悪制約充足度をどれだけ増加させるかを指定します．
	 * @param degree 増加させる制約充足度
	 */
	public void setIncrementStepOfWorstSatisfactionDegree(double degree) {
		degInc_ = degree;
	}

}

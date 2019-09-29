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
 * SRS 3を実装したクラスです．
 * @author Takuto Yanagida and Yasuhiro Sudo
 * @version 2012/07/31
 */
public class SRS3 extends AbstractFuzzySolver implements StochasticSolver, Randomizable {

	static private class ConstraintNode {

		private final List<ConstraintNode> children_ = new ArrayList<>();
		private final Constraint obj_;
		private ConstraintNode parent_;

		public ConstraintNode(Constraint obj) {
			obj_ = obj;
		}

		public void add(ConstraintNode cn) {
			cn.parent_ = this;
			children_.add(cn);
		}

		public void clear() {
			for(int i = 0; i < children_.size(); ++i) children_.get(i).parent_ = null;
			children_.clear();
		}

		public Constraint getConstraint() {
			return obj_;
		}

		/**
		 * このノードとその子孫ノードを指定されたリストに追加します．
		 * @param cns 制約ノードのリスト
		 */
		public void getDescendants(List<ConstraintNode> cns) {  
			cns.add(this);
			for(int i = 0; i < children_.size(); ++i) children_.get(i).getDescendants(cns);
		}

		public ConstraintNode parent() {
			return parent_;
		}

		public void setParent(ConstraintNode parent) {
			parent_ = parent;
		}

	}

	// リペア時に割り当て候補を採用する際の許容幅(SRS 3に厳密に従うなら，0にすること)
	static final private double REPAIR_THRESHOLD = 0;  // Double.MIN_VALUE * 10;

	private final Set<ConstraintNode> closedList_ = new HashSet<>();
	private final Set<ConstraintNode> openList_ = new LinkedHashSet<>();
	private final ConstraintNode[] nodes_;
	private final Constraint[][] neighborConstraints_;  // キャッシュ
	private final List<ConstraintNode> c_stars_ = new ArrayList<>();

	private int iterCount_;
	private long endTime_;
	private boolean isRandomized_ = true;

	public SRS3(Problem p) {
		super(p, "SRS 3");
		List<Constraint> cs = pro_.constraints();
		nodes_ = new ConstraintNode[pro_.constraintSize()];
		for(int i = 0; i < cs.size(); ++i) nodes_[i] = new ConstraintNode(cs.get(i));
		neighborConstraints_ = new Constraint[pro_.constraintSize()][];
	}

	private Constraint[] getNeighborConstraints(Constraint c) {
		int i = c.index();
		if(neighborConstraints_[i] == null) {
			neighborConstraints_[i] = c.neighbors(new HashSet<Constraint>()).toArray(new Constraint[0]);
		}
		return neighborConstraints_[i];
	}

	private boolean repair(Constraint c0) {
		if(debug) debugStream.print("Repair");
		double minDeg0 = c0.satisfactionDegree();  // ターゲットc0は確実にこれよりも改善すること
		double maxDeg0 = c0.satisfactionDegree();  // ターゲットc0をこれまでで最も改善できた場合の充足度
		double min = pro_.worstSatisfactionDegree();  // 近傍制約の下限
		AssignmentList candidates = new AssignmentList();

		// 条件を満たす候補が，過去の候補よりも強ければ入れ替え，最後まで候補が見つからなければ失敗
		for(int i = 0; i < c0.size(); ++i) {
			Variable v = c0.at(i);
			final int v_val = v.value();  // 値を保存
			Constraint[] v_c = v.constraints();
			Domain v_d = v.domain();

			out: for(int j = 0; j < v_d.size(); ++j) {
				int d = v_d.at(j);
				if(v_val == d) continue;
				v.assign(d);
				double deg0 = c0.satisfactionDegree();
				if(minDeg0 > deg0 || maxDeg0 - deg0 > REPAIR_THRESHOLD) continue;  // ターゲットc0を改善できないなら，その割り当ては不採用
				for(Constraint c: v_c) {
					if(c == c0) continue;
					double deg = c.satisfactionDegree();
					if(deg != Constraint.UNDEFINED && deg < min) continue out;  // 近傍制約cが一つでも最悪以下になるなら，その割り当ては不採用
				}
				if(deg0 > maxDeg0) {
					maxDeg0 = deg0;
					candidates.clear();
				}
				candidates.add(v, d);
			}
			v.assign(v_val);  // 値を復元
		}
		if(candidates.size() > 0) {
			Assignment e = isRandomized_ ? candidates.arbitraryAssignment() : candidates.get(0);
			e.apply();
			if(debug) debugStream.println("\t" + e);
			return true;
		}
		if(debug) debugStream.println();
		return false;
	}

	private void shrink(ConstraintNode node) {
		if(debug) debugStream.println("Shrink");
		boolean removeCstar = false;
		while(true) {
			node = node.parent();
			if(c_stars_.remove(node)) {
				removeCstar = true;
				break;
			}
			if(!repair(node.parent().getConstraint())) break;
		}
		List<ConstraintNode> temp = new ArrayList<>();
		node.getDescendants(temp);  // tempはnodeを含む
		for(ConstraintNode cn: temp) cn.clear();  // 再利用に備える
		openList_.removeAll(temp);
		closedList_.removeAll(temp);
		if(!removeCstar) openList_.add(node);
	}

	private void spread(ConstraintNode node) {
		if(debug) debugStream.println("Spread");
		closedList_.add(node);
		for(Constraint c: getNeighborConstraints(node.getConstraint())) {
			ConstraintNode cn = nodes_[c.index()];
			if(!closedList_.contains(cn) && !openList_.contains(cn)) {  // OpenにもClosed含まれていない制約の場合
				node.add(cn);
				openList_.add(cn);
			}
		}
	}

	private void srs() {
		if(debug) debugStream.println("SRS");
		List<Constraint> wsdcs = new ArrayList<>();
		pro_.constraintsWithWorstSatisfactionDegree(wsdcs);
		for(Constraint c: wsdcs) {
			ConstraintNode cn = nodes_[c.index()];
			cn.setParent(null);
			c_stars_.add(cn);
		}
		closedList_.clear();
		openList_.clear();
		openList_.addAll(c_stars_);

		while(!c_stars_.isEmpty() && !openList_.isEmpty()) {
			if(iterLimit_ < iterCount_++ || endTime_ < System.currentTimeMillis()) return;
			ConstraintNode node = openList_.iterator().next();
			openList_.remove(node);
			if(repair(node.getConstraint())) {
				if(c_stars_.remove(node)) continue;  // リペアしたノードがC*に含まれる場合(削除する)
				if(repair(node.parent().getConstraint())) {
					shrink(node);  // 自分の改善が親の改善につながる場合
					continue;
				}
			}
			spread(node);
		}
	}

	@Override
	protected boolean exec() {
		endTime_ = (timeLimit_ == -1) ? Long.MAX_VALUE : (System.currentTimeMillis() + timeLimit_);
		iterCount_ = 0;
		if(targetDeg_ != UNSPECIFIED && targetDeg_ <= pro_.worstSatisfactionDegree()) return true;

		AssignmentList sol = new AssignmentList();

		boolean success = false;
		while(!(iterLimit_ < iterCount_++ || endTime_ < System.currentTimeMillis())) {
			srs();
			if(!c_stars_.isEmpty()) break;
			double solutionWorstDeg = pro_.worstSatisfactionDegree();
			if(debug) debugStream.println("\tFound a solution: " + solutionWorstDeg + "\t" + targetDeg_);
			sol.set(pro_);
			if(foundSolution(sol, solutionWorstDeg)) {  // フック呼び出し
				success = true;
				break;
			}
			if(targetDeg_ == UNSPECIFIED) {  // 充足度指定なし
				success = true;
			} else if(targetDeg_ <= solutionWorstDeg) {  // 充足度指定あり，かつそれを上回った
				success = true;
				break;
			}
		}
		return success;
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

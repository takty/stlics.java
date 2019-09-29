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
 * SRSアルゴリズムをクリスプなCSP用に実装したクラスです．
 * 与えられたクリスプなCSPを最大CSPとして処理します．
 * リペア・アルゴリズムはSRS 3と同様に，周囲の充足数を下げずに，自身も充足するような割り当てを探索します．
 * @author Takuto Yanagida and Yasuhiro Sudo
 * @version 2010/11/17
 */
public class CrispSRS3 extends AbstractCrispSolver implements StochasticSolver {

	static private class TreeNode {

		private final List<TreeNode> children_ = new ArrayList<>();
		private final Constraint obj_;
		private TreeNode parent_;

		public TreeNode(final Constraint obj) {
			obj_ = obj;
		}

		public void add(final TreeNode node) {
			node.parent_ = this;
			children_.add(node);
		}

		public void clear() {
			for(int i = 0; i < children_.size(); ++i) children_.get(i).parent_ = null;
			children_.clear();
		}

		public void getDescendants(final List<TreeNode> tns) {  
			tns.add(this);
			for(int i = 0; i < children_.size(); ++i) children_.get(i).getDescendants(tns);
		}

		public Constraint getObject() {
			return obj_;
		}

		public TreeNode parent() {
			return parent_;
		}

	}

	private final Set<TreeNode> closedList_ = new HashSet<>();
	private final Set<TreeNode> openList_ = new LinkedHashSet<>();
	private final TreeNode[] nodes_;
	private final Constraint[][] neighborConstraints_;  // キャッシュ

	private int iterationLimit_ = Integer.MAX_VALUE;
	private int satisfiedSizeLimit_ = -1;
	private int timeLimit_ = -1;

	public CrispSRS3(final CrispProblem p) {
		super(p);
		nodes_ = new TreeNode[pro_.constraintSize()];
		neighborConstraints_ = new Constraint[pro_.constraintSize()][];
		final List<Constraint> cs = pro_.constraints();
		for(int i = 0; i < cs.size(); ++i) {
			nodes_[i] = new TreeNode(cs.get(i));
		}
	}

	private boolean srs(final List<TreeNode> c_stars) {
		final long time = System.currentTimeMillis() + timeLimit_;
		int iterationCount = 0;

		closedList_.clear();
		openList_.clear();
		openList_.addAll(c_stars);
		while(!c_stars.isEmpty() && !openList_.isEmpty()) {
			if(satisfiedSizeLimit_ != -1 && satisfiedSizeLimit_ <= pro_.satisfiedConstraintSize()) return true;  // 違反率が指定より改善されたら成功
			if(iterationLimit_ < iterationCount++) return false;  // 規定回数繰り返したら失敗
			if(timeLimit_ != -1 && time < System.currentTimeMillis()) return false;  // 制限時間を超えたら失敗
			
			final TreeNode node = openList_.iterator().next();
			openList_.remove(node);
			if(repair(node.getObject())) {
				if(!c_stars.remove(node)) {  // リペアしたノードがC*に含まれる場合(削除する)
					if(node.parent() != null && repair(node.parent().getObject())) {  // 自分の改善が親の改善につながる場合
						shrink(node, c_stars);
					} else {
						spread(node);
					}
				}
			} else {  // リペアに失敗した場合
				spread(node);
			}
		}
		return false;
	}

	private Constraint[] getNeighborConstraints(final Constraint c) {
		final int index = c.index();
		if(neighborConstraints_[index] == null) {
			neighborConstraints_[index] = c.neighbors(new HashSet<Constraint>()).toArray(new Constraint[0]);
		}
		return neighborConstraints_[index];
	}

	private boolean repair(final Constraint c0) {
		final AssignmentList candidates = new AssignmentList();
		double maxDiff = 0.0;

		for(int j = 0; j < c0.size(); ++j) {
			final Variable v = c0.at(j);
			final int v_val = v.value();  // 値を保存
			final Constraint[] v_c = v.constraints();
			final Domain v_d = v.domain();

			double nowVio = 0.0;
			for(Constraint c: v_c) nowVio += (1 - c.isSatisfied());
			out: for(int i = 0; i < v_d.size(); ++i) {
				final int d = v_d.at(i);
				if(v_val == d) continue;
				v.assign(d);
				if(c0.isSatisfied() != 1) continue;  // c0の改善が前提
				double diff = nowVio;
				for(Constraint n: v_c) {
					diff -= (1 - n.isSatisfied());
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
		if(candidates.size() > 0) {
			final Assignment e = candidates.arbitraryAssignment();
			e.apply();
			if(debug) debugStream.println("\t" + e);
			return true;
		}
		return false;
	}

	private void shrink(final TreeNode node, final List<TreeNode> c_stars) {
		final List<TreeNode> temp = new ArrayList<>();
		TreeNode cur = node;
		while(true) {  // 本来は再帰呼び出しであるが，ループに変換
			cur = cur.parent();
			temp.clear();
			cur.getDescendants(temp);
			cur.clear();

			openList_.removeAll(temp);
			closedList_.removeAll(temp);
			if(c_stars.remove(cur)) break;
			openList_.add(cur);
			if(cur.parent() != null && !repair(cur.parent().getObject())) break;
		}
	}

	private void spread(final TreeNode node) {
		closedList_.add(node);
		for(Constraint c: getNeighborConstraints(node.getObject())) {
			final TreeNode tnc = nodes_[c.index()];
			if(!closedList_.contains(tnc) && !openList_.contains(tnc)) {  // OpenにもClosed含まれていない制約の場合
				tnc.clear();  // 再利用故，前回子持ちの可能性があるため
				node.add(tnc);
				openList_.add(tnc);
			}
		}
	}

	@Override
	protected boolean exec() {
		final List<Constraint> vcs = pro_.violatingConstraints(new ArrayList<Constraint>());
		final List<TreeNode> c_stars = new ArrayList<>();
		for(Constraint c: vcs) {
			final TreeNode tnc = nodes_[c.index()];
			c_stars.add(tnc);
		}
		if(srs(c_stars)) return true;
		return c_stars.isEmpty();
	}

	@Override
	public String name() {
		return "SRS 3 for crisp CSPs";
	}

	@Override
	public void setIterationLimit(final int count) {
		iterationLimit_ = (count == -1) ? Integer.MAX_VALUE : count;
	}

	@Override
	public void setTargetRate(final double rate) {
		satisfiedSizeLimit_ = (rate == -1) ? -1 : (int)(pro_.constraintSize() * rate);
	}

	@Override
	public void setTimeLimit(final int msec) {
		timeLimit_ = msec;
	}

}

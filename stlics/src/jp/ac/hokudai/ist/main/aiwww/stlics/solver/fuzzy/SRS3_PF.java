package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.DebugSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.filter.PostStabilize;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.FuzzySolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * SRS 3 + PFのクラスです．
 * TODO ランダム性の排除オプション
 * @author Takuto Yanagida
 * @version 2010/11/16
 */
public class SRS3_PF extends DebugSolver implements Solver, FuzzySolver {

	transient protected Problem pro_;
	private final SRS3 srs3_;

	public SRS3_PF(Problem p) {
		pro_ = p;
		srs3_ = new SRS3(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name() {
		return "SRS 3 + PF";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIterationLimit(int count) {
		srs3_.setIterationLimit(count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTargetRate(double rate) {
		srs3_.setTargetRate(rate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeLimit(int msec) {
		srs3_.setTimeLimit(msec);
	}

	/**
	 * アルゴリズムのランダム性を設定します．
	 * ランダム性を有効にすると局所解に陥る危険性が減りますが，解に再現性がなくなります．
	 * @param flag trueならランダム性が有効
	 */
	public void setRandomized(boolean flag) {
		srs3_.setRandomized(flag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean solve() {
		double deg = 0.0;
		int uvs = 0;
		if(debug) {
			deg = pro_.worstSatisfactionDegree();
			uvs = pro_.emptyVariableSize();
			debugStream.println();
			debugStream.println("---- " + name() + " started ----");
		}
		AssignmentList at = new AssignmentList(pro_);
		final boolean res = srs3_.exec();
		if(res) {
			PostStabilize.apply(pro_, at);
		}
		if(debug) {
			debugStream.println("Result: " + (res ? "success" : "failure"));
			debugStream.println("Satisfaction Degree: " + deg + " -> " + pro_.worstSatisfactionDegree());
			debugStream.println("Unassigned Size: " + uvs + " -> " + pro_.emptyVariableSize());
			debugStream.println("---- " + name() + " finished ----");
			debugStream.println();
		}
		return res;
	}

}

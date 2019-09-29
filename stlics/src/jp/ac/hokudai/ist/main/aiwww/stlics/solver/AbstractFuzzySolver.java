package jp.ac.hokudai.ist.main.aiwww.stlics.solver;

import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.FuzzySolver;

/**
 * ファジィ制約充足問題のソルバを表す抽象クラスです．
 * ソルバに共通の動作(デバッグ)表示を提供します．
 * @author Takuto Yanagida
 * @version 2010/11/16
 */
public abstract class AbstractFuzzySolver extends DebugSolver implements Solver, FuzzySolver {

	/**
	 * このソルバが対象とするファジィ制約充足問題です．
	 */
	protected Problem pro_;

	/**
	 * このソルバの名称です．
	 */
	protected String name_ = "";

	/**
	 * 反復回数の制限です．
	 */
	protected int iterLimit_ = Integer.MAX_VALUE;

	/**
	 * 時間の制限です．
	 */
	protected int timeLimit_ = -1;

	/**
	 * 目標の制約充足度です．
	 */
	protected double targetDeg_ = 0.8;

	/**
	 * ファジィ制約充足問題を指定して，ソルバを生成します．
	 * @param pro ファジィ制約充足問題
	 */
	public AbstractFuzzySolver(final Problem pro) {
		super();
		pro_ = pro;
	}

	/**
	 * ファジィ制約充足問題を指定して，ソルバを生成します．
	 * @param pro ファジィ制約充足問題
	 * @param name ソルバの名称
	 */
	public AbstractFuzzySolver(final Problem pro, final String name) {
		super();
		pro_ = pro;
		name_ = name;
	}

	/**
	 * 各種アルゴリズムを実装するためのプレースホルダです．
	 * solveメソッドはこのメソッドを呼び出し，このメソッドの戻り値を返します．
	 * @return アルゴリズムが成功したらtrue，失敗したらfalse．
	 */
	abstract protected boolean exec();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean solve() {
		double deg = 0.0;
		int uvs = 0;
		long time = 0;
		if(debug) {
			deg = pro_.worstSatisfactionDegree();
			uvs = pro_.emptyVariableSize();
			time = System.currentTimeMillis();
			debugStream.println();
			debugStream.println("---- " + name() + " started ----");
		}
		final boolean res = exec();
		if(debug) {
			debugStream.println("Result: " + (res ? "success" : "failure"));
			debugStream.println("Satisfaction Degree: " + deg + " -> " + pro_.worstSatisfactionDegree());
			debugStream.println("Unassigned Size: " + uvs + " -> " + pro_.emptyVariableSize());
			debugStream.println("Elapsed Time: " + (System.currentTimeMillis() - time));
			debugStream.println("---- " + name() + " finished ----");
			debugStream.println();
		}
		return res;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name() {
		return name_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIterationLimit(int count) {
		iterLimit_ = (count == -1) ? Integer.MAX_VALUE : count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeLimit(int msec) {
		timeLimit_ = msec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTargetRate(double rate) {
		targetDeg_ = rate;
	}

}

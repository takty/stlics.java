package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import java.util.ArrayList;
import java.util.List;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 *
 * @author Takuto Yanagida
 * @version 2012/07/03
 */
public class ObservableFuzzyForwardChecking2 extends FuzzyForwardChecking2 implements ObservableFuzzySolver {

	private List<SolverListener> listeners_ = new ArrayList<>();
	
	/**
	 * ファジィ制約充足問題を指定してソルバを生成します．
	 * @param p ファジィ制約充足問題
	 */
	public ObservableFuzzyForwardChecking2(Problem p) {
		super(p);
	}

	/**
	 * ファジィ制約充足問題と最悪充足度を指定してソルバを生成します．
	 * 最悪充足度に従って枝刈りを開始します．
	 * もし，最悪の充足度が予想できる場合は枝刈りの効率が上がり，解を速く得られるようになりますが，
	 * この値以下の解しか存在しないとき，アルゴリズムは解を見つけることなく停止します．
	 * @param p ファジィ制約充足問題
	 * @param worstSatisfactionDegree 最悪充足度
	 */
	public ObservableFuzzyForwardChecking2(Problem p, double worstSatisfactionDegree) {
		super(p, worstSatisfactionDegree);
	}

	@Override
	public void addListener(final SolverListener sl) {
		listeners_.add(sl);
	}
	
	@Override
	public void removeListener(final SolverListener sl) {
		listeners_.remove(sl);
	}
	
	@Override
	protected boolean foundSolution(final AssignmentList solution, final double worstDegree) {
		boolean finish = false;
		for(int i = 0, n = listeners_.size(); i < n; ++i) {
			if(listeners_.get(i).foundSolution(solution, worstDegree)) finish = true;
		}
		return finish;
	}

}

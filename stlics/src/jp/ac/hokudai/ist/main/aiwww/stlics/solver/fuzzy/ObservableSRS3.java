/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import java.util.ArrayList;
import java.util.List;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 *
 * @author Takty
 */
public class ObservableSRS3 extends SRS3 implements ObservableFuzzySolver {

	private List<SolverListener> listeners_ = new ArrayList<>();

	public ObservableSRS3(Problem p) {
		super(p);
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

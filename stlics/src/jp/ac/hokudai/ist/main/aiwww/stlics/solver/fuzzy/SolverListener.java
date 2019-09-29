package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 *
 * @author Takuto Yanagida
 * @version 2012/07/31
 */
public interface SolverListener {
	
	boolean foundSolution(final AssignmentList solution, final double worstDegree);
	
}

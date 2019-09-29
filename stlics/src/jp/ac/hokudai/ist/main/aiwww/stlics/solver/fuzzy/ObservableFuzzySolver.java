/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

/**
 *
 * @author Takty
 */
public interface ObservableFuzzySolver {

	void addListener(final SolverListener sl);
	
	void removeListener(final SolverListener sl);

}

package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;

/**
 * 複数の変数とその割り当てを表すクラスです．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class AssignmentList extends ArrayList<Assignment> {

	static private final Random RAND = new Random();

	public AssignmentList() {
		super();
	}

	public AssignmentList(final Assignment[] assignments) {
		super();
		for(final Assignment a: assignments) add(a.variable(), a.value());
	}

	public AssignmentList(final Collection<Variable> vars) {
		super();
		for(final Variable v: vars) add(v);
	}

	public AssignmentList(final Problem problem) {
		super();
		add(problem);
	}

	final public void add(final Problem problem) {
		for(final Variable v: problem.variables()) add(v);
	}

	final public void add(final Variable var) {
		add(new Assignment(var));
	}

	final public void add(final Variable var, final int value) {
		add(new Assignment(var, value));
	}

	public void apply() {
		for(final Assignment e: this) e.apply();
	}

	public Assignment arbitraryAssignment() {
		return get(RAND.nextInt(size()));
	}

	public int differenceSize() {
		int diff = 0;
		for(final Assignment e: this) {
			if(e.variable().value() != e.value()) ++diff;
		}
		return diff;
	}

	public void set(final Problem problem) {
		clear();
		for(final Variable v: problem.variables()) add(v);
	}

	public void set(final AssignmentList al) {
		clear();
		addAll(al);
	}
	
}

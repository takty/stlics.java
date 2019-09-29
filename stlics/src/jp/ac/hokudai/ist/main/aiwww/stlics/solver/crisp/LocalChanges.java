package jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractCrispSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.SystematicSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * Local changes法を実装したクラスです．
 * @author Takuto Yanagida
 * @version 2010/10/17
 */
public class LocalChanges extends AbstractCrispSolver implements SystematicSolver {

	private int iterationLimit_ = Integer.MAX_VALUE;
	private int satisfiedSizeLimit_ = -1;
	private int timeLimit_ = -1;
	private int iterationCount_;
	private long time_;
	private boolean globalReturn_;

	public LocalChanges(final CrispProblem p) {
		super(p);
	}

	public LocalChanges(final CrispProblem p, final boolean unassignAll) {
		this(p);
		if(unassignAll) {
			pro_.clearAllVariables();
		}
	}

	private Set<Variable> createNewV3(final Set<Variable> V1_V2, final Variable v, final int val) {
		final Set<Variable> newV3 = new HashSet<>();
		final Set<Constraint> cs = new HashSet<>();
		final List<Constraint> temp = new ArrayList<>();
		for(Variable va: V1_V2) {
			pro_.constraintsBetween(v, va, temp);
			cs.addAll(temp);
		}
		final int orgVal = v.value();  // 値を保存
		v.assign(val);
		for(Constraint c: cs) {
			final int s = c.isSatisfied();
			if(s == 0) {
				for(int i = 0; i < c.size(); ++i) {
					newV3.add(c.at(i));
				}
			}
		}
		v.assign(orgVal);  // 値を復元
		newV3.remove(v);
		return newV3;
	}

	private boolean isConsistent(final Set<Variable> A, final Variable v, final int val) {
		final Set<Constraint> cs = new HashSet<>();
		final List<Constraint> temp = new ArrayList<>();
		for(Variable va: A) {
			pro_.constraintsBetween(v, va, temp);
			cs.addAll(temp);
		}
		final int orgVal = v.value();  // 値を保存
		v.assign(val);
		for(Constraint c: cs) {
			final int s = c.isSatisfied();
			if(s == 0) {
				v.assign(orgVal);  // 値を復元
				return false;
			}
		}
		v.assign(orgVal);  // 値を復元
		return true;
	}

	private boolean lcValue(Set<Variable> V1, Set<Variable> V2, final Variable v, final int val) {
		final Set<Variable> V1_V2 = new HashSet<>(V1);
		V1_V2.addAll(V2);
		if(!isConsistent(V1, v, val)) {
			return false;
		}
		if(isConsistent(V1_V2, v, val)) {
			return true;
		}
		final Set<Variable> V3 = createNewV3(V1_V2, v, val);

		final Set<Variable> T = new HashSet<>(V1_V2);
		T.removeAll(V3);
		if(!isConsistent(T, v, val)) {
			if(debug) {
				debugStream.println("bug");
			}
		}
		
		for(Variable vv: V3) {
			vv.clear();
		}
		V1 = new HashSet<>(V1);
		V1.add(v);
		V2 = new HashSet<>(V2);
		V2.removeAll(V3);
		return lcVariables(V1, V2, V3);
	}

	private boolean lcVariable(final Set<Variable> V1, final Set<Variable> V2, final Variable v, final Set<Integer> d) {
		if(d.isEmpty()) {
			return false;
		}
		final int val = d.iterator().next();
		final AssignmentList al = new AssignmentList(V2);
		v.assign(val);

		final boolean ret = lcValue(V1, V2, v, val);
		if(ret || globalReturn_) {
			return ret;
		}

		v.clear();
		al.apply();
		final Set<Integer> d_new = new HashSet<>(d);
		d_new.remove(val);
		return lcVariable(V1, V2, v, d_new);
	}

	private boolean lcVariables(final Set<Variable> V1, Set<Variable> V2, Set<Variable> V3) {
		if(satisfiedSizeLimit_ != -1 && satisfiedSizeLimit_ <= pro_.satisfiedConstraintSize()) {globalReturn_ = true; return true;}  // 違反率が指定より改善されたら成功
		if(iterationLimit_ < iterationCount_++) {globalReturn_ = true; return false;}  // 規定回数繰り返したら失敗
		if(timeLimit_ != -1 && time_ < System.currentTimeMillis()) {globalReturn_ = true; return false;}  // 制限時間を超えたら失敗

		if(V3.isEmpty()) {
			return true;
		}
		final Variable v = V3.iterator().next();
		final Set<Integer> d = new HashSet<>();
		final Domain dom = v.domain();
		for(int i = 0; i < dom.size(); ++i) {
			d.add(dom.at(i));
		}
		
		final boolean ret = lcVariable(V1, V2, v, d);
		if(!ret || globalReturn_) {
			return ret;
		}

		V2 = new HashSet<>(V2);
		V2.add(v);
		V3 = new HashSet<>(V3);
		V3.remove(v);
		return lcVariables(V1, V2, V3);
	}
	
	@Override
    protected boolean exec() {
    	time_ = System.currentTimeMillis() + timeLimit_;
    	iterationCount_ = 0;
    	globalReturn_ = false;
    	
    	final Set<Variable> unassigned = new HashSet<>();
    	final Set<Variable> notFixed = new HashSet<>();
    	for(Variable v: pro_.variables()) {
    		(!v.isEmpty() ? notFixed : unassigned).add(v);
    	}
    	return lcVariables(new HashSet<Variable>(), notFixed, unassigned);
    }

	@Override
    public String name() {
        return "Local change";
    }

	@Override
    public void setIterationLimit(final int count) {
		iterationLimit_ = (count == -1) ? Integer.MAX_VALUE : count;
    }

	@Override
    public void setTargetRate(final double rate) {
		satisfiedSizeLimit_ = (rate == -1.0) ? -1 : (int)(pro_.constraintSize() * rate);
    }

	@Override
    public void setTimeLimit(final int msec) {
		timeLimit_ = msec;
    }

}

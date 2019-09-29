package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import java.util.*;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractFuzzySolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.SystematicSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * Flexible local changes法を実装したクラスです．
 * @author Takuto Yanagida and Yasuhiro Sudo
 * @version 2010/11/17
 */
public class FlexibleLocalChanges extends AbstractFuzzySolver implements SystematicSolver {

	private double lt_, lb_;

	private int iterCount_;
	private long endTime_;
	private int globalReturn_;

	public FlexibleLocalChanges(Problem p) {
		super(p, "Flexible local changes");
		computeHighestAndLowestConsistencyDegree();
	}

	private Set<Variable> choose(Set<Variable> x2, Set<Constraint> cr) {
		final Map<Variable, Integer> res = new HashMap<>();
		for(Constraint c: cr) {
			if(!c.isDefined()) continue;
			for(int i = 0; i < c.size(); ++i) {
				Integer r = res.get(c.at(i));
				if(r == null) res.put(c.at(i), 1);
				else res.put(c.at(i), r + 1);
			}
		}
		Variable[] vs = x2.toArray(new Variable[0]);
		Arrays.sort(vs, new Comparator<Variable>() {
			@Override
			public int compare(Variable o1, Variable o2) {
				int res1 = 0, res2 = 0;
				if(res.containsKey(o1)) res1 = res.get(o1);
				if(res.containsKey(o2)) res2 = res.get(o2);
				
				if(res1 < res2) return 1;
				if(res1 > res2) return -1;
				return 0;
			}
		});
		Set<Variable> ret = new HashSet<>();
		for(Variable v: vs) {
			boolean remain = false;
			for(Constraint c: cr) {
				if(c.isDefined()) {
					remain = true;
					break;
				}
			}
			if(!remain) break;
			v.clear();
			ret.add(v);
		}
		return ret;
	}

	private void computeHighestAndLowestConsistencyDegree() {
		Set<Constraint> cs = new HashSet<>();
		for(Variable v: pro_.variables()) {
			for(int i = 0; i < v.size(); ++i) cs.add(v.at(i));
		}
		double low = 1.0;
		double high = 0.0;
		for(Constraint c: cs) {
			double r = c.lowestConsistencyDegree();
			if(r < low) low = r;
			r = c.highestConsistencyDegree();
			if(r > high) high = r;
		}
		lb_ = low;
		lt_ = high;
	}

	private double flcRepair(Set<Variable> X1, Set<Variable> X2, Variable xi, double consX1xi, double consX12, Set<Constraint> cr, double rc) {
		Set<Variable> X3p = choose(X2, cr);
		Set<Variable> X1p = new HashSet<>(X1);
		X1p.add(xi);
		Set<Variable> X2p = new HashSet<>(X2);
		X2p.removeAll(X3p);
		return flcVariables(X1p, X2p, X3p, consX1xi, Math.min(consX12, consX1xi), rc);
	}

	private double flcVariable(Set<Variable> X1, Set<Variable> X2, Variable xi, double consX1, double consX12, double rc) {
		double bestCons = lb_;
		if(xi.domain().size() == 0) return bestCons;
		AssignmentList bestX2 = new AssignmentList(X2);
		int bestDij = xi.domain().at(0);
		AssignmentList x2Store = new AssignmentList(X2);

		for(int j = 0; j < xi.domain().size() && bestCons < consX12; ++j) {
			int dij = xi.domain().at(j);
			xi.assign(dij);
			double consX1_xi = Math.min(consX1, testX1(X1, xi, bestCons, rc));
			// System.out.println("consX1_xi = " + consX1_xi);
			if(consX1_xi > Math.max(bestCons, rc)) {
				Set<Constraint> crNew = new HashSet<>();
				double consX12_xi = Math.min(Math.min(consX1_xi, consX12), testX12(X1, X2, xi, consX1_xi, consX12, crNew));
				if(consX12_xi > bestCons) {
					bestCons = consX12_xi;
					bestDij = dij;
					bestX2  = new AssignmentList(X2);
				}
				if(!crNew.isEmpty()) {
					double repairCons = flcRepair(X1, X2, xi, consX1_xi, consX12, crNew, Math.max(rc, bestCons));
					if(globalReturn_ != -1) return bestCons;

					if(repairCons > bestCons) {
						bestCons = repairCons;
						bestDij = dij;
						bestX2  = new AssignmentList(X2);
					}
					x2Store.apply();
				}
			}
		}
		bestX2.apply();
		xi.assign(bestDij);
		return bestCons;
	}

	private double flcVariables(Set<Variable> X1, Set<Variable> X2, Set<Variable> X3, double consX1, double consX12, double rc) {
		if(debug) debugStream.println("X1 " + X1.size() + ", X2' " + X2.size() + ", X3' " + X3.size());
		Iterator<Variable> it = X3.iterator();
		while(it.hasNext()) {
			if(targetDeg_ != UNSPECIFIED && targetDeg_ <= pro_.worstSatisfactionDegree()) {globalReturn_ = 1; return consX12;}  // 違反率が指定より改善されたら成功
			if(iterLimit_ < iterCount_++) {globalReturn_ = 0; return consX12;}  // 規定回数繰り返したら失敗
			if(endTime_ < System.currentTimeMillis()) {globalReturn_ = 0; return consX12;}  // 制限時間を超えたら失敗

			Variable xi = it.next();
			double consX12xi = flcVariable(X1, X2, xi, consX1, consX12, rc);
			if(globalReturn_ != -1) return consX12;
			if(consX12xi < rc) return lb_;
			it.remove();
			X2.add(xi);
			consX12 = consX12xi;
		}
		return consX12;
	}

	private double initTest(Set<Variable> X, Set<Constraint> cr) {
		Set<Constraint> cs = new HashSet<>();
		for(Variable v: X) {
			for(int i = 0; i < v.size(); ++i) cs.add(v.at(i));  // Xはすべての割り当て済み変数
		}
		double ret = 1.0;
		for(Constraint c: cs) {
			double sd = c.satisfactionDegree();
			if(sd == Constraint.UNDEFINED) continue;
			if(sd < ret) ret = sd;
		}
		for(Constraint c: pro_.constraints()) {
			double cd = c.lowestConsistencyDegree();
			if(cd < lt_) cr.add(c);
		}
		return ret;
	}

	private double testX1(Set<Variable> X1, Variable xi, double bestcons, double rc) {
		double cd = 1.0;
		Set<Constraint> cs = new HashSet<>();
		List<Constraint> temp = new ArrayList<>();
		for(Variable v: X1) {
			pro_.constraintsBetween(v, xi, temp);
			cs.addAll(temp);
		}
		for(Constraint c: cs) {
			double d = c.satisfactionDegree();
			if(d == Constraint.UNDEFINED) continue;
			if(d < cd) cd = d;
			if(cd <= bestcons || cd <= rc) return cd;  // 現在より良い解を得られないことが確定した場合
		}
		return cd;
	}

	private double testX12(Set<Variable> X1, Set<Variable> X2, Variable xi, double consX1xi, double consX12, Set<Constraint> cr) {
		double csd = 1.0;
		Set<Constraint> cs = new HashSet<>();
		List<Constraint> temp = new ArrayList<>();
		for(Variable v: X1) {
			pro_.constraintsBetween(v, xi, temp);
			cs.addAll(temp);
		}
		for(Variable v: X2) {
			pro_.constraintsBetween(v, xi, temp);
			cs.addAll(temp);
		}
		for(Constraint c: cs) {
			double sd = c.satisfactionDegree();
			if(sd == Constraint.UNDEFINED) continue;
			if(sd < csd) csd = sd;
		}
		for(Constraint c: cs) {  // HOGE
			double sd = c.satisfactionDegree();
			if(sd == Constraint.UNDEFINED) continue;
			if(sd < consX1xi || sd < consX12) cr.add(c);
		}
		return csd;
	}

	@Override
	protected boolean exec() {
		endTime_ = (timeLimit_ == -1) ? Long.MAX_VALUE : (System.currentTimeMillis() + timeLimit_);
		iterCount_ = 0;

		double wsd = pro_.worstSatisfactionDegree();
		if(pro_.emptyVariableSize() == 0) pro_.clearAllVariables();
		globalReturn_ = -1;

		Set<Variable> X1 = new HashSet<>();
		Set<Variable> X2 = new HashSet<>();  // all currently assigned variables
		Set<Variable> X3 = new HashSet<>();  // all currently unassigned variables
		for(Variable v: pro_.variables()) (!v.isEmpty() ? X2 : X3).add(v);

		Set<Constraint> cr = new HashSet<>();
		double initCons = initTest(X2, cr);
		double rc;
		AssignmentList initSolution = null;
		if(X3.isEmpty()) {
			rc = initCons;
			initSolution = new AssignmentList(X2);
		} else {
			rc = lb_;
		}
		Set<Variable> X3p = new HashSet<>(X3);
		X3p.addAll(choose(X2, cr));
		Set<Variable> X2p = new HashSet<>(X2);
		X2p.removeAll(X3p);
		double result = flcVariables(X1, X2p, X3p, lt_, lt_, rc);
		if(result < rc) {
			if(initSolution != null) initSolution.apply();
//			result = rc;
		}
		result = pro_.worstSatisfactionDegree();
		return result > wsd && result > 0 && (globalReturn_ != 0 || targetDeg_ == UNSPECIFIED);
	}

}

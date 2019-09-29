package jp.ac.hokudai.ist.main.aiwww.stlics.solver.filter;

import java.util.ArrayList;
import java.util.List;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;

/**
 * 辺整合のアルゴリズムの一つであるAC-3を実装したクラスです．
 * @author Takuto Yanagida
 * @version 2010/10/17
 */
public abstract class AC3 {

	static private boolean checkConsistency(final Constraint c, final Variable v_j) {
		final Domain d_j = v_j.domain();
		for(int i = 0; i < d_j.size(); ++i) {  // 制約を満たす相手は存在するか?
			v_j.assign(d_j.at(i));
			if(c.isSatisfied() == 1) {  // 存在した!
				return true;  // 現在のv_iの割り当ては整合する
			}
		}
		return false;
	}
	
	static private boolean reviseDomain(final CrispProblem p, final Variable v_i, final Variable v_j) {
		final int val_i = v_i.value(), val_j = v_j.value();  // 値を保存
		final Domain d_i = v_i.domain();
		final List<Integer> temp = new ArrayList<>();

		final List<Constraint> cs = new ArrayList<>();
		p.constraintsBetween(v_i, v_j, cs);
		vals: for(int i = 0; i < d_i.size(); ++i) {
			v_i.assign(d_i.at(i));
			for(int k = 0; k < cs.size(); ++k) {
				final Constraint c = cs.get(k);
				if(c.size() != 2) continue;  // 次の制約のチェック
				if(!checkConsistency(c, v_j)) continue vals;   // 制約を満たす相手が一つでもいなかったので，次の値を調べる
			}
			temp.add(d_i.at(i));
		}
		v_i.assign(val_i); v_j.assign(val_j);  // 値を復元
		if(temp.size() != d_i.size()) {
			final Domain nd = p.createDomain(temp);
			v_i.setDomain(nd);
			System.out.println(d_i.size() + " -> " + nd.size());
			return true;
		}
		return false;
	}

	static public void apply(final CrispProblem p) {
		final ArrayList<Constraint> cs = new ArrayList<>();  // TODO キューにする
		for(int i = 0; i < p.constraintSize(); ++i) {
			final Constraint c = p.constraintAt(i);
			if(c.size() == 2) cs.add(c);
		}
		while(!cs.isEmpty()) {
			final Constraint c = cs.remove(cs.size() - 1);
			final Variable v_k = c.at(0), v_m = c.at(1);
		    if(reviseDomain(p, v_k, v_m)) {
				for(int i = 0; i < p.constraintSize(); ++i) {
					final Constraint c1 = p.constraintAt(i);
					if(c1.size() == 2 && c1.at(1) == v_k && c1.at(0) != v_m) {
						cs.add(0, c1);
					}
				}
		    }
		}
	}

}

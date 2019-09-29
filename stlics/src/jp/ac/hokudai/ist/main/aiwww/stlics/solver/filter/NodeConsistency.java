package jp.ac.hokudai.ist.main.aiwww.stlics.solver.filter;

import java.util.ArrayList;
import java.util.List;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.*;

/**
 * 節整合を実行するユーティリティクラスです．
 * @author Takuto YANAGIDA
 * @version 2009/09/15
 */
public abstract class NodeConsistency {

	/**
	 * ファジィ単項制約の整合性を保証します．各変数のドメインが必要に応じて置き換えられます．
	 * 指定された最悪充足度に満たないドメインの要素を削除します．
	 * @param p 整合性を保証する制約充足問題
	 * @param threshold 最悪充足度
	 * @return 空のドメインが存在しなければtrue，そうでなければfalse
	 */
	static public boolean apply(final Problem p, final double threshold) {
		final List<Variable> vs = p.variables();
		for(int i = 0; i < vs.size(); ++i) {
			final Variable v = vs.get(i);
			final Domain d = v.domain();
			final int orgVal = v.value();  // 値を保存
			final List<Integer> elms = new ArrayList<>();
			for(int j = 0; j < v.size(); ++j) {
				final Constraint c = v.at(j);
				if(c.size() != 1) continue;
				for(int k = 0; k < d.size(); ++k) {
					final int val = d.at(k);
					v.assign(val);
					final double sd = c.satisfactionDegree();
					if(sd >= threshold) elms.add(val); 
				}
				p.removeConstraint(c);
			}
			v.assign(orgVal);  // 値を復元
			if(elms.isEmpty()) return false;
			final Domain nd = p.createDomain(elms);
			v.setDomain(nd);
		}
		return true;
	}

	/**
	 * クリスプな単項制約の整合性を保証します．各変数のドメインが必要に応じて置き換えられます．
	 * 制約グラフの構造を変化させるため，ファジィ制約充足問題のクリスプなビューに適用することはできません．
	 * @param p 整合性を保証するクリスプ制約充足問題
	 * @return 空のドメインが存在しなければtrue，そうでなければfalse
	 */
	static public boolean apply(final CrispProblem p) {
		final List<Variable> vs = p.variables();
		for(int i = 0; i < vs.size(); ++i) {
			final Variable v = vs.get(i);
			final Domain d = v.domain();
			final int orgVal = v.value();  // 値を保存
			final List<Integer> elms = new ArrayList<>();
			for(int j = 0; j < v.size(); ++j) {
				final Constraint c = v.at(j);
				if(c.size() != 1) continue;
				for(int k = 0; k < d.size(); ++k) {
					final int val = d.at(k);
					v.assign(val);
					if(c.isSatisfied() == 1) elms.add(val); 
				}
				p.removeConstraint(c);
			}
			v.assign(orgVal);  // 値を復元
			if(elms.isEmpty()) return false;
			final Domain nd = p.createDomain(elms);
			v.setDomain(nd);
		}
		return true;
	}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.List;

/**
 * クリスプ制約充足問題を表すクラスです．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class CrispProblem extends Problem {

	/**
	 * 変数間の関係を指定して制約を生成します．
	 * @param r 変数間の関係
	 * @param vs 変数
	 * @return 生成された制約
	 */
	@Override
	public Constraint createConstraint(final Relation r, final Variable ... vs) {
		if(r instanceof FuzzyRelation) throw new IllegalArgumentException(); 
		return super.createConstraint(r, vs);
	}

	/**
	 * 名前と変数間の関係を指定して制約を生成します．
	 * @param name 名前
	 * @param r 変数間の関係
	 * @param vs 変数
	 * @return 生成された制約
	 */
	@Override
	public Constraint createConstraint(final String name, final Relation r, final Variable ... vs) {
		if(r instanceof FuzzyRelation) throw new IllegalArgumentException(); 
		return super.createConstraint(name, r, vs);
	}

	/**
	 * ファジィ制約充足問題であるか，すなわちファジィ制約を含んでいるかどうかを返します．
	 * @return 常にfalseを返します．
	 */
	@Override
	public boolean isFuzzy() {
		return false;
	}

	/**
	 * 全制約中充足している制約の割合を返します．
	 * @return 充足している制約の割合
	 */
	public double satisfiedConstraintRate() {
		return satisfiedConstraintSize() / (double)cons_.size();
	}

	/**
	 * 充足している制約のサイズを返します．
	 * ただし，未定義の制約は無視します．
	 * @return 制約の充足数
	 */
	public int satisfiedConstraintSize() {
		int count = 0;
		for(int i = 0; i < cons_.size(); ++i) {
			if(cons_.get(i).isSatisfied() == 1) ++count;
		}
		return count;
	}

	/**
	 * 違反している制約のリストを返します．
	 * ただし，未定義の制約は無視します．
	 * @param cs 制約を取得するリスト
	 * @return 制約のリスト
	 */
	public List<Constraint> violatingConstraints(final List<Constraint> cs) {
		cs.clear();
		for(int i = 0; i < cons_.size(); ++i) {
			final Constraint c = cons_.get(i);
			if(c.isSatisfied() == 0) cs.add(c);
		}
		return cs;
	}

	/**
	 * 違反している制約のサイズを返します．
	 * ただし，未定義の制約は無視します．
	 * @return 制約の違反数
	 */
	public int violatingConstraintSize() {
		int count = 0;
		for(int i = 0; i < cons_.size(); ++i) {
			if(cons_.get(i).isSatisfied() == 0) ++count;
		}
		return count;
	}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.Collection;

/**
 * 制約を表す抽象クラスです．
 * @author Takuto Yanagida
 * @version 2012/11/30
 */
public abstract class Constraint extends Element {

	/**
	 * 充足度が定義されないことを表す定数です．
	 */
	static public final int UNDEFINED = -1;

	protected final Relation rel_;

	// 変数間の関係を指定して制約を生成するコンストラクタ．Problemからのみ呼び出される．
	Constraint(final Relation r) {
		rel_ = r;
	}

	/**
	 * 変数間のクリスプ関係を返します．
	 * @return 変数間の関係
	 */
	public CrispRelation crispRelation() {
		return (CrispRelation)rel_;
	}

	/**
	 * 変数間のファジィ関係を返します．
	 * @return 変数間の関係
	 */
	public FuzzyRelation fuzzyRelation() {
		return (FuzzyRelation)rel_;
	}

	/**
	 * ファジィ制約かどうかを返します．
	 * @return ファジィ制約ならばtrue
	 */
	public boolean isFuzzy() {
		return rel_ instanceof FuzzyRelation;
	}
	
	/**
	 * 制約の文字列表現を返します．
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		final double s = satisfactionDegree();
		return new StringBuilder("c").append(index()).append(name().isEmpty() ? "" : "(" + name() + ")").append(" = ").append(s == UNDEFINED ? "UNDEFINED" : s).toString();
	}

	/**
	 * 制約の次数，スコープの(関連付けられた)変数の個数を返します．
	 * @return 次数
	 */
	public abstract int size();

	/**
	 * インデックスを指定して関連付けられた変数を取得します．
	 * @param index インデックス
	 * @return 変数
	 */
	public abstract Variable at(final int index);

	/**
	 * 指定された変数が関連付けられているかどうかを返します．
	 * @param v 変数
	 * @return 関連付けられていればtrue，そうでなければfalse
	 */
	public abstract boolean constrains(final Variable v);

	/**
	 * 指定した変数のインデックスを取得します．
	 * 見つからないときは-1を返します．
	 * @param var 変数
	 * @return インデックス
	 */
	public abstract int indexOf(final Variable var);

	/**
	 * 値の割り当てられていないスコープ変数の数を返します．
	 * @return 変数の数
	 */
	public abstract int emptyVariableSize();

	/**
	 * 充足(度)が定義されているかどうかを返します．
	 * 関連するすべての変数に値が割り当てられているとき，充足(度)は定義されます．
	 * @return 定義されているならtrue，されていないならfalse
	 */
	public abstract boolean isDefined();

	/**
	 * 充足しているかどうかを返します．
	 * @return 充足しているなら1, していないなら0，未定義ならUNDEFINED
	 */
	public abstract int isSatisfied();

	/**
	 * 現在の充足度を求めます．
	 * @return 充足度0.0~1.0，未定義ならUNDEFINED
	 */
	public abstract double satisfactionDegree();

	/**
	 * 関連付けられた変数を介して接続されている制約の集合を返します．
	 * @return 制約の集合
	 */
	public abstract Collection<Constraint> neighbors(final Collection<Constraint> dest);

	/**
	 * 最大の整合度を求めます．
	 * すなわち，ある制約ににおいて可能な変数割り当ての組み合わせにおいてもっとも高い充足度を求めます．
	 * 関連するすべての変数に値が割り当てられている時は，getSatisfactionDegree()と同じ値を返します．
	 * @return 最大の整合度
	 */
	public abstract double highestConsistencyDegree();

	/**
	 * 最小の整合度を求めます．
	 * すなわち，ある制約ににおいて可能な変数割り当ての組み合わせにおいてもっとも低い充足度を求めます．
	 * 関連するすべての変数に値が割り当てられている時は，getSatisfactionDegree()と同じ値を返します．
	 * @return 最小の整合度
	 */
	public abstract double lowestConsistencyDegree();

}

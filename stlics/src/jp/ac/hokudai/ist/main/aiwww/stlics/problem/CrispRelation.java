package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

/**
 * 変数間のクリスプ関係を表すクラスです．
 * 必要なメソッドをオーバーライドしたクラスを作成することによって，任意のクリスプ関係を表現します．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class CrispRelation implements Relation {

	/**
	 * クリスプな単項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value 変数の値
	 * @return 充足しているかどうか
	 */
	public boolean isSatisfied(final int value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * クリスプな二項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @return 充足しているかどうか
	 */
	public boolean isSatisfied(final int value1, final int value2) {
		throw new UnsupportedOperationException();
	}

	/**
	 * クリスプな三項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @param value3 三つ目の変数の値
	 * @return 充足しているかどうか
	 */
	public boolean isSatisfied(final int value1, final int value2, final int value3) {
		throw new UnsupportedOperationException();
	}

	/**
	 * クリスプな多項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param vs 各変数の値
	 * @return 充足しているかどうか
	 */
	public boolean isSatisfied(final int ... vs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ファジィ関係としてのビューを返します．
	 * @return ファジィ関係
	 */
	public FuzzyRelation asFuzzyRelation() {
		return new FuzzyRelation() {
			@Override
			public double satisfactionDegree(final int value) {
				return isSatisfied(value) ? 1.0: 0.0;
			}
			@Override
			public double satisfactionDegree(final int value1, final int value2) {
				return isSatisfied(value1, value2) ? 1.0: 0.0;
			}
			@Override
			public double satisfactionDegree(final int value1, final int value2, final int value3) {
				return isSatisfied(value1, value2, value3) ? 1.0: 0.0;
			}
			@Override
			public double satisfactionDegree(final int ... vs) {
				return isSatisfied(vs) ? 1.0: 0.0;
			}
		};
	}

}

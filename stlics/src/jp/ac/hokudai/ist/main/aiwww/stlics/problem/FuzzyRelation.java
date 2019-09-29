package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

/**
 * 変数間のファジィ関係を表すクラスです．
 * 必要なメソッドをオーバーライドしたクラスを作成することによって，任意のファジィ関係を表現します．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class FuzzyRelation implements Relation {

	/**
	 * ファジィな単項関係における充足度を計算します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value 変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	public double satisfactionDegree(final int value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ファジィな二項関係における充足度を計算します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	public double satisfactionDegree(final int value1, final int value2) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ファジィな三項関係における充足度を計算します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @param value3 三つ目の変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	public double satisfactionDegree(final int value1, final int value2, final int value3) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ファジィな多項関係における充足度を計算します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param values 各変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	public double satisfactionDegree(final int... values) {
		throw new UnsupportedOperationException();
	}

	/**
	 * クリスプ関係としてのビューを返します．
	 * @return クリスプ関係
	 */
	public CrispRelation asCrispRelation() {
		return new CrispRelation() {
			@Override
			public boolean isSatisfied(final int value) {
				return satisfactionDegree(value) == 1.0;
			}
			@Override
			public boolean isSatisfied(final int value1, final int value2) {
				return satisfactionDegree(value1, value2) == 1.0;
			}
			@Override
			public boolean isSatisfied(final int value1, final int value2, final int value3) {
				return satisfactionDegree(value1, value2, value3) == 1.0;
			}
			@Override
			public boolean isSatisfied(final int... values) {
				return satisfactionDegree(values) == 1.0;
			}
		};
	}

}

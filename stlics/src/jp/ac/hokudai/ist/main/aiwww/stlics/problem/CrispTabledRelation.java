package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

/**
 * 表によるクリスプな関係を表すクラスです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public class CrispTabledRelation extends CrispRelation {

	private final boolean[] elms_;
	private final Domain[] doms_;
	private final int[] mul_;

	public CrispTabledRelation(final boolean[] elms, final Domain[] doms) {
		elms_ = elms.clone();
		doms_ = doms.clone();
		mul_ = new int[doms.length];
		int m = 1;
		for(int i = mul_.length - 1; i >= 0; --i) {
			mul_[i] = m;
			m *= doms[i].size();
		}
	}

	/**
	 * クリスプな単項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value 変数の値
	 * @return 充足しているかどうか
	 */
	@Override
	public boolean isSatisfied(final int value) {
		if(mul_.length != 1) throw new UnsupportedOperationException();
		return elms_[doms_[0].indexOf(value)];
	}

	/**
	 * クリスプな二項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @return 充足しているかどうか
	 */
	@Override
	public boolean isSatisfied(final int value1, final int value2) {
		if(mul_.length != 2) throw new UnsupportedOperationException();
		return elms_[mul_[0] * doms_[0].indexOf(value1) + mul_[1] * doms_[1].indexOf(value2)];
	}

	/**
	 * クリスプな三項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @param value3 三つ目の変数の値
	 * @return 充足しているかどうか
	 */
	@Override
	public boolean isSatisfied(final int value1, final int value2, final int value3) {
		if(mul_.length != 3) throw new UnsupportedOperationException();
		return elms_[mul_[0] * doms_[0].indexOf(value1) + mul_[1] * doms_[1].indexOf(value2) + mul_[2] * doms_[2].indexOf(value3)];
	}

	/**
	 * クリスプな多項関係において充足しているかどうかを返します．
	 * 任意の関係を表現するようにオーバーライドします．
	 * @param vs 各変数の値
	 * @return 充足しているかどうか
	 */
	@Override
	public boolean isSatisfied(final int... vs) {
		if(mul_.length != vs.length) throw new UnsupportedOperationException();
		int index = 0;
		for(int i = 0; i < mul_.length; ++i) index += mul_[i] * doms_[i].indexOf(vs[i]);
		return elms_[index];
	}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

/**
 * 表によるファジィな関係を表すクラスです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public class FuzzyTabledRelation extends FuzzyRelation {

	private final double[] elms_;
	private final Domain[] doms_;
	private final int[] mul_;

	public FuzzyTabledRelation(final double[] elms, final Domain[] doms) {
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
	 * ファジィな単項関係における充足度を計算します．
	 * @param value 変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	@Override
	public double satisfactionDegree(final int value) {
		if(mul_.length != 1) throw new UnsupportedOperationException();
		return elms_[doms_[0].indexOf(value)];
	}

	/**
	 * ファジィな二項関係における充足度を計算します．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	@Override
	public double satisfactionDegree(final int value1, final int value2) {
		if(mul_.length != 2) throw new UnsupportedOperationException();
		return elms_[mul_[0] * doms_[0].indexOf(value1) + mul_[1] * doms_[1].indexOf(value2)];
	}

	/**
	 * ファジィな三項関係における充足度を計算します．
	 * @param value1 一つ目の変数の値
	 * @param value2 二つ目の変数の値
	 * @param value3 三つ目の変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	@Override
	public double satisfactionDegree(final int value1, final int value2, final int value3) {
		if(mul_.length != 3) throw new UnsupportedOperationException();
		return elms_[mul_[0] * doms_[0].indexOf(value1) + mul_[1] * doms_[1].indexOf(value2) + mul_[2] * doms_[2].indexOf(value3)];
	}

	/**
	 * ファジィな多項関係における充足度を計算します．
	 * @param vs 各変数の値
	 * @return 充足度d (0.0 <= d <= 1.0)
	 */
	@Override
	public double satisfactionDegree(final int... vs) {
		if(mul_.length != vs.length) throw new UnsupportedOperationException();
		int index = 0;
		for(int i = 0; i < mul_.length; ++i) index += mul_[i] * doms_[i].indexOf(vs[i]);
		return elms_[index];
	}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.solver;

/**
 * 制約充足問題の解を求めるソルバのインタフェースです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public interface Solver {

	static public final double UNSPECIFIED = -1.0;

	/**
	 * ソルバの名称を返します．
	 * @return ソルバの名称
	 */
	String name();

	/**
	 * ソルバの動作における繰り返し回数に最大値を設定し，それを制限します．
	 * 指定回数を繰り返すと，ソルバは失敗として停止します．具体的な動作はソルバに依存します．
	 * @param count 最大値．UNSPECIFIEDは未設定を表す．
	 */
	void setIterationLimit(final int count);

	/**
	 * ソルバの動作に制限時間を設定します．
	 * 指定時間を超えると，ソルバは失敗として停止します．具体的な動作はソルバに依存します．
	 * @param msec 制限時間．UNSPECIFIEDは未設定を表す．
	 */
	void setTimeLimit(final int msec);

	/**
	 * ソルバの停止の条件となる達成目標として，制約充足度（ファジィ），もしくは充足した制約の割合（クリスプ）を設定します．
	 * 指定した割合以上になると，ソルバは成功として停止します．具体的な動作はソルバに依存します．
	 * @param rate 割合．UNSPECIFIEDは未設定を表す．
	 */
	void setTargetRate(final double rate);

	/**
	 * 制約充足問題の解を求めます．
	 * 戻り値の具体的な意味はアルゴリズムの実装に依存します．
	 * @return アルゴリズムが成功したらtrue，失敗したらfalse．
	 */
	boolean solve();

}

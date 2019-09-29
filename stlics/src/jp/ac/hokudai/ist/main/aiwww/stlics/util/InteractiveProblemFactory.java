package jp.ac.hokudai.ist.main.aiwww.stlics.util;

/**
 * ダイアログ・ボックスによって設定可能な，
 * 制約充足問題を生成するファクトリ・メソッドを提供するインタフェースです．
 * @author Takuto Yanagida
 * @version 2012/11/22
 */
public interface InteractiveProblemFactory extends ProblemFactory {

	/**
	 * 設定ダイアログ・ボックスを表示します．
	 * @param ownerWindow ダイアログ・ボックスのオーナーとなるオブジェクト
	 * @return OKならばtrue
	 */
	boolean showDialog(final Object ownerWindow);

}

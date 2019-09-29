package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

/**
 * 変数と制約の共通のクラスです．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class Element {

	private int index_ = -1;
	private String name_ = "";

	// Problemからのみ呼び出される
	void setIndex(final int index) {
		index_ = index;
	}

	/**
	 * 利用者が各要素に任意のオブジェクトを関連づけたいときに使います．
	 */
	public Object userObject;

	/**
	 * ソルバが各要素に任意のオブジェクトを関連づけたいときに使います．
	 */
	public Object solverObject;

	/**
	 * 所有されている問題におけるインデックスを取得します．
	 * インデックスとして変数と制約それぞれに通し番号が振られ，Problemを通してアクセスする際に用いられます．
	 * @return インデックスを表す整数値
	 */
	public int index() {
		return index_;
	}

	/**
	 * 名前を取得します．
	 * @return 名前を表す文字列
	 */
	public String name() {
		return name_;
	}

	/**
	 * 名前を設定します．
	 * @param name 名前を表す文字列
	 */
	public void setName(final String name) {
		name_ = name;
	}

}

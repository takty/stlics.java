package jp.ac.hokudai.ist.main.aiwww.stlics.solver;

import java.util.Arrays;

/**
 * ドメインの枝刈り情報を保持するクラスです．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class DomainPruner {

	static private final int UNHIDDEN = Integer.MIN_VALUE;

	private final int[] hiddenLevels_;
	private int hiddenSize_ = 0;

	/**
	 * ドメインの枝刈り情報を保持するクラスを生成します．
	 * @param size 対応するドメインのサイズ
	 */
	public DomainPruner(final int size) {
		hiddenLevels_ = new int[size];
		Arrays.fill(hiddenLevels_, UNHIDDEN);
	}

	/**
	 * 消去された要素のサイズを返します．
	 * @return 消去された要素のサイズ
	 */
	public int hiddenSize() {
		return hiddenSize_;
	}

	/**
	 * 指定したインデックスの要素を消去します．
	 * @param index インデックス
	 * @param level レベル
	 */
	public void hide(final int index, final int level) {
		if(hiddenLevels_[index] == UNHIDDEN) ++hiddenSize_;
		hiddenLevels_[index] = level;
	}

	/**
	 * 要素が空かどうかを返します．
	 * すべての要素が消去されているとき，trueを返します．
	 * @return 空ならばtrue，そうでなければfalse
	 */
	public boolean isEmpty() {
		return hiddenLevels_.length == hiddenSize_;
	}

	/**
	 * 指定したインデックスの要素が消去されているかどうかを返します．
	 * @param index インデックス
	 * @return 消去されていればtrue，そうでなければfalse
	 */
	public boolean isValueHidden(final int index) {
		return hiddenLevels_[index] != UNHIDDEN;
	}

	/**
	 * レベルを指定して，消去されていた値を復活します．
	 * @param level レベル
	 */
	public void reveal(final int level) {
		for(int i = 0; i < hiddenLevels_.length; i++) {
			if(hiddenLevels_[i] == level) {
				hiddenLevels_[i] = UNHIDDEN;
				--hiddenSize_;
			}
		}
	}

	/**
	 * すべての消去されていた値を復活します．
	 */
	public void revealAll() {
		Arrays.fill(hiddenLevels_, UNHIDDEN);
		hiddenSize_ = 0;
	}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 変数を表すクラスです．
 * @author Takuto Yanagida
 * @version 2011/11/20
 */
public class Variable extends Element {

	static private final int INVALID = Integer.MIN_VALUE;

	private final Problem owner_;
	private final List<Constraint> cons_ = new ArrayList<>();
	private Domain dom_;
	private int val_ = INVALID;  // 継承先での初期化タイミングを考慮して

	// Problemからのみ呼び出される．
	protected Variable(final Problem owner, final Domain d) {
		owner_ = owner;
		dom_ = d;
	}

	// Problemからのみ呼び出される．
	void connect(final Constraint c) {
		if(isConstrainedBy(c)) throw new IllegalArgumentException();
		cons_.add(c);
	}
	
	// Problemからのみ呼び出される．
	void deconnect(final Constraint c) {
		if(!isConstrainedBy(c)) throw new IllegalArgumentException();
		cons_.remove(c);
	}

	/**
	 * 値を設定します．
	 * @param value 設定する値
	 */
	public void assign(final int value) {
		val_ = value;  // ここ以外ではval_を変更しないこと．
	}

	/**
	 * 変数の状態を未割り当てにします．
	 */
	final public void clear() {
		assign(INVALID);  // ここと下（isEmpty）以外では無効値を使わないこと．
	}

	/**
	 * 値が未割り当てかどうかを返します．
	 * @return 未割り当てならtrue
	 */
	public boolean isEmpty() {
		return value() == INVALID;
	}

	/**
	 * ドメインを割り当てます，
	 * 変数は未割当の状態になります．
	 * @param d 割り当てるドメイン
	 */
	public void setDomain(final Domain d) {
		dom_ = d;
		clear();
	}

	/**
	 * この変数を所有する問題を返します．
	 * @return 所有者
	 */
	public Problem owner() {
		return owner_;
	}

	/**
	 * 関連付けられている制約の個数を返します．
	 * @return 制約の個数
	 */
	public int size() {
		return cons_.size();
	}

	/**
	 * インデックスを指定して関連付けられている制約を取得します．
	 * @param index インデックス
	 * @return 制約
	 */
	public Constraint at(final int index) {
		return cons_.get(index);
	}

	/**
	 * 変数に関連付けられたすべての制約を含む配列を返します．
	 * 制約が存在しないときは空の配列を返します．
	 * @return 制約の配列
	 */
	public Constraint[] constraints() {
		return cons_.toArray(new Constraint[cons_.size()]);
	}

	/**
	 * 変数のドメインを返します．
	 * @return ドメイン
	 */
	public Domain domain() {
		return dom_;
	}

	/**
	 * 指定された制約に関連付けられているかどうかを返します．
	 * @param c 制約
	 * @return 関連付けられていればtrue
	 */
	public boolean isConstrainedBy(final Constraint c) {
		return cons_.contains(c);
	}

	/**
	 * 文字列表現を返します．
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		return new StringBuilder("x").append(index()).append(name().isEmpty() ? "" : "(" + name() + ")").append(" = ").append(isEmpty() ? "<empty>" : value()).toString();
	}

	/**
	 * 変数の値を返します．
	 * @return 変数の値
	 */
	public int value() {
		return val_;
	}

	/**
	 * 関連付けられた制約を介して接続されている変数の集合を返します．
	 * @return 変数の集合
	 */
	public Collection<Variable> neighbors(final Collection<Variable> dest) {
		for(int i = 0, n = cons_.size(); i < n; ++i) {
			final Constraint c = cons_.get(i);
			for(int j = 0, m = c.size(); j < m; ++j) dest.add(c.at(j));
		}
		dest.remove(this);
		return dest;
	}

}

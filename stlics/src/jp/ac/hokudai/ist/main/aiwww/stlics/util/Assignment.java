package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import java.util.Objects;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;

/**
 * 変数とそれに割り当てる値の組を表現するクラスです．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class Assignment {

	private final Variable variable_;
	private final int value_;

	/**
	 * 既存の割り当てのコピーを生成します．
	 * @param assignment 既存の割り当て
	 */
	public Assignment(final Assignment assignment) {
		this(assignment.variable(), assignment.value());
	}

	/**
	 * 変数を指定して割り当てを生成します．
	 * 現在の変数の値が保存されます．
	 * @param var 保存する変数
	 */
	public Assignment(final Variable var) {
		variable_ = var;
		value_ = var.value();
	}

	/**
	 * 変数と値を指定して割り当てを生成します．
	 * @param variable 保存する変数
	 * @param value 保存する値
	 */
	public Assignment(final Variable variable, final int value) {
		variable_ = variable;
		value_ = value;
	}

	/**
	 * 保存している変数に値を割り当てます．
	 */
	public void apply() {
		variable_.assign(value_);
	}

	/**
	 * 文字列表現を返します．
	 * @return 文字列表現
	 */
	@Override
	public String toString() {
		return new StringBuilder("v").append(variable_.index()).append(" <- ").append(value_).toString();
	}

	/**
	 * 等しいかどうかを調べます．
	 * @param obj 調べる対象のオブジェクト
	 * @return 等しければtrue
	 */
	@Override
	public boolean equals(final Object obj) {
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		final Assignment a = (Assignment)obj;
		if(!Objects.equals(variable_, a.variable_)) return false;
		if(value_ != a.value_) return false;
		return true;
	}

	/**
	 * ハッシュコードを返します．
	 * @return ハッシュコード
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(variable_);
		hash = 37 * hash + value_;
		return hash;
	}
	
	/**
	 * 値を取得します．
	 * @return 値
	 */
	public int value() {
		return value_;
	}

	/**
	 * 変数を取得します．
	 * @return 変数
	 */
	public Variable variable() {
		return variable_;
	}

}

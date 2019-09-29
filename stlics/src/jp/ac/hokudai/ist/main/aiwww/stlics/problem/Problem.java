package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.*;

/**
 * 制約充足問題を表すクラスです．
 * @author Takuto Yanagida
 * @version 2011/11/20
 */
public class Problem {

	private boolean isFuzzy_ = false;
	private final List<Variable> vars_ = new ArrayList<>();
	protected final List<Constraint> cons_ = new ArrayList<>();

	// 生成用メソッド --------

	protected void addVariable(final Variable v) {
		v.setIndex(vars_.size());
		vars_.add(v);
	}

	/**
	 * 要素を指定してドメインを生成します．
	 * @param values 複数の値
	 * @return 生成されたドメイン
	 */
	public Domain createDomain(final Collection<Integer> values) {
		return Domain.newDomainArbitrary(values);
	}

	/**
	 * 要素を指定してドメインを生成します．
	 * @param values 複数の値
	 * @return 生成されたドメイン
	 */
	public Domain createDomain(final int... values) {
		return Domain.newDomainArbitrary(values);
	}

	/**
	 * 範囲を指定してドメインを生成します．
	 * @param min 最小値
	 * @param max 最大値
	 * @return 生成されたドメイン
	 */
	public Domain createDomain(final int min, final int max) {
		return Domain.newDomainRanged(min, max);
	}

	/**
	 * ドメインを指定して変数を生成します．
	 * @param d ドメイン
	 * @return 生成された変数
	 */
	public Variable createVariable(final Domain d) {
		final Variable v = new Variable(this, d);
		addVariable(v);
		return v;
	}

	/**
	 * 値とドメインを指定して変数を生成します．
	 * @param value 値
	 * @param d ドメイン
	 * @return 生成された変数
	 */
	public Variable createVariable(final Domain d, final int value) {
		if(!d.contains(value)) throw new IllegalArgumentException();
		final Variable v = createVariable(d);
		v.assign(value);
		return v;
	}

	/**
	 * 名前とドメインを指定して変数を生成します．
	 * @param name 名前
	 * @param d ドメイン
	 * @return 生成された変数
	 */
	public Variable createVariable(final String name, final Domain d) {
		final Variable v = createVariable(d);
		v.setName(name);
		return v;
	}

	/**
	 * 名前と値とドメインを指定して変数を生成します．
	 * @param name 名前
	 * @param value 値
	 * @param d ドメイン
	 * @return 生成された変数
	 */
	public Variable createVariable(final String name, final Domain d, final int value) {
		if(!d.contains(value)) throw new IllegalArgumentException();
		final Variable v = createVariable(d);
		v.setName(name);
		v.assign(value);
		return v;
	}

	/**
	 * 変数間の関係を指定して制約を生成します．
	 * @param r 変数間の関係
	 * @param vs 変数
	 * @return 生成された制約
	 */
	public Constraint createConstraint(final Relation r, final Variable ... vs) {
		for(Variable v: vs) {
			if(v.owner() != this) return null;
		}
		Constraint c;
		if(vs.length == 1) c = new Constraint1(r, vs[0]);
		else if(vs.length == 2) c = new Constraint2(r, vs[0], vs[1]);
		else if(vs.length == 3) c = new Constraint3(r, vs[0], vs[1], vs[2]);
		else c = new ConstraintN(r, vs);
		c.setIndex(cons_.size());
		cons_.add(c);
		for(Variable v: vs) v.connect(c);
		if(c.isFuzzy()) isFuzzy_ = true;
		return c;
	}

	/**
	 * 名前と変数間の関係を指定して制約を生成します．
	 * @param name 名前
	 * @param r 変数間の関係
	 * @param vs 変数
	 * @return 生成された制約
	 */
	public Constraint createConstraint(final String name, final Relation r, final Variable ... vs) {
		final Constraint c = createConstraint(r, vs);
		c.setName(name);
		return c;
	}

	// 改変用メソッド --------

	/**
	 * 制約を削除します．
	 * @param c 削除する制約
	 */
	public void removeConstraint(final Constraint c) {
		final int index = cons_.indexOf(c);
		cons_.remove(c);
		for(int i = index, n = cons_.size(); i < n; ++i) {
			cons_.get(i).setIndex(i);
		}
		for(int i = 0, n = c.size(); i < n; ++i) {
			c.at(i).deconnect(c);
		}
		isFuzzy_ = false;
		for(int i = 0, n = cons_.size(); i < n; ++i) {
			if(cons_.get(i).isFuzzy()) {
				isFuzzy_ = true;
				break;
			}
		}
	}

	/**
	 * 全ての変数を未割り当て状態にします．
	 */
	public void clearAllVariables() {
		for(Variable v: vars_) v.clear();
	}

	/**
	 * 変数の順序を逆にします．
	 * 各変数のインデックスが付け替えられます．
	 */
	public void reverseVariables() {
		Collections.reverse(vars_);
		for(int i = 0, n = vars_.size(); i < n; ++i) vars_.get(i).setIndex(i);
	}

	/**
	 * 変数を指定のコンパレータを使ってソートします．
	 * 各変数のインデックスが付け替えられます．
	 * @param comparator コンパレータ
	 */
	public void sortVariables(final Comparator<Variable> comparator) {
		Collections.sort(vars_, comparator);
		for(int i = 0, n = vars_.size(); i < n; ++i) vars_.get(i).setIndex(i);  // 必須
	}

	// 変数用メソッド --------

	/**
	 * 問題に含まれる変数の個数を返します．
	 * @return 変数の個数
	 */
	public int variableSize() {
		return vars_.size();
	}

	/**
	 * インデックスを指定して変数を返します．
	 * @param index インデックス(0 <= index < getVariableSize())
	 * @return 変数
	 */
	public Variable variableAt(final int index) {
		return vars_.get(index);
	}

	/**
	 * 名前を指定して変数を返します．
	 * @param name
	 * @return 変数
	 */
	public Variable variableOf(final String name) {
		for(int i = 0, n = vars_.size(); i < n; ++i) {
			final Variable v = vars_.get(i);
			if(v.name().equals(name)) return v;
		}
		return null;
	}

	/**
	 * 変数が含まれているかどうかを返します．
	 * @param v 変数
	 * @return 含まれていればtrue，そうでなければfalse
	 */
	public boolean contains(final Variable v) {
		return vars_.contains(v);
	}

	/**
	 * 変数のリストを返します．
	 * 返されたリストの変数は許されません．
	 * @return 変数のリスト
	 */
	public List<Variable> variables() {
		return vars_;
	}

	// 制約用メソッド --------

	/**
	 * 問題に含まれる制約の個数を取得します．
	 * @return 制約の個数
	 */
	public int constraintSize() {
		return cons_.size();
	}

	/**
	 * インデックスを指定して制約を返します．
	 * @param index インデックス(0 <= index < constraintSize())
	 * @return 制約
	 */
	public Constraint constraintAt(final int index) {
		return cons_.get(index);
	}

	/**
	 * 名前を指定して変数を返します．
	 * @param name
	 * @return 変数
	 */
	public Constraint constraintOf(final String name) {
		for(int i = 0, n = cons_.size(); i < n; ++i) {
			final Constraint c = cons_.get(i);
			if(c.name().equals(name)) return c;
		}
		return null;
	}

	/**
	 * 制約が含まれているかどうかを返します．
	 * @param c 制約
	 * @return 含まれていればtrue，そうでなければfalse
	 */
	public boolean contains(final Constraint c) {
		return cons_.contains(c);
	}

	/**
	 * 制約のリストを返します．
	 * 返されたリストの変更は許されません．
	 * @return 制約のリスト
	 */
	public List<Constraint> constraints() {
		return cons_;
	}

	/**
	 * 指定した変数との間に存在する制約を指定したコレクションに取得します．
	 * 制約が存在しないときは空のコレクションを返します．
	 * 2変数の間に複数の制約が存在する場合(n項制約(2 < n)が存在する場合も含む)それらが返り値のコレクションに含まれることとなります．
	 * @param <T> コレクションの型
	 * @param v1 変数1
	 * @param v2 変数2
	 * @param dest 取得する制約を保持するコレクション
	 */
	public <T extends Collection<Constraint>> void constraintsBetween(final Variable v1, final Variable v2, final T dest) {
		dest.clear();
		for(int i = 0, n = v1.size(); i < n; ++i) {
			final Constraint c = v1.at(i);
			if(c.constrains(v2)) dest.add(c);
		}
	}

	/**
	 * ファジィ制約充足問題において最も充足度の低い制約のコレクションを求めます．
	 * @param <T> コレクションの型
	 * @param dest 取得する制約を保持するコレクション
	 * @return 最悪制約充足度
	 */
	public <T extends Collection<Constraint>> double constraintsWithWorstSatisfactionDegree(final T dest) {
		dest.clear();
		double cs = 1.0;
		for(int i = 0, n = cons_.size(); i < n; ++i) {
			final Constraint c = cons_.get(i);
			final double s = c.satisfactionDegree();
			if(s < cs) {
				cs = s;
				dest.clear();
				dest.add(c);
			} else if(s - cs < Double.MIN_VALUE * 10) {
				dest.add(c);
			}
		}
		return cs;
	}

	// 状態取得メソッド --------

	/**
	 * ファジィ制約充足問題が含む制約について最悪な充足度を返します．
	 * もし，変数に値が未割り当てであるなどのために充足度が求められないときは-1を返します．
	 * @return 最悪制約充足度
	 */
	public double worstSatisfactionDegree() {
		double cs = 1.0;
		for(int i = 0, n = cons_.size(); i < n; ++i) {
			final double s = cons_.get(i).satisfactionDegree();
			if(s == Constraint.UNDEFINED) return Constraint.UNDEFINED;
			if(s < cs) cs = s;
		}
		return cs;
	}

	/**
	 * ファジイ制約の充足度の平均を求めます．
	 * @return 平均充足度
	 */
	public double averageSatisfactionDegree() {
		double ave = 0.0;
		for(int i = 0, n = cons_.size(); i < n; ++i) {
			ave += cons_.get(i).satisfactionDegree();
		}
		ave = ave / cons_.size();
		return ave;
	}

	/**
	 * 問題に含まれる値の割り当てられていない変数の個数を返します．
	 * @return 値の割り当てられていない変数の個数
	 */
	public int emptyVariableSize() {
		int num = 0;
		for(int i = 0, n = vars_.size(); i < n; ++i) {
			if(vars_.get(i).isEmpty()) num++;
		}
		return num;
	}

	/**
	 * 制約密度(制約数/変数数)を求めます．
	 * @return 制約密度
	 */
	public double constraintDensity() {
		return constraintSize() / (double)variableSize();
	}

	/**
	 * 制約充足問題にドメインの空な変数が存在するかどうかを返します．
	 * @return 存在するならtrue
	 */
	public boolean hasEmptyDomain() {
		for(int i = 0, n = vars_.size(); i < n; ++i) {
			if(vars_.get(i).domain().size() == 0) return true;
		}
		return false;
	}

	/**
	 * ファジィ制約充足問題であるか，すなわちファジィ制約を含んでいるかどうかを返します．
	 * @return ファジィ制約充足問題であればtrue
	 */
	public boolean isFuzzy() {
		return isFuzzy_;
	}

}

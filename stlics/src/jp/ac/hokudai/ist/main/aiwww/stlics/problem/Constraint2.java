package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.Collection;

/**
 * 二項制約を表すクラスです．
 * Problemによって生成されるため，直接コンストラクタを呼び出すことはありません．
 * @author Takuto Yanagida
 * @version 2012/11/30
 */
public class Constraint2 extends Constraint {

	private final Variable var1_;
	private final Variable var2_;

	// 変数間の関係を指定して二項制約を生成するコンストラクタ．Problemからのみ呼び出される．
	Constraint2(final Relation r, final Variable v1, final Variable v2) {
		super(r);
		var1_ = v1;
		var2_ = v2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return 2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Variable at(final int index) {
		if(index == 0) return var1_;
		if(index == 1) return var2_;
		throw new IndexOutOfBoundsException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean constrains(final Variable v) {
		return var1_ == v || var2_ == v;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int indexOf(final Variable v) {
		if(v == var1_) return 0;
		if(v == var2_) return 1;
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyVariableSize() {
		int sum = 0;
		if(var1_.isEmpty()) ++sum;
		if(var2_.isEmpty()) ++sum;
		return sum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefined() {
		return !var1_.isEmpty() && !var2_.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int isSatisfied() {
		if(var1_.isEmpty() || var2_.isEmpty()) return UNDEFINED;
		return crispRelation().isSatisfied(var1_.value(), var2_.value()) ? 1 : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double satisfactionDegree() {
		if(var1_.isEmpty() || var2_.isEmpty()) return UNDEFINED;
		return fuzzyRelation().satisfactionDegree(var1_.value(), var2_.value());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Constraint> neighbors(final Collection<Constraint> dest) {
		for(int i = 0, n = var1_.size(); i < n; ++i) dest.add(var1_.at(i));
		for(int i = 0, n = var2_.size(); i < n; ++i) dest.add(var2_.at(i));
		dest.remove(this);
		return dest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double highestConsistencyDegree() {
		final double sd = satisfactionDegree();
		if(sd != UNDEFINED) return sd;
		double cd = 0.0;
		final int val1 = var1_.value(), val2 = var2_.value();
		final Domain d1 = var1_.domain(), d2 = var2_.domain();
		final int n1 = d1.size(), n2 = d2.size();

		if(!var1_.isEmpty() && var2_.isEmpty()) {
			for(int i = 0; i < n2; ++i) {
				final double s = fuzzyRelation().satisfactionDegree(val1, d2.at(i));
				if(s > cd) cd = s;
				if(cd == 1.0) break;
			}
		} else if(var1_.isEmpty() && !var2_.isEmpty()) {
			for(int i = 0; i < n1; ++i) {
				final double s = fuzzyRelation().satisfactionDegree(d1.at(i), val2);
				if(s > cd) cd = s;
				if(cd == 1.0) break;
			}
		} else {
			for(int i = 0; i < n1; ++i) {
				for(int j = 0; j < n2; ++j) {
					final double s = fuzzyRelation().satisfactionDegree(d1.at(i), d2.at(j));
					if(s > cd) cd = s;
					if(cd == 1.0) break;
				}
			}
		}
		return cd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double lowestConsistencyDegree() {
		final double sd = satisfactionDegree();
		if(sd != UNDEFINED) return sd;
		double cd = 1.0;
		final int val1 = var1_.value(), val2 = var2_.value();
		final Domain d1 = var1_.domain(), d2 = var2_.domain();
		final int n1 = d1.size(), n2 = d2.size();

		if(!var1_.isEmpty() && var2_.isEmpty()) {
			for(int i = 0; i < n2; ++i) {
				final double s = fuzzyRelation().satisfactionDegree(val1, d2.at(i));
				if(s < cd) cd = s;
				if(cd == 0.0) break;
			}
		} else if(var1_.isEmpty() && !var2_.isEmpty()) {
			for(int i = 0; i < n1; ++i) {
				final double s = fuzzyRelation().satisfactionDegree(d1.at(i), val2);
				if(s < cd) cd = s;
				if(cd == 0.0) break;
			}
		} else {
			for(int i = 0; i < n1; ++i) {
				for(int j = 0; j < n2; ++j) {
					final double s = fuzzyRelation().satisfactionDegree(d1.at(i), d2.at(j));
					if(s < cd) cd = s;
					if(cd == 0.0) break;
				}
			}
		}
		return cd;
	}

}

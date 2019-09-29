package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.Collection;

/**
 * 単項制約を表すクラスです．
 * Problemによって生成されるため，直接コンストラクタを呼び出すことはありません．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class Constraint1 extends Constraint {

	final private Variable var_;

	// 変数間の関係を指定して単項制約を生成するコンストラクタ．Problemからのみ呼び出される．
	Constraint1(final Relation r, final Variable v) {
		super(r);
		var_ = v;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Variable at(final int index) {
		if(index == 0) return var_;
		throw new IndexOutOfBoundsException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean constrains(final Variable v) {
		return v == var_;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int indexOf(final Variable v) {
		return (v == var_) ? 0 : -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyVariableSize() {
		return var_.isEmpty() ? 1 : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefined() {
		return !var_.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int isSatisfied() {
		if(var_.isEmpty()) return UNDEFINED;
		return crispRelation().isSatisfied(var_.value()) ? 1 : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double satisfactionDegree() {
		if(var_.isEmpty()) return UNDEFINED;
		return fuzzyRelation().satisfactionDegree(var_.value());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Constraint> neighbors(final Collection<Constraint> dest) {
		for(int i = 0, n = var_.size(); i < n; ++i) dest.add(var_.at(i));
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
		final Domain d = var_.domain();

		for(int i = 0, n = d.size(); i < n; ++i) {
			final double s = fuzzyRelation().satisfactionDegree(d.at(i));
			if(s > cd) cd = s;
			if(cd == 1.0) break;
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
		final Domain d = var_.domain();

		for(int i = 0, n = d.size(); i < n; ++i) {
			final double s = fuzzyRelation().satisfactionDegree(d.at(i));
			if(s < cd) cd = s;
			if(cd == 0.0) break;
		}
		return cd;
	}

}

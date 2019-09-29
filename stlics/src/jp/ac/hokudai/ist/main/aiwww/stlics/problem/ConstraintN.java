package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.Collection;

/**
 * n項制約を表すクラスです．
 * Problemによって生成されるため，直接コンストラクタを呼び出すことはありません．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class ConstraintN extends Constraint {

	private final Variable[] vars_;
	private final int[] vals_;  // 再利用用

	// 変数間の関係を指定してn項制約を生成するコンストラクタ．Problemからのみ呼び出される．
	ConstraintN(final Relation r, final Variable... vs) {
		super(r);
		vars_ = vs.clone();
		vals_ = new int[vars_.length];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return vars_.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Variable at(final int index) {
		return vars_[index];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean constrains(final Variable v) {
		for(int i = 0; i < vars_.length; ++i) {
			if(v == vars_[i]) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int indexOf(final Variable v) {
		for(int i = 0; i < vars_.length; ++i) {
			if(v == vars_[i]) return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int emptyVariableSize() {
		int sum = 0;
		for(int i = 0; i < vars_.length; ++i) {
			if(vars_[i].isEmpty()) ++sum;
		}
		return sum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDefined() {
		for(int i = 0; i < vars_.length; ++i) {
			if(vars_[i].isEmpty()) return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int isSatisfied() {
		for(int i = 0; i < vars_.length; ++i) {
			if(vars_[i].isEmpty()) return -1;
			vals_[i] = vars_[i].value();
		}
		return crispRelation().isSatisfied(vals_) ? 1 : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double satisfactionDegree() {
		for(int i = 0; i < vars_.length; ++i) {
			final Variable v = vars_[i];
			if(v.isEmpty()) return UNDEFINED;
			vals_[i] = v.value();
		}
		return fuzzyRelation().satisfactionDegree(vals_);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Constraint> neighbors(final Collection<Constraint> dest) {
		for(int i = 0; i < vars_.length; ++i) {
			final Variable v = vars_[i];
			for(int j = 0, m = v.size(); j < m; ++j) dest.add(v.at(j));
		}
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
		final int[] emptyIndices = new int[emptyVariableSize()];
		int c = 0;
		for(int i = 0; i < vars_.length; ++i) {
			if(vars_[i].isEmpty()) {
				emptyIndices[c++] = i;
			} else {
				vals_[i] = vars_[i].value();
			}
		}
		cd = checkHCD(emptyIndices, 0, cd);
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
		final int[] emptyIndices = new int[emptyVariableSize()];
		int c = 0;
		for(int i = 0; i < vars_.length; ++i) {
			if(vars_[i].isEmpty()) {
				emptyIndices[c++] = i;
			} else {
				vals_[i] = vars_[i].value();
			}
		}
		cd = checkLCD(emptyIndices, 0, cd);
		return cd;
	}

	private double checkHCD(final int[] emptyIndices, final int currentStep, double cd) {
		final int index = emptyIndices[currentStep];
		final Domain d = vars_[index].domain();
		if(currentStep == emptyIndices.length - 1) {
			for(int i = 0, n = d.size(); i < n; ++i) {
				vals_[index] = d.at(i);
				final double s = fuzzyRelation().satisfactionDegree(vals_);
				if(s > cd) cd = s;
				if(cd == 1.0) break;
			}
		} else {
			for(int i = 0, n = d.size(); i < n; ++i) {
				vals_[index] = d.at(i);
				cd = checkLCD(emptyIndices, currentStep + 1, cd);
			}
		}
		return cd;
	}

	private double checkLCD(final int[] emptyIndices, final int currentStep, double cd) {
		final int index = emptyIndices[currentStep];
		final Domain d = vars_[index].domain();
		if(currentStep == emptyIndices.length - 1) {
			for(int i = 0, n = d.size(); i < n; ++i) {
				vals_[index] = d.at(i);
				final double s = fuzzyRelation().satisfactionDegree(vals_);
				if(s < cd) cd = s;
				if(cd == 0.0) break;
			}
		} else {
			for(int i = 0, n = d.size(); i < n; ++i) {
				vals_[index] = d.at(i);
				cd = checkLCD(emptyIndices, currentStep + 1, cd);
			}
		}
		return cd;
	}

}

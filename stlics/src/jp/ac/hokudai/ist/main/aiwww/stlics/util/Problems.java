package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import java.util.*;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.*;

/**
 * 制約充足問題に対するユーティリティ・クラスです．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public class Problems {

	static private class CrispFuzzyProblem extends CrispProblem {

		public Variable createVariable(final Variable original) {
			final Variable v = new ImaginaryVariable(original);
			addVariable(v);
			return v;
		}

	}

	static private class CrispFuzzyRelation extends CrispRelation {

		private final double th_;
		private final FuzzyRelation fr_;

		public CrispFuzzyRelation(final FuzzyRelation fr, final double th) {
			fr_ = fr;
			th_ = th;
		}
		
		@Override
		public boolean isSatisfied(final int value) {
			return fr_.satisfactionDegree(value) >= th_;
		}

		@Override
		public boolean isSatisfied(final int value1, final int value2) {
			return fr_.satisfactionDegree(value1, value2) >= th_;
		}

		@Override
		public boolean isSatisfied(final int value1, final int value2, final int value3) {
			return fr_.satisfactionDegree(value1, value2, value3) >= th_;
		}

		@Override
		public boolean isSatisfied(final int ... vs) {
			return fr_.satisfactionDegree(vs) >= th_;
		}

	}

	static private class ImaginaryVariable extends Variable {

		private final Variable original_;
		
		public ImaginaryVariable(final Variable original) {
			super(original.owner(), original.domain());
			original_ = original;
			setName(original.name());
			assign(original.value());
		}

		@Override
		final public void assign(final int value) {
			original_.assign(value);
		}

		@Override
		public Domain domain() {
			return original_.domain();
		}

		@Override
		public void setDomain(final Domain dom) {
			original_.setDomain(dom);
		}

		@Override
		public int value() {
			return original_.value();
		}

	}

	static private void averagePathLength(final Problem p, final Variable v, final int[] length, final int baseLength, final Set<Variable> vo) {
		final List<Variable> vn = new ArrayList<>();
		for(int j = 0; j < v.size(); ++j) {
			final Constraint c = v.at(j);
			for(int i = 0; i < c.size(); ++i) {
				final Variable vi = c.at(i);
				if(length[vi.index()] == Integer.MAX_VALUE) {
					vn.add(vi);
					length[vi.index()] = baseLength + 1;
				}
			}
		}
		vo.addAll(vn);
		for(final Variable vi: vn) {
			averagePathLength(p, vi, length, baseLength + 1, vo);
		}
	}

	static private void printStructure(final Variable v, final int level, final Set<Variable> alreadyPrinted) {
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; ++i) {
			sb.append(" ");
		}
		System.out.println(sb.toString() + v);
		alreadyPrinted.add(v);
		for(int j = 0; j < v.size(); ++j) {
			final Constraint c = v.at(j);
			if(c.size() != 2) continue;
			for(int i = 0; i < c.size(); ++i) {
				final Variable cv = c.at(i);
				if(cv != v && !alreadyPrinted.contains(cv)) {
					printStructure(cv, level + 1, alreadyPrinted);
				}
			}
		}
	}

	/**
	 * 指定された変数の平均パス長を求めます．
	 * @param p
	 * @param v
	 * @return 平均パス長
	 */
	static public double averagePathLength(final Problem p, final Variable v) {
		final int[] length = new int[p.variableSize()];
		Arrays.fill(length, Integer.MAX_VALUE);
		final Set<Variable> vs = new HashSet<>();
		vs.add(v);
		length[v.index()] = 0;
		averagePathLength(p, v, length, 0, vs);

		int connectedSize = 0, sum = 0;
		for(int i = 0; i < length.length; ++i) {
			if(length[i] != Integer.MAX_VALUE && i != v.index()) {
				++connectedSize;
				sum += length[i];
			}
		}
		if(connectedSize == 0) return 0;
		return sum / (double)connectedSize;
	}

	/**
	 * 平均パス長を求めます．
	 * @param p
	 * @return 平均パス長
	 */
	static public double[] averagePathLengths(final Problem p) {
		final double[] ls = new double[p.variableSize()];
		for(Variable v: p.variables()) {
			ls[v.index()] = averagePathLength(p, v);
		}
		return ls;
	}

	/**
	 * すべてのドメインを含む配列を返します．
	 * @param p 制約充足問題
	 * @return ドメインの配列
	 */
	static public Domain[] domains(final Problem p) {
		final Domain[] ds = new Domain[p.variableSize()];
		for(int i = 0; i < ds.length; ++i) {
			ds[i] = p.variableAt(i).domain();
		}
		return ds;
	}

	/**
	 * すべての単項制約においてとり得る充足度のコレクションを返します．
	 * @param <T> コレクションの型
	 * @param p 制約充足問題
	 * @param degrees コレクション
	 * @return コレクション
	 */
	static public <T extends Collection<Double>> T possibleSatisfactionDegreesOfUnaryConstraints(final Problem p, final T degrees) {
		for(Constraint c: p.constraints()) {
			if(c.size() != 1) continue;
			final Variable v = c.at(0);
			final int orgVal = v.value();  // 値を保存
			for(int i = 0; i < v.domain().size(); ++i) {
				v.assign(v.domain().at(i));
				degrees.add(c.satisfactionDegree());
			}
			v.assign(orgVal);  // 値を復元
		}
		return degrees;
	}

	/**
	 * 指定した変数を中心とした問題の構造を表示します．
	 * @param v 中心とする変数
	 */
	static public void printStructure(final Variable v) {
		final Set<Variable> vs = new HashSet<>();
		printStructure(v, 0, vs);
	}

	/**
	 * すべてのドメインを設定します．
	 * @param p 制約充足問題
	 * @param ds ドメインの配列
	 */
	static public void setDomains(final Problem p, final Domain[] ds) {
		for(int i = 0; i < ds.length; ++i) {
			p.variableAt(i).setDomain(ds[i]);
		}
	}

	/**
	 * ファジィ制約充足問題のクリスプ制約充足問題としてのビューを返します．
	 * 指定したファジィ制約充足問題のリレーションとドメインが再利用されますが，その他の要素は新規に生成された要素となります．
	 * ただし，ビューの変数への割り当て，ドメインの変更は元の問題の変数へ反映されます．
	 * @param p ファジィ制約充足問題
	 * @param threshold 制約充足度の閾値．制約充足度がこの値以上の時，制約は充足していると見なされます．
	 * @return クリスプな制約充足問題
	 */
	static public CrispProblem toViewAsCrispProblem(final Problem p, final double threshold) {
		final CrispFuzzyProblem cp = new CrispFuzzyProblem();
		for(final Variable v: p.variables()) {
			final Variable cv = cp.createVariable(v);
			assert cv.index() == v.index();
		}
		for(final Constraint c: p.constraints()) {
			final Variable[] vs = new Variable[c.size()];
			for(int i = 0; i < vs.length; ++i) vs[i] = cp.variableAt(c.at(i).index());
			Relation r = c.crispRelation();
			if(c.isFuzzy()) {
				r = new CrispFuzzyRelation(c.fuzzyRelation(), threshold);
			}
			cp.createConstraint(r, vs);
		}
		return cp;
	}

	private Problems() {}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.problem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

/**
 * 変数のドメインを表す抽象クラスです．ファクトリ・メソッドも提供します．
 * ドメインは不変（immutable）です．
 * @author Takuto Yanagida
 * @version 2012/11/20
 */
public abstract class Domain {

	static Domain newDomainArbitrary(final Collection<Integer> values) {
		return new DomainArbitrary(values);
	}

	static Domain newDomainArbitrary(final int... values) {
		return new DomainArbitrary(values);
	}

	static Domain newDomainRanged(final int min, final int max) {
		return new DomainRanged(min, max);
	}

	static protected final Random RAND = new Random();

	/**
	 * 指定された値をドメインの要素として含むかどうかを返します．
	 * @param value 調べる値
	 * @return 含むならtrue，そうでなければfalse
	 */
	public abstract boolean contains(final int value);

	/**
	 * 指定された値のインデックスを返します．
	 * 存在しなかったときは-1を返します．
	 * @param value 値
	 * @return インデックス
	 */
	public abstract int indexOf(final int value);

	/**
	 * 消去されている要素も含めたドメインのサイズを返します．
	 * @return サイズ
	 */
	public abstract int size();

	/**
	 * 指定インデックスの値を返します．
	 * 取得した値が消去されている可能性があります．
	 * @param index インデックス
	 * @return 値
	 */
	public abstract int at(final int index);

	/**
	 * 消去されているかどうかに関係なく，任意の値を返します．
	 * @return 値
	 */
	public abstract int random();

}

/**
 * 任意の整数を要素として持つ，変数のドメインを表すクラスです．
 * @author Takuto Yanagida
 * @version 2010/10/19
 */
class DomainArbitrary extends Domain {

	private final int[] values_;

	DomainArbitrary(final Collection<Integer> values) {
		values_ = new int[values.size()];
		int i = 0;
		for(Iterator<Integer> it = values.iterator(); it.hasNext();) {
			values_[i++] = it.next();
		}
	}

	DomainArbitrary(final int ... values) {
		values_ = Arrays.copyOf(values, values.length);
	}

	@Override
	public boolean contains(final int value) {
		for(int v: values_) {
			if(v == value) return true;
		}
		return false;
	}

	@Override
	public int indexOf(final int value) {
		for(int i = 0; i < values_.length; ++i) {
			if(values_[i] == value) return i;
		}
		return -1;
	}

	@Override
	public int size() {
		return values_.length;
	}

	@Override
	public int at(final int index) {
		return values_[index];
	}

	@Override
	public int random() {
		return values_[RAND.nextInt(values_.length)];
	}

}

/**
 * 連続する整数を要素として持つ，変数のドメインを表すクラスです．
 * @author Takuto Yanagida
 * @version 2010/10/19
 */
class DomainRanged extends Domain {

	private final int min_;
	private final int max_;

	DomainRanged(final int min, final int max) {
		min_ = min;
		max_ = max;
	}

	@Override
	public boolean contains(final int value) {
		return min_ <= value && value <= max_;
	}

	@Override
	public int indexOf(final int value) {
		if(!contains(value)) return -1;
		return value - min_;
	}

	@Override
	public int size() {
		return max_ - min_ + 1;
	}

	@Override
	public int at(final int index) {
		return min_ + index;
	}

	@Override
	public int random() {
		return min_ + RAND.nextInt(size());
	}

}

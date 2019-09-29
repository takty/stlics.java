package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import java.util.Arrays;

/**
 * ソルバの動作がループしていることを検出するクラスです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public class LoopDetector {

	transient private int[] indices_;
	transient private int[] values_;
	transient private int cur_;
	private final int loopLength_;
	private final int iterationCount_;

	public LoopDetector() {
		this(30, 3);
	}

	public LoopDetector(final int loopLength, final int iterationCount) {
		loopLength_ = loopLength;
		iterationCount_ = iterationCount;
		initArrays();
	}

	private void assignToVariable(final int index, final int value) {
		indices_[cur_] = index;
		values_[cur_] = value;
		if(--cur_ == -1) {
			cur_ = indices_.length - 1;
		}
	}

	private int checkLooping() {
		final int[] key = new int[loopLength_];
		final int[] val = new int[loopLength_];
		out: for(int length = 1; length <= loopLength_; ++length) {
			int offset = cur_ + 1;
			for(int i = 0; i < length; ++i) {
				if(i + offset == indices_.length) {
					offset -= indices_.length;
				}
				key[i] = indices_[i + offset];
				val[i] = values_[i + offset];
			}
			int fi = length;
			for(int i = 0; i < iterationCount_ - 1; ++i) {
				offset = cur_ + 1;
				for(int j = 0; j < length; ++j) {
					if(fi + j + offset >= indices_.length) {
						offset -= indices_.length;
					}
					if(indices_[fi + j + offset] != key[j] || values_[fi + j + offset] != val[j] ) {
						continue out;
					}
				}
				fi += length;
			}
			return length;
		}
		return 0;
	}

	private void initArrays() {
		indices_ = new int[loopLength_ * iterationCount_];
		values_ = new int[loopLength_ * iterationCount_];
		Arrays.fill(indices_, -1);
		Arrays.fill(values_, -1);
		cur_ = indices_.length - 1;
	}

	public int checkLoop(final int variableIndex, final int value) {
		assignToVariable(variableIndex, value);
		return checkLooping();
	}

	public void clear() {
		Arrays.fill(indices_, -1);
		Arrays.fill(values_, -1);
	}

	public int iterationCount() {
		return iterationCount_;
	}

	public int loopLength() {
		return loopLength_;
	}

	public int[] values() {
		return values_.clone();
	}

	public int[] variableIndices() {
		return indices_.clone();
	}

}

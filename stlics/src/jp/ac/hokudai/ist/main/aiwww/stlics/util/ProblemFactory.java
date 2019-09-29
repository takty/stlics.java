package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;

/**
 * 制約充足問題を生成するファクトリ・メソッドを提供するインタフェースです．
 * @author Takuto Yanagida
 * @version 2012/11/22
 */
public interface ProblemFactory {

	/**
	 * 制約充足問題を生成します．
	 * @param p 生成する問題を含めるオブジェクト
	 * @return 生成された問題
	 */
	Problem createProblem(final Problem p);

	
	/**
	 * 生成されるファジィ制約充足問題であるか，すなわちファジィ制約を含んでいるかどうかを返します．
	 * @return ファジィ制約充足問題であればtrue
	 */
	boolean isFuzzy();
	
}

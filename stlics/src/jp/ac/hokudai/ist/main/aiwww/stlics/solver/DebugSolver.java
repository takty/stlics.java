package jp.ac.hokudai.ist.main.aiwww.stlics.solver;

import java.io.PrintStream;

/**
 * ソルバのデバッグに関するインタフェースです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public class DebugSolver {

	/**
	 * デバッグ表示をするかどうかのフラグです．
	 * trueのとき，各種情報をDEBUG_OUTに出力します．
	 */
	static public boolean debug = true;

	/**
	 * デバッグ表示の出力先です．
	 * デフォルトでは標準出力となります．
	 */
	static public PrintStream debugStream = System.out;	
	
}

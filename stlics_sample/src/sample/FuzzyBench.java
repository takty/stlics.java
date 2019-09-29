package sample;

import java.io.IOException;
import java.util.List;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy.FuzzyForwardChecking;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy.SRS3;

// 引数↓
// T:\0909_Stlics\ProblemData\test\test05.txt
// T:\0909_Stlics\ProblemData\
// T:\0909_Stlics\ProblemData\test\c05_%s.txt

/**
 * ファジィCSP用のベンチマーク・クラスです．
 * @author Takuto YANAGIDA
 * @version 2009/05/15
 */
public class FuzzyBench {

	private Problem p_;

	public static void main(String args[]) {
		new FuzzyBench().bench(args);
	}

	public void bench(String args[]) {
		p_ = new Problem();
		BenchLoader bl = new BenchLoader(p_, args[0], args[1]);
		try {
			if(bl.readFuzzyProblem() == null) {
				System.out.println("Command Error");
				return;  // エラー
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		long t = System.currentTimeMillis();  // 計時開始
		double d = p_.worstSatisfactionDegree();

		for(int j = 0; j < p_.variableSize(); ++j) {
			p_.variableAt(j).assign(p_.variableAt(j).domain().random());
		}
		new FuzzyForwardChecking(p_).solve();
		
//		System.out.println("Start SRS.");
//		new FuzzyGENET(p_).solve();
//		new SRS3(p_).solve();
//		new Breakout(p_).solve();
//		new ForwardChecking(p_).solve();
//		for(Variable v: p_.getVariables()) v.unassign();
//		new FlexibleLocalChanges(p_).solve();
		
		
		double dfst = p_.worstSatisfactionDegree();
		System.out.println("time: " + (System.currentTimeMillis() - t));  // 計時終了
		System.out.println("改善→"+d + "->" + p_.worstSatisfactionDegree());  // 計時終了
		System.out.println(p_.emptyVariableSize() + " / " + p_.variableSize());

//		if(true) return;

		//-------save--------
		int [] mt = new int[50];
		List<Variable> hoge= p_.variables();
		for (int i=0; i<hoge.size(); i++) {
			Variable val = hoge.get(i);
			mt[i] = val.value();
		}
		//-------------------
		
		//-------shuffle-----
		//SRSの場合は最後の引数がtrue;???
		// 
//		bl.shuffle(args[2], 0, 22, 5, true);
//		d = p_.getWorstSatisfactionDegree();
		//------------------
		
		//System.out.print("制約？");
		//------resolve-------
		System.out.println("Restart");
		Solver s = new SRS3(p_);
		s.setTargetRate(dfst);
		s.solve(); //dfst=さっきの答え
		//new FlexibleLocalChanges(p_).solve();
		//-------------------
		
		//------calcDiff------
		int diff = 0;
		List<Variable> hoge2= p_.variables();
		for (int i=0; i<hoge2.size(); i++) {
			if (Math.abs(hoge2.get(i).value() - mt[i]) > 0.0001) diff++;
		}
		
		System.out.println(System.currentTimeMillis() - t);  // 計時終了
		System.out.println(d + "->" + p_.worstSatisfactionDegree());  // 計時終了
		System.out.println(p_.emptyVariableSize() + " / " + p_.variableSize());
		System.out.println("DIFF="+diff);
		
		System.out.println("stb="+(1-(double)diff/(p_.variableSize())));
		
		//-------------------*/
		
	}

}

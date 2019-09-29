package sample;

import java.io.IOException;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp.Breakout;

//java Bench Samples\sample_0812.txt constraints\constraint_08

/**
 * クリスプCSP用のベンチマーク・クラスです．
 * @author Takuto YANAGIDA
 * @version 2009/05/13
 */
public class CrispBench {

	private CrispProblem p_;

	public static void main(String args[]) {
		new CrispBench().bench(args);
	}

	public void bench(String args[]) {
		p_ = new CrispProblem();
		BenchLoader bl = new BenchLoader(p_, args[0], args[1]);
		try {
			if(bl.readCrispProblem() == null) {
				System.out.println("Command error");
				return;  // エラー
			}
		} catch(IOException e) {
			e.printStackTrace();
			return;  // エラー
		}
		long t = System.currentTimeMillis();  // 計時開始

		Solver s;
//		s = new MaxForwardChecking(p_); 
		s = new Breakout(p_);
//		s = new GENET(p_);
		s.setTargetRate(1.0);
		s.solve();
		
//		AssignmentList saved = new AssignmentList(p_);
//		AssignmentList shuffled = p_.shuffle(args[1]);

		System.out.println("Violation count: " + p_.violatingConstraintSize());
//		for(Variable v: p_.getVariables()) v.unassign();
//		new LocalChanges(p_).solve();

//		System.out.println("Unassigned = " + p_.unassignedVariableSize());
//		System.out.println("Diff = " + saved.differenceSize());

//		shuffled.restore();  // reset

//		new SRS(p_).solve();
//		System.out.println("Diff = " + saved.differenceSize());
		
//		shuffled.restore();  // reset
//		for(Variable v: p_.getVariables()) {
//			if(!v.isAssigned()) v.setValueAtRandom();
//		}
//
//		new Breakout(p_, 0).solve();
//		System.out.println("Diff = " + saved.differenceSize());

		System.out.println(System.currentTimeMillis() - t);  // 計時終了
	}

}
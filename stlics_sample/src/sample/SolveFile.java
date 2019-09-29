package sample;

import java.io.FileNotFoundException;
import java.io.FileReader;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.Solver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp.ForwardChecking;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.filter.AC3;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.filter.NodeConsistency;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.ProblemReader;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.Problems;

/**
 * 任意のファイル出力された問題を解く実装サンプル．
 * @author Takuto YANAGIDA
 * @version 2009/08/18
 */
@SuppressWarnings("unused")
public class SolveFile {

	final static int COUNT = 1;  // 試行回数
//	final static String FILE_NAME = "T:\\0906_Stlics\\N1Queens.txt";
	final static String FILE_NAME = "T:\\0906_Stlics\\FWL.txt";

	static public void main(String args[]) {
		double sum_time = 0.0, sum_degree = 0.0;
		for(int i = 0; i < COUNT; ++i) {
			Problem p = null;
			try {
				p = new Problem();
				new ProblemReader(new FileReader(FILE_NAME), p).read();
			} catch(FileNotFoundException e) {
				e.printStackTrace();
            }
			long t = System.currentTimeMillis();  // 計時開始

			NodeConsistency.apply(p, 0.5);
			Solver s;

//			s = new FlexibleLocalChanges(p);
//			s = new FuzzyBreakout(p);
//			s = new FuzzyForwardChecking(p, 0.8);
//			s = new FuzzyForwardCheckingEx(p, 0.8);
//			s = new FuzzyForwardCheckingExMRV(p, 0.5);
//			s = new FuzzyGENET(p);
//			s = new SRS3(p);
//			s = new SRS3Ex(p);
//			s = new SRS4(p);

			CrispProblem cp = Problems.toViewAsCrispProblem(p, 0.5);
			AC3.apply(cp);
//			s = new Breakout(cp);
//			s = new CrispSRS3(cp);
			s = new ForwardChecking(cp);
//			s = new GENET(cp);
//			s = new LocalChanges(cp, true);
//			s = new LocalChangesEx(cp, true);
//			s = new MaxForwardChecking(cp);

//			s.setIterationCountLimit(1000);
//			s.setRateLimit(0.5);
//			s.setTimeLimit(1500);
			s.solve();

			double ct = System.currentTimeMillis() - t;  // 計時終了
			double cd = p.worstSatisfactionDegree();
			System.out.println("Time: " + ct + "   Degree: " + cd);
			sum_time += ct; sum_degree += cd;
		}
		System.out.println("Average Time: " + (sum_time / COUNT) + "   Average Degree: " + (sum_degree / COUNT));
	}

}

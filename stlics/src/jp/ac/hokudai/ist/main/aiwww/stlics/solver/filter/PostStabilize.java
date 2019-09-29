package jp.ac.hokudai.ist.main.aiwww.stlics.solver.filter;

import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.Assignment;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * 安定化用後フィルタのクラスです．
 * @author Takuto Yanagida
 * @version 2010/05/07
 */
public class PostStabilize {

	static public boolean apply(final Problem p, final AssignmentList original) {
		System.out.println("start PostStabilize");
		boolean stabilized;
		int count = 0;
		do {
			System.out.println("PostStabilize: count " + count++);
			stabilized = false;
			double C_min = p.worstSatisfactionDegree();
			for(int i = 0; i < p.variableSize(); ++i) {
				Variable v = p.variableAt(i);
				int org = v.value();
				Assignment a = original.get(i);
				if(org == a.value()) continue;
				a.apply();  // 元々を割り当ててみる
				if(p.worstSatisfactionDegree() >= C_min) {
					stabilized = true;
				} else {
					v.assign(org);  // 復元
				}
			}
		} while(stabilized);
		System.out.println("finish PostStabilize");
		return true;
	}

}

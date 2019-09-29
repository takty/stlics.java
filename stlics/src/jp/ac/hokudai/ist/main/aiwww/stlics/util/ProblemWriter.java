package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.*;

/**
 * 制約充足問題のライター・クラスです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public class ProblemWriter {

	static private final String VARIABLES_SECTION = "[variables]";
	static private final String DOMAINS_SECTION = "[domains]";
	static private final String CONSTRAINTS_SECTION = "[constraints]";
	static private final String RELATIONS_SECTION = "[relations]";

	static private final String VARIABLE_PREFIX = "v";
	static private final String CONSTRAINT_PREFIX = "c";

	static private final String KEY_DELI = " = ";
	static private final String VAL_DELI = ", ";

	transient private final PrintWriter writer_;
	transient private final Problem problem_;

	/**
	 * 制約充足問題のライターを生成します．
	 * @param writer 書き込み先のライター
	 * @param problem 書き込む制約充足問題
	 */
	public ProblemWriter(final Writer writer, final Problem problem) {
		writer_ = new PrintWriter(writer);
		problem_ = problem;
	}

	private void writeVariables() {
        writer_.println(VARIABLES_SECTION);
    	for(final Variable v: problem_.variables()) {
    		writer_.println(VARIABLE_PREFIX + v.index() + KEY_DELI + (v.isEmpty() ? "UNASSIGNED" : v.value()));
    	}
    }

	private void writeDomains() {
        writer_.println(DOMAINS_SECTION);
    	for(final Variable v: problem_.variables()) {
    		writer_.print(VARIABLE_PREFIX + v.index() + KEY_DELI);  // ドメインだけどvである必要あり
    		final Domain dom = v.domain();
    		for(int i = 0; i < dom.size(); ++i) {
    			writer_.print(dom.at(i));
    			if(i != dom.size() - 1) {
    				writer_.print(VAL_DELI);
    			}
    		}
    		writer_.println();
    	}
    }

	private void writeConstraints() {
        writer_.println(CONSTRAINTS_SECTION);
    	for(final Constraint c: problem_.constraints()) {
    		writer_.print(CONSTRAINT_PREFIX + c.index() + KEY_DELI);
    		for(int i = 0; i < c.size(); ++i) {
    			writer_.print(VARIABLE_PREFIX + c.at(i).index());
    			if(i != c.size() - 1) {
    				writer_.print(VAL_DELI);
    			}
    		}
    		writer_.println();
    	}
    }

	private void writeRelation() {
    	writer_.println(RELATIONS_SECTION);
    	for(final Constraint c: problem_.constraints()) {
    		writer_.print(CONSTRAINT_PREFIX + c.index() + KEY_DELI);
        	switch(c.size()) {
        	case 1:
        		writeRelation1(c);
        		break;
        	case 2:
        		writeRelation2(c);
        		break;
        	case 3:
        		writeRelation3(c);
        		break;
        	default:
        		writeRelationN(c);
        	}
    		writer_.println();
    	}
    }

	private void writeRelation1(final Constraint con) {
		final FuzzyRelation rel = con.fuzzyRelation();
   		final Domain dom = con.at(0).domain();
   		for(int i = 0; i < dom.size(); ++i) {
   			final int val = dom.at(i);
   			writer_.print(rel.satisfactionDegree(val));
   			if(i != dom.size() - 1) {
   				writer_.print(VAL_DELI);
   			}
   		}
	}

	private void writeRelation2(final Constraint con) {
		final FuzzyRelation rel = con.fuzzyRelation();
		final Domain dom0 = con.at(0).domain();
		final Domain dom1 = con.at(1).domain();
		for(int i = 0; i < dom0.size(); ++i) {
			final int val0 = dom0.at(i);
			for(int j = 0; j < dom1.size(); ++j) {
				final int val1 = dom1.at(j);
				writer_.print(rel.satisfactionDegree(val0, val1));
				if(!(i == dom0.size() - 1 && j == dom1.size() - 1)) {
					writer_.print(VAL_DELI);
				}
			}
		}
	}

	private void writeRelation3(final Constraint con) {
		final FuzzyRelation rel = con.fuzzyRelation();
		final Domain dom0 = con.at(0).domain();
		final Domain dom1 = con.at(1).domain();
		final Domain dom2 = con.at(2).domain();
		for(int i = 0; i < dom0.size(); ++i) {
			final int val0 = dom0.at(i);
			for(int j = 0; j < dom1.size(); ++j) {
				final int val1 = dom1.at(j);
				for(int k = 0; k < dom2.size(); ++k) {
					final int val2 = dom2.at(k);
					writer_.print(rel.satisfactionDegree(val0, val1, val2));
					if(!(i == dom0.size() - 1 && j == dom1.size() - 1 && k == dom2.size() - 1)) {
						writer_.print(VAL_DELI);
					}
				}
			}
		}
	}

	private void writeRelationN(final Constraint con) {
		final FuzzyRelation rel = con.fuzzyRelation();
		final Domain[] doms = new Domain[con.size()];
		for(int i = 0; i < doms.length; ++i) doms[i] = con.at(i).domain();
		final int[] idxs = new int[doms.length];
		Arrays.fill(idxs, 0);
		final int[] vals = new int[doms.length];
		int comb = 1;
		for(int i = 0; i < doms.length; ++i) comb *= doms[i].size();
		for(int i = 0; i < comb; ++i) {
			for(int j = 0; j < doms.length; ++j) {
				vals[j] = doms[j].at(idxs[j]);
			}
			writer_.print(rel.satisfactionDegree(vals));
			if(i != comb - 1) writer_.print(VAL_DELI);
			
			// 繰り上げ
			for(int j = idxs.length - 1; j >= 0; --j) {
				idxs[j]++;
				if(idxs[j] < doms[j].size()) break;
				idxs[j] = 0;
			}
		}
	}

	/**
	 * 制約充足問題をテキストとして書き込みます．
	 * ファイルのフォーマットは将来変更される可能性があります．
	 */
	public void write() {
		writeVariables();
		writer_.println();
		writeDomains();
		writer_.println();
		writeConstraints();
		writer_.println();
		writeRelation();
		writer_.close();
	}

}

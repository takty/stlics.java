package jp.ac.hokudai.ist.main.aiwww.stlics.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.*;

/**
 * 制約充足問題に対するユーティリティ・クラスです．
 * @author Takuto YANAGIDA
 * @version 2012/11/20
 */
public class ProblemReader {

	static private final String VARIABLES_SECTION = "[variables]";
	static private final String DOMAINS_SECTION = "[domains]";
	static private final String CONSTRAINTS_SECTION = "[constraints]";
	static private final String RELATIONS_SECTION = "[relations]";

	transient private final BufferedReader reader_;
	transient private final Problem problem_;

	/**
	 * 制約充足問題のリーダーを生成します．
	 * @param reader 読み込むリーダー
	 * @param problem 読み込み先の制約充足問題
	 */
	public ProblemReader(final Reader reader, final Problem problem) {
		reader_ = new BufferedReader(reader);
		problem_ = problem;
	}

	/**
	 * テキストとして書かれた制約充足問題を読み込みます．
	 * ファイルのフォーマットは将来変更される可能性があります．
	 * @return 成功したらtrue，さもなくばfalse
	 */
	public boolean read() {
		int mode = 0;
		final Map<String, Integer> vi2val = new HashMap<>();
		final Map<String, Domain> vi2dom = new HashMap<>();
		final Map<String, String[]> ci2vs = new HashMap<>();
		final Map<String, double[]> ci2elms = new HashMap<>();

		try {
			String line;
			while((line = reader_.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0 || line.charAt(0) == ';') continue;
				if(line.charAt(0) == '[') {
					switch (line) {
					case VARIABLES_SECTION:   mode = 1; break;
					case DOMAINS_SECTION:     mode = 2; break;
					case CONSTRAINTS_SECTION: mode = 3; break;
					case RELATIONS_SECTION:   mode = 4; break;
					}
					continue;
				}
				final String kv[] = line.split("\\s*=\\s*");
				final String key = kv[0];
				final String val = kv[1].replaceAll("[{}]", "");
				final String vs[] = val.replaceAll("\\(|\\)", " ").split("\\s*[,\\s]\\s*");
				switch(mode) {
				case 1:  // variables
				{
					int va = Integer.MIN_VALUE;  // Variable.UNASSIGNED;
					if(!"UNASSIGNED".equals(val)) va = Integer.valueOf(val);
					vi2val.put(key, va);
					break;
				}
				case 2:  // domains
				{
					final List<Integer> vals = new ArrayList<>();
					for(String s: vs) vals.add(Integer.valueOf(s));
					final Domain d = problem_.createDomain(vals);
					vi2dom.put(key, d);
					break;
				}
				case 3:  // constraints
					ci2vs.put(key, vs);
					break;
				case 4:  // relations
					final double[] elms = new double[vs.length];
					for(int i = 0; i < elms.length; ++i) {
						elms[i] = Double.valueOf(vs[i]);
					}
					ci2elms.put(key, elms);
					break;
				}
			}
		} catch(NumberFormatException | IOException e) {
			e.printStackTrace();
			return false;
		}
		// make variable using vi2val and vi2dom
		final Map<String, Variable> vi2v = new HashMap<>();
		final String[] keys = new String[vi2val.size()];
		for(String key: vi2val.keySet()) {
			keys[Integer.valueOf(key.substring(1))] = key;
		}
		for(final String key: keys) {
			final int val = vi2val.get(key);
			final Domain d = vi2dom.get(key);
			final Variable v = null;
			if(val == Integer.MIN_VALUE) problem_.createVariable(d);
			else problem_.createVariable(d, val);
			vi2v.put(key, v);
		}

		// make constraints
		final String[] keys2 = new String[ci2elms.size()];
		for(final String key: ci2elms.keySet()) {
			keys2[Integer.valueOf(key.substring(1))] = key;
		}
		for(final String key: keys2) {
			final String[] vs = ci2vs.get(key);
			final Domain[] doms = new Domain[vs.length];
			for(int i = 0; i < doms.length; ++i) doms[i] = vi2v.get(vs[i]).domain();
			final double[] vals = ci2elms.get(key);
			boolean isCrisp = true;
			for(final double v: vals) {
				if(v != 1.0 && v != 0.0) {
					isCrisp = false;
					break;
				}
			}
			Relation r1;
			if(isCrisp) {
				final boolean[] bs = new boolean[vals.length];
				for(int i = 0; i < bs.length; ++i) bs[i] = (vals[i] == 1.0);
				r1 = new CrispTabledRelation(bs, doms);
			} else {
				r1 = new FuzzyTabledRelation(vals, doms);
			}
			final Variable[] vvs = new Variable[vs.length];
			for(int i = 0; i < vs.length; ++i) vvs[i] = vi2v.get(vs[i]);
			problem_.createConstraint(r1, vvs);
		}
		return true;
	}

}

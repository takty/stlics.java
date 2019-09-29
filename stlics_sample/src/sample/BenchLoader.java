package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispRelation;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.FuzzyRelation;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;

/**
 * 問題をファイルから読み込みます．
 * TODO shuffleにバグがある!!
 * 
 * @author Takuto Yanagida
 * @version 2010/10/28
 */
public class BenchLoader {

	/**
	 * 表によるファジィ二項関係を表すクラスです．
	 * @author Takuto YANAGIDA
	 * @version 2009/05/28
	 */
	static private class TabledFuzzyBinaryRelation extends FuzzyRelation {

		private double[][] table_;

		/**
		 * ファジィな二項関係における充足度を計算します．
		 * 任意の関係を表現するようにオーバーライドします．
		 * @param value1 1つめの変数の値
		 * @param value2 2つめの変数の値
		 * @return 充足度d (0.0 <= d <= 1.0)
		 */
		@Override
		public double satisfactionDegree(int value1, int value2) {
			return table_[value1][value2];
		}

		/**
		 * 制約充足度のテーブルのコピーを返します．
		 * @return 制約充足度のテーブル
		 */
		public double[][] getTable() {
			return table_.clone();
		}

		/**
		 * 2変数間の制約充足度を表すテーブルを設定します．
		 * @param table 制約充足度のテーブル
		 */
		public void setTable(double[][] table) {
			double[][] nt = new double[table.length][];
			for(int i = 0; i < table.length; ++i) {
				nt[i] = new double[table[i].length];
				nt[i] = Arrays.copyOf(table[i], table[i].length);
			}
			table_ = nt;
		}
		
	}

	/**
	 * 表によるクリスプ二項関係を表すクラスです．
	 * @author Takuto YANAGIDA
	 * @version 2009/05/28
	 */
	static private class TabledCrispBinaryRelation extends CrispRelation {

		private int[][] table_;

		/**
		 * クリスプな二項関係において充足しているかどうかを返します．
		 * 任意の関係を表現するようにオーバーライドします．
		 * @param value1 一つ目の変数の値
		 * @param value2 二つ目の変数の値
		 * @return 充足しているかどうか
		 */
		@Override
		public boolean isSatisfied(int value1, int value2) {
			return table_[value1][value2] == 1;
		}

		/**
		 * 制約充足度のテーブルのコピーを返します．
		 * @return 制約充足度のテーブル
		 */
		@SuppressWarnings("unused")
        public int[][] getTable() {
			return table_.clone();
		}

		/**
		 * 2変数間の制約充足度を表すテーブルを設定します．
		 * @param table 制約充足度のテーブル
		 */
		public void setTable(int[][] table) {
			int[][] nt = new int[table.length][];
			for(int i = 0; i < table.length; ++i) {
				nt[i] = new int[table[i].length];
				nt[i] = Arrays.copyOf(table[i], table[i].length);
			}
			table_ = nt;
		}
		
	}

	private final Problem p_;
	private final String filePath_;
	private final String tabledConstraintsFilePathBase_;

	public BenchLoader(Problem p, String filePath, String tabledConstraintsFilePathBase) {
		p_ = p;
		filePath_ = filePath;
		tabledConstraintsFilePathBase_ = tabledConstraintsFilePathBase;
	}

	private void setFuzzyTable(TabledFuzzyBinaryRelation tr, String fileName) {
		double[][] table = new double[50][50];
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(fileName));
			String line;
			int fir = 0;
			while((line = br.readLine()) !=	null) {
				StringTokenizer stk = new StringTokenizer(line, " ");
				int sec = 0;
				while(stk.hasMoreTokens()) {
					String key = stk.nextToken();
					table[fir][sec] = Double.parseDouble(key);
					sec++;
				}
				fir++;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		tr.setTable(table);
	}

	private void setCrispTable(TabledCrispBinaryRelation tr, String fileName) {
		int[][] table = new int[50][50];
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(fileName));
			String line;
			int fir = 0;
			while((line = br.readLine()) !=	null) {
				StringTokenizer stk = new StringTokenizer(line, " ");
				int sec = 0;
				while(stk.hasMoreTokens()) {
					String key = stk.nextToken();
					table[fir][sec] = Integer.parseInt(key);
					sec++;
				}
				fir++;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		tr.setTable(table);
	}

    public Problem readFuzzyProblem() throws IOException {
		Map<String, TabledFuzzyBinaryRelation> trs = new HashMap<String, TabledFuzzyBinaryRelation>();
		BufferedReader br = new BufferedReader(new FileReader(filePath_));

		while(true) {
			String line = br.readLine();
			if(line == null) break;
//			System.out.println(line);  // TODO
			StringTokenizer st = new StringTokenizer(line, " ");
			if(!st.hasMoreTokens()) continue;
			String key = st.nextToken();

			if(key.equals("NewPoint")) {
				String name = st.nextToken();
				int value = Integer.parseInt(st.nextToken());
				ArrayList<Integer> es = new ArrayList<Integer>();
				while(st.hasMoreTokens()) es.add(Integer.parseInt(st.nextToken()));
				p_.createVariable(name, p_.createDomain(es), value);
			} else if(key.equals("NewConstraint")) {
				String name = st.nextToken();
				String fn;
				if(st.hasMoreTokens()) {
					fn = tabledConstraintsFilePathBase_ + st.nextToken();
				} else {
//					System.out.println(tabledConstraintsFilePathBase_ + ", " + name);  // TODO
					fn = tabledConstraintsFilePathBase_ + name + ".txt";
				}
				TabledFuzzyBinaryRelation tr = new TabledFuzzyBinaryRelation();
				setFuzzyTable(tr, fn);
				trs.put(name, tr);
			} else if(key.equals("Connect")) {
				String v1Name = st.nextToken();
				String v2Name = st.nextToken();
				String cName = st.nextToken();
				Variable v1 = p_.variableOf(v1Name);
				Variable v2 = p_.variableOf(v2Name);
				p_.createConstraint(cName, trs.get(cName), v1, v2);
			}
		}
		br.close();
		return p_;
	}

    public Problem readCrispProblem() throws IOException {
		Map<String, TabledCrispBinaryRelation> trs = new HashMap<String, TabledCrispBinaryRelation>();
		BufferedReader br = new BufferedReader(new FileReader(filePath_));

		while(true) {
			String line = br.readLine();
			if(line == null) break;
			StringTokenizer st = new StringTokenizer(line, " ");
			if(!st.hasMoreTokens()) continue;
			String key = st.nextToken();

			if(key.equals("NewPoint")) {
				String name = st.nextToken();
				int value = Integer.parseInt(st.nextToken());
				ArrayList<Integer> es = new ArrayList<Integer>();
				while(st.hasMoreTokens()) es.add(Integer.parseInt(st.nextToken()));
				p_.createVariable(name, p_.createDomain(es), value);
			} else if(key.equals("NewConstraint")) {
				String name = st.nextToken();
				String fn;
				if(st.hasMoreTokens()) {
					fn = tabledConstraintsFilePathBase_ + st.nextToken();
				} else {
					fn = tabledConstraintsFilePathBase_ + name + ".txt";
				}
				TabledCrispBinaryRelation tr = new TabledCrispBinaryRelation();
				setCrispTable(tr, fn);
				trs.put(name, tr);
			} else if(key.equals("Connect")) {
				String v1Name = st.nextToken();
				String v2Name = st.nextToken();
				String cName = st.nextToken();
				Variable v1 = p_.variableOf(v1Name);
				Variable v2 = p_.variableOf(v2Name);
				p_.createConstraint(cName, trs.get(cName), v1, v2);
			}
		}
		br.close();
		return p_;
	}

	public void shuffle(String parameterizedFilePath, int min, int max, int times, boolean unassign) {
		int c = 0;
		double wcs = p_.worstSatisfactionDegree();

		do {
			Constraint con = p_.constraintAt((int)(Math.random() * p_.constraintSize()));
			TabledFuzzyBinaryRelation tc = (TabledFuzzyBinaryRelation)con.fuzzyRelation();
			double[][] table = tc.getTable();
			int n = (int)(Math.random() * (max - min + 1)) + min;
			String fn = String.format(parameterizedFilePath, n);
			setFuzzyTable(tc, fn);
			double s = con.satisfactionDegree();
			if(s >= wcs) {
				tc.setTable(table);  // 元に戻す
			} else {
				c++;
				System.out.println("問題変更されました -> " + s);
				if(!unassign) {
					for(int i = 0; i < con.size(); ++i) con.at(i).clear();
				}
			}
		} while(c < times);
	}

}

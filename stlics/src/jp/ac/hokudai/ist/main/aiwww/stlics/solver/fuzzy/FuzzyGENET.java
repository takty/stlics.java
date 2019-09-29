package jp.ac.hokudai.ist.main.aiwww.stlics.solver.fuzzy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Problem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractFuzzySolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.StochasticSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * Fuzzy GENETを実装したクラスです．
 * CSPとFCSP(ただしBinary (F)CSPのみ)に対応します．
 * @author Takuto Yanagida
 * @version 2010/11/17
 */
public class FuzzyGENET extends AbstractFuzzySolver implements StochasticSolver {

	private final static Random r_ = new Random();

	static private class Cluster {

		static private final List<Integer> maxNeurons_ = new ArrayList<>();

		private final Variable variable_;
		private final Neuron[] neurons_;
		private int index_;

		public Cluster(Variable v) {
			variable_ = v;
			Domain d = v.domain();
			neurons_ = new Neuron[d.size()];
			for(int i = 0; i < neurons_.length; ++i) {
				neurons_[i] = new Neuron(d.at(i));
			}
			setActivity(r_.nextInt(neurons_.length));
		}

		private void setActivity(int index) {
			for(Neuron n: neurons_) n.isActive_ = false;
			neurons_[index].isActive_ = true;
			index_ = index;
		}

		public void applyToVariable() {
			variable_.assign(neurons_[index_].value_);
		}

		public Neuron get(int index) {
			return neurons_[index];
		}

		// 入力が最大のノードをONにする	
		public boolean setActivityMaximumInput() {
			double max = Double.NEGATIVE_INFINITY;
			maxNeurons_.clear();
			boolean alreadyOn = false;
			for(int i = 0; i < neurons_.length; ++i) {
				double input = neurons_[i].getInput();
				if(max <= input) {
					if(max < input) {
						max = input;
						maxNeurons_.clear();
						alreadyOn = false;
					}
					maxNeurons_.add(i);
					if(index_ == i) alreadyOn = true;
				}
			}
			if(alreadyOn || maxNeurons_.isEmpty()) return false;
			setActivity(maxNeurons_.get(r_.nextInt(maxNeurons_.size())));
			return true;
		}

		public int size() {
			return neurons_.length;
		}
		
	}

	static private class Connection {

		private final Neuron first_, second_;
		double weight_;  // 直接参照(read)を許可

		// ニューロンの順序は制約が持つ変数の順序と同一である必要あり
		@SuppressWarnings("LeakingThisInConstructor")
		public Connection(double satisfactionDegree, Neuron first, Neuron second) {
			first_ = first;
			first_.addConnection(this);
			second_ = second;
			second_.addConnection(this);
			weight_ = satisfactionDegree - 1.0;
		}

		// ニューロンの順序は制約が持つ変数の順序と同一である必要あり
		@SuppressWarnings("LeakingThisInConstructor")
		public Connection(double satisfactionDegree, Neuron first) {
			first_ = first;
			first_.addConnection(this);
			second_ = null;
			weight_ = satisfactionDegree - 1.0;
		}

		public Neuron getNeuron(Neuron self) {
			if(self == first_) return second_;
			if(self == second_) return first_;
			return null;
		}

		public void refreshWeight(Constraint c) {
			if(!first_.isActive_ || (second_ != null && !second_.isActive_)) return;
			if(c.size() == 1) {
				Variable v = c.at(0);
				int val = v.value();  // 値を保存
				v.assign(first_.value_);
				weight_ += (c.satisfactionDegree() - 1.0);
				v.assign(val);  // 値を復元
			} else {
				Variable v1 = c.at(0), v2 = c.at(1);
				int val1 = v1.value(), val2 = v2.value();  // 値を保存
				v1.assign(first_.value_);
				v2.assign(second_.value_);
				weight_ += (c.satisfactionDegree() - 1.0);
				v1.assign(val1); v2.assign(val2);  // 値を復元
			}
		}
	
	}

	static private class Neuron {

		private List<Connection> conTemp_;
		private Connection[] connections_;
		int value_;  // 直接参照(read)を許可
		boolean isActive_ = false;  // 直接参照(read, write)を許可

		public Neuron(int value) {
			value_ = value;
			conTemp_ = new ArrayList<>();
		}

		public void addConnection(Connection c) {
			conTemp_.add(c);
		}
		
		public void lockConnections() {
			connections_ = conTemp_.toArray(new Connection[0]);
			conTemp_ = null;  // もう使わない
		}

		public double getInput() {
			double ret = 0;
			for(Connection c: connections_) {
				Neuron n = c.getNeuron(this);  // nがnullなら単項制約の場合
				ret += c.weight_ * ((n == null || n.isActive_) ? 1 : 0);
			}
			return ret;
		}

	}

	private final Cluster[] clusters_;
	private double worstSatisfactionDegree_ = 1.0;  // オリジナルのアルゴリズムの挙動は1.0で再現される

	private int iterCount_;
	private long endTime_;

	public FuzzyGENET(Problem p) {
		super(p, "Fuzzy GENET");
		clusters_ = new Cluster[pro_.variables().size()];
		if(!createNetwork()) throw new IllegalArgumentException();
	}

	public FuzzyGENET(Problem p, double worstSatisfactionDegree) {
		this(p);
		worstSatisfactionDegree_ = worstSatisfactionDegree;
	}

	private boolean createNetwork() {
		if(debug) System.out.println("Network creation start");
		for(int i = 0; i < clusters_.length; ++i) {
			Variable v = pro_.variables().get(i);
			if(v.domain().size() == 0) return false;
			clusters_[i] = new Cluster(v);
		}
		Constraint[] cs = pro_.constraints().toArray(new Constraint[0]);
		for(Constraint c: cs) {
			if(c.size() == 1) {  // 単項制約の場合
				final Variable v = c.at(0);
				final Cluster cl = clusters_[c.at(0).index()];
				for(int i = 0; i < cl.size(); ++i) {
					final int orgVal = v.value();  // 値を保存
					v.assign(cl.get(i).value_);
					double deg = c.satisfactionDegree();
					if(deg <= worstSatisfactionDegree_) {
						c.solverObject = new Connection(deg, cl.get(i));
					}
					v.assign(orgVal);  // 値を復元
				}
			} else {  // 2項制約の場合
				final Variable v1 = c.at(0), v2 = c.at(1);
				Cluster cl_f = clusters_[c.at(0).index()];
				Cluster cl_s = clusters_[c.at(1).index()];
				for(int i = 0; i < cl_f.size(); ++i) {
					final int orgVal1 = v1.value();  // 値を保存
					v1.assign(cl_f.get(i).value_);
					for(int j = 0; j < cl_s.size(); ++j) {
						final int orgVal2 = v2.value();  // 値を保存
						v2.assign(cl_s.get(j).value_);
						double deg = c.satisfactionDegree();
						if(deg <= worstSatisfactionDegree_) {
							c.solverObject = new Connection(deg, cl_f.get(i), cl_s.get(j));
						}
						v2.assign(orgVal2);  // 値を復元
					}
					v1.assign(orgVal1);  // 値を復元
				}
			}
		}
		for(Cluster cl: clusters_) {
			for(Neuron n: cl.neurons_) n.lockConnections();
		}
		if(debug) System.out.println("Network creation complete");
		return true;
	}

	private int[] shuffle(int[] is) {
		final Random r = new Random();
		for(int i = is.length; i > 1; --i) {
			int j = r.nextInt(i);
			int temp = is[i - 1];
			is[i - 1] = is[j];
			is[j] = temp;
		}
		return is;
	}

	// 制限に達したかどうかをチェックする．
	private boolean isReachingLimit() {
		return iterLimit_ < iterCount_++ || endTime_ < System.currentTimeMillis();
	}

	@Override
	protected boolean exec() {
		endTime_ = (timeLimit_ == -1) ? Long.MAX_VALUE : (System.currentTimeMillis() + timeLimit_);
		iterCount_ = 0;

		AssignmentList solution = new AssignmentList();
		double wsd = pro_.worstSatisfactionDegree();
		int order[] = new int[clusters_.length];
		for(int i = 0; i < clusters_.length; i++) order[i] = i;

		boolean success = false;
		while(!isReachingLimit()) {
			boolean modified = false;
			for(int i: shuffle(order)) {
				if(clusters_[i].setActivityMaximumInput()) modified = true;  // 各クラスタ内で入力が最大のノードをONにする
			}
			if(!modified) {  // 局所最小解に到達した場合
				for(int i = 0, n = pro_.constraintSize(); i < n; ++i) {
					Constraint c = pro_.constraintAt(i);
					if(c.solverObject != null) {
						((Connection)c.solverObject).refreshWeight(c);
					}
				}
				continue;
			}
			for(Cluster clu: clusters_) clu.applyToVariable();  // 変数に適用
			// これまでよりも良い割り当てだったら保存する
			double d = pro_.worstSatisfactionDegree();
			if(wsd < d) {
				wsd = d;
				solution.set(pro_);
				if(debug) debugStream.println("Worst Satisfaction Degree: " + d);
				if(targetDeg_ == UNSPECIFIED) {  // 充足度指定なし
					success = true;
				} else if(targetDeg_ <= wsd) {  // 充足度指定あり，かつそれを上回った
					success = true;
					break;
				}
			}
		}
		if(success) solution.apply();
		return success;
	}

}

package jp.ac.hokudai.ist.main.aiwww.stlics.solver.crisp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Constraint;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.CrispProblem;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Domain;
import jp.ac.hokudai.ist.main.aiwww.stlics.problem.Variable;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.AbstractCrispSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.solver.trait.StochasticSolver;
import jp.ac.hokudai.ist.main.aiwww.stlics.util.AssignmentList;

/**
 * GENETを実装したクラスです．
 * CSP(ただしBinary CSPのみ)に対応します．
 * 最大CSPとして問題の解を求めます．
 * @author Takuto YANAGIDA
 * @version 2009/09/16
 */
public class GENET extends AbstractCrispSolver implements StochasticSolver {

	static private class Cluster {

		private final static Random r_ = new Random();
		private final Variable variable_;
		private final Neuron[] neurons_;
		private int index_;
		private final List<Integer> maxNeurons_ = new ArrayList<>();

		public Cluster(final Variable v) {
			variable_ = v;
			final Domain d = v.domain();
			neurons_ = new Neuron[d.size()];
			for(int i = 0; i < neurons_.length; ++i) neurons_[i] = new Neuron(d.at(i));
			setActivity(r_.nextInt(neurons_.length));
		}

		private void setActivity(final int index) {
			for(Neuron n: neurons_) n.isActive_ = false;
			neurons_[index].isActive_ = true;
			index_ = index;
		}

		public void applyToVariable() {
			variable_.assign(neurons_[index_].value_);
		}

		public Neuron get(final int index) {
			return neurons_[index];
		}

		// 入力が最大のノードをONにする	
		public boolean setActivityMaximumInput() {
			int max = Integer.MIN_VALUE;
			boolean alreadyOn = false;
			for(int i = 0; i < neurons_.length; ++i) {
				final int input = neurons_[i].getInput();
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
		protected int weight_;  // 直接参照(read)を許可

		// ニューロンの順序は制約が持つ変数の順序と同一である必要あり
		public Connection(final Neuron first, final Neuron second) {
			first_ = first;
			first_.addConnection(this);
			second_ = second;
			if(second_ != null) second_.addConnection(this);
			weight_ = -1;
		}

		public Neuron getNeuron(final Neuron self) {
			if(self == first_) return second_;
			if(self == second_) return first_;
			return null;
		}

		public void refreshWeight() {
			if(!first_.isActive_ || (second_ != null && !second_.isActive_)) return;
			weight_ += -1;
		}
	
	}

	static private class Neuron {

		private List<Connection> conTemp_;
		private Connection[] connections_;
		protected final int value_;  // 直接参照(read)を許可
		protected boolean isActive_ = false;  // 直接参照(read, write)を許可

		public Neuron(final int value) {
			value_ = value;
			conTemp_ = new ArrayList<>();
		}

		public void addConnection(final Connection c) {
			conTemp_.add(c);
		}

		public void lockConnections() {
			connections_ = conTemp_.toArray(new Connection[conTemp_.size()]);
			conTemp_ = null;  // もう使わない
		}

		public int getInput() {
			int ret = 0;
			for(Connection c: connections_) {
				final Neuron n = c.getNeuron(this);  // nがnullなら単項制約の場合
				ret += c.weight_ * ((n == null || n.isActive_) ? 1 : 0);
			}
			return ret;
		}

	}

	private final Cluster[] clusters_;
	private Connection[] connections_;

	private int iterationLimit_ = Integer.MAX_VALUE;
	private double satisfiedRateLimit_ = 1.0;
	private int timeLimit_ = -1;
	
	public GENET(final CrispProblem p) {
		super(p);
		clusters_ = new Cluster[pro_.variables().size()];
	}

	private boolean createNetwork() {
		final List<Connection> cons = new ArrayList<>();

		for(int i = 0; i < clusters_.length; ++i) {
			final Variable v = pro_.variables().get(i);
			if(v.domain().size() == 0) return false;
			clusters_[i] = new Cluster(v);
		}
		final Constraint[] cs = pro_.constraints().toArray(new Constraint[0]);
		for(Constraint c: cs) {
			if(c.size() == 1) {  // 単項制約の場合
				final Variable v = c.at(0);
				final Cluster cl = clusters_[c.at(0).index()];
				for(int i = 0; i < cl.size(); ++i) {
					final int orgVal = v.value();  // 値を保存
					v.assign(cl.get(i).value_);
					if(c.isSatisfied() == 0) {
						cons.add(new Connection(cl.get(i), null));
					}
					v.assign(orgVal);  // 値を復元
				}
			} else {  // 2項制約の場合
				final Variable v1 = c.at(0), v2 = c.at(1);
				final Cluster cl_f = clusters_[c.at(0).index()];
				final Cluster cl_s = clusters_[c.at(1).index()];
				for(int i = 0; i < cl_f.size(); ++i) {
					final int orgVal1 = v1.value();  // 値を保存
					v1.assign(cl_f.get(i).value_);
					for(int j = 0; j < cl_s.size(); ++j) {
						final int orgVal2 = v2.value();  // 値を保存
						v2.assign(cl_s.get(j).value_);
						if(c.isSatisfied() == 0) {
							cons.add(new Connection(cl_f.get(i), cl_s.get(j)));
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
		connections_ = cons.toArray(new Connection[cons.size()]);
		return true;
	}

	private int[] shuffle(int[] is) {
		final Random r = new Random();
		for(int i = is.length; i > 1; --i) {
			final int j = r.nextInt(i);
			final int temp = is[i - 1];
			is[i - 1] = is[j];
			is[j] = temp;
		}
		return is;
	}

	@Override
    protected boolean exec() {
		if(!createNetwork()) return false;
		final long time = System.currentTimeMillis() + timeLimit_;
    
		final AssignmentList candidates = new AssignmentList();
    	double scr = pro_.satisfiedConstraintRate();
    	int order[] = new int[clusters_.length];
    	for(int i = 0; i < clusters_.length; i++) order[i] = i;
    
    	for(int count = 0; count < iterationLimit_; ++count) {
    		if(timeLimit_ != -1 && time < System.currentTimeMillis()) break;  // 制限時間を超えたら失敗(breakしても良い？)
    		
    		boolean modified = false;
    		for(int i: shuffle(order)) {
    			if(clusters_[i].setActivityMaximumInput()) modified = true;  // 各クラスタ内で入力が最大のノードをONにする
    		}
    		if(!modified) {  // 局所最小解に到達した場合
    			for(Connection con: connections_) con.refreshWeight();  // すべてのコネクションの重みを更新する
    		} else {
    			for(Cluster clu: clusters_) clu.applyToVariable();  // 変数に適用
    			final double d = pro_.satisfiedConstraintRate();
    			if(scr < d) {  // これまでよりも良い割り当てだったら保存する
    				scr = d;
    				if(debug) debugStream.println("Satisfied Constraint Rate: " + d);
    				candidates.set(pro_);
    				if(satisfiedRateLimit_ <= scr) {  // 違反率が指定より改善されたら成功
    					candidates.apply();
    					return true;
    				}
    			}
    		}
    	}
    	candidates.apply();  // 失敗はしたがとりあえずこれまでで一番良かった割り当てを適用する．
    	return false;
    }

	@Override
    public String name() {
        return "GENET";
    }

	@Override
    public void setIterationLimit(final int count) {
		iterationLimit_ = (count == -1) ? Integer.MAX_VALUE : count;
	}

	@Override
    public void setTargetRate(final double rate) {
		satisfiedRateLimit_ = (rate == -1) ? 1.0 : rate;
    }

	@Override
    public void setTimeLimit(final int msec) {
		timeLimit_ = msec;
    }

}

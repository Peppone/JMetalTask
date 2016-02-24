package task;

import java.util.ArrayList;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Int;
import jmetal.util.JMException;

public class TaskProblem extends Problem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4349477568414601200L;
	private int taskNum;
	private int serverNum;
	private int serverPerRack;
	private int rackNum;
	private int rackPerPod; // Number of racks in a pod
	private int podNum; // Number of pods
	private int core;
	private double cpuInstructionPerSecond[];
	private double pServerIdle;
	private double pServerPeak;
	private double pTorSwitchPeak;
	private double pTorSwitchIdle;
	private double pAggSwitchIdle; // Peak power of an aggregation switch [W]
	private double pAggSwitchPeak; // Idle aggregation switch power consumption
									// [W]

	private double serverLinkMaximumBandwidth[]; // Single link Server - ToR
													// switch capacity
													// [bits/sec]
	private double rackLinkMaximumBandwidth[]; // Single link ToR - Aggregation
												// switch capacity [bits/sec]

	private ArrayList<Task> task;

	/**
	 * 
	 * @param task
	 * @param server
	 * @param serverPerRack
	 * @param rackPerPod
	 * @param pTorPeak
	 * @param pAggPeak
	 * @param serverLinkMaximumBandwidth
	 * @param rackLinkMaximumBandwidth
	 * @param cpuInstructionPerSecond
	 * @param t
	 */
	public TaskProblem(int task, int server, int serverPerRack, int rackPerPod, double pServerPeak, double pTorPeak,
			double pAggPeak, double/* [] */ serverLinkMaximumBandwidth, double/* [] */ rackLinkMaximumBandwidth,
			double cpuInstructionPerSecond/* [] */, int core, ArrayList<Task> t) {
		// Initialize all internal fields
		this.numberOfObjectives_ = 2;
		this.numberOfConstraints_ = 2;
		this.taskNum = task;
		this.serverNum = server;
		this.serverPerRack = serverPerRack;
		this.rackPerPod = rackPerPod;
		this.rackNum = (serverNum / serverPerRack) + (serverNum % serverPerRack == 0 ? 0 : 1);
		this.podNum = (rackNum / rackPerPod) + (rackNum % rackPerPod == 0 ? 0 : 1);
		this.pServerPeak = pServerPeak;
		this.pServerIdle = 0;
		this.pTorSwitchPeak = pTorPeak;
		this.pTorSwitchIdle = this.pTorSwitchPeak * 0.8;
		this.pAggSwitchPeak = pAggPeak;
		this.pAggSwitchIdle = this.pAggSwitchPeak * 0.8;
		this.task = t;

		// Define solution space
		this.numberOfVariables_ = taskNum;
		this.solutionType_ = new IntSolutionType(this);

		this.upperLimit_ = new double[numberOfVariables_];
		this.lowerLimit_ = new double[numberOfVariables_];
		for (int i = 0; i < numberOfVariables_; ++i) {

			this.upperLimit_[i] = serverNum - 1;

			this.lowerLimit_[i] = 0;
		}
		// this.serverLinkMaximumBandwidth = serverLinkMaximumBandwidth;

		// this.rackLinkMaximumBandwidth = rackLinkMaximumBandwidth;
		this.serverLinkMaximumBandwidth = new double[serverNum];
		this.cpuInstructionPerSecond = new double[serverNum];
		this.rackLinkMaximumBandwidth = new double[rackNum];
		for (int i = 0; i < serverNum; ++i) {
			this.serverLinkMaximumBandwidth[i] = serverLinkMaximumBandwidth;
			this.cpuInstructionPerSecond[i] = cpuInstructionPerSecond;
		}

		for (int i = 0; i < rackNum; ++i) {
			this.rackLinkMaximumBandwidth[i] = rackLinkMaximumBandwidth;
		}
		this.core = core;
		// this.cpuInstructionPerSecond = cpuInstructionPerSecond;
	}

	@Override
	public void evaluate(Solution solution) throws JMException {
		Variable[] variable = solution.getDecisionVariables();
		// Considero tutti i server idle, inizialmente
		
		double bandwidthPerServer[] = new double[serverNum];
		double bandwidthPerRack[] = new double[rackNum];
		double bandwidthPerPod[] = new double[podNum];
		int instructionPerServer[] = new int[serverNum];
		ArrayList<ArrayList<Task>> taskPerServer = new ArrayList<ArrayList<Task>>(serverNum);
		;
		for (int i = 0; i < serverNum; ++i) {
			taskPerServer.add(new ArrayList<Task>());
		}

		int allocatedOnServer[] = new int[serverNum];
		double serverBwConstraint = 0;
		double rackBwConstraint = 0;
		for (int i = 0; i < variable.length; ++i) {
			int server = (int) ((Int) variable[i]).getValue();
			ArrayList<Task> serverTask = taskPerServer.get(server);
			serverTask.add(task.get(i));
			instructionPerServer[server] += (int) (task.get(i).getInstructionNumber());
			allocatedOnServer[server]++;
			// if (allocatedOnServer[server] == false) {
			// // Se un server è attivo, aggiungo la differenza (dato che pIdle
			// // era stata già aggiunta)
			// powerConsumption += pServerPeak - pServerIdle;
			// allocatedOnServer[server] = true;
			// }

			double bw = task.get(i).getBandwidth();
			bandwidthPerServer[server] += bw;
			if (bandwidthPerServer[server] > serverLinkMaximumBandwidth[server]) {
				bw = bandwidthPerServer[server] - serverLinkMaximumBandwidth[server];
				/*
				 * Se prima di aggiungere questa banda il link non era saturato
				 * allora la parte che satura il riempie il link viene aggiunta.
				 * In caso contrario tutta la banda del task verrà aggiunta.
				 */
				bandwidthPerServer[server] = serverLinkMaximumBandwidth[server];
				if (bw != 0)
					serverBwConstraint += bw;
			}

			int rack = server / serverPerRack;
			bandwidthPerRack[rack] += bw;
			if (bandwidthPerRack[rack] > this.rackLinkMaximumBandwidth[rack]) {
				bw = bandwidthPerRack[rack] - rackLinkMaximumBandwidth[rack];
				/*
				 * Stessa cosa è valida per il rack
				 */
				bandwidthPerRack[rack] = this.rackLinkMaximumBandwidth[rack];
				if (bw != 0)
					rackBwConstraint += bw;
			}
			/*
			 * mentre i pod non satuarano mai per ipotesi.
			 */
			int pod = rack / rackPerPod;
			bandwidthPerPod[pod] += bw;

		}
		double executionTimes[] = new double[serverNum];
		for (int i = 0; i < serverNum; ++i) {
			executionTimes[i] = computeMulticoreTime(taskPerServer.get(i), i);
		}
		double makespan = -1;
		for (double d : executionTimes) {
			if (makespan < d) {
				makespan = d;
			}
		}
		double powerConsumption;
		powerConsumption = computeTotalServerConsumption(allocatedOnServer);
		powerConsumption += computeSwitchPowerConsumption(bandwidthPerRack, bandwidthPerPod);
		solution.setObjective(0, makespan);
		solution.setObjective(1, powerConsumption);

		int violatedConstraintNum = 0;
		if (serverBwConstraint > 0)
			violatedConstraintNum++;
		if (rackBwConstraint > 0)
			violatedConstraintNum++;
		solution.setOverallConstraintViolation(rackBwConstraint + serverBwConstraint);
		solution.setNumberOfViolatedConstraint(violatedConstraintNum);

	}

	private double computeSwitchPowerConsumption(double bandwidthPerRack[], double bandwidthPerPod[]) {
		double tor_switch_power_consumption = 0;
		int rack = bandwidthPerRack.length;
		int pod = bandwidthPerPod.length;
		for (int i = 0; i < rack; ++i) {
			if (bandwidthPerRack[i] != 0) {
				tor_switch_power_consumption += pTorSwitchIdle + (pTorSwitchPeak - pTorSwitchIdle)
						* (bandwidthPerRack[i] / (serverPerRack * serverLinkMaximumBandwidth[i]));
			}
		}

		double agg_switch_power_consumption = 0;
		for (int i = 0; i < pod; ++i) {
			if (bandwidthPerPod[i] != 0) {
				/*
				 * La formula per il calcolo della potenza degli AGG Switch che
				 * sfruttano la politica del load balancing è P_COPPIA= 2 *
				 * [(P_PEAK - P_IDLE)* BW/2 + P_IDLE] che corrisponde a P_COPPIA
				 * = (P_PEAK - P_IDLE)* BW + 2 P_IDLE
				 */
				agg_switch_power_consumption += (2 * pAggSwitchIdle + (pAggSwitchPeak - pAggSwitchIdle)
						* (bandwidthPerPod[i] / (rackPerPod * rackLinkMaximumBandwidth[i])));
			}
		}

		return tor_switch_power_consumption + agg_switch_power_consumption;
	}

	private double computeMakespan(int[] instructionPerServer) {
		double makespan = -1;
		for (int i = 0; i < serverNum; ++i) {
			double serverTime = (1.0 * instructionPerServer[i]) / cpuInstructionPerSecond[i];
			if (serverTime > makespan) {
				makespan = serverTime;
			}
		}
		assert (makespan != -1);
		return makespan;
	}
	
	private double computeTotalServerConsumption(int allocationArray[]){
		double powerConsumption = 0;
		for(int i =0;i< allocationArray.length;++i){
			if(allocationArray[i]>=core){
				powerConsumption+=pServerPeak;
			}
			else{
				powerConsumption+=pServerIdle+ (pServerPeak - pServerIdle)*(((double)allocationArray[i])/core);
			}
		}
		return powerConsumption;
	}

	private double computeMulticoreTime(ArrayList<Task> task, int server) {
		int instructionInParallel = 0;
		int multicoreAllocation[] = new int[core];
		if (task.size() < core) {
			for (int i = 0; i < task.size(); ++i) {
				multicoreAllocation[i] = (int) task.get(i).getInstructionNumber();
			}
		} else {
			for (int i = 0; i < core; ++i) {
				multicoreAllocation[i] = (int) task.get(i).getInstructionNumber();
			}
		}
		int next = core;
		int toReplace = 0;
		while (next < task.size()) {
			for (int i = 0; toReplace > 0 && next < task.size(); ++i) {
				if (multicoreAllocation[i] == 0) {
					toReplace--;
					multicoreAllocation[i] = (int) task.get(next).getInstructionNumber();
					next++;
				}

			}
			if (next >= task.size())
				break;
			int minimumInstruction = min(multicoreAllocation);
			for (int i = 0; i < multicoreAllocation.length; ++i) {
				multicoreAllocation[i] -= minimumInstruction;
				if(multicoreAllocation[i]==0)toReplace++;
			}
			instructionInParallel += minimumInstruction;
		}
		instructionInParallel += max(multicoreAllocation);
		return instructionInParallel / cpuInstructionPerSecond[server];
	}

	public double getServerNumber() {
		return this.serverNum;
	}

	public int min(int array[]) {
		int minimum = array[0];
		for (int i : array) {
			if (i < minimum) {
				minimum = i;
			}
		}
		return minimum;
	}

	public int max(int array[]) {
		int maximum = array[0];
		for (int i : array) {
			if (i > maximum) {
				maximum = i;
			}
		}
		return maximum;
	}

}

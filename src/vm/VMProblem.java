package vm;

import java.util.ArrayList;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.Int;
import jmetal.util.JMException;

public class VMProblem extends Problem{
	private static final long serialVersionUID = -4349477568414601200L;
	private int vmNum;
	private int serverNum;
	private int serverPerRack;
	private int rackNum;
	private int rackPerPod; // Number of racks in a pod
	private int podNum; // Number of pods
	private int core[];
	private double memory[];
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

	private ArrayList<VM> vm;

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
	public VMProblem(int vm, int server, int serverPerRack, int rackPerPod, double pServerPeak, double pTorPeak,
			double pAggPeak, double/* [] */ serverLinkMaximumBandwidth, double/* [] */ rackLinkMaximumBandwidth,
			int[] core, double memory[], ArrayList<VM> v) {
		// Initialize all internal fields
		this.vmNum = vm;
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
		this.vm = v;

		// Define solution space
		this.numberOfVariables_ = vmNum;
		this.solutionType_ = new IntSolutionType(this);

		this.upperLimit_ = new double[numberOfVariables_];
		this.lowerLimit_ = new double[numberOfVariables_];
		for (int i = 0; i < numberOfVariables_; ++i) {

			this.upperLimit_[i] = serverNum - 1;

			this.lowerLimit_[i] = 0;
		}
		
		//Define constraints and objective number
		this.numberOfObjectives_ = 1;
		this.numberOfConstraints_ = 3*serverNum+rackNum;
		
		this.serverLinkMaximumBandwidth = new double[serverNum];
		this.core = core;
		this.memory = memory;
		this.rackLinkMaximumBandwidth = new double[rackNum];
		for (int i = 0; i < serverNum; ++i) {
			this.serverLinkMaximumBandwidth[i] = serverLinkMaximumBandwidth;
		}

		for (int i = 0; i < rackNum; ++i) {
			this.rackLinkMaximumBandwidth[i] = rackLinkMaximumBandwidth;
		}
	}

	@Override
	//TODO modify objective computation
	public void evaluate(Solution solution) throws JMException {
		Variable[] variable = solution.getDecisionVariables();
		// Considero tutti i server idle, inizialmente
		
		double bandwidthPerServer[] = new double[serverNum];
		double bandwidthPerRack[] = new double[rackNum];
		double bandwidthPerPod[] = new double[podNum];
		int corePerServer[] = new int[serverNum];
		double memPerServer[]= new double[serverNum];
		ArrayList<ArrayList<VM>> vmPerServer = new ArrayList<ArrayList<VM>>(serverNum);
		;
		for (int i = 0; i < serverNum; ++i) {
			vmPerServer.add(new ArrayList<VM>());
		}

		int allocatedOnServer[] = new int[serverNum];
		double serverBwConstraint = 0;
		double rackBwConstraint = 0;
		int violatedConstraintNum = 0;
		
		for (int i = 0; i < variable.length; ++i) {
			int server = (int) ((Int) variable[i]).getValue();
			ArrayList<VM> serverVm = vmPerServer.get(server);
			serverVm.add(vm.get(i));
			corePerServer[server] += (int) (vm.get(i).getCoreNumber());
			memPerServer[server] += (double) (vm.get(i).getMemory());
			allocatedOnServer[server]++;
			double bw = vm.get(i).getBandwidth();
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
					violatedConstraintNum++;
			}

			int rack = server / serverPerRack;
			bandwidthPerRack[rack] += bw;
			if (bandwidthPerRack[rack] > this.rackLinkMaximumBandwidth[rack]) {
				bw = bandwidthPerRack[rack] - rackLinkMaximumBandwidth[rack];
				/*
				 * Stessa cosa è valida per il rack
				 */
				bandwidthPerRack[rack] = this.rackLinkMaximumBandwidth[rack];
				if (bw != 0){
					rackBwConstraint += bw;
					violatedConstraintNum++;
				}
			}
			/*
			 * mentre i pod non satuarano mai per ipotesi.
			 */
			int pod = rack / rackPerPod;
			bandwidthPerPod[pod] += bw;

		}

		double powerConsumption;
		powerConsumption = computeTotalServerConsumption(allocatedOnServer);
		powerConsumption += computeSwitchPowerConsumption(bandwidthPerRack, bandwidthPerPod);
		solution.setObjective(0, powerConsumption);
		
		int serverCoreConstraint[] = evaluateCoreConstraint(corePerServer,core);
		double serverMemoryConstraint[] = evaluateMemConstraint(memPerServer,memory);
		violatedConstraintNum += serverCoreConstraint[0]+ serverMemoryConstraint[0];
		
		solution.setOverallConstraintViolation(rackBwConstraint + serverBwConstraint+serverCoreConstraint[1]+serverMemoryConstraint[1]);
		solution.setNumberOfViolatedConstraint(violatedConstraintNum);

	}

	private int[] evaluateCoreConstraint(int[] allocatedCore, int[] totalCore ) {
		int result[] =new int[2];
		for (int i=0;i<serverNum;++i){
			if(allocatedCore[i]>totalCore[i]){
				result[0]++;
				result[1]+=allocatedCore[i]-totalCore[i];
			}
		}
		return result;
	}
	
	private double[] evaluateMemConstraint(double[] allocatedMem, double[] totalMem ) {
		double result[] =new double[2];
		for (int i=0;i<serverNum;++i){
			if(allocatedMem[i]>totalMem[i]){
				result[0]++;
				result[1]+=allocatedMem[i]-totalMem[i];
			}
		}
		return result;
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

	
	//TODO: to be updated
	private double computeTotalServerConsumption(int allocationArray[]){
		double powerConsumption = 0;
		for(int i =0;i< allocationArray.length;++i){
			if(allocationArray[i]>=core[i]){
				powerConsumption+=pServerPeak;
			}
			else{
				powerConsumption+=pServerIdle+ (pServerPeak - pServerIdle)*(((double)allocationArray[i])/core[i]);
			}
		}
		return powerConsumption;
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

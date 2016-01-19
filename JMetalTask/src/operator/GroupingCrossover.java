package operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jmetal.core.Operator;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import task.Task;

public class GroupingCrossover extends Operator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double probability;
	private int server;
	private ArrayList<Task> task = null;

	@SuppressWarnings("unchecked")
	public GroupingCrossover(HashMap<String, Object> parameters)
			 {
		super(parameters);
		if (!parameters.containsKey("crossoverProbability"))
			try {
				throw new JMException("Missing crossoverProbability");
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (!parameters.containsKey("serverNumber"))
			try {
				throw new JMException("Missing the number of servers");
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (!parameters.containsKey("taskList"))
			try {
				throw new JMException("Missing the taskList");
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		probability = (double) parameters.get("crossoverProbability");
		server = (int) parameters.get("serverNumber");
		task = (ArrayList<Task>) parameters.get("taskList");

	}

	public Solution[] doCrossover(double probability, Solution parent1,
			Solution parent2) throws JMException {
		Solution[] offSpring = new Solution[2];
		Random r = new Random();
		if (r.nextDouble() >= 0.5) {
			offSpring[0] = new Solution(parent1);
			offSpring[1] = new Solution(parent1);
		} else {
			offSpring[1] = new Solution(parent2);
			offSpring[0] = new Solution(parent2);

		}
		if (PseudoRandom.randDouble() < probability) {
			Variable[] of1 = offSpring[0].getDecisionVariables();
			Variable[] of2 = offSpring[1].getDecisionVariables();
			double[] instruction1 = new double[server];
			double[] instruction2 = new double[server];
			double max1 = -1;
			// double max2 = -1;
			double smax1 = -1;
			// double smax2 = -1;
			double smin1 = -1;
			// double smin2 = -1;
			double min1 = Double.MAX_VALUE;
			// double min2 = Double.MAX_VALUE;

			for (int i = 0; i < offSpring[0].numberOfVariables(); ++i) {
				int currentServer = (int) ((Int) of1[i]).getValue();
				instruction1[currentServer] += task.get(i)
						.getInstructionNumber();
			}
			for (int i = 0; i < offSpring[1].numberOfVariables(); ++i) {
				int currentServer = (int) ((Int) of1[i]).getValue();
				instruction2[currentServer] += task.get(i)
						.getInstructionNumber();
			}

			for (int i = 0; i < instruction1.length; ++i) {
				if (instruction1[i] > max1) {
					max1 = instruction1[i];
					smax1 = i;
				} else {

					if (instruction1[i] == max1) {
						if (r.nextDouble() >= 0.5) {
							max1 = instruction1[i];
							smax1 = i;
						}
					}
				}

				if (instruction1[i] < min1) {
					min1 = instruction1[i];
					smin1 = i;
				} else {

					if (instruction1[i] == min1) {
						if (r.nextDouble() >= 0.5) {
							min1 = instruction1[i];
							smin1 = i;
						}
					}
				}

				// if (instruction2[i] < min2) {
				// min2 = instruction2[i];
				// smin2 = i;
				// } else {
				//
				// if (instruction2[i] == min2) {
				// if (r.nextDouble() >= 0.5) {
				// min2 = instruction2[i];
				// smin2 = i;
				// }
				// }
				// }
				//
				// if (instruction2[i] > max2) {
				// max2 = instruction2[i];
				// smax2 = i;
				// } else {
				//
				// if (instruction2[i] == max2) {
				// if (r.nextDouble() >= 0.5) {
				// max2 = instruction2[i];
				// smax2 = i;
				// }
				// }
				// }
				//
			}
			Variable p1var[] = parent1.getDecisionVariables();
			Variable p2var[] = parent2.getDecisionVariables();
			for (int i = 0; i < of1.length; ++i) {
				if (p1var[i].getValue() - smax1 < 1E-3) {
					of1[i] = new Int(p2var[i]);
				}
				if (p1var[i].getValue() - smin1 < 1E-3) {
					of2[i] = new Int(p2var[i]);
				}
			}
//			// DEBUG
//			System.err.println("p1");
//			for (int i = 0; i < of1.length; ++i) {
//				System.err.print(p1var[i] + " ");
//			}
//			System.err.println();
//
//			System.err.println("p2");
//			for (int i = 0; i < of1.length; ++i) {
//				System.err.print(p2var[i] + " ");
//			}
//			System.err.println();
//
//			System.err.println("o1");
//			for (int i = 0; i < of1.length; ++i) {
//				System.err.print(of1[i] + " ");
//			}
//			System.err.println();
//
//			System.err.println("o2");
//			for (int i = 0; i < of1.length; ++i) {
//				System.err.print(of2[i] + " ");
//			}
//			System.err.println();
//			System.err.println();
//			//DEBUG
		}
		return offSpring;
	} // doCrossover

	/**
	 * Executes the operation
	 * 
	 * @param object
	 *            An object containing an array of two solutions
	 * @param none
	 * @return An object containing an array with the offSprings
	 * @throws JMException
	 */
	public Object execute(Object object) throws JMException {
		Solution [] parents = null;
		try{
		parents = (Solution[]) object;
		}catch(ClassCastException c){
		
		 parents = (Solution [])((Object[])object)[1];
		}
		if (parents.length < 2) {
			Configuration.logger_.severe("GroupingCrossover.execute: operator "
					+ "needs two parents");
			throw new JMException("Exception in GroupingCrossover.execute()");
		} // if

		Solution[] offSpring;
		offSpring = doCrossover(probability, parents[0], parents[1]);

		for (int i = 0; i < offSpring.length; i++) {
			offSpring[i].setCrowdingDistance(0.0);
			offSpring[i].setRank(0);
		} // for
		return offSpring;
	} // execute

}

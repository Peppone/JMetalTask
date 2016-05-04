package vm;

import java.util.ArrayList;
import java.util.HashMap;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.experiments.Experiment;
import jmetal.metaheuristics.ibea.IBEA;
import jmetal.metaheuristics.mocell.MOCell;
import jmetal.metaheuristics.mochc.MOCHC;
import jmetal.metaheuristics.moead.MOEAD;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.metaheuristics.spea2.SPEA2;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.crossover.SinglePointCrossover;
import jmetal.operators.mutation.MyRebalanceMutation;
import jmetal.operators.selection.BinaryTournament;
import jmetal.util.JMException;
import operator.TaskGroupingCrossover;
import task.Task;

public class VMExperiment extends Experiment {

	VMProblem problem;
	ArrayList<VM> vm;
	
	public VMExperiment(VMProblem p, ArrayList<VM> vm){
		super();
		super.experimentName_="VMExperiment";
		this.problem=p;
		this.vm= vm;
	}
	
	@Override
	public void algorithmSettings(String problemName, int problemId,
			Algorithm[] algorithm) throws ClassNotFoundException {
			NSGAII nsga = new NSGAII(problem);
			nsga.setInputParameter("populationSize", 100);
			nsga.setInputParameter("maxEvaluations",1000);
			
			NSGAII nsga2 = new NSGAII(problem);
			nsga2.setInputParameter("populationSize", 100);
			nsga2.setInputParameter("maxEvaluations",1000);
			
			HashMap <String,Object> map = new HashMap<String, Object>(2);
			map.put("probability", 0.5);
			map.put("mutationProbability",0.1);
			map.put("serverNumber",(int)problem.getServerNumber());
			map.put("vmList", vm);
			MyRebalanceMutation mutation=null;
			
			try {
				mutation = new MyRebalanceMutation(map);
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			SinglePointCrossover crossover = new SinglePointCrossover(map);
			map.put("crossoverProbability",0.9);
			TaskGroupingCrossover crossover2 = new TaskGroupingCrossover(map);
			BinaryTournament selection = new BinaryTournament(null);
			map.put("probability",0.9);
			nsga.addOperator("crossover", crossover);
			nsga.addOperator("selection",selection);
			nsga.addOperator("mutation",  mutation);
			
			nsga2.addOperator("crossover", crossover);
			nsga2.addOperator("selection",selection);
			nsga2.addOperator("mutation",  mutation);
			
			MOCell mocell = new MOCell(problem);
			mocell.setInputParameter("populationSize", 100);
			mocell.setInputParameter("maxEvaluations",1000);
			mocell.setInputParameter("archiveSize", 10);
			mocell.addOperator("crossover", crossover);
			mocell.addOperator("selection",selection);
			mocell.addOperator("mutation",  mutation);
			
			MOCell mocell2 = new MOCell(problem);
			mocell2.setInputParameter("populationSize", 100);
			mocell2.setInputParameter("maxEvaluations",1000);
			mocell2.setInputParameter("archiveSize", 10);
			mocell2.addOperator("crossover", crossover2);
			mocell2.addOperator("selection",selection);
			mocell2.addOperator("mutation",  mutation);
			
			MOCHC c = new MOCHC (problem);
			c.setInputParameter("populationSize", 100);
			c.setInputParameter("maxEvaluations",1000);
			c.setInputParameter("archiveSize", 10);
			c.setInputParameter("initialConvergenceCount",100.0);
			c.setInputParameter("preservedPopulation",10.0);
			c.setInputParameter("convergenceValue", 50);
			c.addOperator("crossover", crossover);
			c.addOperator("selection",selection);
			c.addOperator("mutation",  mutation);
			
			SPEA2 d = new SPEA2 (problem);
			d.setInputParameter("populationSize", 100);
			d.setInputParameter("maxEvaluations",1000);
			d.setInputParameter("archiveSize", 10);
			d.addOperator("crossover", crossover);
			d.addOperator("selection",selection);
			d.addOperator("mutation",  mutation);
			
			IBEA ibea = new IBEA (problem);
			ibea .setInputParameter("populationSize", 100);
			ibea .setInputParameter("maxEvaluations",1000);
			ibea .setInputParameter("archiveSize", 10);
			ibea .addOperator("crossover", crossover);
			ibea .addOperator("selection",selection);
			ibea .addOperator("mutation",  mutation);
			
			IBEA ibea2 = new IBEA (problem);
			ibea2 .setInputParameter("populationSize", 100);
			ibea2 .setInputParameter("maxEvaluations",1000);
			ibea2 .setInputParameter("archiveSize", 10);
			ibea2 .addOperator("crossover", crossover2);
			ibea2 .addOperator("selection",selection);
			ibea2 .addOperator("mutation",  mutation);
			
			MOEAD moead = new MOEAD (problem);
			int populationSize = 100;
			moead.setInputParameter("populationSize", populationSize);
			moead.setInputParameter("maxEvaluations",1000);
			moead.setInputParameter("archiveSize", 10);
			moead.setInputParameter("dataDirectory", "/home/portaluri/workspace/JMetalTask/res/MOEA-D/data");
			moead.setInputParameter("T",  10);
			moead.setInputParameter("nr", 20);
			moead.setInputParameter("delta", 0.9);
			mutation=null;
			
			try {
				mutation = new MyRebalanceMutation(map);
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  // Crossover operator 
		    map.put("CR", 1.0) ;
		    map.put("F", 0.5) ;
		    Operator crossover3 =null;
		    try {
				crossover3 = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover", map);
			} catch (JMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//crossover = new SinglePointCrossover(map);
			map.put("crossoverProbability",0.9);
			selection = new BinaryTournament(null);
			moead.addOperator("crossover", crossover3);
			moead.addOperator("selection",selection);
			moead.addOperator("mutation",  mutation);
			
			MOEAD moead2 = new MOEAD (problem);
			moead2.setInputParameter("populationSize", 100);
			moead2.setInputParameter("maxEvaluations",1000);
			moead2.setInputParameter("archiveSize", 10);
			moead2.setInputParameter("dataDirectory", "/home/portaluri/workspace/JMetalTask/res/MOEA-D/data");
			moead2.setInputParameter("T", 10);
			moead2.setInputParameter("nr", 20);
			moead2.setInputParameter("delta", 0.9);
			mutation=null;
			
			try {
				mutation = new MyRebalanceMutation(map);
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			map.put("crossoverProbability",0.9);
			crossover2 = new TaskGroupingCrossover(map);
			selection = new BinaryTournament(null);
			moead2.addOperator("crossover", crossover2);
			moead2.addOperator("selection",selection);
			moead2.addOperator("mutation",  mutation);
			
			algorithm[0]= nsga;
			algorithm[1]=ibea;
			//algorithm[2]=moead;
			algorithm[2]=mocell;
			algorithm[3]=nsga2;
			algorithm[4]=ibea2;
			//algorithm[6]=moead2;
			algorithm[5]=mocell2;
			//algorithm[1]=moead2;
		
	}

}

package main;
import java.io.IOException;
import java.util.ArrayList;

import jmetal.util.JMException;
import task.Task;
import task.TaskExperiment;
import task.TaskProblem;

public class Main {

	public static void main(String[] args) throws JMException, IOException {
		int task = 2000;
		int server = 100;
		int spr = 24;
		int rpd = 4;
		double serverPP = 200;
		double torPP = 500;
		double aggPP = 1000;
		//double torLink[] = new double[] { 1000, 1000, 1000,1000,1000,1000,1000,1000,1000,1000 };
		//double aggLink[] = new double[] { 10000 };
		double torLink = 1000;
		double aggLink = 10000;
		//double cpuCycle[] = new double[] { 12e6, 12e6, 12e6, 12e6, 12e6, 12e6, 12e6, 12e6, 12e6, 12e6 };
		double cpuCycle = 12e6;
		ArrayList<Task> taskList = generateRandom(task);
		TaskProblem tp = new TaskProblem(task, server, spr, rpd, serverPP,
				torPP, aggPP, torLink, aggLink, cpuCycle, taskList);
		TaskExperiment exp = new TaskExperiment(tp,taskList);
		exp.algorithmNameList_ = new String[] { "NSGAII","IBEAD","MOCELL","gNSGAII","gIBEAD","gMOCELL" };
		//exp.algorithmNameList_ = new String[]{"MOEAD"};
		exp.independentRuns_ = 40;
		exp.experimentBaseDirectory_ = "/home/portaluri/workspace/JMetalTask/res";
		exp.paretoFrontDirectory_=exp.experimentBaseDirectory_;
		exp.indicatorList_ = new String[] { "HV", "SPREAD", "IGD", "EPSILON" };
		exp.problemList_ = new String[] { "TaskProblem" };
		
		//exp.runExperiment();
		//exp.runCompleteExperiment();
		//exp.generateQualityIndicators();
		//exp.generateLatexTables();
		//exp.generateReferenceFronts();
		String prefix = new String("Problems");
		exp.generateRWilcoxonScripts(exp.problemList_, prefix, exp);
		
		exp.generateRBoxplotScripts(1, 1, exp.problemList_,prefix, false, exp);

	}
	public static ArrayList <Task> generateRandom (int task){
		ArrayList<Task> taskList = new ArrayList<Task>(task);
		for(int i =0;i<task;++i){
			Task t =new Task(Math.random()*1e5, 100);
			taskList.add(t);
		}
		return taskList;
	}

}

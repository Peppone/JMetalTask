package algorithm;
import jmetal.core.Algorithm;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.encodings.variable.Int;
import jmetal.encodings.variable.Real;
import jmetal.util.JMException;

public class SimulatedAnnealing extends Algorithm {

	private static final long serialVersionUID = 966647285807739640L;

	double temperature;

	public SimulatedAnnealing(Problem problem) {
		super(problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setInputParameter(String name, Object object) {
		// TODO Auto-generated method stub
		super.setInputParameter(name, object);
		if (name.equalsIgnoreCase("temperature")) {
			temperature = (Double) temperature;
		}

	}

	@Override
	public SolutionSet execute() throws JMException, ClassNotFoundException {
		
		//Begin setup
		SolutionSet population = new SolutionSet();
		Solution newSolution;
		newSolution = new Solution(problem_);
		Variable[] variables = new Variable[problem_.getNumberOfVariables()];
		for (int i = 0; i < variables.length; ++i) {
			if (problem_.getSolutionType().getClass() == IntSolutionType.class) {
				int minimum = (int) problem_.getLowerLimit(i);
				int maximum = (int) problem_.getUpperLimit(i);
				variables[i] = new Int(minimum, maximum);
			} else if (problem_.getSolutionType().getClass() == RealSolutionType.class) {
				double minimum = (double) problem_.getLowerLimit(i);
				double maximum = (double) problem_.getUpperLimit(i);
				variables[i] = new Real(minimum, maximum);
			}

		}
		newSolution.setDecisionVariables(variables);
		problem_.evaluate(newSolution);
		problem_.evaluateConstraints(newSolution);
		
		population.add(newSolution);
		//End Setup
		
		return null;
	}

}

package task;



public class Task {

	private double instruction_number;
	private double bandwidth;

	public Task(double instr, double band) {
		instruction_number =Math.round(instr);
		bandwidth = band;

	}

	public double getInstructionNumber() {
		return instruction_number;
	}

	public double getBandwidth() {
		return bandwidth;
	}

}
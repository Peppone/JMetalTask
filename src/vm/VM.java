package vm;

public class VM {

	private int core;
	private double memory;
	private double bandwidth;

	public VM(int core, double mem, double band) {
		this.core= core;
		this.memory=mem;
		this.bandwidth = band;

	}

	public int getCoreNumber() {
		return core;
	}
	
	public double getMemory(){
		return memory;
	}

	public double getBandwidth() {
		return bandwidth;
	}

}

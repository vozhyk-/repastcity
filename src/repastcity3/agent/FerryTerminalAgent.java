package repastcity3.agent;

import java.util.List;
import java.util.logging.Logger;

import repastcity3.environment.Building;

public class FerryTerminalAgent implements IAgent {
	public static final double DEPART_EACH_N_TICKS = 1000;

	private static Logger LOGGER = Logger.getLogger(FerryTerminalAgent.class.getName());
	private Building home;
	private static int uniqueID = 0;
	private int id;

	public FerryTerminalAgent() {
		this.id = uniqueID++;
	}
	
	@Override
	public void step() throws Exception {
		// TODO Auto-generated method stub

	}

	public boolean isFerryDepartureTime(double time) {
		return time % DEPART_EACH_N_TICKS == 0;
	}

	@Override
	public boolean isThreadable() {
		return true;
	}

	@Override
	public void setHome(Building home) {
		this.home = home;
	}

	@Override
	public Building getHome() {
		return this.home;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "Ferry terminal agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FerryTerminalAgent))
			return false;
		FerryTerminalAgent b = (FerryTerminalAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}
}

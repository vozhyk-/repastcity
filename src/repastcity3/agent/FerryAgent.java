package repastcity3.agent;

import java.util.List;
import java.util.logging.Logger;

import repastcity3.environment.Building;

public class FerryAgent implements IAgent {
	private static Logger LOGGER = Logger.getLogger(FerryAgent.class.getName());

	private Building home;

	private static int uniqueID = 0;
	private int id;

	public FerryAgent() {
		this.id = uniqueID++;
	}

	@Override
	public void step() throws Exception {
		// TODO Auto-generated method stub

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
		return "Ferry agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FerryAgent))
			return false;
		FerryAgent b = (FerryAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

}

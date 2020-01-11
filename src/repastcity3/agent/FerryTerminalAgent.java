package repastcity3.agent;

import java.util.List;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import repastcity3.environment.Building;
import repastcity3.main.ContextManager;

public class FerryTerminalAgent implements IAgent {
	// TODO This should really be as close to 0 as possible,
	// as ferry terminal agents are placed exactly at ferry terminals.
	static final double TERMINAL_TO_TERMINAL_AGENT_DISTANCE = 0.005;

	// The time of the first possible ferry departure.
	private double scheduleStartTime;
	// The ferry will be able to depart every schedulePeriod ticks.
	private double schedulePeriod;

	private static Logger LOGGER = Logger.getLogger(FerryTerminalAgent.class.getName());
	private Building home;
	private static int uniqueID = 0;
	private int id;

	public FerryTerminalAgent(double scheduleStartTime, double schedulePeriod) {
		this.scheduleStartTime = scheduleStartTime;
		this.schedulePeriod = schedulePeriod;

		this.id = uniqueID++;
	}
	
	@Override
	public void step() throws Exception {
		// TODO Auto-generated method stub

	}

	public boolean isFerryDepartureTime(double time) {
		// TODO This will not make sense if scheduleStartTime > schedulePeriod
		return time % schedulePeriod == scheduleStartTime;
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

	public static FerryTerminalAgent getClosest(Coordinate coord) {
		Envelope envelope = ContextManager.getSquareEnvelope(coord, TERMINAL_TO_TERMINAL_AGENT_DISTANCE);
		Iterable<FerryTerminalAgent> agents = ContextManager.getAgentsWithin(envelope, FerryTerminalAgent.class);
		return agents.iterator().next();
	}
}

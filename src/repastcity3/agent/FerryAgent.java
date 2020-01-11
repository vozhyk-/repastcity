package repastcity3.agent;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.util.collections.IndexedIterable;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.main.ContextManager;

public class FerryAgent implements IAgent {
	private static final int MAX_TRAVEL_PER_TURN = 50;
	private static final int MIN_TRAVEL_PER_TURN = 50;
	private static final double CLOSEST_CARS_DISTANCE = 0.005;
	private static final double CLOSEST_TERMINAL_DISTANCE = 0.005;
	// TODO This should really be as close to 0 as possible,
	// as ferry terminal agents are placed exactly at ferry terminals.
	private static final double TERMINAL_TO_TERMINAL_AGENT_DISTANCE = 0.005;

	private static Logger LOGGER = Logger.getLogger(FerryAgent.class.getName());

	private Building home;

	private static int uniqueID = 0;
	private int id;

	private List<DefaultAgent> loadedCars;
	private Route route;
	private int maxLoadedCarCount = 5; // TODO Add this to the constructor
	private int lastLoadedCarCount = 0;

	public FerryAgent() {
		this.id = uniqueID++;
	}

	@Override
	public void step() throws Exception {
		if (this.route == null) {
			if (!timeToLeave())
				return;
			loadCars();
			setRouteToOppositeTerminal();
		}

		if (!this.route.atDestination()) {
			this.route.travel();
		} else {
			unloadCars();
			this.route = null;
		}
	}

	private boolean timeToLeave() {
		double now = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		return getClosestTerminalAgent().isFerryDepartureTime(now);
	}

	private FerryTerminalAgent getClosestTerminalAgent() {
		Envelope envelope = getSquareEnvelope(getClosestTerminal().getCoords(), TERMINAL_TO_TERMINAL_AGENT_DISTANCE);
		Iterable<FerryTerminalAgent> agents = ContextManager.getAgentsWithin(envelope, FerryTerminalAgent.class);
		return agents.iterator().next();
	}

	private void loadCars() {
		this.loadedCars = StreamSupport.stream(getClosestCars().spliterator(), false)
			    .filter(DefaultAgent::isWaitingForFerry)
			    .limit(this.maxLoadedCarCount)
			    .collect(Collectors.toList());
		for (DefaultAgent car: this.loadedCars) {
			car.onLoadingToFerry();
			ContextManager.removeAgentFromContext(car);
		}
		this.lastLoadedCarCount = this.loadedCars.size();
	}

	private void unloadCars() {
		for (DefaultAgent car: this.loadedCars) {
			ContextManager.addAndPlaceAgent(car, ContextManager.getAgentGeometry(this).getCentroid());
			car.onUnloadedFromFerry();
		}
		this.loadedCars = null;
	}

	private void setRouteToOppositeTerminal() {
		Building destination = getOppositeTerminal();
		this.route = new Route(this, destination.getCoords(), destination, MIN_TRAVEL_PER_TURN, MAX_TRAVEL_PER_TURN);
		this.route.setTravelingAsFerry();
	}

	private Building getOppositeTerminal() {
		Building thisTerminal = getClosestTerminal();
		// TODO This works only if there are only 2 terminals in the world.
		// Find a way to define the opposite terminal in the shapefile.
		IndexedIterable<Building> otherTerminals = ContextManager.ferryTerminalContext.getObjects(Building.class);
		Optional<Building> oppositeTerminal = StreamSupport.stream(otherTerminals.spliterator(), false).filter(
				(terminal) -> !terminal.equals(thisTerminal)).findAny();
		return oppositeTerminal.get();
	}

	private Building getClosestTerminal() {
		Envelope envelope = getClosestSquareEnvelope(FerryAgent.CLOSEST_TERMINAL_DISTANCE);
		Iterable<Building> closestTerminals = ContextManager.buildingProjection.getObjectsWithin(envelope, Building.class);
		Optional<Building> thisTerminal = StreamSupport.stream(closestTerminals.spliterator(), false).findAny();
		return thisTerminal.get();
	}

	private Iterable<DefaultAgent> getClosestCars() {
		Envelope envelope = getClosestSquareEnvelope(FerryAgent.CLOSEST_CARS_DISTANCE);
		return ContextManager.getAgentsWithin(envelope, DefaultAgent.class);
	}

	private Envelope getClosestSquareEnvelope(double dist) {
		Coordinate myCoordinate = ContextManager.getAgentGeometry(this).getCoordinate();
		return getSquareEnvelope(myCoordinate, dist);
	}

	private Envelope getSquareEnvelope(Coordinate origin, double dist) {
		return new Envelope(origin.x - dist, origin.x + dist, origin.y - dist, origin.y + dist);
	}

	public int getLastLoadedCarCount() {
		return this.lastLoadedCarCount;
	}

	public double getLastUtilization() {
		return (double)getLastLoadedCarCount() / this.maxLoadedCarCount;
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

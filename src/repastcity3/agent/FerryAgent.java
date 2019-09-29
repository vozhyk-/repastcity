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

import repast.simphony.util.collections.IndexedIterable;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.environment.SpatialIndexManager;
import repastcity3.main.ContextManager;

public class FerryAgent implements IAgent {
	private static final double CLOSEST_CARS_DISTANCE = 0.005;
	private static final double CLOSEST_TERMINAL_DISTANCE = 0.005;

	private static Logger LOGGER = Logger.getLogger(FerryAgent.class.getName());

	private Building home;

	private static int uniqueID = 0;
	private int id;

	private List<DefaultAgent> loadedCars;
	private Route route;

	public FerryAgent() {
		this.id = uniqueID++;
	}

	@Override
	public void step() throws Exception {
		if (this.route == null) {
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

	private void loadCars() {
		this.loadedCars = StreamSupport.stream(getClosestCars().spliterator(), false)
			    .filter(DefaultAgent::isWaitingForFerry)
			    .collect(Collectors.toList());
		for (DefaultAgent car: this.loadedCars)
			ContextManager.removeAgentFromContext(car);
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
		this.route = new Route(this, destination.getCoords(), destination);
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
		Geometry location = ContextManager.getAgentGeometry(this);
		Coordinate coord = location.getCoordinate();
		Envelope envelope = new Envelope(coord.x - dist, coord.x + dist, coord.y - dist, coord.y + dist);
		return envelope;
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
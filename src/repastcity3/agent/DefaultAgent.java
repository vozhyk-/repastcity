/*
ęCopyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of  the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
*/

package repastcity3.agent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import repast.simphony.engine.environment.RunEnvironment;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.main.ContextManager;

public class DefaultAgent implements IAgent, TravellingAgent {

	private static final int MAX_TRAVEL_PER_TURN = 125;

	private static final int MIN_TRAVEL_PER_TURN = 75;

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

	private Building home; // Where the agent lives
	private Route route; // An object to move the agent around the world

	private boolean goingHome = false; // Whether the agent is going to or from their home

	private static int uniqueID = 0;
	private int id;

	private double waitForFerryStartTimestamp;
	private double lastFerryWaitingTime;

	public DefaultAgent() {
		this.id = uniqueID++;
	}

	@Override
	public void step() throws Exception {

		LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping.");
		if (this.route == null) {
			this.goingHome = false; // Must be leaving home
			// Choose a new building to go to
			Building b = ContextManager.buildingContext.getRandomObject();
			this.route = new Route(this, b.getCoords(), b, MIN_TRAVEL_PER_TURN, MAX_TRAVEL_PER_TURN);
			LOGGER.log(Level.FINE, this.toString() + " created new route to " + b.toString());
		}
		if (isWaitingForFerry())
			return;
		if (!this.route.atDestination()) {
			this.route.travel();
			if (isWaitingForFerry())
				onStartedWaitingForFerry();
			LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
		} else {
			// Have reached destination, now either go home or onto another building
			if (this.goingHome) {
				this.goingHome = false;
				Building b = ContextManager.buildingContext.getRandomObject();
				this.route = new Route(this, b.getCoords(), b, MIN_TRAVEL_PER_TURN, MAX_TRAVEL_PER_TURN);
				LOGGER.log(Level.FINE, this.toString() + " reached home, now going to " + b.toString());
			} else {
				LOGGER.log(Level.FINE, this.toString() + " reached " + this.route.getDestinationBuilding().toString()
						+ ", now going home");
				this.goingHome = true;
				this.route = new Route(this, this.home.getCoords(), this.home, MIN_TRAVEL_PER_TURN, MAX_TRAVEL_PER_TURN);
			}

		}

	} // step()

	public boolean isWaitingForFerry() {
		return this.route != null && this.route.isFerryControlled();
	}

	private void onStartedWaitingForFerry() {
		this.waitForFerryStartTimestamp = ticksNow();
	}

	public void onLoadingToFerry() {
		this.lastFerryWaitingTime = ticksNow() - this.waitForFerryStartTimestamp;
	}

	public void onUnloadedFromFerry() {
		this.route = new Route(this, this.route.getDestination(), this.route.getDestinationBuilding(), MIN_TRAVEL_PER_TURN, MAX_TRAVEL_PER_TURN);
	}

	private double ticksNow() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	public double getLastFerryWaitingTime() {
		return this.lastFerryWaitingTime;
	}

	/**
	 * There will be no inter-agent communication so these agents can be executed simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
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
		return "Agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultAgent))
			return false;
		DefaultAgent b = (DefaultAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see repastcity3.agent.TravellingAgent#receiveBestArrivalTime(double)
	 */
	@Override
	public void receiveBestArrivalTime(double bestArrivalTime) {
		if (this.route != null)
			this.route.setBestArrivalTime(bestArrivalTime);
	}

	@Override
	public void sendExpectedArrivalTime(FerryTerminalAgent terminalAgent, double expectedArrivalTime) {
		terminalAgent.receiveExpectedArrivalTime(this, expectedArrivalTime);
	}

}

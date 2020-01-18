package repastcity3.agent;

public interface TravellingAgent extends IAgent {

	void receiveBestArrivalTime(double bestArrivalTime);
	void sendExpectedArrivalTime(FerryTerminalAgent terminalAgent, double expectedArrivalTime);
}
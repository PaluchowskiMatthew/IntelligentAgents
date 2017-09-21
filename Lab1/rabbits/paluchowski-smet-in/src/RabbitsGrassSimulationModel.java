import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {
	
		private Schedule schedule;
		private int numRabbits;

		public static void main(String[] args) {
			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		    init.loadModel(model, "", false);
		}
		
		public void setup() {
			// TODO Auto-generated method stub
			
		}

		public void begin() {
			buildModel();
			buildSchedule();
			buildDisplay();
		}

		public void buildModel(){
  	    }
		
	    public void buildSchedule(){
		}
		
		public void buildDisplay(){
		}

		public String getName() {
			return "RabbitsGrassSimulation";
		}

		public Schedule getSchedule() {
			// TODO Auto-generated method stub
			return schedule;
		}
		
		public String[] getInitParam() {
			String[] initParams = { "NumRabbits" };
		    return initParams;
		}

		public int getNumRabbits(){
			return numRabbits;
		}

		public void setNumRabbits(int numberOfRabbits){
			numRabbits = numberOfRabbits;
		}
}

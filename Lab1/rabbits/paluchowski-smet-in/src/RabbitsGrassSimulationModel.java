import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author
 * 
 * 
 * FINISHED HERE http://liapc3.epfl.ch/repast/HowTo27.htm
 * 
 * 
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private Schedule schedule;

	private RabbitsGrassSimulationSpace rabbitsGrassSpace;

	private DisplaySurface displaySurface;

	private ArrayList rabbitList;

	// Default Values
	private static final int NUMRABBITS = 50;
	private static final int WORLDXSIZE = 20;
	private static final int WORLDYSIZE = 20;
	private static final int TOTALGRASS = 100;
	private static final int RABBIT_BIRTH_THRESHOLD = 10;

	private int numRabbits = NUMRABBITS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int grass = TOTALGRASS;
	private int rabbitBirthThreshold = RABBIT_BIRTH_THRESHOLD;

	public static void main(String[] args) {
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
	}

	public void setup() {
		// TODO Auto-generated method stub
		System.out.println("Running setup");
		rabbitsGrassSpace = null;
		rabbitList = new ArrayList();
		schedule = new Schedule(1);

		if (displaySurface != null) {
			displaySurface.dispose();
		}
		displaySurface = null;

		displaySurface = new DisplaySurface(this, "Rabbits Grass Simulation Model Window 1");

		registerDisplaySurface("Rabbits Grass Simulation Model Window 1", displaySurface);
	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurface.display();
	}

	public void buildModel() {
		System.out.println("Running BuildModel");
		rabbitsGrassSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
		rabbitsGrassSpace.spreadGrass(grass);

		for (int i = 0; i < numRabbits; i++) {
			addNewRabbit();
		}
		for (int i = 0; i < rabbitList.size(); i++) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitList.get(i);
			rgsa.report();
		}

	}

	public void buildSchedule() {
		System.out.println("Running BuildSchedule");

		class RabbitGrassStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(rabbitList);
				for (int i = 0; i < rabbitList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitList.get(i);
					rgsa.step();
				}
				bringToLifeRabbits();
				reapDeadRabbits();
				
				displaySurface.updateDisplay();
			}
		}

		schedule.scheduleActionBeginning(0, new RabbitGrassStep());

		class RabbitGrassCountLiving extends BasicAction {
			public void execute() {
				countLivingRabbits();
			}
		}

		schedule.scheduleActionAtInterval(10, new RabbitGrassCountLiving());
	}

	public void buildDisplay() {
		System.out.println("Running BuildDisplay");

		ColorMap map = new ColorMap();

		for (int i = 1; i < 16; i++) {
			map.mapColor(i, new Color(0, (int) (i * 8 + 127), 0));
		}
		map.mapColor(0, Color.black);

		Value2DDisplay displayGrass = new Value2DDisplay(rabbitsGrassSpace.getCurrentGrassSpace(), map);

		Object2DDisplay displayRabbits = new Object2DDisplay(rabbitsGrassSpace.getCurrentRabbitSpace());
		displayRabbits.setObjectList(rabbitList);

		displaySurface.addDisplayable(displayGrass, "Grass");
		displaySurface.addDisplayable(displayRabbits, "Rabbits");

	}

	private void addNewRabbit() {
		RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent(rabbitBirthThreshold);
		rabbitList.add(rabbit);
		rabbitsGrassSpace.addRabbit(rabbit);
	}
	
	private void bringToLifeRabbits() {
		for (int i = (rabbitList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitList.get(i);
			int rabbitEnergy = rgsa.getEnergy();
			if (rabbitEnergy >= rabbitBirthThreshold) {
				rgsa.setEnergy(rabbitEnergy/2);
				addNewRabbit();
			}
		}
	}

	private void reapDeadRabbits() {
		for (int i = (rabbitList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitList.get(i);
			if (rgsa.getEnergy() < 1) {
				rabbitsGrassSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
				rabbitList.remove(i);
			}
		}
	}

	private int countLivingRabbits() {
		int livingRabbits = 0;
		for (int i = 0; i < rabbitList.size(); i++) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitList.get(i);
			if (rgsa.getEnergy() > 0)
				livingRabbits++;
		}
		System.out.println("Number of living rabbits is: " + livingRabbits);

		return livingRabbits;
	}

	public String getName() {
		return "RabbitsGrassSimulation";
	}

	public Schedule getSchedule() {
		// TODO Auto-generated method stub
		return schedule;
	}

	// Sliders for:
	// Grid size
	// The number of rabbits defines the initial number of rabbits
	// The birth threshold of rabbits defines the energy level at which the rabbit
	// reproduces.
	// The grass growth rate controls the rate at which grass grows (total amount of
	// grass added to the whole world within one simulation tick).
	public String[] getInitParam() {
		String[] initParams = { "NumRabbits", "WorldXSize", "WorldYSize", "Grass", "RabbitBirthThreshold" };
		return initParams;
	}

	public int getNumRabbits() {
		return numRabbits;
	}

	public void setNumRabbits(int numberOfRabbits) {
		numRabbits = numberOfRabbits;
	}

	public int getWorldXSize() {
		return worldXSize;
	}

	public void setWorldXSize(int wxs) {
		worldXSize = wxs;
	}

	public int getWorldYSize() {
		return worldYSize;
	}

	public void setWorldYSize(int wys) {
		worldYSize = wys;
	}

	public int getGrass() {
		return grass;
	}

	public void setGrass(int g) {
		grass = g;
	}

	public int getRabbitBirthThreshold() {
		return rabbitBirthThreshold;
	}

	public void setRabbitBirthThreshold(int birthThreshold) {
		rabbitBirthThreshold = birthThreshold;
	}
}

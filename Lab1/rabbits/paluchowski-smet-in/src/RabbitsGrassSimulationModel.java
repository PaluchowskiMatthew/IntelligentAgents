import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;

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

	private ArrayList agentList;

	private DisplaySurface displaySurface;

	private OpenSequenceGraph amountOfGrassInSpace;
	private OpenSequenceGraph amountOfRabbitsInSpace;
	private OpenHistogram rabbitEnergyDistribution;

	class grassInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) rabbitsGrassSpace.getTotalGrass();
		}
	}
	
	class rabbbitsInSpace implements DataSource, Sequence {

		public Object execute() {
			return new Double(getSValue());
		}

		public double getSValue() {
			return (double) rabbitsGrassSpace.getTotalRabbits();
		}
	}

	class rabbitEnergy implements BinDataSource {
		public double getBinValue(Object o) {
			RabbitsGrassSimulationAgent ra = (RabbitsGrassSimulationAgent) o;
			return (double) ra.getEnergy();
		}
	}


	// Default Values
	private static final int NUMRABBITS = 1;
	private static final int WORLDXSIZE = 20;
	private static final int WORLDYSIZE = 20;
	private static final int TOTALGRASS = 50;
	private static final int BIRTHTRESHHOLD = 70;


	private int numRabbits = NUMRABBITS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int grass = TOTALGRASS;
	private int birthTreshhold = BIRTHTRESHHOLD;


	public static void main(String[] args) {
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
	}

	public void setup() {
		// TODO Auto-generated method stub
		System.out.println("Running setup");
		rabbitsGrassSpace = null;
		agentList = new ArrayList();

		schedule = new Schedule(1);

		if (displaySurface != null) {
			displaySurface.dispose();
		}
		displaySurface = null;

		if (amountOfGrassInSpace != null) {
			amountOfGrassInSpace.dispose();
		}
		amountOfGrassInSpace = null;
		
		
		if (amountOfRabbitsInSpace != null) {
			amountOfRabbitsInSpace.dispose();
		}
		amountOfRabbitsInSpace = null;
		
		if (rabbitEnergyDistribution != null){
		      rabbitEnergyDistribution.dispose();
		    }
		    rabbitEnergyDistribution = null;

		// Create Displays
		displaySurface = new DisplaySurface(this, "Carry Drop Model Window 1");
		amountOfGrassInSpace = new OpenSequenceGraph("Amount Of Grass In Space", this);
		amountOfRabbitsInSpace = new OpenSequenceGraph("Amount Of Rabbits In Space", this);
		rabbitEnergyDistribution = new OpenHistogram("Rabbit energy", 8, 0);

		// Register Displays
		registerDisplaySurface("Carry Drop Model Window 1", displaySurface);
		this.registerMediaProducer("Plot", amountOfGrassInSpace);
		this.registerMediaProducer("Plot", amountOfRabbitsInSpace);

	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();

		displaySurface.display();
		amountOfGrassInSpace.display();
		amountOfRabbitsInSpace.display();
		rabbitEnergyDistribution.display();
	}

	public void buildModel() {
		System.out.println("Running BuildModel");
		rabbitsGrassSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
		rabbitsGrassSpace.spreadGrass(grass);

		for (int i = 0; i < numRabbits; i++) {
			addNewAgent();
		}
		for (int i = 0; i < agentList.size(); i++) {
			RabbitsGrassSimulationAgent ra = (RabbitsGrassSimulationAgent) agentList.get(i);
			ra.report();

		}

	}

	public void buildSchedule() {
		System.out.println("Running BuildSchedule");

		class RabbitStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(agentList);
				for (int i = 0; i < agentList.size(); i++) {
					RabbitsGrassSimulationAgent ra = (RabbitsGrassSimulationAgent) agentList.get(i);
					ra.step();
				}
				int reproduction = reproduceAgents();
				for (int i = 0; i < reproduction; i++) {
					addNewAgent();
				}
				int deadAgents = reapDeadAgents();
				rabbitsGrassSpace.spreadGrass(grass);

				displaySurface.updateDisplay();
			}
		}

		schedule.scheduleActionBeginning(0, new RabbitStep());

		class RabbitCountLiving extends BasicAction {
			public void execute() {
				countLivingAgents();
			}
		}

		schedule.scheduleActionAtInterval(10, new RabbitCountLiving());
		class RabbitsGrassSimulationUpdateGrassInSpace extends BasicAction {
			public void execute() {
				amountOfGrassInSpace.step();
			}
		}

		schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateGrassInSpace());
		
		schedule.scheduleActionAtInterval(10, new RabbitCountLiving());
		class RabbitsGrassSimulationUpdateRabbitsInSpace extends BasicAction {
			public void execute() {
				amountOfRabbitsInSpace.step();
			}
		}

		schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateRabbitsInSpace());
		
		class CarryDropUpdateRabbitEnergy extends BasicAction {
		      public void execute(){
		        rabbitEnergyDistribution.step();
		      }
		    }

		    schedule.scheduleActionAtInterval(10, new CarryDropUpdateRabbitEnergy());

	}

	public void buildDisplay() {
		System.out.println("Running BuildDisplay");

		ColorMap map = new ColorMap();

		for (int i = 1; i < 16; i++) {
			map.mapColor(i, new Color(0, (int) (i * 8 + 127), 0));
		}
		map.mapColor(0, Color.black);

		Value2DDisplay displayGrass = new Value2DDisplay(rabbitsGrassSpace.getCurrentGrassSpace(), map);

		Object2DDisplay displayAgents = new Object2DDisplay(rabbitsGrassSpace.getCurrentAgentSpace());
		displayAgents.setObjectList(agentList);

		displaySurface.addDisplayableProbeable(displayGrass, "Grass");
		displaySurface.addDisplayableProbeable(displayAgents, "Agents");

		amountOfGrassInSpace.addSequence("Grass In Space", new grassInSpace());
		amountOfRabbitsInSpace.addSequence("Rabbits in space", new rabbbitsInSpace());
		rabbitEnergyDistribution.createHistogramItem("Rabbit Energy",agentList,new rabbitEnergy());

<<<<<<< Updated upstream

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
=======
	} 
>>>>>>> Stashed changes

	public String getName() {
		return "RabbitsGrassSimulation";
	}

	private void addNewAgent() {
		RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(birthTreshhold);
		agentList.add(a);
		rabbitsGrassSpace.addAgent(a);
	}

	private int reapDeadAgents() {
		int count = 0;
		for (int i = (agentList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent ra = (RabbitsGrassSimulationAgent) agentList.get(i);
			if (ra.getEnergy() < 1) {
				rabbitsGrassSpace.removeAgentAt(ra.getX(), ra.getY());
				agentList.remove(i);
				count++;
			}
		}
		return count;
	}

	private int reproduceAgents() {
		int count = 0;
		for (int i = 0; i < agentList.size(); i++) {
			RabbitsGrassSimulationAgent ra = (RabbitsGrassSimulationAgent) agentList.get(i);
			if (ra.getEnergy() > ra.getbt()) {
				count++;
				ra.reproduce();
			}
		}
		return count;
	}

	private int countLivingAgents() {
		int livingAgents = 0;
		for (int i = 0; i < agentList.size(); i++) {
			RabbitsGrassSimulationAgent ra = (RabbitsGrassSimulationAgent) agentList.get(i);
			if (ra.getEnergy() > 0)
				livingAgents++;
		}
		System.out.println("Number of living agents is: " + livingAgents);

		return livingAgents;
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
		String[] initParams = { "NumRabbits", "WorldXSize", "WorldYSize", "Grass", "BirthTreshhold" };
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

	public int getbirthTreshhold() {
		return birthTreshhold;
	}

	public void setbirthTreshhold(int bt) {
		birthTreshhold = bt;
	}

}

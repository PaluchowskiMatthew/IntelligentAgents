import java.awt.Color;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author
 * 
 * 
 * 
 * 		http://liapc3.epfl.ch/repast/HowTo15.htm
 * 
 * 
 * 
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	private Schedule schedule;

	private RabbitsGrassSimulationSpace rabbitsGrassSpace;

	private DisplaySurface displaySurface;

	// Default Values
	private static final int NUMRABBITS = 50;
	private static final int WORLDXSIZE = 20;
	private static final int WORLDYSIZE = 20;
	private static final int TOTALGRASS = 100;

	private int numRabbits = NUMRABBITS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int grass = TOTALGRASS;

	public static void main(String[] args) {
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);
	}

	public void setup() {
		// TODO Auto-generated method stub
		System.out.println("Running setup");
		rabbitsGrassSpace = null;

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
	}

	public void buildSchedule() {
		System.out.println("Running BuildSchedule");
	}

	public void buildDisplay() {
		System.out.println("Running BuildDisplay");

		ColorMap map = new ColorMap();

		for (int i = 1; i < 16; i++) {
			map.mapColor(i, new Color(0, (int) (i * 8 + 127), 0));
		}
		map.mapColor(0, Color.white);

		Value2DDisplay displayMoney = new Value2DDisplay(rabbitsGrassSpace.getCurrentGrassSpace(), map);

		displaySurface.addDisplayable(displayMoney, "Grass");

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
		String[] initParams = { "NumRabbits", "WorldXSize", "WorldYSize", "Grass" };
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
}

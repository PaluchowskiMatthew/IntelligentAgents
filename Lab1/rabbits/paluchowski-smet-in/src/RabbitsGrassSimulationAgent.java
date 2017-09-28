import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 * 
 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	private int x;
	private int y;
	private int vX;
	private int vY;
	private int energy;
	private static int birthThreshold;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rabbitsGrassSpace;

	public RabbitsGrassSimulationAgent(int rabbitBirthThreshold) {
		x = -1;
		y = -1;
		energy = (int) (Math.random() * (birthThreshold - 1)) + 1;
		setVxVy();
		birthThreshold = rabbitBirthThreshold;
		IDNumber++;
		ID = IDNumber;
	}

	public void draw(SimGraphics G) {
		G.drawFastRoundRect(Color.white);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	private void setVxVy() {
		vX = 0;
		vY = 0;
		List<String> directions = Arrays.asList("North", "South", "East", "West");
		String direction = directions.get(new Random().nextInt(directions.size()));

		switch (direction) {
		case "North":
			vX = 0;
			vY = -1;
			break;
		case "South":
			vX = 0;
			vY = 1;
			break;
		case "East":
			vX = 1;
			vY = 0;
			break;
		case "West":
			vX = -1;
			vY = 0;
			break;
		}
	}

	public void setRabbitsGrassSpace(RabbitsGrassSimulationSpace rgss) {
		rabbitsGrassSpace = rgss;
	}

	public String getID() {
		return "Rabbit-" + ID;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int newEnergy) {
		energy = newEnergy;
	}

	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void report() {
		System.out.println(getID() + " at " + x + ", " + y + " has " + getEnergy() + " energy" + " and "
				+ getBirthThreshold() + " birth threshold.");
	}

	public void step() {
		int newX = x + vX;
		int newY = y + vY;

		Object2DGrid grid = rabbitsGrassSpace.getCurrentRabbitSpace();
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
		newY = (newY + grid.getSizeY()) % grid.getSizeY();

		energy += rabbitsGrassSpace.takeGrassAt(x, y);
		while (tryMove(newX, newY)) {	// to be verified if correct
			setVxVy();
		}
		
		energy--;
	}

	private boolean tryMove(int newX, int newY) {
		return rabbitsGrassSpace.moveRabbitAt(x, y, newX, newY);
	}

}

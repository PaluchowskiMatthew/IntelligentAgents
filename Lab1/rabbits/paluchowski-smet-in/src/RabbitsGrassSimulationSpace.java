
/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */
import uchicago.src.sim.space.Object2DGrid;

public class RabbitsGrassSimulationSpace {
	private Object2DGrid grassSpace;

	// initialize the space object by filling each cell with an Integer object, with
	// the initial value of zero
	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		grassSpace = new Object2DGrid(xSize, ySize);
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}

	// Randomly place grass in grassSpace
	public void spreadGrass(int grass) {
		for (int i = 0; i < grass; i++) {

			// Choose coordinates
			int x = (int) (Math.random() * (grassSpace.getSizeX()));
			int y = (int) (Math.random() * (grassSpace.getSizeY()));

			// Get the value of the object at those coordinates
			int currentValue = getGrassAt(x, y);
			// Replace the Integer object with another one with the new value
			grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
		}
	}

	public int getGrassAt(int x, int y) {
		int i;
		if (grassSpace.getObjectAt(x, y) != null) {
			i = ((Integer) grassSpace.getObjectAt(x, y)).intValue();
		} else {
			i = 0;
		}
		return i;
	}
	
	public Object2DGrid getCurrentGrassSpace(){
	    return grassSpace;
	  }
}

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

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
	private int bt;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rabbitsGrassSpace;
	
	public RabbitsGrassSimulationAgent(int birthTreshhold){
	    x = -1;
	    y = -1;
	    energy = 50;
	    setVxVy();
	    bt = birthTreshhold;
	    IDNumber++;
	    ID = IDNumber;
	}
	
	private void setVxVy(){
	    vX = 0;
	    vY = 0;
	    int v = 0;
	    while(v == 0){
	      v = (int)Math.floor(Math.random() * 3) - 1;
	    }
	    int dir = (int)Math.floor(Math.random()*2);
	    if(dir == 0) {
	    		vX = v;
	    }
	    	else {
	    		vY = v;
	    }
	  }
    public void setXY(int newX, int newY){
        x = newX;
        y = newY;
    }

    public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace rgs){
        rabbitsGrassSpace = rgs;
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

	public String getID(){
	    return "A-" + ID;
	  }

	  public int getEnergy(){
	    return energy;
	  }
	  
	  public int getbt(){
		    return bt;
		  }


	  

	  public void report(){
	    System.out.println(getID() + 
	                       " at " + 
	                       x + ", " + y + 
	                       " has an energy level of " + 
	                       getEnergy() );
	  }
	  public void step(){
		  int newX = x + vX;
		    int newY = y + vY;

		    Object2DGrid grid = rabbitsGrassSpace.getCurrentAgentSpace();
		    newX = (newX + grid.getSizeX()) % grid.getSizeX();
		    newY = (newY + grid.getSizeY()) % grid.getSizeY();

		    if(tryMove(newX, newY)){
		      energy += rabbitsGrassSpace.eatGrass(x, y) - 1;
		    }
		    else{
		      setVxVy();
		    }
	  }
	  private boolean tryMove(int newX, int newY){
		    return rabbitsGrassSpace.moveAgentAt(x, y, newX, newY);
		  }
	  public void reproduce() {
		  energy = 50;
	  }
}

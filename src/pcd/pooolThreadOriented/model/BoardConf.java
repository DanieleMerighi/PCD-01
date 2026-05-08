package pcd.pooolThreadOriented.model;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();
	
	Ball getHumanBall();

	Ball getBotBall();
	
	List<Ball> getSmallBalls();

	List<Hole> getHoles();

}

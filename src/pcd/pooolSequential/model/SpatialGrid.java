package pcd.pooolSequential.model;

import java.util.ArrayList;
import java.util.List;

public class SpatialGrid {
    private final double cellSize;
    private final int cols;
    private final int rows;
    private final List<Ball>[][] cells;

    @SuppressWarnings("unchecked")
    public SpatialGrid(Boundary bounds, double maxRadius) {
        this.cellSize = maxRadius * 2.01;
        this.cols = (int) Math.ceil((bounds.x1() - bounds.x0()) / cellSize);
        this.rows = (int) Math.ceil((bounds.y1() - bounds.y0()) / cellSize);
        this.cells = new List[cols][rows];

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                cells[i][j] = new ArrayList<>();
            }
        }
    }

    public void clearAndPopulate(List<Ball> balls, Boundary bounds) {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                cells[i][j].clear();
            }
        }
        for (Ball b : balls) {
            int col = (int) Math.max(0, Math.min(cols - 1, (b.getPos().x() - bounds.x0()) / cellSize));
            int row = (int) Math.max(0, Math.min(rows - 1, (b.getPos().y() - bounds.y0()) / cellSize));
            cells[col][row].add(b);
        }
    }

    public List<Ball> getNearbyBalls(int col, int row) {
        List<Ball> nearby = new ArrayList<>();
        for (int i = Math.max(0, col - 1); i <= Math.min(cols - 1, col + 1); i++) {
            for (int j = Math.max(0, row - 1); j <= Math.min(rows - 1, row + 1); j++) {
                nearby.addAll(cells[i][j]);
            }
        }
        return nearby;
    }

    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public List<Ball> getCell(int col, int row) { return cells[col][row]; }
}
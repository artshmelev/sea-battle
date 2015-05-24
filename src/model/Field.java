package model;
import model.Cell;
import model.CellType;
import static model.Constants.*;

public class Field {

    private Cell[][] cells = new Cell[SIZE][SIZE];

    public Field() {
        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j)
                cells[i][j] = new Cell();
    }

    public Field(Field field) {
        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j) {
                cells[i][j] = new Cell();
                cells[i][j].setType(field.getCell(i, j));
            }
    }

    public void clear() {
        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j)
                setCell(i, j, CellType.EMPTY);
    }

    public boolean isCellSet(int row, int col) {
        return !(cells[row][col].getType() == CellType.EMPTY);
    }

    public CellType getCell(int row, int col) {
        return cells[row][col].getType();
    }

    public void setCell(int row, int col, CellType type) {
        cells[row][col].setType(type);
    }
}

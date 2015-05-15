package model;
import model.CellType;

public class Cell {

    private CellType type;

    public Cell() {
        type = CellType.EMPTY;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }
}

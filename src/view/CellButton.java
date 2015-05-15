package view;
import java.awt.Button;

public class CellButton extends Button {

    private int row;
    private int col;

    public CellButton(String label, int row, int col) {
        super(label);
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}

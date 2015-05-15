package model;
import java.io.*;
import java.net.*;

import model.CellType;
import model.Field;
import model.Message;
import model.State;
import model.TcpThread;
import static model.Constants.*;

public class Model {

    private State state;
    private Field[] fields;
    private boolean isReady;
    private boolean isUpdated;
    private int whoPlay;

    private TcpThread thread;
    private volatile Message request;
    private volatile Message response;

    public Model() {
        state = State.MENU;

        fields = new Field[2];
        fields[LEFT] = new Field();
        fields[RIGHT] = new Field();

        isReady = false;
        isUpdated = true;
        whoPlay = LEFT;

        request = new Message();
        response = new Message();
    }

    public State getState() {
        return state;
    }

    public Field[] getFields() {
        return fields;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }

    public void setRequest(Message request) {
        this.request = request;
    }

    public void setResponse(Message response) {
        this.response = response;
    }

    public void tryHit(int side, int row, int col) {
        if (isReady && side == RIGHT && whoPlay == LEFT &&
            !fields[side].isCellSet(row, col)) {
            sendRequest(row, col);
        } else if (!isReady && side == LEFT && !fields[side].isCellSet(row, col)) {
            fields[side].setCell(row, col, CellType.SHIP);
            isUpdated = true;
        }
    }

    public void update(State state) {
        this.state = state;

        switch (state) {
            case CONNECT:
                whoPlay = RIGHT;
                thread = new TcpThread(this, DEFAULT_PORT+1);
                break;
            case GAME:
                if (!checkIsAllowed()) {
                    resetPrepare();
                    break;
                }
                isReady = true;
                if (thread == null)
                    thread = new TcpThread(this, DEFAULT_PORT);
                thread.start();
                break;
        }

        isUpdated = true;
    }

    public void update() {
        if (!request.isEmpty()) {
            int[] data = request.getData();
            int row = data[0];
            int col = data[1];
            int type = checkHit(row, col).ordinal();
            sendResponse(row, col, type);

            isUpdated = true;
            request.clear();
        }

        if (!response.isEmpty()) {
            int[] data = response.getData();
            int row = data[0];
            int col = data[1];
            CellType type = CellType.values()[data[2]];
            applyHit(RIGHT, row, col, type);

            isUpdated = true;
            response.clear();
        }
    }

    private void changeWhoPlay() {
        whoPlay ^= 1;
    }

    private void setDeadShip(int row, int col) {
        fields[RIGHT].setCell(row, col, CellType.SHIP);
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        if (row == 0)
            up = false;
        if (row == SIZE-1)
            down = false;
        if (col == 0)
            left = false;
        if (col == SIZE-1)
            right = false;

        if (up && fields[RIGHT].getCell(row-1, col) == CellType.HIT)
            setDeadShip(row-1, col);
        if (down && fields[RIGHT].getCell(row+1, col) == CellType.HIT)
            setDeadShip(row+1, col);
        if (left && fields[RIGHT].getCell(row, col-1) == CellType.HIT)
            setDeadShip(row, col-1);
        if (right && fields[RIGHT].getCell(row, col+1) == CellType.HIT)
            setDeadShip(row, col+1);
    }

    private void applyHit(int side, int row, int col, CellType type) {
        if (type != CellType.SHIP) {
            fields[side].setCell(row, col, type);
        } else {
            setDeadShip(row, col);
        }
        changeWhoPlay();
    }

    private boolean checkDeadShip(int row, int col, Field field) {
        CellType type = field.getCell(row, col);
        field.setCell(row, col, CellType.MISS);
        if (type == CellType.SHIP)
            return false;
        if (type != CellType.HIT)
            return true;
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        if (row == 0)
            up = false;
        if (row == SIZE-1)
            down = false;
        if (col == 0)
            left = false;
        if (col == SIZE-1)
            right = false;

        boolean wrongFlag = false;
        if (up && !checkDeadShip(row-1, col, field))
            wrongFlag = true;
        if (down && !checkDeadShip(row+1, col, field))
            wrongFlag = true;
        if (left && !checkDeadShip(row, col-1, field))
            wrongFlag = true;
        if (right && !checkDeadShip(row, col+1, field))
            wrongFlag = true;

        if (wrongFlag)
            return false;
        return true;
    }

    private CellType checkHit(int row, int col) {
        CellType type = fields[LEFT].getCell(row, col);
        CellType newType;

        if (type == CellType.EMPTY)
            newType = CellType.MISS;
        else
            newType = CellType.HIT;

        applyHit(LEFT, row, col, newType);

        Field field = new Field(fields[LEFT]);
        if (field.getCell(row, col) == CellType.HIT &&
            checkDeadShip(row, col, field))
            return CellType.SHIP;
        return newType;
    }

    private void resetPrepare() {
        state = State.PREPARE;
        fields[LEFT].clear();
    }

    private int doBfs(int row, int col, Field field) {
        field.setCell(row, col, CellType.MISS);
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        if (row == 0)
            up = false;
        if (row == SIZE-1)
            down = false;
        if (col == 0)
            left = false;
        if (col == SIZE-1)
            right = false;

        boolean neighUp = up;
        boolean neighDown = down;
        boolean neighLeft = left;
        boolean neighRight = right;

        if (neighUp && !field.isCellSet(row-1, col))
            neighUp = false;
        if (neighDown && !field.isCellSet(row+1, col))
            neighDown = false;
        if (neighLeft && !field.isCellSet(row, col-1))
            neighLeft = false;
        if (neighRight && !field.isCellSet(row, col+1))
            neighRight = false;

        boolean wrongFlag = false;
        if (neighUp && neighLeft || neighUp && neighRight ||
            neighDown && neighLeft || neighDown && neighRight)
            wrongFlag = true;

        if (up && field.getCell(row-1, col) != CellType.SHIP)
            up = false;
        if (down && field.getCell(row+1, col) != CellType.SHIP)
            down = false;
        if (left && field.getCell(row, col-1) != CellType.SHIP)
            left = false;
        if (right && field.getCell(row, col+1) != CellType.SHIP)
            right = false;

        int sumResult = 1;
        int result;
        if (up) {
            result = doBfs(row-1, col, field);
            sumResult += result;
            if (result == 0)
                wrongFlag = true;
        }
        if (down) {
            result = doBfs(row+1, col, field);
            sumResult += result;
            if (result == 0)
                wrongFlag = true;
        }
        if (left) {
            result = doBfs(row, col-1, field);
            sumResult += result;
            if (result == 0)
                wrongFlag = true;
        }
        if (right) {
            result = doBfs(row, col+1, field);
            sumResult += result;
            if (result == 0)
                wrongFlag = true;
        }

        if (wrongFlag)
            return 0;
        return sumResult;
    }

    private boolean checkIsAllowed() {
        Field field = new Field(fields[LEFT]);
        int shipSize = 0;

        int[] numShips = new int[NUM_KINDS+1];
        numShips[4] = NUM_LARGE;
        numShips[3] = NUM_MIDDLE;
        numShips[2] = NUM_SMALL;
        numShips[1] = NUM_MINI;

        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j)
                if (field.getCell(i, j) == CellType.SHIP) {
                    shipSize = doBfs(i, j, field);
                    if (shipSize == 0)
                        return false;
                    numShips[shipSize]--;
                }

        for (int i = 1; i <= NUM_KINDS; ++i)
            if (numShips[i] != 0)
                return false;
        return true;
    }

    private int getOpponentPort(int myPort) {
        if (myPort == DEFAULT_PORT)
            return DEFAULT_PORT+1;
        return DEFAULT_PORT;
    }

    private String pack(int... integers) {
        String result = "";
        for (int i : integers)
            result += "_" + Integer.toString(i);
        return result;
    }

    private void send(String data, int port) throws Exception {
        Socket clientSocket = new Socket("localhost", port);
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeBytes(data + '\n');
        clientSocket.close();
    }

    private void sendCommon(String prefix, int... integers) {
        String data = prefix + pack(integers);
        int port = getOpponentPort(thread.getPort());
        try {
            send(data, port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendRequest(int row, int col) {
        sendCommon(REQUEST_PREFIX, row, col);
    }

    private void sendResponse(int row, int col, int type) {
        sendCommon(RESPONSE_PREFIX, row, col, type);
    }
}

package model;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;

import model.CellType;
import model.Field;
import model.IpAddress;
import model.Message;
import model.GameState;
import model.TcpThread;
import static model.Constants.*;

public class Model {

    private GameState state;
    private Field[] fields;
    private boolean isReady;
    private boolean isUpdated;
    private boolean arrangementError;
    private boolean connectionError;
    private int whoPlay;
    private int whoWon;

    private TcpThread thread;
    private volatile IpAddress oppAddress;
    private volatile boolean oppReady;
    private volatile Message request;
    private volatile Message response;

    public Model() {
        state = GameState.MENU;

        fields = new Field[2];
        fields[LEFT] = new Field();
        fields[RIGHT] = new Field();

        isReady = false;
        isUpdated = true;
        arrangementError = false;
        connectionError = false;
        whoPlay = LEFT;
        whoWon = NONE;

        oppAddress = new IpAddress();
        oppReady = false;
        request = new Message();
        response = new Message();
    }

    public GameState getGameState() {
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

    public boolean getArrangementError() {
        return arrangementError;
    }

    public boolean getConnectionError() {
        return connectionError;
    }

    public int getWhoPlay() {
        return whoPlay;
    }

    public int getWhoWon() {
        return whoWon;
    }

    public void setOppAddress(IpAddress oppAddress) {
        this.oppAddress = oppAddress;
    }

    public void setOppReady(boolean oppReady) {
        this.oppReady = oppReady;
    }

    public void setRequest(Message request) {
        this.request = request;
    }

    public void setResponse(Message response) {
        this.response = response;
    }

    public void waitOppReady() {
        if (!checkIsAllowed()) {
            resetPrepare();
            return;
        }

        sendRequest(0, 0);
        if (oppReady)
            update(GameState.GAME);
        else
            state = GameState.WAIT_READY;

        isUpdated = true;
    }

    public void doConnect(String address) {
        try {
            oppAddress = new IpAddress(address);
        } catch (Exception ex) {
            return;
        }

        sendRequest(SECOND_PORT, 0);
        state = GameState.PREPARE;
        isUpdated = true;
    }

    public void tryHit(int side, int row, int col) {
        if (whoWon != NONE || state == GameState.WAIT || state == GameState.WAIT_READY)
            return;

        if (isReady && side == RIGHT && whoPlay == LEFT &&
            !fields[side].isCellSet(row, col)) {
            sendRequest(row, col);
        } else if (!isReady && side == LEFT) {
            if (fields[side].isCellSet(row, col))
                fields[side].setCell(row, col, CellType.EMPTY);
            else
                fields[side].setCell(row, col, CellType.SHIP);
            isUpdated = true;
        }
    }

    public void update(GameState state) {
        this.state = state;

        switch (state) {
            case WAIT:
                thread = new TcpThread(this, FIRST_PORT);
                thread.start();
                break;
            case CONNECT:
                whoPlay = RIGHT;
                thread = new TcpThread(this, SECOND_PORT);
                thread.start();
                break;
            case GAME:
                isReady = true;
                break;
        }

        isUpdated = true;
    }

    public void update() {
        if (state == GameState.WAIT)
            if (!oppAddress.isEmpty()) {
                state = GameState.PREPARE;
                isUpdated = true;
            }

        if (state == GameState.WAIT_READY)
            if (oppReady)
                update(GameState.GAME);

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

    public void loadArrangement(String path) {
        List<String> lines = new ArrayList<String>();

        try {
            lines = Files.readAllLines(Paths.get(path),
                                       StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        int i = 0;
        for (String s : lines) {
            for (int j = 0; j < s.length(); ++j) {
                if (s.charAt(j) == '1')
                    fields[LEFT].setCell(i, j, CellType.SHIP);
                else
                    fields[LEFT].setCell(i, j, CellType.EMPTY);
            }
            i++;
        }

        isUpdated = true;
    }

    public void saveArrangement(String path) {
        List<String> lines = new ArrayList<String>();
        String s;

        for (int i = 0; i < SIZE; ++i) {
            s = "";
            for (int j = 0; j < SIZE; ++j) {
                if (fields[LEFT].getCell(i, j) == CellType.SHIP)
                    s += "1";
                else
                    s += "0";
            }
            lines.add(s);
        }

        try {
            Files.write(Paths.get(path), lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void changeWhoPlay() {
        whoPlay ^= 1;
    }

    private void setDeadShip(int row, int col) {
        fields[RIGHT].setCell(row, col, CellType.DEAD);
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
        if (type != CellType.DEAD && type != CellType.DEAD_WON) {
            fields[side].setCell(row, col, type);
            if (type == CellType.MISS)
                changeWhoPlay();
        } else {
            setDeadShip(row, col);
            if (type == CellType.DEAD_WON) {
                whoWon = LEFT;
                state = GameState.VICTORY;
            }
        }
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

    private boolean checkVictory() {
        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j)
                if (fields[LEFT].getCell(i, j) == CellType.SHIP)
                    return false;
        return true;
    }

    private CellType checkHit(int row, int col) {
        Field field = new Field(fields[LEFT]);

        CellType type = field.getCell(row, col);
        CellType newType;

        if (type == CellType.EMPTY)
            newType = CellType.MISS;
        else
            newType = CellType.HIT;

        applyHit(LEFT, row, col, newType);
        field.setCell(row, col, newType);

        if (newType == CellType.HIT && checkDeadShip(row, col, field)) {
            if (checkVictory()) {
                whoWon = RIGHT;
                state = GameState.VICTORY;
                return CellType.DEAD_WON;
            }
            return CellType.DEAD;
        }
        return newType;
    }

    private void resetPrepare() {
        state = GameState.PREPARE;
        arrangementError = true;
        isUpdated = true;
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

    private String pack(int... integers) {
        String result = "";
        for (int i : integers)
            result += "_" + Integer.toString(i);
        return result;
    }

    private void send(String data, IpAddress addr) throws Exception {
        Socket clientSocket = new Socket(addr.getHost(), addr.getPort());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        out.writeBytes(data + '\n');
        clientSocket.close();
    }

    private void sendCommon(String prefix, int... integers) {
        String data = prefix + pack(integers);
        try {
            send(data, oppAddress);
        } catch (Exception ex) {
            connectionError = true;
            isUpdated = true;
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

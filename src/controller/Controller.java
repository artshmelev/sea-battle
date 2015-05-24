package controller;

import model.Model;
import model.State;

public class Controller {

    private Model model;

    public Controller(Model model) {
        this.model = model;
    }

    public void handleStartEvent() {
        model.update(State.PREPARE);
    }

    public void handleConnectEvent() {
        model.update(State.CONNECT);
    }

    public void handleReadyEvent() {
        model.update(State.GAME);
    }

    public void handleDoConnectEvent(String address) {
        System.out.println(address);
    }

    public void handleHitEvent(int side, int row, int col) {
        model.tryHit(side, row, col);
    }

    public void handleQuitEvent() {
        System.exit(0);
    }
}

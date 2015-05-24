package controller;

import model.Model;
import model.GameState;

public class Controller {

    private Model model;

    public Controller(Model model) {
        this.model = model;
    }

    public void handleStartEvent() {
        model.update(GameState.WAIT);
    }

    public void handleConnectEvent() {
        model.update(GameState.CONNECT);
    }

    public void handleReadyEvent() {
        model.waitOppReady();
    }

    public void handleDoConnectEvent(String address) {
        model.doConnect(address);
    }

    public void handleHitEvent(int side, int row, int col) {
        model.tryHit(side, row, col);
    }

    public void handleLoadFileEvent(String path) {
        model.loadArrangement(path);
    }

    public void handleSaveFileEvent(String path) {
        model.saveArrangement(path);
    }

    public void handleQuitEvent() {
        System.exit(0);
    }
}

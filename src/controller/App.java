package controller;
import model.Model;
import view.View;
import controller.Controller;

public class App {

    private Model model;
    private View view;
    private Controller controller;

    public App() {
        model = new Model();
        controller = new Controller(model);
        view = new View(model, controller);
    }

    public void run() {
        while (true) {
            model.update();
            view.update();

            try {
                Thread.sleep(17);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


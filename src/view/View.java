package view;
import java.awt.*;
import java.awt.event.*;

import controller.Controller;
import model.Model;

import model.Cell;
import model.CellType;
import model.Field;
import model.State;
import view.CellButton;
import static model.Constants.*;

public class View {

    private Model model;
    private Controller controller;

    private Frame frame;
    private CellButton[][] leftButtons;
    private CellButton[][] rightButtons;
    private TextField textField;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    public View(Model model, Controller controller) {
        this.model = model;
        this.controller = controller;

        frame = new Frame("Sea-Battle");
        frame.setSize(WIDTH, HEIGHT);
        frame.setLayout(new CardLayout());
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        Panel menuPanel = new Panel(new GridLayout(3, 1));
        menuPanel.setSize(WIDTH, HEIGHT);

        Button startButton = new Button("START");
        Button connectButton = new Button("CONNECT TO");
        Button quitButton = new Button("QUIT");
        startButton.setActionCommand("START");
        connectButton.setActionCommand("CONNECT");
        quitButton.setActionCommand("QUIT");
        startButton.addActionListener(new ButtonClickListener());
        connectButton.addActionListener(new ButtonClickListener());
        quitButton.addActionListener(new ButtonClickListener());

        menuPanel.add(startButton);
        menuPanel.add(connectButton);
        menuPanel.add(quitButton);

        Panel connectMenuPanel = new Panel(new FlowLayout());

        connectMenuPanel.setSize(WIDTH, HEIGHT);
        textField = new TextField("Enter HOST:PORT", 21);
        Button doConnectButton = new Button("Connect");
        doConnectButton.setActionCommand("DO_CONNECT");
        doConnectButton.addActionListener(new ButtonClickListener());

        connectMenuPanel.add(textField);
        connectMenuPanel.add(doConnectButton);

        Panel gamePanel = new Panel(new GridLayout(1, 2));
        gamePanel.setSize(WIDTH, HEIGHT);

        Panel leftPanel = new Panel();
        Panel rightPanel = new Panel();

        leftButtons = new CellButton[SIZE][SIZE];
        rightButtons = new CellButton[SIZE][SIZE];

        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j) {
                leftButtons[i][j] = new CellButton(" ", i, j);
                leftButtons[i][j].setPreferredSize(new Dimension(33, 33));
                leftButtons[i][j].setActionCommand("LEFT_BUTTONS");
                leftButtons[i][j].addActionListener(new ButtonClickListener());

                rightButtons[i][j] = new CellButton(" ", i, j);
                rightButtons[i][j].setPreferredSize(new Dimension(33, 33));
                rightButtons[i][j].setActionCommand("RIGHT_BUTTONS");
                rightButtons[i][j].addActionListener(new ButtonClickListener());

                leftPanel.add(leftButtons[i][j]);
                rightPanel.add(rightButtons[i][j]);
            }

        Button readyButton = new Button("Ready");
        readyButton.setActionCommand("READY");
        readyButton.addActionListener(new ButtonClickListener());
        leftPanel.add(readyButton);

        gamePanel.add(leftPanel);
        gamePanel.add(rightPanel);

        frame.add(menuPanel);
        frame.add(connectMenuPanel);
        frame.add(gamePanel);

        frame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals("START")) {
                controller.handleStartEvent();
            } else if (cmd.equals("CONNECT")) {
                controller.handleConnectEvent();
            } else if (cmd.equals("QUIT")) {
                controller.handleQuitEvent();
            } else if (cmd.equals("LEFT_BUTTONS")) {
                CellButton btn = (CellButton)e.getSource();
                controller.handleHitEvent(LEFT, btn.getRow(), btn.getCol());
            } else if (cmd.equals("RIGHT_BUTTONS")) {
                CellButton btn = (CellButton)e.getSource();
                controller.handleHitEvent(RIGHT, btn.getRow(), btn.getCol());
            } else if (cmd.equals("READY")) {
                controller.handleReadyEvent();
            } else if (cmd.equals("DO_CONNECT")) {
                controller.handleDoConnectEvent(textField.getText());
            }
        }
    }

    public void update() {
        if (model.isUpdated()) {
            model.setUpdated(false);

            CardLayout layout = (CardLayout)frame.getLayout();
            switch (model.getState()) {
                case MENU:
                    layout.first(frame);
                    break;
                case CONNECT:
                    layout.next(frame);
                    break;
                case PREPARE:
                case GAME:
                    Field[] fields = model.getFields();

                    for (int k = 0; k < fields.length; ++k)
                        for (int i = 0; i < SIZE; ++i)
                            for (int j = 0; j < SIZE; ++j) {
                                Color color = new Color(120, 120, 120);

                                switch (fields[k].getCell(i, j)) {
                                    case SHIP:
                                        color = Color.blue;
                                        break;
                                    case MISS:
                                        color = new Color(200, 200, 200);
                                        break;
                                    case HIT:
                                        color = Color.red;
                                        break;
                                }

                                if (k == 0)
                                    leftButtons[i][j].setBackground(color);
                                else
                                    rightButtons[i][j].setBackground(color);
                            }

                    layout.last(frame);
                    break;
                case VICTORY:
                    break;
            }
        }
    }
}

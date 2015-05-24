package view;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import controller.Controller;
import model.Model;

import model.Cell;
import model.CellType;
import model.Field;
import model.GameState;
import view.CellButton;
import static model.Constants.*;

public class View {

    private Model model;
    private Controller controller;

    private Frame frame;
    private CellButton[][] leftButtons;
    private CellButton[][] rightButtons;
    private TextField textField;
    private Label notifyLabel;
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
        frame.setResizable(false);

        Font menuFont = new Font("Monospace", Font.BOLD, 30);
        Font myFont = new Font("Monospace", Font.PLAIN, 20);

        Panel menuPanel = new Panel(new GridLayout(3, 1));
        menuPanel.setSize(WIDTH, HEIGHT);

        Button startButton = new Button("START");
        Button connectButton = new Button("CONNECT TO");
        Button quitButton = new Button("QUIT");
        startButton.setFont(menuFont);
        connectButton.setFont(menuFont);
        quitButton.setFont(menuFont);
        startButton.setActionCommand("START");
        connectButton.setActionCommand("CONNECT");
        quitButton.setActionCommand("QUIT");
        startButton.addActionListener(new ButtonClickListener());
        connectButton.addActionListener(new ButtonClickListener());
        quitButton.addActionListener(new ButtonClickListener());

        menuPanel.add(startButton);
        menuPanel.add(connectButton);
        menuPanel.add(quitButton);

        Panel connectMenuPanel = new Panel(new GridLayout(3, 1));
        connectMenuPanel.setSize(WIDTH, HEIGHT);
        connectMenuPanel.add(new Panel());

        Panel connectMenuSubPanel = new Panel();
        textField = new TextField("Enter HOST:PORT", 21);
        textField.setFont(myFont);
        Button doConnectButton = new Button("Connect");
        doConnectButton.setFont(myFont);
        doConnectButton.setActionCommand("DO_CONNECT");
        doConnectButton.addActionListener(new ButtonClickListener());

        connectMenuSubPanel.add(textField);
        connectMenuSubPanel.add(doConnectButton);
        connectMenuPanel.add(connectMenuSubPanel);
        connectMenuPanel.add(new Panel());

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

        notifyLabel = new Label("");
        notifyLabel.setFont(myFont);
        rightPanel.add(notifyLabel);

        Button loadButton = new Button("Load");
        loadButton.setFont(myFont);
        loadButton.setActionCommand("LOAD");
        loadButton.addActionListener(new ButtonClickListener());
        leftPanel.add(loadButton);

        Button saveButton = new Button("Save");
        saveButton.setFont(myFont);
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(new ButtonClickListener());
        leftPanel.add(saveButton);

        Button readyButton = new Button("Ready");
        readyButton.setFont(myFont);
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
                if (model.getGameState() != GameState.PREPARE)
                    return;
                controller.handleReadyEvent();
            } else if (cmd.equals("DO_CONNECT")) {
                controller.handleDoConnectEvent(textField.getText());
            } else if (cmd.equals("LOAD")) {
                if (model.getGameState() != GameState.PREPARE)
                    return;

                FileDialog dialog = new FileDialog(new Frame());
                dialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".txt");
                    }
                });
                dialog.setVisible(true);

                if (dialog.getFile() != null) {
                    String path = dialog.getDirectory() + dialog.getFile();
                    controller.handleLoadFileEvent(path);
                }
            } else if (cmd.equals("SAVE")) {
                if (model.getGameState() != GameState.PREPARE)
                    return;

                FileDialog dialog = new FileDialog(new Frame(), "Save", FileDialog.SAVE);
                dialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".txt");
                    }
                });
                dialog.setFile("Untitled.txt");
                dialog.setVisible(true);

                if (dialog.getFile() != null) {
                    String path = dialog.getDirectory() + dialog.getFile();
                    controller.handleSaveFileEvent(path);
                }
            }
        }
    }

    private void updateFields() {
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
                        case DEAD:
                            color = Color.black;
                            break;
                    }

                    if (k == 0)
                        leftButtons[i][j].setBackground(color);
                    else
                        rightButtons[i][j].setBackground(color);
                }
    }

    private String getPrepareMsg() {
        if (!model.getArrangementError())
            return "Place your ships left";
        return "Error: wrong ships arrangement";
    }

    private String getWhoPlayMsg() {
        if (model.getConnectionError())
            return "Cannot connect to opponent";

        if (model.getWhoPlay() == LEFT)
            return "You move";
        return "Opponent move";
    }

    private String getWhoWonMsg() {
        int who = model.getWhoWon();
        if (who == LEFT)
            return "You won";
        else if (who == RIGHT)
            return "You lost";
        return "";
    }

    public void update() {
        if (model.isUpdated()) {
            model.setUpdated(false);

            CardLayout layout = (CardLayout)frame.getLayout();
            switch (model.getGameState()) {
                case MENU:
                    layout.first(frame);
                    break;
                case WAIT:
                    updateFields();
                    notifyLabel.setText("Waiting opponent to connect");
                    layout.last(frame);
                    break;
                case CONNECT:
                    layout.next(frame);
                    break;
                case PREPARE:
                    updateFields();
                    notifyLabel.setText(getPrepareMsg());
                    layout.last(frame);
                    break;
                case WAIT_READY:
                    updateFields();
                    notifyLabel.setText("Waiting opponent to ready");
                    layout.last(frame);
                    break;
                case GAME:
                    updateFields();
                    notifyLabel.setText(getWhoPlayMsg());
                    layout.last(frame);
                    break;
                case VICTORY:
                    updateFields();
                    notifyLabel.setText(getWhoWonMsg());
                    layout.last(frame);
                    break;
            }
        }
    }
}

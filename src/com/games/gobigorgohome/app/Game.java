package com.games.gobigorgohome.app;

import com.apps.util.Prompter;
import com.games.gobigorgohome.*;
import com.games.gobigorgohome.characters.Player;
import com.games.gobigorgohome.parsers.ParseJSON;
import com.games.gobigorgohome.parsers.ParseTxt;
import com.games.gobigorgohome.parsers.SoundHandler;
import org.json.simple.JSONArray;
import com.apps.util.Console;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Game {

    boolean isGameOver = false;

    private final Gym gym = Gym.getInstance();
    private final Player player = new Player();
    private final int energy = player.getEnergy();
    private final int currentEnergy = player.getEnergy();
    private final String playerName = player.getName();
    private String currentRoomName = gym.getStarterRoomName();
    private final String musicPath = "sounds/gainz.wav";
    private final String doorFxPath = "sounds/door.wav";
    private final String steroidFxPath = "sounds/airhorn.wav";
    private final String energyDrinkFxPath = "sounds/energy-drink.wav";
    private final String quitFxPath = "sounds/goodbye.wav";
    private final String inspectFxPath = "sounds/inspect.wav";
    private final String loseFxPath = "sounds/lose.wav";
    private final String winFxPath = "sounds/player-wins.wav";
    private final String workoutFxPath = "sounds/woo.wav";
    private final String getItemFxPath = "sounds/item-picked-up.wav";
    private Room currentRoom = gym.getStarterRoom();
    private final Object rooms = gym.getRooms();
    private final Prompter gamePrompt;
    private final ParseTxt page = new ParseTxt();
    private final ParseJSON jsonParser = new ParseJSON();
    private JFrame frame;
    private GameMap gamemap = new GameMap(gym.getStarterRoomName());
    private SoundHandler musicHandler = new SoundHandler();
    private SoundHandler fxHandler = new SoundHandler();
    PlayerBody playerBody;
    Container container;
    JMenuBar menuBar = new JMenuBar();
    JPanel gameTextArea;
    JPanel mapPanel;
    JPanel imagePanel;
    UserInput userInput;
    JTextField textInput = new JTextField(20);
    JLabel instructionText;
    GamePrompter gamePrompter2;


    public Game(Prompter prompter) throws IOException, ParseException {
        this.gamePrompt = prompter;
    }

    //    collects current input from user to update their avatar

    private void getNewPlayerInfo() {
        String playerName = validName();
        double playerHeight = validDouble("What is your height? ", "height", "inches");
        double playerWeight = validDouble("What is your weight? ", "weight", "lbs");
        int playerAge = validInt("What is your age? ", "age", "years");
        createPlayer(playerName, playerAge, playerHeight, playerWeight);
        gamePrompter2.display(gameStatus());
    }

    // validates name requesting one and rejecting empty space(s).

    private String validName() {
//        String text = textInput1.getText();
        String playerName = gamePrompter2.prompt("What is your name? ");
        gamePrompter2.display(playerName);
        if (playerName.isBlank() || playerName.isEmpty() || playerName.length() > 16) {
            try {
                gamePrompter2.display("You need to type your name or it exceeds 16 characters: ");
                validName();
            } catch (NullPointerException e) {
                gamePrompter2.display("You need to type your name: ");
                validName();
            }
        } else {
            gamePrompter2.display("Hello " + playerName + " let's get more about you...");
        }
        return playerName;
    }

    // validates height and weight taking integers or doubles only

    private double validDouble(String msg, String measureName, String unit) {
        String measurementString = gamePrompter2.prompt(msg);
        double measurement = 0;
        try {
            measurement = Double.parseDouble(measurementString);
            //validDouble(measure, "you need to type your " + measureName + " in " + unit + ": ", measureName, unit);
        } catch (NumberFormatException | NullPointerException e) {
            gamePrompter2.display("You need to type your " + measureName + " using numbers (" + unit + "): " + measureName + unit);
            return validDouble("", measureName, unit);
        }
        return measurement;
    }
    // validates age taking only an integer

    private int validInt(String msg, String measureName, String unit) {
        String measurement = gamePrompter2.prompt(msg);
        int measureNum = 0;
        try {
            measureNum = Integer.parseInt(measurement);
            //validInt(measure, "you need to type your "+ measureName+" in " + unit + " or you aren't an adult: ", measureName, unit);
        } catch (NumberFormatException e) {
            gamePrompter2.display("You need to type your " + measureName + " using numbers integers (" + unit + "): " + measureName + unit);
            return validInt("", measureName, unit);
        }
        return measureNum;
    }

    private void createPlayer(String playerName, int playerAge, double playerHeight, double playerWeight) {
        player.setName(playerName);
        player.setAge(playerAge);
        player.setHeight(playerHeight);
        player.setWeight(playerWeight);
    }

    //    updates player with current game status e.g. player inventory, current room etc.
    private String gameStatus() {
        StringBuilder status = new StringBuilder();
        status.append("------------------------------\n");
        status.append("Commands: GO <room name>, GET <item>, CONSUME <item>, WORKOUT <workout name>, HEAL <player name>\n (Hit Q to quit)\n\n");
        status.append("You are in the " + currentRoomName + " room.\n");
        status.append(inspectRoom());
        status.append(player.toString() + "\n");
        status.append("------------------------------\n");
        return status.toString();


    }

    //    main function running the game, here we call all other functions necessary to run the game
    public void playGame() throws IOException, ParseException {
        MainFrame();

        musicHandler.RunMusic(musicPath);

//        System.out.println(page.instructions());

        getNewPlayerInfo();
        // runs a while loop
        while (!isGameOver()) {
            gameStatus();
            promptForPlayerInput();
            if (checkGameStatus()) {
                break;
            }
        }
        gameResult();


    }

    private void newGame() throws IOException, ParseException, InterruptedException {
        //reset the map
        player.resetBody();
        player.setEnergy(100);
        currentRoomName = "front desk";
        repaintPlayerBody();
        repaintMap();
        gamePrompter2.display(gameStatus());
        getNewPlayerInfo();

    }


    private boolean checkGameStatus() {
        return player.isWorkoutComplete() || player.isSteroidsUsed() || player.isExhausted();
    }

    private void gameResult() {
        Console.clear();
        String result = "";
        if (player.isSteroidsUsed()) {
            musicHandler.playFx(loseFxPath);
            result = "YOU ARE A LOSER AND A CHEATER!";
        } else if (player.isExhausted()) {
            result = "You're too tired, go home dude";
        } else if (player.isWorkoutComplete()) {
            musicHandler.playFx(winFxPath);
            result = "CONGRATULATIONS! YOU WORKED OUT!";
        }
        gamePrompter2.display(result);

    }

    public void promptForPlayerInput() throws IOException, ParseException {
        String command = gamePrompter2.prompt("What is your move?");
//        gamePrompter2.display(gameStatus());
        String[] commandArr = command.split(" ");
        parseThroughPlayerInput(commandArr);
    }


    public void parseThroughPlayerInput(String[] action) throws IOException, ParseException {

        List<String> actionList = Arrays.asList(action);

        String actionPrefix = "";
        String playerAction = "";

        if (actionList.size() >= 1) {
            actionPrefix = actionList.get(0);
        }
        if (actionList.size() == 2) {
            playerAction = actionList.get(1);
        } else if (actionList.size() == 3) {
            playerAction = (actionList.get(1) + " " + actionList.get(2));
        }

        validatePlayerCommands(actionPrefix.toLowerCase(), playerAction.toLowerCase());
    }

    private void validatePlayerCommands(String actionPrefix, String playerAction) throws IOException, ParseException {
        try {
            switch (actionPrefix) {
                case "get":
                    musicHandler.playFx(getItemFxPath);
                    grabItem(playerAction);
                    gamePrompter2.display(gameStatus());
                    break;
                case "go":
                    // Console.clear();
                    fxHandler.playFx(doorFxPath);
                    goSomewhere(playerAction);
                    break;
                case "workout":
                    musicHandler.playFx(workoutFxPath);
                    playerUseMachine(playerAction);
                    break;
                case "consume":
                    if (playerAction.equals("energy drink")) {
                        musicHandler.playFx(energyDrinkFxPath);
                    } else if (playerAction.equals("steroids")) {
                        musicHandler.playFx(steroidFxPath);
                    }

                    if (player.consumeItem(playerAction)) {
                        player.removeItemFromInventory(playerAction);
                    }
                    repaintPlayerBody();
                    gamePrompter2.display(gameStatus());
                    break;
//                case "inspect":
//                    musicHandler.playFx(inspectFxPath);
//                    //inspectRoom();
//                    break;
                case "talk":
                    talkToNPC();
                    break;
                case "q":
                    musicHandler.playFx(quitFxPath);
                    quit();
                    break;
                case "new":
                    newGame();
                    break;
                case "up":
                    musicHandler.musicVolumeUp();
                    break;
                case "down":
                    musicHandler.musicVolumeDown();
                    break;
                case "mute":
                    musicHandler.muteMusicVolume();
                    break;
                case "heal":
                    if(player.getName().equals(playerAction)){
                        player.setEnergy(100);
                        gamePrompter2.display(gameStatus());
                        repaintPlayerBody();
                    }
                    break;
            }
        } catch (Exception exception) {
          exception.printStackTrace();
            gamePrompter2.display(actionPrefix + " was sadly and invalid answer. \n please ensure you are using a valid and complete command. ");
            promptForPlayerInput();
        }
    }


    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    public static boolean isItemRequired(List items) {
        return !"none".equals(items.get(0));
    }

    private void getRoomMap() throws IOException {
        currentRoom.getRoomMap(currentRoomName);
    }


    private void handleInput(String input) throws IOException, ParseException {
        gameStatus();
        String[] commandArr = input.split(" ");
        parseThroughPlayerInput(commandArr);

    }

    private void talkToNPC() {
        String dialog = currentRoom.getNpc().generateDialog();
        gamePrompter2.display(dialog);

        String npcItem = (String) currentRoom.npc.getInventory().get(0);

        player.getInventory().add(npcItem);
        gamePrompter2.display("You added " + npcItem + " to your gym bag.");
    }

    private String inspectRoom() {
        return currentRoom.toString();
    }

    private void playerUseMachine(String playerExcerciseInput) {
        //gamePrompter2.display("you're using the: " + playerExcerciseInput);
        Object exercises = getCurrentRoom().getExercises();
    try {
        Exercise exercise = new Exercise(exercises, playerExcerciseInput);
        Object targetMuscle = exercise.getTargetMuscles();
        String exerciseStatus = exercise.getExerciseStatus();
        Long energyCost = exercise.getEnergyCost();
        if ("fixed".equals(exerciseStatus)) {
            player.workout(targetMuscle, energyCost);
            player.subtractFromPlayerEnergy(Math.toIntExact(energyCost));
            repaintPlayerBody();
        } else {
            fixBrokenMachine(targetMuscle, energyCost);
        }
        gamePrompter2.display(gameStatus());

    }catch (Exception e){
        gamePrompter2.display("Sorry we we didn't understand Workout + " + playerExcerciseInput);

    }



    }
    private void goSomewhere(String playerAction){
        if (jsonParser.getObjectFromJSONObject(rooms, playerAction) != null) {
            gamePrompter2.display("you're going here: " + playerAction);
            currentRoomName = playerAction;
            setCurrentRoom(jsonParser.getObjectFromJSONObject(rooms, playerAction));
            gamePrompter2.display(gameStatus());
        } else {
            gamePrompter2.display("Sorry we didn't recognize GO " + playerAction);
        }
        repaintMap();
    }
    private void fixBrokenMachine(Object targetMuscle, Long energyCost) {
        if (player.getInventory().contains("wrench")) {
            String playerResponse = gamePrompter2.prompt("This machine is broken. Would you like to use your wrench to fix it? (y/n) \n >");
            if ("y".equalsIgnoreCase(playerResponse)) {
                player.getInventory().remove("wrench");
                player.workout(targetMuscle, energyCost);
                player.subtractFromPlayerEnergy(Math.toIntExact(energyCost));
            } else {
                gamePrompter2.display("When you are ready to workout, come back with the wrench and get to it.");
            }
        } else {
            gamePrompter2.display("This machine is broken, please come back with a wrench to fix it.");
        }
    }

    private void grabItem(String playerAction) {
        final String[] currentItem = new String[1];
        JSONArray roomItemsObjectArray = (JSONArray) currentRoom.getItems();
        roomItemsObjectArray.forEach(item -> {
            if (item.equals(playerAction)) {
                currentItem[0] = (String) item;
            }
        });

        try {
            if (currentItem[0].equals(playerAction)) {
                gamePrompter2.display("\nYou got the : " + playerAction);
                player.getInventory().add(playerAction);
            }
        } catch (Exception e) {
            gamePrompter2.display("\nSorry, you can't GET " + playerAction.toUpperCase() + ". Try again!");
        }
    }

    private void repaintPlayerBody() {
        frame.remove(playerBody);
        playerBody = new PlayerBody(getMuscleGroups(player), player.getEnergy());
        frame.add(playerBody, 2);
        frame.invalidate();
        frame.validate();
        frame.repaint();
    }

    private void repaintMap() {
        frame.remove(gamemap);
        gamemap = new GameMap(currentRoomName);
        frame.add(gamemap, 1);
        frame.invalidate();
        frame.validate();
        frame.repaint();
    }

    public void MainFrame() {
        //frame Setting
        frame = new JFrame("Go Big Or Go Home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.setLayout(new GridLayout(2, 2));
        frame.setTitle("Go Big or Go Home");
        frame.setVisible(true);
        frame.getContentPane().setBackground(Color.BLACK);
        container = frame.getContentPane();
        ImageIcon image = new ImageIcon("gym.png");
        frame.setIconImage(image.getImage());

        //three panels
        gameTextArea = new JPanel();
        mapPanel = new JPanel();
        imagePanel = new JPanel();
        playerBody = new PlayerBody(getMuscleGroups(player), player.getEnergy());
        playerBody.setPanelSize(frame.getWidth() / 2, frame.getHeight() / 2);

        userInput = new UserInput(this);
        gamePrompter2 = userInput;

        mapPanel.setBackground(Color.RED);
        mapPanel.setBounds(500, 0, 200, 200);
        mapPanel.add(gamemap);

        frame.setJMenuBar(menuBar);
        JMenu settings = new JMenu("Sound Settings");
        JMenuItem musicUp = new JMenuItem("+ Music");
        JMenuItem musicDown = new JMenuItem("- Music");
        JMenuItem musicMute = new JMenuItem("Mute Music");
        JMenuItem fxUp = new JMenuItem("+ Fx");
        JMenuItem fxDown = new JMenuItem("- Fx");
        JMenuItem fxMute = new JMenuItem("Mute Fx");

        menuBar.add(settings);
        settings.add(musicUp);
        settings.add(musicDown);
        settings.add(musicMute);
        settings.addSeparator();
        settings.add(fxUp);
        settings.add(fxDown);
        settings.add(fxMute);
        settings.addSeparator();

        musicUp.addActionListener(e -> musicHandler.musicVolumeUp());

        musicDown.addActionListener(e -> musicHandler.musicVolumeDown());

        musicMute.addActionListener(e -> musicHandler.muteMusicVolume());

        fxUp.addActionListener(e -> fxHandler.fxVolumeUp());

        fxDown.addActionListener(e -> fxHandler.fxVolumeDown());

        fxMute.addActionListener(e -> fxHandler.muteFxVolume());


        //set image
        imagePanel.setBackground(Color.YELLOW);

        //setTextArea
        gameTextArea.setBackground(Color.WHITE);


        //set userInput
        userInput.setBackground(Color.BLACK);

        //Set text within text area
        JTextArea wrapperText = new JTextArea(page.instructions(), 16, 46);
        wrapperText.setWrapStyleWord(true);
        wrapperText.setLineWrap(true);
        wrapperText.setOpaque(false);
        wrapperText.setEditable(false);
        wrapperText.setFocusable(false);
        wrapperText.setBackground(UIManager.getColor("Label.background"));
        wrapperText.setFont(UIManager.getFont("Label.font"));
        wrapperText.setBorder(UIManager.getBorder("Label.border"));
        wrapperText.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JScrollPane scroll = new JScrollPane(wrapperText);


        //add text to
        gameTextArea.add(scroll);

        //add components to container
        container.add(gameTextArea);
        container.add(gamemap);

        container.add(playerBody);
        container.add(userInput);

        frame.setResizable(false);
        frame.invalidate();
        frame.validate();
        frame.repaint();

    }

    //    gives player ability to quit
    private void quit() {
        gamePrompter2.display("--------------------------------------\n"
                + " YOU ARE A QUITTER!! GAME OVER" + "" +
                "------------------------------------");
        musicHandler.stopMusic();
        System.exit(0);
    }

    private void setCurrentRoom(Object currentRoom) {

        this.currentRoom = new Room(currentRoom);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    // accessor methods

    public boolean isGameOver() {
        return isGameOver;
    }

    public int getEnergy() {
        return energy;
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean[] getMuscleGroups(Player player) {
        boolean[] muscleGroup = new boolean[6];

        if (player.isLegsWorked()) {
            muscleGroup[0] = true;
        }
        if (player.isBackWorked()) {
            muscleGroup[1] = true;
        }
        if (player.isChestWorked()) {
            muscleGroup[2] = true;
        }
        if (player.isCoreWorked()) {
            muscleGroup[3] = true;
        }
        if (player.isShoulderWorked()) {
            muscleGroup[4] = true;
        }
        if (player.isTricepsWorked()) {
            muscleGroup[5] = true;
        }
        return muscleGroup;
    }

}



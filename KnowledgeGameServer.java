// ref:https://cs.smu.ca/~porter/csc/465/code/deitel/examples/ch24/fig24_13_16/TicTacToeServer.java2html

package knowledgegame;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Formatter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class KnowledgeGameServer extends JFrame {

    private JTextArea outputArea;
    private Player[] players;
    private ServerSocket server;
    private int currentPlayer;
    private final static int PLAYER_1 = 0;
    private final static int PLAYER_2 = 1;
    private final static String[] player1_OR_2 = {"1", "2"};
    private int sessionNo = 1; // Number a session
    private boolean isGameOver = false;
    private String[] Questions;
    private String[] Choices;
    private String[] ans;
    private int rand;
    private int PlayersAns[];
    private boolean shouldTrue;

    // construct and initialize variables
    public KnowledgeGameServer() {
        // title
        super("Knowledge Game Server");

        // create array of players
        players = new Player[2];

        // set current player to first player
        currentPlayer = PLAYER_1;

        //Game over
        isGameOver = false;
        // create array of Questions
        Questions = new String[10];
        // create array of Choices
        Choices = new String[10];
        // create array of Answers
        ans = new String[10];
        // set index to zero , for count number of questions
        rand = 0;
        // create array of count answer to players
        PlayersAns = new int[2];
        // set value to zero
        PlayersAns[0] = 0;
        PlayersAns[1] = 0;
        // boolean for finish join to session 1
        shouldTrue = true;
        try {
            // read data from the file
            File file = new File("Questions.txt");

            BufferedReader br = new BufferedReader(new FileReader(file));

            /*
            read questions 
            ,read choices
            ,read answers
             */
            String st;
            int i = 0;
            while ((st = br.readLine()) != null) {
                Questions[i] = (st.split("  "))[0];
                Choices[i] = "";
                String subch = (st.split("  "))[1];
                for (int j = 0; j < (subch.length()); j++) {
                    if (j + 2 <= (subch.length() - 1) && subch.charAt(j + 1) == 'C' && subch.charAt(j + 2) == '-') {
                        Choices[i] += "\n";
                    } else if (j + 2 <= (subch.length() - 1) && (subch.charAt(j + 1) == 'B' || subch.charAt(j + 1) == 'D') && subch.charAt(j + 2) == '-') {
                        Choices[i] += "      ";
                    } else {
                        Choices[i] += subch.charAt(j);
                    }
                }

                st = br.readLine();
                ans[i] = st;
                i++;
            }
            // set up ServerSocket
            server = new ServerSocket(9000,100);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1);
        }

        // create JTextArea for output
        outputArea = new JTextArea();
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        outputArea.setText(new Date() + ": Server started at socket 9000\n");

        // set size of window
        setSize(450, 450);
        // show window
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void UpdataVariable() {
        // create array of players
        players = new Player[2];

        // set current player to first player
        currentPlayer = PLAYER_1;
        //Game over
        isGameOver = false;
        // set index to zero
        rand = 0;
        // create array of count answer to players
        PlayersAns = new int[2];
        // set value to zero
        PlayersAns[0] = 0;
        PlayersAns[1] = 0;
        // boolean for finish join to session 1
        shouldTrue = true;
        sessionNo++;
        displayMessage("\n");
    }

    // wait for two connections so game can be played
    public void execute() {
      
            while (true) {
                displayMessage("Wait for players to join session " + sessionNo + "\n");
                for (int i = 0; i < players.length; i++) {
                    try // wait for connection, create Player, start runnable
                    {
                        players[i] = new Player(server.accept(), i);
                        // execute player runnable
                        players[i].start();
                       
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                        System.exit(1);
                    }
                }
                 
                // server wait two players to end game for receive two other players
                while (players[0].isAlive() || players[1].isAlive());
                
                  UpdataVariable();
        }
    }

    // display message in outputArea
    private void displayMessage(final String messageToDisplay) {
        // updates outputArea
        SwingUtilities.invokeLater(() -> {
            outputArea.append(messageToDisplay); // add message
        });
    }

    // determine whether Answer is correct
    public boolean CorrectAns(String cli, int qNum) {
        if (ans[qNum].equals(cli)) {
            return true;
        } else {
            return false;
        }
    }

    // private inner class Player manages each Player as a runnable
    private class Player extends Thread {

        private Socket connection;
        private Scanner input;
        private Formatter output;
        private int playerNumber;
        private String  player1or2;
        private boolean suspended = true;

        // set up Player thread
        public Player(Socket socket, int number) {
            // store this player's number
            playerNumber = number;
            // specify player's player1or2
            // player1or2 ---> 0 is player1, player1or2 ---> 1 is player2
            player1or2 = player1_OR_2[playerNumber];
            // store socket for client
            connection = socket;

            try {
                // obtain streams from Socket
                // functions used for sending and receiving information using sockets
                input = new Scanner(connection.getInputStream());
                output = new Formatter(connection.getOutputStream());
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
        }

        // control thread's execution
        @Override
        public void run() {
            // send client its  player1or2 (1 or 2), process messages from client
            try {
                displayMessage(new Date() + ": Player " +  player1or2 + " joined session "+sessionNo+"\n");
                displayMessage("Player " +  player1or2 + "'s IP address " + input.nextLine() + "\n");
                output.format("%s\n",  player1or2); // send player's  player1or2
                output.flush();

                // if player 1, wait for another player to arrive
                if (playerNumber == PLAYER_1) {
                    output.format("%s\n", "Waiting for player 2 to join!");
                    output.flush();

                    //wait until other player join
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(KnowledgeGameServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    // send message that other player connected
                    output.format("Player 2 has joined. You start first!\n");
                    output.flush();
                } else {
                    // notify waiting for player 1
                    synchronized (players[0]) {
                        players[0].notify();
                    }
                    // wait player 1 to start the game 
                    output.format("Waiting for player 1 to start the game!\n");
                    output.flush();
                    // wait players to join to session 2
                    displayMessage(new Date() + ": Start a threat for "+sessionNo+"\n");
                    displayMessage("Wait for players to join session "+sessionNo+"\n");
                    shouldTrue = false;

                }
                while (shouldTrue == true);
                // while game not over
                while (!isGameOver) {

                    // while not current player, must wait for turn
                    if (playerNumber != currentPlayer) {
                        synchronized (this) {
                            try {
                                wait();
                                wait(500);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(KnowledgeGameServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    }
                    // if answer of one player equal of anther 3 at minimum or the question has finished
                    if (Math.abs(PlayersAns[0] - PlayersAns[1]) == 3 || rand == 10) {
                        isGameOver = true;
                        break;
                    }
                    // start the turn of player and set to him the question
                    displayMessage("Now it is  player " +  player1or2 + " turn!\n");
                    output.format("%s\n", "It is now your turn!");
                    output.format("%s\n%s\n", Questions[rand], Choices[rand]);
                    output.flush();
                   

                    // initialize the answer
                    String clientAns = "";

                    if (input.hasNext()) {
                        // get the answer from the client
                        clientAns = input.next();
                    }
                    // check for correct answer
                    if (validateAndMove(clientAns, playerNumber, rand)) {
                        PlayersAns[playerNumber]++;
                        // notify client
                        output.format("Correct Answer.\n");
                        output.format("%d\n", rand);
                        output.flush();
                    } // end if
                    else // answer was not correct
                    {
                        // notify client
                        output.format("Wrong Answer.\n");
                        output.format("%d\n", rand);
                        output.flush();
                    }
                    //increas counter of questions
                    rand++;
                    
                    if (Math.abs(PlayersAns[0] - PlayersAns[1]) == 3 || rand == 10) {
                        isGameOver = true;
                        break;
                    }

                }
                


                // if player 1 won
                if (PlayersAns[0] > PlayersAns[1]) {
                    output.format("Win Player 1\n");
                    output.format("%d\n", playerNumber + 1);
                    output.flush();
                } // if player 2 won
                else if (PlayersAns[1] > PlayersAns[0]) {
                    output.format("Win Player 2\n");
                    output.format("%d\n", playerNumber + 1);
                    output.flush();
                } // if player 1 score equale to player 2
                else {
                    output.format("Game Over\n");
                    output.flush();
                }
            } finally {
                try {
                    // close connection to client
                    connection.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    System.exit(1);
                }
            }
        }

        public boolean validateAndMove(String clientAns, int player, int QuestionNum) {

            // if Answer Correct, make move
            if (CorrectAns(clientAns, QuestionNum)) {
                // change player
                synchronized (players[(currentPlayer + 1) % 2]) {
                    currentPlayer = (currentPlayer + 1) % 2;
                    players[currentPlayer].notify();
                }
                // let new current player know that move occurred
                players[currentPlayer].otherPlayerMoved(true, player, QuestionNum);

                // notify player that answer was correct
                return true;
            } // if Answer not Correct, make move
            else {
                // change player
                synchronized (players[(currentPlayer + 1) % 2]) {
                    currentPlayer = (currentPlayer + 1) % 2;
                    players[currentPlayer].notify();
                }
                // let new current player know that move occurred
                players[currentPlayer].otherPlayerMoved(false, player, QuestionNum);

                // notify player that answer was not correct
                return false;
            }
        }

        // send message(Feedbake) that other player moved
        public void otherPlayerMoved(boolean co, int p, int Qnum) {
            if (co == true) {
                output.format("Correct other.\n");
                output.format("%d\n", p);
                output.format("%d\n", Qnum);
                output.flush();
            } else {
                output.format("Wrong other.\n");
                output.format("%d\n", p);
                output.format("%d\n", Qnum);
                output.flush();
            }

        }

    }

}

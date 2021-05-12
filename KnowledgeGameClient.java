
package knowledgegame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class KnowledgeGameClient extends JFrame{
    private Socket connection;
    private String Host; 
    private Scanner input; 
    private Formatter output;
    private String whoPlayer; 
    private boolean myTurn;
    private final String player_1 = "1";
    private final String player_2 = "2";
    private DateTimeFormatter dtf;
    private LocalDateTime now;
    private boolean endPlayer;
    // Element of GUI used: TextArea , Label , Panel ,RadioButton
    private JTextArea displayArea; 
    private JPanel inputPanel; 
    private JLabel EnterLabel;
    private ButtonGroup group;
    //for Button
    private JRadioButton NoneRadioButton;
    private JRadioButton aRadioButton;
    private JRadioButton bRadioButton;
    private JRadioButton cRadioButton;
    private JRadioButton dRadioButton;
    public KnowledgeGameClient(String host) {
        // set title
        super("Knowledge Game Client");
        // set name of server
        Host = host;
        //end player game
        endPlayer = true;
        // set up JTextArea
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
        
        
        
        // set up new panel
        inputPanel = new JPanel();
        EnterLabel = new JLabel("Select the correct answer:");
        // set up ButtonGroup and JRadioButton
        group = new ButtonGroup();
        NoneRadioButton = new JRadioButton("None");
        aRadioButton = new JRadioButton("A");
        bRadioButton = new JRadioButton("B");
        cRadioButton = new JRadioButton("C");
        dRadioButton = new JRadioButton("D");
        
        ActionListener sliceActionListener = new ActionListener() {
          public void actionPerformed(ActionEvent actionEvent) {
            AbstractButton aButton = (AbstractButton) actionEvent.getSource();
            //get text selected and send answer
            sendChangedField(aButton.getText());
          }
        };
         // add labelto panel
        inputPanel.add(EnterLabel, BorderLayout.CENTER);
        
        // add radio to group and add to panel
        inputPanel.add(NoneRadioButton,BorderLayout.SOUTH);
        group.add(NoneRadioButton);
        inputPanel.add(aRadioButton,BorderLayout.SOUTH);
        group.add(aRadioButton);
        inputPanel.add(bRadioButton,BorderLayout.SOUTH);
        group.add(bRadioButton);
        inputPanel.add(cRadioButton,BorderLayout.SOUTH);
        group.add(cRadioButton);
        inputPanel.add(dRadioButton,BorderLayout.SOUTH);
        group.add(dRadioButton);
        //set the none first value selected
        NoneRadioButton.setSelected(true);
        //add action to radio button
        aRadioButton.addActionListener(sliceActionListener);
        bRadioButton.addActionListener(sliceActionListener);
        cRadioButton.addActionListener(sliceActionListener);
        dRadioButton.addActionListener(sliceActionListener);

        // add panel to window
        add(inputPanel,BorderLayout.SOUTH);
        
        // set size of window
        setSize(450, 550); 
        // show window
        setVisible(true);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startClient();

    }
    // start the client thread
    public void startClient() {
        try // connect to server, get streams and start outputThread
        {
            displayArea.setText(new Date()+": Connectiog to server\n");
            // use TCP sockets for make connection to server
            connection = new Socket(InetAddress.getByName(Host), 9000);
                
            // functions used for sending and receiving information using sockets
            // get streams for input and output
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());
            // send player's mark
            output.format("%s\n", (Host)); 
            output.flush(); 
        
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        run();
    } 

    // control thread that allows continuous update of displayArea
    public void run() {
        // get player's mark (1 or 2)
        whoPlayer = input.nextLine(); 

        SwingUtilities.invokeLater(() -> {
            // add name of player to text area
            displayArea.append("You are player " + whoPlayer + "!\n");
        });
        // determine if client's turn
        myTurn = (whoPlayer.equals(player_1)); 

        // receive messages sent to client and output them
        while (endPlayer) {
            if (input.hasNextLine()) {
                processMessage(input.nextLine());
            }
        }
    }
    
    // process messages received by client
    private void processMessage(String message) {
        // Correct answer
        if (message.equals("Correct Answer.")) {
            int NumQu = Integer.parseInt(input.nextLine());
            displayMessage("You Answered question No."+(NumQu+1)+" Correctly!\n");
        }
        // Wrong answer
        else if (message.equals("Wrong Answer.")) {
            int NumQu = Integer.parseInt(input.nextLine());
            displayMessage("WRONG Answer for question No."+(NumQu+1)+"\n");
        }
        // add the correct of other player to text area
        else if (message.equals("Correct other.")) {
            int current = Integer.parseInt(input.nextLine());
            int NumQu  = Integer.parseInt(input.nextLine());
            displayMessage("Player "+(current+1)+" answered question No."+(NumQu +1)+" Correctly!\n");
            myTurn = true; // now this client's turn
        } 
        // add the wrong of other player to text area
        else if (message.equals("Wrong other.")) {
            int current = Integer.parseInt(input.nextLine());
           int NumQu = Integer.parseInt(input.nextLine());
            displayMessage("Player "+(current+1)+" WRONG ANSWER for question No."+(NumQu +1)+"!\n");
            myTurn = true; // now this client's turn
            
        }
        // if player 1 won
        else if(message.equals("Win Player 1")){
            int pnum = input.nextInt();
            if(pnum == 1)
            {
                displayMessage("YOU WON!\n");

            }
            else
                displayMessage("Player 1 has WON!\n");
            
            endPlayer = false;
            myTurn = false;
            
        }
        // if player 2 won
        else if(message.equals("Win Player 2")){
            int pnum = input.nextInt();
            if(pnum == 2)
            {
                displayMessage("YOU WON!\n");

            }
            else
                displayMessage("Player 2 has WON!\n");
            
            endPlayer = false;
            myTurn = false;
            
        }
        // Game over and equal score
        else if(message.equals("Game Over")){
            displayMessage("Game is over no winner!\n");
            endPlayer = false;
            myTurn = false;
        }
        // display the message
        else {
            displayMessage(message + "\n"); 
        }
    }
    // display message on text area
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(() -> {
            displayArea.append(messageToDisplay); // updates output
        });
    }
    // send answer to server
    public void sendChangedField(String answer) {
        // if it is my turn
        if (myTurn) {
            output.format("%s\n", answer);
            output.flush();
            myTurn = false; // not my turn anymore
        } 
    }
    
}

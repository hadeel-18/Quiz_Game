
package knowledgegame;

public class KnowledgeGameClientTest {

    public static void main(String args[]) {
        KnowledgeGameClient kgc; // declare client application
        // if no command line args
        if (args.length == 0) {
            kgc = new KnowledgeGameClient("127.0.0.1"); // localhost
        } else {
            kgc = new KnowledgeGameClient(args[0]); // use args
        }
    }
}

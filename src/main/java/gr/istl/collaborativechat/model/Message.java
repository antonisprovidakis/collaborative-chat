package gr.istl.collaborativechat.model;

/**
 *
 * @author Antonis
 */
public class Message {

    public String message;
    public String author;

    public Message(String message, String author) {
        this.message = message;
        this.author = author;
    }

}

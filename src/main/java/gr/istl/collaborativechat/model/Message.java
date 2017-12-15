package gr.istl.collaborativechat.model;

/**
 *
 * @author Antonis
 */
public class Message {

    private final String message;
    private final String author;

    public Message(String message, String author) {
        this.message = message;
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }
}

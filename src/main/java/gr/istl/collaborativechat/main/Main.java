package gr.istl.collaborativechat.main;

import gr.istl.collaborativechat.ui.ChatWindow;
import java.awt.EventQueue;

/**
 *
 * @author Antonis
 */
public class Main {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatWindow("User1").setVisible(true);
                new ChatWindow("User2").setVisible(true);
            }
        });
    }
}

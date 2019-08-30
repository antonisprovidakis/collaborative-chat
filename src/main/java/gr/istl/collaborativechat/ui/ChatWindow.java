package gr.istl.collaborativechat.ui;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import gr.istl.collaborativechat.firebase.FirebaseDBManager;
import gr.istl.collaborativechat.model.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Antonis
 */
public class ChatWindow extends JFrame {

    private final String username;

    private JLabel labelUsersCounter;
    private JTextPane textPaneMessages;
    private JTextField textFieldInput;
    private JButton buttonSend;

    private SimpleAttributeSet localUserAttributeSet;
    private SimpleAttributeSet otherUsersAttributeSet;
    private SimpleAttributeSet authorAttributeSet;

    private DatabaseReference dbRefOnlineUsersCounter;
    private DatabaseReference dbRefMessages;

    public ChatWindow(String username) {
        this.username = username;
        initComponents();
    }

    private void initComponents() {
        setTitle("Collaborative Chat - Username: " + username);
        setSize(400, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initAttributeSets();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goOffline();
            }
        });

        JPanel panelTop = new JPanel();
        panelTop.add(new JLabel("Online Users: "));
        labelUsersCounter = new JLabel("0");
        panelTop.add(labelUsersCounter);

        add(panelTop, BorderLayout.NORTH);

        textPaneMessages = new JTextPane(new DefaultStyledDocument());
        textPaneMessages.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textPaneMessages.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(textPaneMessages);

        JPanel panelBottom = new JPanel(new BorderLayout());

        textFieldInput = new JTextField();

        SendMessageHandler sendMessageHandler = new SendMessageHandler(textFieldInput, username);
        textFieldInput.addActionListener(sendMessageHandler);

        panelBottom.add(textFieldInput, BorderLayout.CENTER);

        buttonSend = new JButton("Send");
        buttonSend.addActionListener(sendMessageHandler);

        panelBottom.add(buttonSend, BorderLayout.EAST);
        add(scrollPane);
        add(panelBottom, BorderLayout.SOUTH);

        setupDBReferences();
        goOnline();
    }

    public void goOnline() {
        dbRefOnlineUsersCounter.child(username).setValueAsync(true);
    }

    public void goOffline() {
        dbRefOnlineUsersCounter.child(username).removeValueAsync();
    }

    public void sendMessage(Message message) {
        DatabaseReference newMessageRef = dbRefMessages.push();
        newMessageRef.setValueAsync(message);
    }

    private void appendMessageLocally(Message message) {
        boolean messageSentByLocalUser = message.getAuthor().equals(this.username);

        SimpleAttributeSet as = messageSentByLocalUser ? localUserAttributeSet : otherUsersAttributeSet;
        StyledDocument doc = textPaneMessages.getStyledDocument();

        try {
            doc.setParagraphAttributes(doc.getLength(), message.getText().length(), as, false);
            doc.insertString(doc.getLength(), message.getText() + "\n", null);

            String authorAlias = messageSentByLocalUser ? "Me" : message.getAuthor();

            doc.insertString(doc.getLength(), "- " + authorAlias + emptyLine(), authorAttributeSet);
            emptyLine();

        } catch (BadLocationException ex) {
            Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String emptyLine() {
        return "\n\n";
    }

    private void setupDBReferences() {
        FirebaseDBManager dbManager = FirebaseDBManager.getInstance();

        dbRefOnlineUsersCounter = dbManager.getDBRef("/session1/onlineUsers");
        dbRefOnlineUsersCounter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                updateLabelUsersCounter((int) data.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError de) {
            }
        });

        dbRefMessages = dbManager.getDBRef("/session1/messages");
        dbRefMessages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot data, String prevChildKey) {
                HashMap<String, String> messageObj = (HashMap<String, String>) data.getValue();
                String text = messageObj.get("text");
                String author = messageObj.get("author");
                appendMessageLocally(new Message(text, author));
            }

            @Override
            public void onChildChanged(DataSnapshot ds, String string) {
            }

            @Override
            public void onChildRemoved(DataSnapshot ds) {
            }

            @Override
            public void onChildMoved(DataSnapshot ds, String string) {
            }

            @Override
            public void onCancelled(DatabaseError de) {
            }
        });
    }

    private void updateLabelUsersCounter(int newCounter) {
        int oldCounter = Integer.valueOf(labelUsersCounter.getText());

        if (oldCounter == newCounter) {
            return;
        }

        labelUsersCounter.setText(String.valueOf(newCounter));
    }

    private void initAttributeSets() {
        localUserAttributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(localUserAttributeSet, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(localUserAttributeSet, Color.RED);

        otherUsersAttributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(otherUsersAttributeSet, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(otherUsersAttributeSet, Color.BLUE);

        authorAttributeSet = new SimpleAttributeSet();
        StyleConstants.setBold(authorAttributeSet, true);
    }

    private class SendMessageHandler implements ActionListener {

        private final JTextField textFieldInput;
        private final String author;

        public SendMessageHandler(JTextField textFieldInput, String author) {
            this.textFieldInput = textFieldInput;
            this.author = author;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (textFieldInput.getText().trim().length() > 0) {
                sendMessage(new Message(textFieldInput.getText(), author));
                textFieldInput.setText("");
            }
        }
    }
}

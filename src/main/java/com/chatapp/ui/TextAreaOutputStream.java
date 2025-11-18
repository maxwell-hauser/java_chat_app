package com.chatapp.ui;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Custom OutputStream that redirects output to a JTextArea.
 * Handles line buffering and ensures thread-safe updates to the UI.
 */
public class TextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final StringBuilder buffer;

    /**
     * Creates a new TextAreaOutputStream.
     *
     * @param textArea The JTextArea to write output to
     */
    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {
        // Ignore carriage returns
        if (b == '\r') {
            return;
        }

        // On newline, flush the buffer to the text area
        if (b == '\n') {
            final String text = buffer.toString() + "\n";
            SwingUtilities.invokeLater(() -> {
                textArea.append(text);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
            buffer.setLength(0);
        } else {
            buffer.append((char) b);
        }
    }

    @Override
    public void flush() {
        if (buffer.length() > 0) {
            final String text = buffer.toString();
            SwingUtilities.invokeLater(() -> {
                textArea.append(text);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
            buffer.setLength(0);
        }
    }
}

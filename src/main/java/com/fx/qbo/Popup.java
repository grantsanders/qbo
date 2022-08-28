package com.fx.qbo;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Popup extends JFrame {

    public Popup(String label, String error) {
        setTitle(label);
        setSize(300, 150);

        JLabel messageField = new JLabel(error, SwingConstants.CENTER);


        add(messageField);
        
        setLocationRelativeTo(null);
    }
    
}

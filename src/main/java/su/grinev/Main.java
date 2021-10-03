package su.grinev;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Main {

    public static void main(String[] args) {
        int width = 0;
        int height = 0;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice curGs : gs) {
            DisplayMode mode = curGs.getDisplayMode();
            width += mode.getWidth();
            if (mode.getHeight() > height) {
                height = mode.getHeight();
            }
        }
        Matrix matrix = new Matrix(width, height);
        JFrame frame = new JFrame();
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                System.exit(0);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.exit(0);

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        frame.setUndecorated(true);
        frame.getContentPane().add(matrix);
        frame.setVisible(true);
    }
}

package su.grinev;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static su.grinev.MultiMonitorJFrame.showFrameOnScreen;

public class Main {

    public static class ScreenContext {
        public int logicNumber;
        public int width;
        public int height;
        public JFrame frame;
        public Matrix matrix;

        public ScreenContext(int logicNumber) {
            this.logicNumber = logicNumber;
            this.frame = new JFrame("First frame" + logicNumber);
            showFrameOnScreen(frame, logicNumber);
            this.width = frame.getWidth();
            this.height = frame.getHeight();
            this.matrix = new Matrix(this.width, this.height, logicNumber, 200);
            this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.frame.setUndecorated(true);
            this.frame.setVisible(true);
            this.frame.setAlwaysOnTop(false);
            this.frame.add(matrix);
            this.frame.validate();
            this.frame.repaint();

            // Transparent 16 x 16 pixel cursor image.
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            // Create a new blank cursor.
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    cursorImg, new Point(0, 0), "blank cursor");
            // Set the blank cursor to the JFrame.
            this.frame.getContentPane().setCursor(blankCursor);

//            frame.addKeyListener(new KeyListener() {
//                @Override
//                public void keyTyped(KeyEvent e) {
//                    System.exit(0);
//                }
//
//                @Override
//                public void keyPressed(KeyEvent e) {
//                    System.exit(0);
//                }
//
//                @Override
//                public void keyReleased(KeyEvent e) {
//
//                }
//            });
        }
    }

    public static void main(String[] args) {
        int screenNumber = MultiMonitorJFrame.getScreenNumber();
        List<ScreenContext> screenContextList = new ArrayList<>();

        for (int i = 0; i != screenNumber; i++) {
            screenContextList.add(new ScreenContext(i));
        }
    }
}

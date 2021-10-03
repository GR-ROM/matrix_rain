package su.grinev;

import javax.swing.*;
import java.awt.*;

public class MultiMonitorJFrame extends JFrame {

    public static void showFrameOnScreen(Window frame, int screen) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
        GraphicsDevice graphicsDevice = ( screen > -1 && screen < graphicsDevices.length ) ? graphicsDevices[screen] : graphicsDevices.length > 0 ? graphicsDevices[0] : null;
        if (graphicsDevice == null) {
            throw new RuntimeException( "There are no screens !" );
        }
        Rectangle bounds = graphicsDevice.getDefaultConfiguration().getBounds();
        frame.setSize(bounds.width, bounds.height);
        frame.setLocation(bounds.x, bounds.y);
    }

    public static int getScreenNumber() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
    }
}

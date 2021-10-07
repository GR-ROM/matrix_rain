package su.grinev;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Stripe {

    public static class Speed {
        public static final Integer SLOW = 0;
        public static final Integer NORMAL = 1;
        public static final Integer FAST = 2;
    }

    private int xPos;
    private int yPos;
    private final int screenHeight;
    private final int size;
    private final int len;
    private int speed;
    private final int step;
    private final Lock lock;
    private final List<Character> matrixElements;

    public Stripe(int len, final int screenHeight) {
        Random random = new Random();
        this.lock = new ReentrantLock();
        this.speed = Math.abs(random.nextInt(3));
        this.matrixElements = new LinkedList<>();
        this.size = 21;
        this.len = len;
        this.yPos = 0;
        this.step = 20;
        this.screenHeight = screenHeight;
    }

    public void setRandomElement(Random random, Character character) {
        lock.lock();
        try {
            if (matrixElements.size() < 5) {
                return;
            }
            int elementNum = random.nextInt(matrixElements.size() - 1);
            matrixElements.set(elementNum, character);
        }
        finally {
            lock.unlock();
        }
    }

    public void pushElement(Character character) {
        lock.lock();
        try {
            matrixElements.add(0, character);
            if (matrixElements.size() >= this.len) {
                matrixElements.remove(matrixElements.size() - 1);
            }
            this.yPos += this.step;
        }
        finally {
            lock.unlock();
        }
    }

    public int getXPos() {
        return xPos;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isVisible() {
        return yPos <= screenHeight + matrixElements.size() * this.step;
    }

    public boolean isTailOut() {
        return (yPos - ((matrixElements.size() * this.step) + (this.step * 5))) > 0;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void draw(Graphics2D g) {
        if (yPos < 0 && yPos > screenHeight ) {
            return;
        }
        lock.lock();
        try {
            final int[] y = { yPos };
            final int[] i = {0};
            g.setFont(new Font("Console", Font.BOLD, this.size));
            matrixElements.forEach(matrixElement -> {
                if (i[0] == 0) g.setColor(new Color(0xFFFFFF));
                else if (i[0] >= 1 && i[0] <= 3)
                    g.setColor(new Color(0x00FFFF));
                else if (i[0] >= 4 && i[0] <= 8)
                    g.setColor(new Color(0x00FF70));
                else if (i[0] >= 9 && i[0] <= 20)
                    g.setColor(new Color(0x00FF00));
                else if (i[0] >= 21 && i[0] <= 30)
                    g.setColor(new Color(0x00AF00));
                else if (i[0] >= 31 && i[0] <= 40)
                    g.setColor(new Color(0x007F00));
                else if (i[0] >= 40)
                    g.setColor(new Color(0xFF003700, true));
                g.drawString(matrixElement.toString(), xPos, y[0]);
                y[0] -= this.step;
                i[0]++;
            });
        } finally {
            lock.unlock();
        }
    }

}
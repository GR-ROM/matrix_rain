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


    private final Range[] ranges;
    private int xPos;
    private int yPos;
    private final int screenHeight;
    private final int size;
    private final int len;
    private int speed;
    private final int step;
    private final char[] chars;
    private final Lock lock;
    private final Font font;
    private final List<Character> matrixElements;

    public Stripe(int len, final int screenHeight, final Range[] ranges) {
        Random random = new Random();
        this.lock = new ReentrantLock();
        this.speed = Math.abs(random.nextInt(3));
        this.matrixElements = new LinkedList<>();
        this.size = 24;
        this.chars = new char[1];
        this.font = new Font("Console", Font.BOLD, this.size);
        this.len = len;
        this.yPos = 0;
        this.step = 20;
        this.screenHeight = screenHeight;
        this.ranges = ranges;
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

    public int getColumn() { return xPos / 18; }

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

    public void setColumn(int column) { this.xPos = column * 18; }

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
            g.setFont(font);
            matrixElements.forEach(matrixElement -> {
                Arrays.stream(ranges)
                        .filter(range -> i[0] >= range.low && i[0] <= range.high)
                        .findAny()
                        .ifPresent(range -> g.setColor(range.color));
                this.chars[0] = matrixElement;
                g.drawChars(this.chars, 0, 1, xPos, y[0]);
                y[0] -= this.step;
                i[0]++;
            });
        } finally {
            lock.unlock();
        }
    }

}
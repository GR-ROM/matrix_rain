package su.grinev;

import org.w3c.dom.ranges.Range;

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

    private class Range {
        public int low;
        public int high;
        public int color;

        public Range(int low, int high, int color) {
            this.low = low;
            this.high = high;
            this.color = color;
        }
    }

    private final Range[] ranges;
    private final Color[] colors;
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

    public Stripe(int len, final int screenHeight, final Color[] colors) {
        Random random = new Random();
        this.lock = new ReentrantLock();
        this.speed = Math.abs(random.nextInt(3));
        this.matrixElements = new LinkedList<>();
        this.size = 24;
        this.colors = colors;
        this.chars = new char[1];
        this.font = new Font("Console", Font.BOLD, this.size);
        this.len = len;
        this.yPos = 0;
        this.step = 20;
        this.screenHeight = screenHeight;
        this.ranges = new Range[10];
        this.ranges[0] = new Range(0, 1, 0);
        this.ranges[1] = new Range(2, 5, 1);
        this.ranges[2] = new Range(6, 10, 2);
        this.ranges[3] = new Range(10, 40, 3);
        this.ranges[4] = new Range(41, 60, 4);
        this.ranges[5] = new Range(61, 80, 5);
        this.ranges[6] = new Range(81, 100, 6);
        this.ranges[7] = new Range(101, 2000, 7);
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
                        .ifPresent(range -> g.setColor(colors[range.color]));
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
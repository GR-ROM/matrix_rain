package su.grinev;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Stripe {

    public static class Speed {
        public static final Integer SLOW = 0;
        public static final Integer NORMAL = 1;
        public static final Integer FAST = 2;
    }

    private final int xPos;
    private int yPos;
    private final int screenHeight;
    private final int size;
    private final int len;
    private final int speed;
    private final int step;
    private final Lock lock;
    private final List<MatrixElement> matrixElements;

    public Stripe(int len, int xPos, final int screenHeight) {
        Random random = new Random();
        this.lock = new ReentrantLock();
        this.speed = Math.abs(random.nextInt(3));
        this.matrixElements = new ArrayList<>();
        this.size = 21;
        this.len = len;
        this.xPos = xPos;
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
            MatrixElement matrixElement = matrixElements.get(elementNum);
            matrixElement.setCharacter(character);
            matrixElements.set(elementNum, matrixElement);
        }
        finally {
            lock.unlock();
        }
    }

    public void pushElement(MatrixElement element) {
        lock.lock();
        try {
            matrixElements.add(0, element);
            if (matrixElements.size() >= this.len) {
                matrixElements.remove(matrixElements.size() - 1);
            }
            AtomicInteger colorIndex = new AtomicInteger();
            matrixElements.forEach(i -> i.setColorIndex(colorIndex.getAndIncrement()));
            this.yPos += this.step;
        }
        finally {
            lock.unlock();
        }
    }

    public int getX() {
        return xPos;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isVisible() {
        return (yPos >= 0 && yPos <= screenHeight + matrixElements.size() * this.step);
    }

    public void draw(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        lock.lock();
        try {
            final int[] y = {yPos};
            g.setFont(new Font("Console", Font.BOLD, this.size));
            matrixElements.forEach(matrixElement -> {
                if (matrixElement.getColorIndex() == 0) g.setColor(new Color(0xFFFFFF));
                else if (matrixElement.getColorIndex() >= 1 && matrixElement.getColorIndex() <= 3)
                    g.setColor(new Color(0x00FFFF));
                else if (matrixElement.getColorIndex() >= 4 && matrixElement.getColorIndex() <= 8)
                    g.setColor(new Color(0x00FF70));
                else if (matrixElement.getColorIndex() >= 9 && matrixElement.getColorIndex() <= 20)
                    g.setColor(new Color(0x00FF00));
                else if (matrixElement.getColorIndex() >= 21 && matrixElement.getColorIndex() <= 30)
                    g.setColor(new Color(0x00AF00));
                else if (matrixElement.getColorIndex() >= 31 && matrixElement.getColorIndex() <= 40)
                    g.setColor(new Color(0x007F00));
                else if (matrixElement.getColorIndex() >= 40) g.setColor(new Color(0xFF003700, true));
                g.drawString(matrixElement.getCharacter().toString(), xPos, y[0]);
                y[0] -= this.step;
            });
        } finally {
            lock.unlock();
        }
    }
}
package su.grinev;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Matrix extends JPanel {

    private final List<Stripe> stripeList;
    private final Random random;
    private final StripePool stripePool;
    private final List<Stripe>[] busyColumns;
    private final int maxStripes;
    private final int maxColumns;
    private final int width;
    private final int height;
    private Point lastMousePosition;
    private final Lock lock;
    private final RenderingHints renderingHints;

    private class Task extends TimerTask {
        private int counter;
        private final char[] alphabet = {'あ', 'た', 'ア',
                'カ', 'サ', 'ザ', 'ジ', 'ズ', 'ゼ', 'ゾ',
                'シ', 'ス', 'セ', 'ソ', 'キ', 'ク', 'ケ',
                'コ', 'イ', 'ウ', 'エ', 'オ', 'ジ', 'ャ', 'な'};

        public Task() {
            this.counter = 0;
        }

        public void run() {
            counter++;
            int speed = Stripe.Speed.FAST;
            if (counter % 2 == 0) {
                speed = Stripe.Speed.NORMAL;
            }
            if (counter % 4 == 0) {
                speed = Stripe.Speed.SLOW;
                if (isMouseMoved()) {
                    System.exit(0);
                }
            }
            if (speed == Stripe.Speed.FAST) {
                if (stripeList.size() < maxStripes && counter % 10 == Math.abs(random.nextInt(10)) ) {
                    generateStripes(1);
                }
            }
            int finalSpeed = speed;
            stripeList.stream()
                    .filter(stripe -> stripe.getSpeed() == finalSpeed)
                    .collect(Collectors.toList())
                    .forEach(stripe -> {
                        stripe.pushElement(alphabet[random.nextInt(alphabet.length - 1)]);
                        stripe.setRandomElement(random, alphabet[random.nextInt(alphabet.length - 1)]);
                    });
            repaint();
            generateStripes(releaseStripes());
        }
    }

    private void generateStripes(int len) {
        AtomicInteger column = new AtomicInteger(Math.abs(random.nextInt(maxColumns)));
        AtomicInteger speed = new AtomicInteger(Math.abs(random.nextInt(3)));
        lock.lock();
        try {
            for (int i = 0; i != len; i++) {
                if (stripeList.size() < maxColumns) {
                    if (stripeList.size() < (maxColumns - 20)) {
                        while (this.busyColumns[column.get()].size() != 0) {
                            column.set(Math.abs(random.nextInt(maxColumns)));
                        }
                    } else {
                        Arrays.stream(busyColumns)
                                .filter(cmn -> cmn.size() == 0)
                                .findAny()
                                .ifPresent(c -> column.set(Arrays.asList(busyColumns).indexOf(c)));
                    }
                } else {
                    stripeList.stream()
                            .filter(Stripe::isTailOut)
                            .findFirst()
                            .ifPresent(stripe -> {
                                if (busyColumns[stripe.getColumn()].stream().allMatch(Stripe::isTailOut)) {
                                    column.set(stripe.getColumn());
                                    speed.set(stripe.getSpeed());
                                }  else {
                                    column.set(-1);
                                }
                            });
                }
                Stripe stripe = stripePool.pop();
                if (stripe == null || column.get() < 0) {
                    return;
                }
                stripe.setSpeed(speed.get());
                stripe.setYPos(-20);
                stripe.setXPos(column.get() * 18);
                this.busyColumns[column.get()].add(stripe);
                stripeList.add(stripe);
            }
        }
        finally {
            lock.unlock();
        }
    }

    private int releaseStripes() {
        lock.lock();
        try {
            List<Stripe> stripeOutList = stripeList.stream()
                    .filter(s -> !s.isVisible())
                    .collect(Collectors.toList());
                stripeOutList.forEach(stripe -> busyColumns[stripe.getColumn()].remove(stripe));
                stripeList.removeAll(stripeOutList);
                stripePool.addAll(stripeOutList);
            return stripeOutList.size();
        }
        finally {
            lock.unlock();
        }
    }

    public Matrix(int width, int height, int instanceNum, int maxStripes) {
        Color[] colors = new Color[8];
        colors[0] = new Color(0xFFFFFF);
        colors[1] = new Color(0x00FFFF);
        colors[2] = new Color(0x00FF70);
        colors[3] = new Color(0x00FF00);
        colors[4] = new Color(0x00AF00);
        colors[5] = new Color(0x007F00);
        colors[6] = new Color(0x005700);
        colors[7] = new Color(0x003700);
        this.lock = new ReentrantLock();
        this.width = width;
        this.height = height;
        this.stripeList = new ArrayList<>();
        this.stripePool = new StripePool(maxStripes);
        this.random = new Random();
        this.maxStripes = maxStripes;
        this.lastMousePosition = MouseInfo.getPointerInfo().getLocation();
        this.maxColumns = width / 18;
        this.busyColumns = new List[maxColumns];
        this.renderingHints = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        for (int i = 0; i != maxColumns; i++) {
            busyColumns[i] = new ArrayList<>();
        }
        for (int i = 0; i != maxStripes; i++) {
            stripePool.push(new Stripe(20 + random.nextInt(100), height, colors));
        }
        generateStripes(1);
        Timer timer = new Timer("Timer" + instanceNum);
        timer.scheduleAtFixedRate(new Task(), 0, 50);
    }

    @Override
    public void paintComponent(Graphics g){
        if (lock.tryLock()) {
            try {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0x000000));
                g2.fillRect(0, 0, this.width, this.height);
                g2.setRenderingHints(this.renderingHints);
                stripeList.forEach(stripe -> stripe.draw(g2));
            }
            finally {
                lock.unlock();
            }
        }
    }

    private boolean isMouseMoved() {
        if (null == MouseInfo.getPointerInfo() || null == MouseInfo.getPointerInfo().getLocation()) return false;
        if (MouseInfo.getPointerInfo().getLocation().x != lastMousePosition.x || MouseInfo.getPointerInfo().getLocation().y != lastMousePosition.y) {
            return true;
        } else {
            this.lastMousePosition = MouseInfo.getPointerInfo().getLocation();
            return false;
        }
    }
}
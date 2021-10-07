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
    private final List<Stripe> stripeListPool;
    Map<Integer, List<Stripe>> busyColumns;
    private final int maxStripes;
    private final int maxColumns;
    private final int width;
    private final int height;
    private Point lastMousePosition;
    private final Lock lock;

    public class Task extends TimerTask {

        private int counter;
        private int time = 0;
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
                   // System.exit(0);
                }
            }
            if (speed == Stripe.Speed.NORMAL) {
                time++;
                if (stripeList.size() < maxStripes && time % 10 == random.nextInt(10)) {
                        generateStripes(1);
                }
            }
            int finalSpeed = speed;
            List<Stripe> stripeListBySpeed = stripeList.stream()
                    .filter(stripe -> stripe.getSpeed() == finalSpeed)
                    .collect(Collectors.toList());
            stripeListBySpeed.forEach(stripe -> {
                stripe.pushElement(alphabet[random.nextInt(alphabet.length - 1)]);
                stripe.setRandomElement(random, alphabet[random.nextInt(alphabet.length - 1)]);
            });
            repaint();
            generateStripes(releaseStripes());
        }
    }

    private void generateStripes(int len) {
        AtomicInteger column = new AtomicInteger();
        AtomicInteger speed = new AtomicInteger(Math.abs(random.nextInt(3)));
        lock.lock();
        try {
            for (int i = 0; i != len; i++) {
                if (stripeList.size() < maxColumns) {
                    do {
                        column.set(Math.abs(random.nextInt(maxColumns)));
                    } while (this.busyColumns.get(column.get()).size() != 0);
                } else {
                    stripeList.stream()
                            .filter(Stripe::isTailOut)
                            .findFirst()
                            .ifPresent(stripe -> {
                                if (busyColumns.get(stripe.getXPos() / 18).size() == 0
                                        || busyColumns.get(stripe.getXPos() / 18).stream().allMatch(Stripe::isTailOut)) {
                                    column.set(stripe.getXPos() / 18);
                                    speed.set(stripe.getSpeed());
                                }
                            });
                }
                Stripe stripe;
                if (!stripeListPool.isEmpty()) {
                    stripe = stripeListPool.remove(stripeListPool.size() - 1);
                } else {
                    return;
                }
                stripe.setSpeed(speed.get());
                stripe.setYPos(-20);
                stripe.setXPos(column.get() * 18);
                this.busyColumns.get(column.get()).add(stripe);
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
                stripeOutList.forEach(stripe -> busyColumns.get(stripe.getXPos() / 18).remove(stripe));
                stripeList.removeAll(stripeOutList);
                stripeListPool.addAll(stripeOutList);
            return stripeOutList.size();
        }
        finally {
            lock.unlock();
        }
    }

    public Matrix(int width, int height, int num, int maxStripes) {
        this.lock = new ReentrantLock();
        this.width = width;
        this.height = height;
        this.stripeList = new ArrayList<>();
        this.stripeListPool = new ArrayList<>();
        this.busyColumns = new HashMap<>();
        this.random = new Random();
        this.maxStripes = maxStripes;
        this.lastMousePosition = MouseInfo.getPointerInfo().getLocation();
        maxColumns = width / 18;
        for (int i = 0; i != width / 18; i++) {
            busyColumns.put(i, new ArrayList<>());
        }
        for (int i = 0; i != maxStripes; i++) {
            stripeListPool.add(new Stripe(20 + random.nextInt(20), height));
        }
        generateStripes(1);
        Timer timer = new Timer("Timer" + num);
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
                stripeList.forEach(stripe -> stripe.draw(g2));
            }
            finally {
                lock.unlock();
            }
        }
    }

    private boolean isMouseMoved() {
        if (null == MouseInfo.getPointerInfo()) return false;
        if (MouseInfo.getPointerInfo().getLocation().x != lastMousePosition.x || MouseInfo.getPointerInfo().getLocation().y != lastMousePosition.y) {
            return true;
        } else {
            this.lastMousePosition = MouseInfo.getPointerInfo().getLocation();
            return false;
        }
    }
}
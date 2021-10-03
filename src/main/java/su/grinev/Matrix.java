package su.grinev;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Matrix extends JPanel {

    private final List<Stripe> stripeList;
    private final Random random;
    private final Set<Integer> busyColumns;
    private final int maxColumns;
    private final int width;
    private final int height;
    private Point lastMousePosition;
    private final Lock lock;

    public class Task extends TimerTask {

        private final JPanel jPanel;
        private int counter;
        private int time = 0;
        private final char[] alphabet = { 'あ', 'た', 'ア',
                'カ', 'サ', 'ザ', 'ジ', 'ズ', 'ゼ', 'ゾ',
                'シ', 'ス', 'セ', 'ソ', 'キ', 'ク', 'ケ',
                'コ', 'イ', 'ウ', 'エ', 'オ', 'ジ', 'ャ', 'な'};

        public Task(JPanel jPanel) {
            this.jPanel = jPanel;
            this.counter = 0;
        }

        public void run() {
            counter++;
            int speed = Stripe.Speed.FAST;
            jPanel.repaint();
            jPanel.invalidate();
            if (counter % 2 == 0) {
                speed = Stripe.Speed.NORMAL;
            }
            if (counter % 4 == 0) {
                speed = Stripe.Speed.SLOW;
                if (isMouseMoved()) {
                    System.exit(0);
                }
            }

            if (speed == Stripe.Speed.NORMAL) {
                time++;
                if (stripeList.size() < maxColumns) {
                    if (time % 10 == random.nextInt(10)) {
                        lock.lock();
                        try {
                            stripeList.addAll(generateStripes(1));
                        }
                        finally {
                            lock.unlock();
                        }
                    }
                }
            }

            int finalSpeed = speed;
            List<Stripe> stripeListBySpeed = stripeList.stream()
                    .filter(stripe -> stripe.getSpeed() == finalSpeed)
                    .collect(Collectors.toList());
            stripeListBySpeed.forEach(stripe -> {
                stripe.pushElement(new MatrixElement(alphabet[random.nextInt(alphabet.length - 1)]));
                stripe.setRandomElement(random, alphabet[random.nextInt(alphabet.length - 1)]);
            });

            if (speed == Stripe.Speed.SLOW) {
                lock.lock();
                try {
                    stripeList.addAll(generateStripes(releaseStripes()));
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private List<Stripe> generateStripes(int len) {
        int column;
        List<Stripe> stripeList = new ArrayList<>();
        for (int i = 0; i != len; i++) {
            do {
                column = Math.abs(random.nextInt(maxColumns));
            } while (this.busyColumns.contains(column));
            this.busyColumns.add(column);
            stripeList.add(new Stripe(15 + random.nextInt(70), column * 18, height));
        }
        return stripeList;
    }

    private int releaseStripes() {
        List<Stripe> stripeOutList = stripeList.stream()
                .filter(s -> !s.isVisible())
                .collect(Collectors.toList());
        busyColumns.removeAll(stripeOutList.stream().map(s -> s.getX() / 18).collect(Collectors.toSet()));
        stripeList.removeAll(stripeOutList);
        return stripeOutList.size();
    }

    public Matrix(int width, int height, int num) {
        this.lock = new ReentrantLock();
        this.width = width;
        this.height = height;
        this.stripeList = new ArrayList<>();
        this.busyColumns = new HashSet<>();
        this.random = new Random();
        this.lastMousePosition = MouseInfo.getPointerInfo().getLocation();
        maxColumns = width / 18;
        stripeList.addAll(generateStripes(1));
        Timer timer1 = new Timer("Timer" + num);
        timer1.scheduleAtFixedRate(new Task(this), 0, 40);
    }

    public void paint(Graphics g){
        if (lock.tryLock()) {
            try {
                g.setColor(new Color(0x000000));
                g.fillRect(0, 0, this.width, this.height);
                stripeList.forEach(stripe -> stripe.draw(g));
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
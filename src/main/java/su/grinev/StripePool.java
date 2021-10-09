package su.grinev;

import java.util.LinkedList;
import java.util.List;

public class StripePool {

    private final List<Stripe> stripes;
    private final int maxSize;

    public StripePool(int max) {
        this.stripes = new LinkedList<>();
        this.maxSize = max;
    }

    public void push(Stripe stripe) {
        if (this.stripes.size() + 1 <= maxSize) {
            this.stripes.add(this.stripes.size(), stripe);
        } else {
            throw new IndexOutOfBoundsException("Stripe list is full");
        }
    }

    public Stripe pop() {
        if (this.stripes.size() > 0) {
            return this.stripes.remove(0);
        } else {
            return null;
        }
    }

    public void addAll(List<Stripe> stripes) {
        if (this.stripes.size() + stripes.size() <= maxSize) {
            this.stripes.addAll(stripes);
        } else {
            throw new IndexOutOfBoundsException("Stripe list is full");
        }
    }

    public int getSize() {
        return this.stripes.size();
    }

}

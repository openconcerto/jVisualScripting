package com.jvisualscripting;

public class Link {
    private final Pin from;
    private final Pin to;

    public Link(Pin from, Pin to) {
        this.from = from;
        this.to = to;
        to.addConnectedPin(from);
        from.addConnectedPin(to);
    }

    public Pin getFrom() {
        return this.from;
    }

    public Pin getTo() {
        return this.to;
    }

    public boolean hasPin(Pin p) {
        return this.from.equals(p) || this.to.equals(p);
    }

    @Override
    public String toString() {
        return super.toString() + " [" + this.from + "=>" + this.to + "]";
    }

}

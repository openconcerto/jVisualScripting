package com.jvisualscripting.editor;

import com.jvisualscripting.Link;
import com.jvisualscripting.Pin;

public class HalfConnectedVLink extends VLink {
    private final TempPin t;
    private final Pin connected;

    public HalfConnectedVLink(Pin from, Pin to) {
        super(new Link(from, to));
        if (from instanceof TempPin) {
            this.t = (TempPin) from;
            this.connected = to;
        } else {
            this.connected = from;
            this.t = (TempPin) to;
        }
    }

    public TempPin getTempPin() {
        return this.t;
    }

    public Pin getConnectedPin() {
        return this.connected;
    }

}

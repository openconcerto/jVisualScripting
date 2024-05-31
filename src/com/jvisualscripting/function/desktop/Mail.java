package com.jvisualscripting.function.desktop;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.StringPin;

public class Mail extends FlowNode {

    public Mail() {
        super("Mail");
        this.inputs.add(new StringPin(this, "Subject", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "Body", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "To", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "CC", PinMode.INPUT));
    }

    @Override
    public boolean run() {
        Object subject = this.getInputValue(1);
        if (subject == null) {
            subject = "";
        }
        Object body = this.getInputValue(2);
        if (body == null) {
            body = "";
        }
        Object to = this.getInputValue(3);
        if (to == null) {
            to = "";
        }
        Object cc = this.getInputValue(4);
        if (cc == null) {
            cc = "";
        }

        try {
            URI uri = new URI("mailto:" + URLEncoder.encode((String) to, "UTF-8") + "?subject=" + URLEncoder.encode((String) subject, "UTF-8") + "&body=" + URLEncoder.encode((String) body, "UTF-8")
                    + "&cc=" + URLEncoder.encode((String) cc, "UTF-8"));
            Desktop.getDesktop().mail(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean canBeExecuted() {
        final DataPin dataPin = (DataPin) this.inputs.get(2);
        return dataPin.isConnected();
    }

}

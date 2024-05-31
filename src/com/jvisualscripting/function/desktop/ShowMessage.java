package com.jvisualscripting.function.desktop;

import javax.swing.JOptionPane;

import com.jvisualscripting.DataPin;
import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.StringPin;

public class ShowMessage extends FlowNode {

    public ShowMessage() {
        super("Show Message");
        this.inputs.add(new StringPin(this, "Title", PinMode.INPUT));
        this.inputs.add(new StringPin(this, "Message", PinMode.INPUT));
    }

    @Override
    public boolean run() {
        Object title = this.getInputValue(1);
        if (title == null) {
            title = "";
        }
        Object message = this.getInputValue(2);
        if (message == null) {
            message = "";
        }
        JOptionPane.showMessageDialog(null, (String) message, (String) title, JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        final DataPin dataPin = (DataPin) this.inputs.get(2);
        return dataPin.isConnected();
    }

}

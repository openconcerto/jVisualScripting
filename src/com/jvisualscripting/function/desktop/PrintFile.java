package com.jvisualscripting.function.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.jvisualscripting.FlowNode;
import com.jvisualscripting.Pin.PinMode;
import com.jvisualscripting.variable.FilePin;

public class PrintFile extends FlowNode {

    public PrintFile() {
        super("Print File");
        this.inputs.add(new FilePin(this, "File", PinMode.INPUT));

    }

    @Override
    public boolean run() {
        Object file = this.getInputValue(1);
        if (file != null) {
            try {
                Desktop.getDesktop().print((File) file);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBeExecuted() {
        return this.inputs.get(1).isConnected();
    }

}

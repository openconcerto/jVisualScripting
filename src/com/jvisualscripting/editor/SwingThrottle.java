package com.jvisualscripting.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SwingThrottle {
    private Timer timer;
    private Runnable runnable;
    long last = System.currentTimeMillis();
    private int delay;

    public SwingThrottle(int delayInMs, final Runnable runnable) {
        this.delay = delayInMs;
        this.runnable = runnable;
        timer = new Timer(delayInMs, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                SwingUtilities.invokeLater(runnable);

            }
        });
    }

    public synchronized void execute() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalArgumentException("must be called in EDT");
        }
        long t = System.currentTimeMillis();
        if (t - last < delay) {
            timer.restart();

        } else {
            SwingUtilities.invokeLater(runnable);
            last = t;
        }

    }

    public synchronized void executeNow() {
        if (timer.isRunning()) {
            timer.stop();
            runnable.run();
        }
    }
}

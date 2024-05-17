package com.jvisualscripting.editor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;

public class History {
    private int depth;
    private final LinkedList<CheckPoint> list = new LinkedList<>();
    private int currentIndex = 0;

    public History(int depth) {
        this.depth = depth;
    }

    public void clear() {
        this.currentIndex = 0;
        this.list.clear();
    }

    public void addCheckPoint(CheckPoint p) throws IOException {
        p.park();

        int d = this.list.size() - this.currentIndex;
        for (int i = 0; i < d; i++) {
            this.list.removeLast();
        }
        CheckPoint last = null;
        if (!this.list.isEmpty()) {
            last = this.list.getLast();
        }
        if (last == null || !Arrays.equals(p.getBytes(), last.getBytes())) {
            this.list.add(p);
            this.currentIndex = this.list.size();
        }
        if (this.list.size() > this.depth) {
            this.list.removeFirst();
        }

    }

    private void dump(PrintStream out) {
        out.println("History, index:" + this.currentIndex + ", size:" + this.list.size());
        for (CheckPoint c : this.list) {
            out.println(c);
        }
    }

    CheckPoint undo() throws IOException {
        if (this.currentIndex <= 1) {
            return null;
        }
        this.currentIndex--;
        byte[] bytes = this.list.get(this.currentIndex - 1).getBytes();
        CheckPoint checkPoint = new CheckPoint(bytes);
        checkPoint.unpark();

        dump(System.err);
        return checkPoint;
    }

    CheckPoint redo() throws IOException {

        if (this.currentIndex < this.list.size()) {

            byte[] bytes = this.list.get(this.currentIndex).getBytes();
            CheckPoint checkPoint = new CheckPoint(bytes);
            checkPoint.unpark();
            this.currentIndex++;
            dump(System.err);
            return checkPoint;
        }
        return null;
    }

    public boolean canUndo() {
        return this.currentIndex > 1;
    }

    public boolean canRedo() {
        return this.currentIndex < this.list.size();
    }

}

package com.unseen.cc.common.entity.base;

public class TimedEvent implements Comparable<TimedEvent>{
    Runnable callback;
    int ticks;

    public TimedEvent(Runnable callback, int ticks) {
        this.callback = callback;
        this.ticks = ticks;
    }

    @Override
    public int compareTo(TimedEvent event) {
        return event.ticks < ticks ? 1 : -1;
    }
}

package cc.spea.CoreProtectTimeLapse;

public class InterruptableThread extends Thread {
    boolean interrupted;

    public InterruptableThread(Runnable run) {
        super(run);
        this.interrupted = false;
    }

    public void setInterrupt(boolean flag) {
        this.interrupted = flag;
    }

    public boolean getInterrupt() {
        return this.interrupted;
    }
}

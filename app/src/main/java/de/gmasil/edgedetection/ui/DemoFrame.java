package de.gmasil.edgedetection.ui;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DemoFrame extends JFrame {

    private final Object lock = new Object();

    public DemoFrame(boolean waitUntilClose) {
        setTitle("Edge Detection");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
        if(waitUntilClose) {
            waitForClose();
        }
    }

    private void waitForClose() {
        Thread t = new Thread(() -> {
            synchronized(lock) {
                while (this.isVisible()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                synchronized (lock) {
                    DemoFrame.this.setVisible(false);
                    lock.notify();
                }
            }

        });

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkScannerApp {
    private JFrame frame;
    private JButton detectButton;
    private JPanel gridPanel;
    private ArrayList<String> foundDevices;
    private ExecutorService executor;
    private static final int MAX_DEVICES = 36;

    public NetworkScannerApp() {
        foundDevices = new ArrayList<>();
        executor = Executors.newFixedThreadPool(10); // Thread pool for parallel pinging
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("Network Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        detectButton = new JButton("Detect");
        detectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                detectDevices();
            }
        });

        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(9, 9, 5, 5));

        frame.add(detectButton, BorderLayout.NORTH);
        frame.add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void detectDevices() {
        detectButton.setEnabled(false);
        foundDevices.clear();
        gridPanel.removeAll();

        String localNetwork = getLocalNetworkBase();
        if (localNetwork == null) {
            JOptionPane.showMessageDialog(frame, "Failed to detect local network.");
            detectButton.setEnabled(true);
            return;
        }

        for (int i = 1; i <= 254; i++) {
            String ipAddress = localNetwork + i;
            executor.submit(() -> pingIPAddress(ipAddress));
        }

        executor.submit(() -> {
            try {
                Thread.sleep(15000); // Wait for threads to finish
            } catch (InterruptedException ignored) {
            }
            SwingUtilities.invokeLater(() -> {
                detectButton.setEnabled(true);
                executor.shutdownNow(); // Cleanup executor
            });
        });
    }

    private String getLocalNetworkBase() {
        try {
            Process process = new ProcessBuilder("ipconfig").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("IPv4 Address")) {
                    String ip = line.substring(line.indexOf(":") + 2).trim();
                    return ip.substring(0, ip.lastIndexOf('.') + 1); // Get base IP
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void pingIPAddress(String ipAddress) {
        try {
            Process process = new ProcessBuilder("ping", "-n", "1", ipAddress).start();
            process.waitFor();
            if (process.exitValue() == 0) { // Successful ping
                SwingUtilities.invokeLater(() -> addDevice(ipAddress));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addDevice(String ipAddress) {
        if (foundDevices.size() >= MAX_DEVICES) {
            return;
        }
        if (!foundDevices.contains(ipAddress)) {
            foundDevices.add(ipAddress);
            JButton button = new JButton(ipAddress);
            button.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Clicked: " + ipAddress));
            gridPanel.add(button);
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NetworkScannerApp::new);
    }
}

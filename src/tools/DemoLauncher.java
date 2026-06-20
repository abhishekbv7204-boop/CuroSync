import com.curasync.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Demo Launcher — skips login and directly opens the MainFrame as Admin,
 * then auto-navigates through each tab with a 3-second pause on each
 * so you can see every panel working with real demo data.
 */
public class DemoLauncher {

    public static void main(String[] args) throws Exception {
        // Apply FlatLaf look and feel
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}

        System.out.println("=================================================");
        System.out.println("  CuraSync — FULL DEMO (Auto-Navigate Mode)");
        System.out.println("=================================================");
        System.out.println("Opening as: Admin");
        System.out.println("Tabs will auto-cycle: Dashboard → Patients → Doctors → Appointments → Emergency");
        System.out.println();

        // Launch MainFrame directly (bypassing login)
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame("Admin");
            frame.setTitle("CuraSync — DEMO (Auto-Navigate)");
            frame.setVisible(true);

            // Get the JTabbedPane from inside MainFrame
            JTabbedPane tabs = findTabbedPane(frame);
            if (tabs == null) {
                System.err.println("Could not find JTabbedPane!");
                return;
            }


            System.out.println("▶  Starting tab-by-tab demo tour...\n");

            // Schedule auto-tab switching
            final int[] tabIndex = {0};
            Timer autoNav = new Timer(4000, null);
            autoNav.addActionListener(e -> {
                if (tabIndex[0] < tabs.getTabCount()) {
                    tabs.setSelectedIndex(tabIndex[0]);
                    System.out.printf("  [Tab %d/%d] Showing: %s%n",
                        tabIndex[0] + 1, tabs.getTabCount(),
                        tabs.getTitleAt(tabIndex[0]).trim());
                    tabIndex[0]++;
                } else {
                    autoNav.stop();
                    System.out.println("\n✅ Demo tour complete! All panels shown.");
                    System.out.println("   The app stays open — explore freely.");
                }
            });
            autoNav.setInitialDelay(500);
            autoNav.start();
        });
    }

    /** Recursively searches a container for the first JTabbedPane */
    static JTabbedPane findTabbedPane(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JTabbedPane t) return t;
            if (comp instanceof Container container) {
                JTabbedPane found = findTabbedPane(container);
                if (found != null) return found;
            }
        }
        return null;
    }
}

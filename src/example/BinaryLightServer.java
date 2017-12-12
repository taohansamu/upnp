package example;

import example.SwitchPower;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class BinaryLightServer implements Runnable {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    BinaryLightServer(){
        prepareGUI();
    }
    public static void main(String[] args) throws Exception {
        BinaryLightServer  binaryLightServer= new BinaryLightServer();
        binaryLightServer.showImageIconDemo(false);
//         Start a user thread that runs the UPnP stack
        Thread serverThread = new Thread(binaryLightServer);
        serverThread.setDaemon(false);
        serverThread.start();
    }
    private void prepareGUI(){
        mainFrame = new JFrame("Vi du Java Swing");
        mainFrame.setSize(400,400);
        mainFrame.setLayout(new GridLayout(3, 1));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        headerLabel = new JLabel("", JLabel.CENTER);
        statusLabel = new JLabel("",JLabel.CENTER);

        statusLabel.setSize(350,100);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.add(statusLabel);
        mainFrame.setVisible(true);
    }

    // Tra ve mot ImageIcon, hoac null neu path la khong hop le.
    private static ImageIcon createImageIcon(String path,
                                             String description) {
        java.net.URL imgURL = BinaryLightServer.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    private void showImageIconDemo(boolean b){
        headerLabel.setText("Control in action: ImageIcon");
        ImageIcon tv_on_icon = createImageIcon("tvon.jpeg","Java");
        ImageIcon tv_off_icon = createImageIcon("tvoff.png", "TV off");
        JLabel commentlabel = new JLabel("", tv_on_icon,JLabel.CENTER);
        if(b == true) commentlabel.setIcon(tv_on_icon);
        else commentlabel.setIcon(tv_off_icon);
        controlPanel.add(commentlabel);
        mainFrame.setVisible(true);
    }
    public void run() {
        try {

            final UpnpService upnpService = new UpnpServiceImpl();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    upnpService.shutdown();
                }
            });

            // Add the bound local device to the registry
            upnpService.getRegistry().addDevice(
                    createDevice()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
    LocalDevice createDevice()
            throws ValidationException, LocalServiceBindingException, IOException {

        DeviceIdentity identity =
                new DeviceIdentity(
                        UDN.uniqueSystemIdentifier("Demo Binary Light")
                );

        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Friendly Binary Light",
                        new ManufacturerDetails("ACME"),
                        new ModelDetails(
                                "BinLight2000",
                                "A demo light with on/off switch.",
                                "v1"
                        )
                );

        Icon icon =
                new Icon(
                        "image/png", 48, 48, 8,
                        getClass().getResource("icon.png")
                );

        LocalService<SwitchPower> switchPowerService =
                new AnnotationLocalServiceBinder().read(SwitchPower.class);

        switchPowerService.setManager(
                new DefaultServiceManager<>(switchPowerService, SwitchPower.class)
        );

        return new LocalDevice(identity, type, details, icon, switchPowerService);

    /* Several services can be bound to the same device:
    return new LocalDevice(
            identity, type, details, icon,
            new LocalService[] {switchPowerService, myOtherService}
    );
    */

    }

}
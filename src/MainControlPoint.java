

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.controlpoint.*;
import org.fourthline.cling.model.action.*;
import org.fourthline.cling.model.message.*;
import org.fourthline.cling.model.message.header.*;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.types.*;
import org.fourthline.cling.registry.*;
import org.xml.sax.SAXException;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;


public class MainControlPoint implements Runnable {
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    RemoteDevice tivi;
    RemoteDevice light;
    MainControlPoint(){
        prepareGUI();
    }
    public static void main(String[] args) throws Exception {
        // Start a user thread that runs the UPnP stack
        Thread clientThread = new Thread(new MainControlPoint());
        clientThread.setDaemon(false);
        clientThread.start();

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

    public void run() {
        try {

            ArrayList<RemoteDevice> devices = new ArrayList();// mang luu cac devices
            ServiceId serviceId = new UDAServiceId("SwitchPower:1");
            UpnpService upnpService = new UpnpServiceImpl();

            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService, devices)
            );

            //Load setting
            Setting f = new Setting();
//            f.setRoot(f.parseNow());

            headerLabel.setText("Control in action: Button");

            //resource folder nen o ben trong SWING folder.

            JButton mode1Button = new JButton("Mode 1");
            JButton mode2Button = new JButton("Mode 2");
            JButton refreshButton = new JButton("Reload setting");
            refreshButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        f.refresh();
                    } catch (ParserConfigurationException e1) {
                        e1.printStackTrace();
                    } catch (SAXException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            mode1Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    statusLabel.setText("Kich hoat che do 1");
                            if(devices.get(0).getDetails().getModelDetails().getModelName().equals("Tivi")){
                                tivi = devices.get(0);
                                light = devices.get(1);
                            }else {
                                light = devices.get(0);
                                tivi = devices.get(1);
                            }
                            executeAction(upnpService, tivi.findService(serviceId), f.getDeviceSettingOfModeByName("1","tivi"));
                            executeAction(upnpService, light.findService(serviceId), f.getDeviceSettingOfModeByName("1","light"));
                }
            });
            mode2Button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    statusLabel.setText("Kich hoat che do 2");
                    if(devices.get(0).getDetails().getModelDetails().getModelName().equals("Tivi")){
                        tivi = devices.get(0);
                        light = devices.get(1);
                    }else {
                        light = devices.get(0);
                        tivi = devices.get(1);
                    }
                    executeAction(upnpService, tivi.findService(serviceId), f.getDeviceSettingOfModeByName("2","tivi"));
                    executeAction(upnpService, light.findService(serviceId), f.getDeviceSettingOfModeByName("2","light"));
                }
            });

            controlPanel.add(mode1Button);
            controlPanel.add(mode2Button);
            controlPanel.add(refreshButton);
            mainFrame.setVisible(true);


            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(
                    new STAllHeader()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

    RegistryListener createRegistryListener(final UpnpService upnpService, ArrayList devices) {
        return new DefaultRegistryListener() {

            ServiceId serviceId = new UDAServiceId("SwitchPower:1");

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

                Service switchPower;
                if ((switchPower = device.findService(serviceId)) != null) {

                    System.out.println("Service discovered: " + switchPower);
                    System.out.println("Tim duoc thiet bi co url: " + device.getIdentity().getDescriptorURL().toString());
//                    device.getIdentity().get
                    devices.add(device);

//                    executeAction(upnpService, switchPower);

                }

            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                Service switchPower;
                if ((switchPower = device.findService(serviceId)) != null) {
                    System.out.println("Service disappeared: " + switchPower);
                }
            }

        };
    }
    void executeAction(UpnpService upnpService, Service switchPowerService,Boolean target) {

        ActionInvocation setTargetInvocation =
                new SetTargetActionInvocation(switchPowerService, target);

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(
                new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        assert invocation.getOutput().length == 0;
                        System.out.println("Successfully called action!");
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                    }
                }
        );

    }

    class SetTargetActionInvocation extends ActionInvocation {

        SetTargetActionInvocation(Service service, Boolean target) {
            super(service.getAction("SetTarget"));
            try {

                // Throws InvalidValueException if the value is of wrong type
                setInput("NewTargetValue", target);

            } catch (InvalidValueException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }

}
//Doc xml
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;

public class Setting {
    private Element root;
    public static final String ON = "on";
    public static final String OFF = "off";
    Setting() throws IOException, SAXException, ParserConfigurationException {
        this.root = parseNow();
    }
    public Element parseNow() throws ParserConfigurationException , SAXException,
            IOException {
        // Tạo một đối tượng DocumentBuilderFactory từ method tĩnh của nó
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Sét đặt việc kiểm tra tính hợp lệ của tài liệu sẽ phân tích sau này.
        factory.setValidating(true);

        // Tạo đối tượng DocumentBuilder
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Lay ra nut Document (mo ta toan bo tai lieu xml cua file xmlFile.xml
        Document xmlDoc = builder.parse("setting.xml");

        // Tu nut Document xmlDoc co the lay ra phan tu goc cua tai lieu XML
        Element root = (Element) xmlDoc.getDocumentElement();
        /**
         * Vi du nay khong phai la mot mau muc vi no bo qua viec su ly nhieu cac
         * kieu nut vi du co nhieu nut la con cua Document nhung khong phai la
         * con cua phan tu goc ...Vi du nay khong nhac den
         */
//        processNode(root);
        return root;
    }
    public NodeList getAllModes(){
        return this.root.getChildNodes();
    }
    public void setRoot(Element root){
        this.root = root;
    }
    public Element getModeById(String id){
        NodeList modes = this.getAllModes();
        for (int i = 0; i < modes.getLength(); i++) {
            Node mode = modes.item(i);
            short type = mode.getNodeType();
            if(type == Node.ELEMENT_NODE){
                NamedNodeMap nodeMap = mode.getAttributes();
                Attr idAttr = (Attr) nodeMap.getNamedItem("id");
                print(idAttr.getValue());
                if(idAttr.getValue().equals(id))
                    return  (Element) mode;
            }
        }
        return null;
    }
    public boolean getDeviceSettingOfModeByName(String modeId, String deviceName){
        Element mode = getModeById(modeId);
        print(mode.getNodeName());
        NodeList devices = mode.getElementsByTagName("device");
        for(int i = 0; i<devices.getLength(); i++){
            Node device = devices.item(i);
            short type = mode.getNodeType();
            if(type == Node.ELEMENT_NODE){
                NamedNodeMap nodeMap = device.getAttributes();
                Attr nameAttr = (Attr) nodeMap.getNamedItem("name");
                print(nameAttr.getValue());
                if(nameAttr.getValue().equals(deviceName)){
                    Attr setAttr = (Attr) nodeMap.getNamedItem("set");
                    if(setAttr.getValue().equals(Setting.ON)){
                        print("ON");
                        return true;
                    }else {
                        print("OFF");
                        return false;
                    }
                }
            }
        }
        return  false;
    }

    /** Su dung trong method processNode(...) */
    private void print(String s) {
        System.out.println(s);
    }
    public void refresh() throws IOException, SAXException, ParserConfigurationException {
        setRoot(parseNow());
    }

}

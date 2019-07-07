import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PomConvert {
    private  String filename;
    private  String target;

    public PomConvert(String filename, String target) throws Exception {
        this.filename = filename;
        this.target =target;
        this.init();
    }

    private void init() throws Exception {

        List<Node> profiles = this.getProfiles();
        HashMap<String, List<Entry<String, String>>> properties = new HashMap<>();
        for (Node profile : profiles
        ) {
            properties.put(this.getProfileName(profile), this.getProfileProperties(profile));
        }

    }
//
//    public String getLines() throws FileNotFoundException {
//
//
//        String file = getClass().getClassLoader().getResource(filename).getFile();
//        System.out.println(file);
//
//        Scanner s = new Scanner(new File(file));
//        String lines = "";
//        while (s.hasNext()) {
//
//            lines += s.next();
//            // System.out.println("-" + lines);
//
//        }
//
//        return lines;
//    }

    public List<Node> getProfiles() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        Document doc = getXmlDoc(filename);
        XPath xpath = getxPath();

        XPathExpression expr = xpath.compile("//profile");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);

        ArrayList<Node> profileList = new ArrayList<Node>();
        NodeList profiles = (NodeList) result;

        for (int i = 0; i < profiles.getLength(); i++) {
            profileList.add(profiles.item(i));
        }

        return profileList;
    }


    public String getProfileName(Node profile) throws Exception {

        for (int i = 0; i < profile.getChildNodes().getLength(); i++) {
            Node e = profile.getChildNodes().item(i);
            if (e.getNodeName().equals("id")) {
                return e.getTextContent();
            }
        }

        throw new Exception("the profile of the pom has no id");
    }


    public Node getProfileByName(String id) throws Exception {


        for (Node profile : this.getProfiles()) {

            if (this.getProfileName(profile).equals(id))
                return profile;
        }

        return null;

    }

    public List<Entry<String, String>> getProfileProperties(Node profile) {


        for (int i = 0; i < profile.getChildNodes().getLength(); i++) {
            Node e = profile.getChildNodes().item(i);

            if (e.getNodeName().equals("properties")) {
                return getProperties(e);
            }
        }

        return new ArrayList<>();

    }


    public void saveProfileInFile(String path, String extension) throws Exception {


        List<Node> profiles = getProfiles();
        for (int i = 0; i < profiles.size(); i++) {

            List<Entry<String, String>> properties = getProfileProperties(profiles.get(i));
            String id = getProfileName(profiles.get(i));

            String filename = path + id + extension;

            saveProperties(filename, properties);


        }
    }

    private void saveProperties(String fileName, List<Entry<String, String>> properties) throws Exception {

        FileWriter writer = new FileWriter(new File(fileName));
        for (Entry<String, String> entry : properties) {

            String line = entry.getFirst() + "=" + entry.getSecond();
            writer.write(line);
            writer.write(System.lineSeparator());
        }

        writer.flush();
        writer.close();
    }


    private List<Entry<String, String>> getProperties(Node properties) {
        List<Entry<String, String>> list = new ArrayList<>();


        for (int i = 0; i < properties.getChildNodes().getLength(); i++) {

            Node property = properties.getChildNodes().item(i);

            String key = property.getNodeName();
            String value = property.getTextContent();
            if (key.equals("#text"))
                continue;

            // System.out.println( key + "=" + value);
            list.add(new Entry(key, value));

        }


        return list;
    }


    private XPath getxPath() {
        XPathFactory xpathfactory = XPathFactory.newInstance();
        return xpathfactory.newXPath();
    }


    private Document getXmlDoc(String filename) throws IOException, SAXException, ParserConfigurationException {


        String file = getClass().getClassLoader().getResource(filename).getFile();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);

    }


    public List<Entry<String, String>> removeOtherPropeties(String other, String prod) throws Exception {


        Node prodProfile = getProfileByName(prod);
        List<Entry<String, String>> prodProperties = this.getProfileProperties(prodProfile);

        Node otherProfile = this.getProfileByName(other);
        List<Entry<String, String>> properties = this.getProfileProperties(otherProfile);

        return this.removePropertiesNotInProd(properties, prodProperties);


    }

    private List<Entry<String, String>> removePropertiesNotInProd(List<Entry<String, String>> otherProperties, List<Entry<String, String>> prodProperties) {


        List<Entry<String, String>> willbeDelete = new ArrayList<>();
        for (Entry<String, String> entry : otherProperties) {

            if (!contain(prodProperties, entry.getFirst())) {

                willbeDelete.add(entry);
            }
        }

        otherProperties.removeAll(willbeDelete);

        return otherProperties;

    }


    private boolean contain(List<Entry<String, String>> prodProperties, String key) {

        return prodProperties.stream().anyMatch(e -> e.getFirst().equals(key));
    }

    public void sycnProfileBaseProd(String prod) throws Exception {

        List<Node> profiles = this.getProfiles();
        List<Entry<String, String>> prodPropeties = this.getProfileProperties(this.getProfileByName(prod));

        this.saveProperties(target+ prod + ".properties", prodPropeties);


        for (Node profile : profiles) {
            List<Entry<String, String>> properties = removePropertiesNotInProd(getProfileProperties(profile), prodPropeties);

            String id = getProfileName(profile);

            if (!id.equals("prod"))

                this.saveProperties(target+ id + ".properties", properties);

        }
    }

    public void setPom(String pom) {
         this. filename = pom;
    }
}
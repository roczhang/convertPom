import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;


public class PomTestTest {


   private PomConvert pom = new PomConvert("pom.xml", "./conf/parent_");

    public PomTestTest() throws Exception {
    }


    @Test
    public void FindProfileElement() throws Exception {

    
        List<Node>  profiles =  pom.getProfiles();
        Assert.assertThat( profiles.size(), is(2));

    }


    @Test
    public void FindProfileNameTest() throws Exception {

        List<Node>  profiles =  pom.getProfiles();

        String name  = pom.getProfileName( profiles.get(0));
        Assert.assertThat( name, is("prod"));
    }




    @Test
    public void SaveProperties() throws Exception {

        pom.saveProfileInFile("./conf/server_" ,".properties");

    }

    @Test
    public void getProfileByName() throws Exception {

        Assert.assertThat( pom.getProfileName( pom.getProfileByName("prod")) .equals( "prod"), is(true));

    }

    @Test
    public void RemoveKeyBaseByProdandSave() throws Exception {

        List<Entry<String, String>> scPropeties = pom.removeOtherPropeties("sc", "prod");

        scPropeties.stream().forEach( e -> System.out.println( e.getFirst()));
        boolean no = scPropeties.stream().anyMatch( e -> e.getFirst().equals("script"));
        Assert.assertThat( no, is(false));

    }


    @Test
    public void deleteKeyAndSaveFile() throws Exception {

        pom.sycnProfileBaseProd( "prod");


    }

    @Test
    public void  ReadPomFile() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {


        Document doc = getXmlDoc("pom.xml");

        //Create XPath
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();

        // 1) Get book titles written after 2001
        XPathExpression expr = xpath.compile("//profile");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        System.out.println(nodes.getLength());
        System.out.println(nodes.item(0).getNodeName());

        System.out.println("-------");

        for (int i = 0; i < nodes.getLength(); i++) {

            Node item = nodes.item(i);
            String id = getIdName(item);
            System.out.println(item.getNodeName() +"=" + id);



            for ( int j = 0; j< item.getChildNodes().getLength(); j++){

                 Node e = item.getChildNodes().item(j);

                 printProperties(e, id);

                 }



            }


            System.out.println("-----------");

        }

    private String getIdName(Node item) {

        for (int j = 0; j < item.getChildNodes().getLength(); j++) {

            if( item.getChildNodes().item(j).getNodeName().equals("id"))
                return  item.getChildNodes().item(j).getTextContent();

        }

        System.out.println(" does not find any id");
        return "tmp";

    }

    private void printProperties(Node e, String id) throws IOException {


        Properties prop = new Properties();

        if( e.getNodeName().equals( "properties")){

            for ( int k = 0 ; k < e.getChildNodes().getLength(); k++){

                String name = e.getChildNodes().item(k).getNodeName() ;

                if( name .equals("#text")) continue;

                System.out.println( name + "=" + e.getChildNodes().item(k).getTextContent());

                prop.setProperty( e.getChildNodes().item(k).getNodeName(), e.getChildNodes().item(k).getTextContent());

            }

            prop.store(new FileOutputStream(id+ ".properties"), null);
    }


}


    @Test
    public void ReadXmlFile()
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {


        Document doc = getXmlDoc("inventory.xml");

        //Create XPath
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();


        // 1) Get book titles written after 2001
        XPathExpression expr = xpath.compile("//book");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        System.out.println(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {

            Node item = nodes.item(i);
            System.out.println(nodes.item(i).getNodeName() + " " + nodes.item(i).getBaseURI());

            System.out.println(   );
        }



    }

    private Document getXmlDoc(String filename) throws IOException, SAXException, ParserConfigurationException {

        //Build DOM

        String file = getClass().getClassLoader().getResource(filename).getFile();
        System.out.println(file);


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);


        return  doc;
    }


}
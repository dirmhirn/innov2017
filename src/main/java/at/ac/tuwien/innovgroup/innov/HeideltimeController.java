package at.ac.tuwien.innovgroup.innov;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Bernhard on 11.06.2017.
 */

@Controller
public class HeideltimeController {
    @RequestMapping("/heideltime")
    public String heideltime(
            @RequestParam(value = "text") String text,
            @RequestParam(value = "date") String date,
            Model model) throws ParserConfigurationException, IOException, SAXException {

        String result = "";

        List<TextTime> list = new ArrayList<>();




        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date dateDate = df.parse(date);

            HeidelTimeStandalone heidelTime = new HeidelTimeStandalone(
                    Language.ENGLISH,
                    DocumentType.NEWS,
                    OutputType.TIMEML,
                    "src/main/resources/config.props",
                    POSTagger.NO);

            result = heidelTime.process(text, dateDate);

        } catch (ParseException pe) {
            pe.printStackTrace();
        } catch (DocumentCreationTimeMissingException e) {
            e.printStackTrace();
        }

        //System.out.println(result);

        String file = result;


        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(file)));


        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        doc.normalize();

        if (doc.hasChildNodes()) {
            modNode(doc.getChildNodes(), list);
        }

        //Serialize DOM
        OutputFormat format    = new OutputFormat (doc);
        // as a String
        StringWriter stringOut = new StringWriter ();
        XMLSerializer serial   = new XMLSerializer (stringOut, format);
        serial.serialize(doc);
        // Display the XML
        System.out.println(stringOut.toString());



        result = stringOut.toString();

        TextTime tt = new TextTime();

        tt.setText("test");
        tt.setType("text");
        list.add(tt);

        model.addAttribute("text", text);
        model.addAttribute("date", date);
        model.addAttribute("result", result);
        model.addAttribute("list", list);

        return "heideltime";
    }

    private static void modNode(NodeList nodeList, List<TextTime> list) {

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);
            TextTime tt = new TextTime();

            if (tempNode.getNodeType() == Node.TEXT_NODE) {
                tt.setText(tempNode.getTextContent());
                tt.setType("text");
                list.add(tt);
            }
            else if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                if ("TIMEX3".equals(tempNode.getNodeName())) {
                    //tempNode.setTextContent(tempNode.getTextContent() + "<span style=\"background-color: yellow;\">Date: " + tempNode.getAttributes().getNamedItem("value").getNodeValue() + "</span>");
                    //System.out.println(tempNode.getTextContent() + "Date: " + tempNode.getAttributes().getNamedItem("value").getNodeValue());

                    tt.setText(tempNode.getAttributes().getNamedItem("value").getNodeValue());
                    tt.setType("timex3");
                    list.add(tt);

                }
            }

            if (nodeList.item(count).hasChildNodes()) {

                // loop again if has child nodes
                modNode(tempNode.getChildNodes(), list);
                //printNote(tempNode.getChildNodes());

            }
        }
    }



     /*private static void printNote(NodeList nodeList) {

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);


            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                // get node name and value
                System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
                System.out.println("Node Value =" + tempNode.getTextContent());

                if (tempNode.hasAttributes()) {

                    // get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();

                    for (int i = 0; i < nodeMap.getLength(); i++) {

                        Node node = nodeMap.item(i);
                        System.out.println("attr name : " + node.getNodeName());
                        System.out.println("attr value : " + node.getNodeValue());

                    }

                }

                if (tempNode.hasChildNodes()) {

                    // loop again if has child nodes
                    printNote(tempNode.getChildNodes());

                }

                System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

            } else if  (tempNode.getNodeType() == Node.TEXT_NODE) {

                System.out.println("TEXT     TEXT    " + tempNode.getTextContent());
            }


        }
    }*/
}
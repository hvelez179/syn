package com.teva.dimension;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This class is a build task that scales dimension resources into screen size specific resource folders.
 *
 * It is not part of the Respiratory App source.  It is only a build task.
 */
public class DimensionScalerTask extends DefaultTask {
    private ScaleProps[] scaleProps;

    private FileCollection from;
    private File to;

    /**
     * Sets the File object for the directory where the resources directories and files will be generated.
     */
    public void setTo(File to) {
        this.to = to;
    }

    /**
     * Sets the collection of scale factors.
     */
    public void setScaleProps(ScaleProps[] scaleProps) {
        this.scaleProps = scaleProps;
    }

    /**
     * Sets the collection of source files.
     */
    public void setFrom(FileCollection from) {
        this.from = from;
    }

    /**
     * Configure method for the task that is called by Gradle.
     */
    @Override
    public Task configure(groovy.lang.Closure closure) {
        Task task = super.configure(closure);

        if (!task.getInputs().getHasInputs()) {
            task.getInputs().files(from.getFiles().toArray());
        }

        if (!task.getOutputs().getHasOutput()) {
            task.getInputs().files(to);
        }

        return task;
    }

    /**
     * The task action for the DimensionScalerTask.
     */
    @TaskAction
    public void convert() {
        getLogger().info("scaling the dimensions.");

        for(ScaleProps props : scaleProps) {
            getLogger().info("ScaleProp " + props.name + " - " + props.value);
        }

        if (from != null) {
            for (File file : from) {
                getLogger().info("Processing " + file.getName());

                if (file.isDirectory()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles != null) {
                        for (File child : listFiles) {
                            getLogger().info("    " + child.getName());
                            convertDimensions(child);
                        }
                    }
                } else {
                    convertDimensions(file);
                }
            }
        }
    }

    /**
     * Converts all of the dimensions found in a values resource file.
     * @param file The file to convert.
     */
    private void convertDimensions(File file) {
        getLogger().info("convertDimensions: " + file.getName());

        Map<String, String> dimensions = new HashMap<>();

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document sourceDocument = documentBuilder.parse(file);

            sourceDocument.getDocumentElement().normalize();

            NodeList nodeList = sourceDocument.getElementsByTagName("dimen");
            for(int index = 0; index < nodeList.getLength(); index++) {
                Node node = nodeList.item(index);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)node;
                    String name = element.getAttribute("name");
                    String value = element.getTextContent();
                    dimensions.put(name, value);
                    getLogger().info("\tdimen: " + name + " = " + value);
                }
            }

            if (dimensions.size() > 0) {
                createScaledFiles(file.getName(), dimensions);
            }

        }
        catch (Exception ex) {
            getLogger().info("Exception occurred: " + ex);
        }
    }

    /**
     * Creates the scaled resource in generated resource files.
     * @param fileName The name of the source file.
     * @param dimensions The collection of dimenion names and values.
     */
    private void createScaledFiles(String fileName, Map<String, String> dimensions) throws Exception {
        getLogger().info("createScaledFiles: " + fileName);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        DecimalFormat decimalFormat = new DecimalFormat("0.0");

        for(int i = 0; i<scaleProps.length; i++) {
            String dirName = "values-" + scaleProps[i].name;
            float scale = scaleProps[i].value;

            getLogger().info("creating " + dirName + "/" + fileName + " with scale " + scale);

            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement("resources");

            document.appendChild(rootElement);

            for(Map.Entry<String, String> dimen : dimensions.entrySet()) {
                Element element = document.createElement("dimen");
                element.setAttribute("name", dimen.getKey());

                String valueString = dimen.getValue();
                String suffix = valueString.substring(valueString.length()-2);
                String number = valueString.substring(0, valueString.length()-2);

                float value = Float.parseFloat(number);
                value *= scale;

                element.setTextContent(decimalFormat.format(value) + suffix);
                getLogger().info("\t" + dimen.getKey() + " = " + decimalFormat.format(value) + suffix);

                rootElement.appendChild(element);
            }

            File directory = new File(to, dirName);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream outputStream = new FileOutputStream(file);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(outputStream);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        }
    }
}

package Components.MainWindowComponents.Highlighter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


public class StyleManager
{
    public static final String XML_NAME = "styles.xml";
    
    public List<SpecialStyle> styles = new ArrayList<SpecialStyle>();
    
    private static XmlManager xmlManager;
    
    private static StyleManager myInstance = null;
        
    static
    {
        try
        {
            File file = new File(XML_NAME);
            xmlManager = new XmlManager(file);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }  
    }
    
    private StyleManager(){}
    
    public static StyleManager getInstance()
    {
        if (myInstance == null)
            myInstance = new StyleManager();
        
        return myInstance;
    }
    
    
    public SpecialStyle getStyle(Styles name)
    {
        for (SpecialStyle style : styles)
            if (style.getName().equals(name.name())) {
            	//System.out.println(style);
                return style;
            }
        
        return null;
    }
    
    public void createStyles(StyledDocument document)
    {
        for (Element element : xmlManager.parseElements("style"))
        {
            NamedNodeMap attributes = element.getAttributes();
            
            String name		 = attributes.getNamedItem("name").getNodeValue();
            String red		 = attributes.getNamedItem("red").getNodeValue();
            String green	 = attributes.getNamedItem("green").getNodeValue();
            String blue		 = attributes.getNamedItem("blue").getNodeValue();
            String bold		 = attributes.getNamedItem("bold").getNodeValue();
            String italic	 = (attributes.getNamedItem("italic") == null ? "" : attributes.getNamedItem("italic").getNodeValue());
            String underline = (attributes.getNamedItem("underline") == null ? "" : attributes.getNamedItem("underline").getNodeValue());
            
            Color color = createColor(red, green, blue);            
            Style style = createStyle(document, name, color, Boolean.parseBoolean(bold), Boolean.parseBoolean(italic), Boolean.parseBoolean(underline));
            
            styles.add(new SpecialStyle(name, style));
        }
    }

    private Style createStyle(StyledDocument document, String name, Color color, boolean bold, boolean italic, boolean underline)
    {
        Style style = document.addStyle(name, null);
        StyleConstants.setForeground(style, color);        
        if (bold) StyleConstants.setBold(style, true);
        if (italic) StyleConstants.setItalic(style, true);
        if (underline) StyleConstants.setUnderline(style, true);
        
        return style;
        
    }

    private Color createColor(String red, String green, String blue)
    {
        int r = Integer.parseInt(red,16);
        int g = Integer.parseInt(green, 16);
        int b = Integer.parseInt(blue, 16);
        
        return new Color(r, g, b);
    }
}

package gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.*;

import main.Settings;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import main.AssetManager;

public class GUIParser {
	
	private static final String GUI_RES = "res" + File.separator + "gui"
			+ File.separator;
	
	private static HashMap<String, GUIElement> elements = 
			new HashMap<String, GUIElement>();
	
	public static HashMap<String, GUIElement> loadGUI() {
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser parser = null;
		XMLReader xmlReader = null;
		
		try {
			
			parser = spf.newSAXParser();
			xmlReader = parser.getXMLReader();
			
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		
		xmlReader.setContentHandler(new GUIContentHandler(elements));
		
		String[] files = new File(GUI_RES).list();
		
		for (String filename : files) {
			
			File file = new File(GUI_RES + filename);
			
			if (!file.isFile()) continue;
			
			try {
				
				xmlReader.parse(convertToFileURL(file));
				
			} catch (IOException | SAXException e) {
				e.printStackTrace();
			}
		}
		
		((GUIContentHandler) xmlReader.getContentHandler()).link();
		
		return elements;
	}
	
	private static String convertToFileURL(File file) {
		
		String path = file.getAbsolutePath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}
		
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return "file:" + path;
	}
}

class GUIContentHandler extends DefaultHandler {
	
	HashMap<String, GUIElement> elements;
	HashMap<GUIElement, String> linkParent = 
			new HashMap<GUIElement, String>();
	HashMap<GUIElement, String> linkConstraint = 
			new HashMap<GUIElement, String>();
	GUIElement currentElement;
	
	private static HashMap<String, Object> variables = 
			new HashMap<String, Object>();
	
	GUIContentHandler(HashMap<String, GUIElement> elements) {
		
		this.elements = elements;
	}
	
	public void endDocument() {
		
		variables.clear();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
		
		int attrLength = attributes.getLength();
		
		switch (localName) {
		case "var":
			variables.put(attributes.getValue("name"), 
					resolve(attributes.getValue("value")));
			return;
		case "element":
			break;
		case "button":
			currentElement = new Button();
			for (int i = 0; i < attrLength; i++) {
				switch (attributes.getLocalName(i)) {
				case "image":
					((Button) currentElement).setImage(
							AssetManager.getTexture(attributes.getValue(i)));
					break;
				case "text":
					((Button) currentElement).setText(
							(String) resolve(attributes.getValue(i)));
				case "button_onEnter":
				case "button_onExit":
				case "button_onPress":
				case "button_onRelease":
				case "button_onClick":
					String action;
					switch (localName) {
					case "button_onEnter":
						action = Button.ENTERED;
					case "button_onExit":
						action = Button.EXITED;
					case "button_onPress":
						action = Button.PRESSED;
					case "button_onRelease":
						action = Button.RELEASED;
					case "button_onClick":
					default:
						action = Button.CLICKED;
					}
					
					((Button) currentElement).addActionListener(
							new java.awt.event.ActionListener() {
								
								script.Script script;
								String action;
								
								@Override
								public void actionPerformed(ActionEvent e) {
									
									if (e.getActionCommand() == action)
										script.eval();
								}
								
								private java.awt.event.ActionListener init(
										script.Script script, String action) {
									
									this.script = script;
									this.action = action;
									return this;
								}
						
					}.init(AssetManager.getScript(attributes.getValue(i)), action));
					break;
				}
			}
			break;
		case "textbox":
			currentElement = new TextBox();
			for (int i = 0; i < attrLength; i++) {
				switch (attributes.getLocalName(i)) {
				case "text":
					((TextBox) currentElement).setText(
							(String) resolve(attributes.getValue(i)));
				case "textbox_font":
					break;
				case "textbox_insets":
					break;
				case "textbox_renderAsTexture":
					break;
				}
			}
			break;
		case "panel":
			currentElement = new Panel();
			break;
		case "menubar":
			currentElement = new MenuBar();
			break;
		case "menu":
			currentElement = new Menu();
			for (int i = 0; i < attrLength; i++) {
				switch (attributes.getLocalName(i)) {
				case "title":
					((Menu) currentElement).setTitle(
							(String) resolve(attributes.getValue(i)));
					break;
				}
			}
		}
		
		for (int i = 0; i < attrLength; i++) {
			switch (attributes.getLocalName(i)) {
			case "name":
				currentElement.setName(attributes.getValue(i));
				break;
			case "layout_width":
				currentElement.setSize((int) resolve(attributes.getValue(i)),
						currentElement.height);
				break;
			case "layout_height":
				currentElement.setSize(currentElement.width,
						(int) resolve(attributes.getValue(i)));
				break;
			case "layout_x":
				currentElement.setPosition((int) resolve(attributes.getValue(i)), currentElement.y);
				break;
			case "layout_y":
				currentElement.setPosition(currentElement.x, (int) resolve(attributes.getValue(i)));
				break;
			case "layout_autowidth":
				currentElement.autoWidth = (boolean) resolve(attributes.getValue(i));
				break;
			case "layout_autoheight":
				currentElement.autoHeight = (boolean) resolve(attributes.getValue(i));
				break;
			case "layout_parent":
				linkParent.put(currentElement, attributes.getValue(i));
				break;
			case "layout_constraint":
				linkConstraint.put(currentElement, attributes.getValue(i));
				break;
			case "visible":
				currentElement.setVisible((boolean) resolve(attributes
						.getValue(i)));
			}
		}
	}
	
	public void endElement(String uri, String localname, String qname) {
		
		if (currentElement != null)
			elements.put(currentElement.getName(), currentElement);
		currentElement = null;
	}
	
	private static Object resolve(String input) {
		
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
		}
		
		if (input.toLowerCase().equals("true"))
			return true;
		else if (input.toLowerCase().equals("false"))
			return false;
		if (input.toLowerCase().equals("null"))
			return null;
		
		char[] temp = new char[input.length()];
		int index = 0;
		
		switch (input) {
		case "@CONTENT":
			return GUI.content;
		case "@TOP":
			return 0;
		case "@LEFT":
			return 0;
		case "@BOTTOM":
			return Settings.windowHeight;
		case "@RIGHT":
			return Settings.windowWidth;
		}
		
		for(int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);
			
			switch (currentChar) {
			case '/':
				i++;
				currentChar = input.charAt(i);
				break;
			case '@':
				if (index == 0) {
					
					temp = null;
					return variables.get(input.substring(++i, input.length()));
				}
			}
			
			temp[index] = currentChar;
			index++;
		}
		
		input = String.valueOf(temp, 0, index);
		temp = null;
		
		return input;
	}
	
	public void link() {
		
		//TODO use magic to iterate and remove elements from linkparent
		//source of magic --> http://stackoverflow.com/questions/1884889/iterating-over-and-removing-from-a-map
		for (GUIElement child : linkParent.keySet()) {
			
			Container parent = (Container) elements.get(linkParent.get(child));
			if (parent == null) continue;
			
			Object constraint = linkConstraint.get(child);
			
			if (constraint != null) 
				parent.addChild(child, constraint);
			else parent.addChild(child);
			
			elements.remove(child);
			linkConstraint.remove(child);
		}
	}
}
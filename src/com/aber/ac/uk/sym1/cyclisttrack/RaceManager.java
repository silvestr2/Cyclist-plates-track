package com.aber.ac.uk.sym1.cyclisttrack;

/**
 * This class contains raw cyclist data. This class sorts data and saves it to xml file
 * @author Sylwester Mazur
 */
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.os.Environment;

public class RaceManager extends Thread {

	public ArrayList<RecognizedPicture> rawdata = new ArrayList<RecognizedPicture>();

	public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/CyclistAppData/";
	MainActivity context;

	RaceManager(MainActivity context) {
		this.context = context;
	}

	/**
	 * This method sorts rawdata arraylist that contain unsorted cyclist data.
	 * 
	 * @return returns sortedData arraylist with data sorted
	 */
	public ArrayList<RecognizedPicture> getSorted() {
		ArrayList<RecognizedPicture> sortedData = new ArrayList<RecognizedPicture>();
		if(!rawdata.isEmpty()){
		sortedData.add(rawdata.get(0)); // add first element to the sorted
										// arraylist
			for (int i = 1; i < rawdata.size(); i++) {
			boolean cointains = false;
			for (int j = 0; j < sortedData.size(); j++) { // if sortedData contains this number then dont add again
				if (0 == sortedData.get(j).getRaceNumber().compareTo(rawdata.get(i).getRaceNumber())) {
					cointains = true;
					break;
				}
			}
			if (!cointains) // if does not contain add it to the sorted ones
				sortedData.add(rawdata.get(i));
		}
		}
		return sortedData;
	}
	
	/**
	 * This method saves sorted cyclist data on the external storage (micro sd)
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void saveToXml() throws ParserConfigurationException, IOException,
			TransformerException {
		
		DocumentBuilderFactory dFactBuilder = DocumentBuilderFactory.newInstance();
		DocumentBuilder build = dFactBuilder.newDocumentBuilder();
		Document document = build.newDocument();
		Element listRoot = document.createElement("CyclistScoreTable");
		document.appendChild(listRoot);

		for (RecognizedPicture rp : getSorted()) {
			Element number = document.createElement("CyclistNo");
			number.appendChild(document.createTextNode(rp.getRaceNumber()));
			listRoot.appendChild(number);
			
			Element time = document.createElement("CyclistTime");
			time.appendChild(document.createTextNode(rp.getTime()));
			listRoot.appendChild(time);

		}
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource dSource = new DOMSource(document);

		FileWriter fos = new FileWriter(DATA_PATH + "scores.xml");
		StreamResult sResult = new StreamResult(fos);
		transformer.transform(dSource, sResult);

	}

	/**
	 * This method adds all detected race numbers to the rawdata arraylist
	 * (cannot contain spaces, new line signs), and cannot be more than 100 as
	 * app requirement was up to number 99
	 * 
	 * @param num
	 *            recognized number
	 */
	public void registerNumber(final RecognizedPicture num) {

		try {
			if (!num.getRaceNumber().contains(" ")
					&& !num.getRaceNumber().contains("\n")
					&& !num.getRaceNumber().contains("\\n")) {
				int number = Integer.parseInt(num.getRaceNumber());

				if (number > 0 && num.getRaceNumber().length()<=2) //race numner only 2 digit as the maximum number in the number plates is 99
				{
					rawdata.add(num);
				}
			}
		} catch (Exception e) {
		}
	}

	public void run() {
		while (MainActivity.ThreadRun) {
			RecognizedPicture p = null;
			while (p == null) {
				p = MainActivity.recognized.poll();
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.registerNumber(p);
		}
	}
}

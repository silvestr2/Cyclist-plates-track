package com.aber.ac.uk.sym1.cyclisttrack;
/**
 * This class contains cyclist unrecognized data
 * @author Sylwester Mazur
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.core.Mat;

public class UnrecognizedPicture {
	private Mat plate;
	private String time;
	private long milisecTime;


	public UnrecognizedPicture() {
	    milisecTime=System.currentTimeMillis();
		time = new SimpleDateFormat("hh:mm:ss:SSS").format(new Date(milisecTime));

	}

	public void setPlate(Mat plate) {
		this.plate = plate;
	}

	public Mat getPlate() {
		return plate;
	}

	public String getTime() {
		return time;
	}
	
	public String getMilisecTime() {
		String mili = String.valueOf(milisecTime);
		return mili;
	}

}

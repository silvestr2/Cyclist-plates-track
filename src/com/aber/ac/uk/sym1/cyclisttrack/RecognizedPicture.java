package com.aber.ac.uk.sym1.cyclisttrack;
/**
 * This class contains cyclist recognized data
 * @author Sylwester Mazur
 *
 */
public class RecognizedPicture {
	private String time;
	private String raceNumber;

	public RecognizedPicture(String time, String raceNumber) {
		this.time = time;
		this.raceNumber = raceNumber;
	}

	public String getTime() {
		return time;
	}

	public String getRaceNumber() {
		return raceNumber;
	}

	public String toString() {
		return "number " + raceNumber + " detected at " + time;
	}
}

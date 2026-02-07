package entity;

import java.time.LocalDate;

public class SiblingInfo {

	private String firstName;
	private LocalDate birthday;
	private boolean inSchool;
	private String gender;

	public SiblingInfo(String firstName, LocalDate birthday, boolean inSchool, String gender) {
		this.firstName = firstName;
		this.birthday = birthday;
		this.inSchool = inSchool;
		this.gender = gender;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public boolean isInSchool() {
		return inSchool;
	}

	public void setInSchool(boolean inSchool) {
		this.inSchool = inSchool;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Returns "brother", "sister", or "sibling" based on gender.
	 */
	public String getRelationLabel() {
		if (gender == null) {
			return "sibling";
		}
		return switch (gender.toLowerCase()) {
			case "male" -> "brother";
			case "female" -> "sister";
			default -> "sibling";
		};
	}
}

package entity;

import java.time.LocalDate;

public class SiblingInfo {

	private String firstName;
	private LocalDate birthday;
	private boolean inSchool;

	public SiblingInfo(String firstName, LocalDate birthday, boolean inSchool) {
		this.firstName = firstName;
		this.birthday = birthday;
		this.inSchool = inSchool;
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
}



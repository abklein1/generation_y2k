package entity;

public class ParentInfo {

	private String role;
	private String firstName;

	public ParentInfo(String role, String firstName) {
		this.role = role;
		this.firstName = firstName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
}



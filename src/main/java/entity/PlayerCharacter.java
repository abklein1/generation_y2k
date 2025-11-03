package entity;

public class PlayerCharacter extends Student {

    private int siblingsNumber;
	private FamilyInfo familyInfo = new FamilyInfo();

    public PlayerCharacter() {
        super();
    }

    @Override
    public String toString() {
        this.siblingsNumber = 0;
        return super.toString();
    }

    public void setSiblings(int siblings) {
        this.siblingsNumber = siblings;
    }

    public int getSiblings() {
        return this.siblingsNumber;
    }

	public FamilyInfo getFamilyInfo() {
		return familyInfo;
	}

	public void setFamilyInfo(FamilyInfo familyInfo) {
		this.familyInfo = familyInfo;
	}
}

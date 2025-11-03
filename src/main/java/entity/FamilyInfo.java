package entity;

import java.util.ArrayList;
import java.util.List;

public class FamilyInfo {

	private ParentInfo mother;
	private ParentInfo father;
	private final List<SiblingInfo> siblings;

	public FamilyInfo() {
		this.siblings = new ArrayList<>();
	}

	public ParentInfo getMother() {
		return mother;
	}

	public void setMother(ParentInfo mother) {
		this.mother = mother;
	}

	public ParentInfo getFather() {
		return father;
	}

	public void setFather(ParentInfo father) {
		this.father = father;
	}

	public List<SiblingInfo> getSiblings() {
		return siblings;
	}

	public void addSibling(SiblingInfo sibling) {
		this.siblings.add(sibling);
	}
}



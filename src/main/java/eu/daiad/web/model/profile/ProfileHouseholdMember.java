package eu.daiad.web.model.profile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.EnumGender;

public class ProfileHouseholdMember {

	private int id;

	private String name;

	@JsonDeserialize(using = EnumGender.Deserializer.class)
	private EnumGender gender;

	private int age;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EnumGender getGender() {
		return gender;
	}

	public void setGender(EnumGender gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}

package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.EnumGender;

@Entity(name = "survey")
@Table(schema = "public", name = "survey")
public class SurveyEntity {

    @Id()
    @Column(name = "username")
    private String username;

    @ManyToOne()
    @JoinColumn(name = "utility_id", nullable = false)
    private Utility utility;

    @Basic()
    private String firstname;

    @Basic()
    private String lastname;

    @Basic()
    private String address;

    @Basic()
    private String city;

    @Enumerated(EnumType.STRING)
    private EnumGender gender;

    @Column(name = "number_of_showers")
    private int numberOfShowers;

    @Column(name = "smart_phone_os")
    private String smartPhoneOs;

    @Column(name = "table_os")
    private String tabletOs;

    @Column(name = "apartment_size_bracket")
    private String apartmentSizeBracket;

    @Basic()
    private Integer age;

    @Column(name = "household_member_total")
    private int householdMemberTotal;

    @Column(name = "household_member_female")
    private int householdMemberFemale;

    @Column(name = "household_member_male")
    private int householdMemberMale;

    @Column(name = "income_bracket")
    private String incomeBracket;

    @Column(name = "meter_id")
    private String meter;

    @Column(name = "shower_per_week")
    private Integer showersPerWeek;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Utility getUtility() {
        return utility;
    }

    public void setUtility(Utility utility) {
        this.utility = utility;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public EnumGender getGender() {
        return gender;
    }

    public void setGender(EnumGender gender) {
        this.gender = gender;
    }

    public int getNumberOfShowers() {
        return numberOfShowers;
    }

    public void setNumberOfShowers(int numberOfShowers) {
        this.numberOfShowers = numberOfShowers;
    }

    public String getSmartPhoneOs() {
        return smartPhoneOs;
    }

    public void setSmartPhoneOs(String smartPhoneOs) {
        this.smartPhoneOs = smartPhoneOs;
    }

    public String getTabletOs() {
        return tabletOs;
    }

    public void setTabletOs(String tabletOs) {
        this.tabletOs = tabletOs;
    }

    public String getApartmentSizeBracket() {
        return apartmentSizeBracket;
    }

    public void setApartmentSizeBracket(String apartmentSizeBracket) {
        this.apartmentSizeBracket = apartmentSizeBracket;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public int getHouseholdMemberTotal() {
        return householdMemberTotal;
    }

    public void setHouseholdMemberTotal(int householdMemberTotal) {
        this.householdMemberTotal = householdMemberTotal;
    }

    public int getHouseholdMemberFemale() {
        return householdMemberFemale;
    }

    public void setHouseholdMemberFemale(int householdMemberFemale) {
        this.householdMemberFemale = householdMemberFemale;
    }

    public int getHouseholdMemberMale() {
        return householdMemberMale;
    }

    public void setHouseholdMemberMale(int householdMemberMale) {
        this.householdMemberMale = householdMemberMale;
    }

    public String getIncomeBracket() {
        return incomeBracket;
    }

    public void setIncomeBracket(String incomeBracket) {
        this.incomeBracket = incomeBracket;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public Integer getShowersPerWeek() {
        return showersPerWeek;
    }

    public void setShowersPerWeek(Integer showersPerWeek) {
        this.showersPerWeek = showersPerWeek;
    }

}

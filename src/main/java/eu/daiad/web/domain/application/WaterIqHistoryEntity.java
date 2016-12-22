package eu.daiad.web.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "water_iq_history")
@Table(schema = "public", name = "water_iq_history")
public class WaterIqHistoryEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "water_iq_history_id_seq", name = "water_iq_history_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "water_iq_history_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn;

    @Column(name = "interval_from", length = 8)
    private String from;

    @Column(name = "interval_to", length = 8)
    private String to;

    @Column(name = "user_value", length = 2)
    private String userValue;

    @Column(name = "user_volume")
    private double userVolume;

    @Column(name = "similar_value", length = 2)
    private String similarUserValue;

    @Column(name = "similar_volume")
    private double similarUserVolume;

    @Column(name = "nearest_value", length = 2)
    private String nearestUserValue;

    @Column(name = "nearest_volume")
    private double nearestUserVolume;

    @Column(name = "all_value", length = 2)
    private String allUserValue;

    @Column(name = "all_volume")
    private double allUserVolume;

    @Column(name = "user_1m_consumption")
    private double userLast1MonthConsmution;

    @Column(name = "similar_1m_consumption")
    private double similarLast1MonthConsmution;

    @Column(name = "nearest_1m_consumption")
    private double nearestLast1MonthConsmution;

    @Column(name = "all_1m_consumption")
    private double allLast1MonthConsmution;

    @Column(name = "interval_year")
    private int year;

    @Column(name = "interval_month")
    private int month;

    public int getId() {
        return id;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUserValue() {
        return userValue;
    }

    public void setUserValue(String userValue) {
        this.userValue = userValue;
    }

    public double getUserVolume() {
        return userVolume;
    }

    public void setUserVolume(double userVolume) {
        this.userVolume = userVolume;
    }

    public String getSimilarUserValue() {
        return similarUserValue;
    }

    public void setSimilarUserValue(String similarUserValue) {
        this.similarUserValue = similarUserValue;
    }

    public double getSimilarUserVolume() {
        return similarUserVolume;
    }

    public void setSimilarUserVolume(double similarUserVolume) {
        this.similarUserVolume = similarUserVolume;
    }

    public String getNearestUserValue() {
        return nearestUserValue;
    }

    public void setNearestUserValue(String nearestUserValue) {
        this.nearestUserValue = nearestUserValue;
    }

    public double getNearestUserVolume() {
        return nearestUserVolume;
    }

    public void setNearestUserVolume(double nearestUserVolume) {
        this.nearestUserVolume = nearestUserVolume;
    }

    public String getAllUserValue() {
        return allUserValue;
    }

    public void setAllUserValue(String allUserValue) {
        this.allUserValue = allUserValue;
    }

    public double getAllUserVolume() {
        return allUserVolume;
    }

    public void setAllUserVolume(double allUserVolume) {
        this.allUserVolume = allUserVolume;
    }

    public double getUserLast1MonthConsmution() {
        return userLast1MonthConsmution;
    }

    public void setUserLast1MonthConsmution(double userLast1MonthConsmution) {
        this.userLast1MonthConsmution = userLast1MonthConsmution;
    }

    public double getSimilarLast1MonthConsmution() {
        return similarLast1MonthConsmution;
    }

    public void setSimilarLast1MonthConsmution(double similarLast1MonthConsmution) {
        this.similarLast1MonthConsmution = similarLast1MonthConsmution;
    }

    public double getNearestLast1MonthConsmution() {
        return nearestLast1MonthConsmution;
    }

    public void setNearestLast1MonthConsmution(double nearestLast1MonthConsmution) {
        this.nearestLast1MonthConsmution = nearestLast1MonthConsmution;
    }

    public double getAllLast1MonthConsmution() {
        return allLast1MonthConsmution;
    }

    public void setAllLast1MonthConsmution(double allLast1MonthConsmution) {
        this.allLast1MonthConsmution = allLast1MonthConsmution;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

}

package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "water_iq")
@Table(schema = "public", name = "water_iq")
public class WaterIqEntity {

    @Id
    @GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "account"))
    @GeneratedValue(generator = "generator")
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn()
    private AccountEntity account;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Set<WaterIqHistoryEntity> history = new HashSet<WaterIqHistoryEntity>();

    @Column(name = "updated_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedOn;

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

    public int getId() {
        return id;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public Set<WaterIqHistoryEntity> getHistory() {
        return history;
    }

    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
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

}

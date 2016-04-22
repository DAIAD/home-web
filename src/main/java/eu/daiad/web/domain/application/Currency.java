//package eu.daiad.web.domain.application;
//
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.SequenceGenerator;
//import javax.persistence.Table;
//import org.hibernate.annotations.Type;
//import org.joda.time.DateTime;
//
///**
// *
// * @author nkarag
// */
//@Entity(name = "currency")
//@Table(schema = "public", name = "currency")
//public class Currency {
// 
//    @Id()
//    @Column(name = "id")
//    @SequenceGenerator(sequenceName = "currency_id_seq", name = "currency_id_seq", allocationSize = 1, initialValue = 1)
//    @GeneratedValue(generator = "currency_id_seq", strategy = GenerationType.SEQUENCE)
//    private int id;  
//
//    @Column(name = "ISO_code")
//    private String ISO_code;
//
//    @Column(name = "name")
//    private String name;
//    
//    @Column(name = "to_eur")
//    private float toEur;    
//
//    @Column(name = "to_gbp")
//    private float toGbp;     
//    
//    @Column(name = "date_modified")
//    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
//    private DateTime date_modified;
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getISO_code() {
//        return ISO_code;
//    }
//
//    public void setISO_code(String ISO_code) {
//        this.ISO_code = ISO_code;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public float getToEur() {
//        return toEur;
//    }
//
//    public void setToEur(float toEur) {
//        this.toEur = toEur;
//    }
//
//    public float getToGbp() {
//        return toGbp;
//    }
//
//    public void setToGbp(float toGbp) {
//        this.toGbp = toGbp;
//    }
//
//    public DateTime getDate_modified() {
//        return date_modified;
//    }
//
//    public void setDate_modified(DateTime date_modified) {
//        this.date_modified = date_modified;
//    }
//    
//
//}

package eu.daiad.common.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "device_amphiro_config_default")
@Table(schema = "public", name = "device_amphiro_config_default")
public class DeviceAmphiroDefaultConfigurationEntity {

	public static final int CONFIG_DEFAULT = 1;
	public static final int CONFIG_ENABLED_METRIC = 2;
	public static final int CONFIG_ENABLED_IMPERIAL = 3;

	@Id()
	@Column(name = "id")
	private int id;

	@Basic()
	private String title;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "configuration_block")
	private int block;

	@Column(name = "value_1")
	private int value1;

	@Column(name = "value_2")
	private int value2;

	@Column(name = "value_3")
	private int value3;

	@Column(name = "value_4")
	private int value4;

	@Column(name = "value_5")
	private int value5;

	@Column(name = "value_6")
	private int value6;

	@Column(name = "value_7")
	private int value7;

	@Column(name = "value_8")
	private int value8;

	@Column(name = "value_9")
	private int value9;

	@Column(name = "value_10")
	private int value10;

	@Column(name = "value_11")
	private int value11;

	@Column(name = "value_12")
	private int value12;

	@Column(name = "frame_number")
	private int numberOfFrames;

	@Column(name = "frame_duration")
	private int frameDuration;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public int getBlock() {
		return block;
	}

	public void setBlock(int block) {
		this.block = block;
	}

	public int getValue1() {
		return value1;
	}

	public void setValue1(int value1) {
		this.value1 = value1;
	}

	public int getValue2() {
		return value2;
	}

	public void setValue2(int value2) {
		this.value2 = value2;
	}

	public int getValue3() {
		return value3;
	}

	public void setValue3(int value3) {
		this.value3 = value3;
	}

	public int getValue4() {
		return value4;
	}

	public void setValue4(int value4) {
		this.value4 = value4;
	}

	public int getValue5() {
		return value5;
	}

	public void setValue5(int value5) {
		this.value5 = value5;
	}

	public int getValue6() {
		return value6;
	}

	public void setValue6(int value6) {
		this.value6 = value6;
	}

	public int getValue7() {
		return value7;
	}

	public void setValue7(int value7) {
		this.value7 = value7;
	}

	public int getValue8() {
		return value8;
	}

	public void setValue8(int value8) {
		this.value8 = value8;
	}

	public int getValue9() {
		return value9;
	}

	public void setValue9(int value9) {
		this.value9 = value9;
	}

	public int getValue10() {
		return value10;
	}

	public void setValue10(int value10) {
		this.value10 = value10;
	}

	public int getValue11() {
		return value11;
	}

	public void setValue11(int value11) {
		this.value11 = value11;
	}

	public int getValue12() {
		return value12;
	}

	public void setValue12(int value12) {
		this.value12 = value12;
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

	public void setNumberOfFrames(int numberOfFrames) {
		this.numberOfFrames = numberOfFrames;
	}

	public int getFrameDuration() {
		return frameDuration;
	}

	public void setFrameDuration(int frameDuration) {
		this.frameDuration = frameDuration;
	}

	public int getId() {
		return id;
	}

}

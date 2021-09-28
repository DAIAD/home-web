package eu.daiad.scheduler.model.thingslog;

import java.time.ZonedDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThingsLogMeasurement {

	private float reading;

	private ZonedDateTime date;

}

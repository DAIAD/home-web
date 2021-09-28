package eu.daiad.scheduler.model.thingslog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class ThingsLogLoginRequest {

	private String username;

	private String password;

}

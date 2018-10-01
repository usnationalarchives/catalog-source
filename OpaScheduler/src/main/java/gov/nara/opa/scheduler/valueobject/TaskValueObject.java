package gov.nara.opa.scheduler.valueobject;

import java.sql.Time;
import java.util.Date;

public class TaskValueObject extends TaskValueObjectConstants {

	private String taskName;
	private String endpoint;
	private Time time;

	public TaskValueObject(String taskName) {
		this.taskName = taskName;
	}

	public TaskValueObject(String taskName, String endpoint, Time time) {
		this.taskName = taskName;
		this.endpoint = endpoint;
		this.time = time;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public String toString() {
		return String.format("Task: %1$s, API endpoint= %2$s time=%3$s", getTaskName(), getEndpoint(), getTime().toString());
	}
}

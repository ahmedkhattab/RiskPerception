package models;

import twitter4j.Status;

public class ClassifiedStatus {
	Status status;
	String classOfstatus;
	
	public ClassifiedStatus(Status status, String classOfstatus){
		this.status = status;
		this.classOfstatus = classOfstatus;
	}
	
	public String getClassOfstatus()
	{
		return this.classOfstatus;
	}
	
	public Status getStatus()
	{
		return this.status;
	}
}

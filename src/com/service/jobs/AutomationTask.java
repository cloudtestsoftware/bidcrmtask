package com.service.jobs;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class AutomationTask extends Thread {

	
	AutomationTaskService service=new AutomationTaskService();
	
	EmailListTaskService emailservice=new EmailListTaskService();
	
	ArrayList <ExecutorService> servicelist= new ArrayList<ExecutorService>();
	
	public  void run(){
	  
		//start automation
		service.executeJob(); 
		servicelist.add(service.getExecutorService());
		
		//start emaillist
		emailservice.executeJob(); 
		servicelist.add(emailservice.getExecutorService());
		try{
			service.start();
			emailservice.start();
			
		}catch(Exception e){
			
		}
	
			
	}
	
	public ArrayList <ExecutorService> getExecutorList(){
		return servicelist;
	}

	
}

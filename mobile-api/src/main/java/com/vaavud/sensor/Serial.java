package com.vaavud.sensor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public class Serial implements javax.servlet.ServletContextListener {
  
  public void contextInitialized(ServletContext context) {
    System.out.println("Hello I ran on startup!");
 }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void contextInitialized(ServletContextEvent arg0) {
    // TODO Auto-generated method stub
    System.out.println("Hello I ran on startup2!");
  }
}

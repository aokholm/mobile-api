package com.vaavud.sensor;

public class MyBean {
  
  private int counter = 0;
  private String foo = "Default Foo";

  public String getFoo() {
    return (this.foo);
  }

  public void setFoo(String foo) {
    this.foo = foo;
  }

  private int bar = 0;

  public int getBar() {
    return (this.bar);
  }

  public void setBar(int bar) {
    this.bar = bar;
  }
  
  public void increaseCounter() {
    counter++;
  }
  
  @Override
  public String toString() {
    return "MyBean [counter=" + counter + ", foo=" + foo + ", bar=" + bar + "]";
  }
  
  
}

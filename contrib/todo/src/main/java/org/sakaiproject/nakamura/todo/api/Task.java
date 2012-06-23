package org.sakaiproject.nakamura.todo.api;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Task {

  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private Long id;
  
  @Column(allowsNull="false")
  private String name;
  
  @Column
  private String description;
  
  @Column
  private String instructions;
  
  @Column(allowsNull="false")
  private String who;

  public Task() {
    
  }
  
  public Task(String name, String who) {
    this.name = name;
    this.who = who;
  }
  
  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the instructions
   */
  public String getInstructions() {
    return instructions;
  }

  /**
   * @param instructions the instructions to set
   */
  public void setInstructions(String instructions) {
    this.instructions = instructions;
  }

  /**
   * @return the who
   */
  public String getWho() {
    return who;
  }

  /**
   * @param who the who to set
   */
  public void setWho(String who) {
    this.who = who;
  }
  
  @Override
  public String toString() {
    return String.format("{ id=%s, name=%s }", String.valueOf(id), name);
  }
  
}

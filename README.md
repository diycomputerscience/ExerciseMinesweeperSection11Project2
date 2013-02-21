<h1>Section 11 Project 2</h1>

<h2>Overview</h2>

In the previous exercise, we wrote database code to persist the board in a relational database. In that exercise, we used JDBC to interact with a relational database. We used JDBC with the chief purpose of being able to work with SQL queries in Java. However, most modern Java applications (both web and desktop), do not use JDBC and SQL directly. Instead they use an [ORM](http://en.wikipedia.org/wiki/Object-relational_mapping) (object relational mapping) tool, like Hibernate, or in many cases, [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API) (Java Persistence API), which is a layer of abstraction over an ORM. Using JPA let's us switch the underlying ORM implementation.

In this exercise, we will write database code to the JPA API, and use [Hibernate](http://www.hibernate.org/) as the underlying implementation of JPA. As a side note it is interesting that Hibernate in turn uses JDBC as the underlying technology. Isn't it fascinating that in effect our database code is going across 3 layers ?

Since, we now have model classes which use different technologies, we have further split the model package into two sub-packages: ```com.diycomputerscience.minesweeper.model.jdbc```, and ```com.diycomputerscience.minesweeper.model.jpa```. Classes that you worked with in the previous exercise, such as ```JDBCPersistenceStrategy```, ```DBInit```, and ```BoardDaoJDBC``` have now been moved to the ```jdbc``` package. Classes for JPA will be in the ```jpa``` package.

Since we do have write any SQL code, when working with JPA, how do you think data is persisted and retrieved ? Someone needs to communicate with the database, isn't it ? 

The JPA is a specification for communicating with the database using object notation rather than SQL. Hibernate implements the JPA specification. As you will soon see in the code, we communicate with JPA through Entity objects and the EntityManager, and these classes in turn communicate with the database. 

Let's examine the SquareState Entity object to understand them better.

    package com.diycomputerscience.minesweeper.model.jpa;
    
    import javax.persistence.Column;
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;

    @Entity
    public class SquareState {
    
	    private long id;
	    private String state;
	
	    public SquareState() {
		
	    }
	
	    public SquareState(String state) {
		    this.state = state;
	    }
	
	    public SquareState(com.diycomputerscience.minesweeper.SquareDo.SquareState state) {
		    this.state = state.toString();
	    }

	    @Id
	    @GeneratedValue	
	    public long getId() {
		    return id;
	    }
	
	    public void setId(long id) {
		    this.id = id;
	    }
	
	    @Column(nullable=false)
	    public String getState() {
		    return state;
	    }
	
	    public void setState(String state) {
		    this.state = state;
	    }
    }

_**Code Snippet 1**_

The first thing you might have noticed in the code above is the @Entity annotation. All Entity objects are annotated with this. When a class is annotated with @Entity, it signifies, this is an Entity class, whose attributes will be stored in the database. Entities are objects that represent data. We use JPA to persist these objects, which in turn will persist them into database tables. Attributes of an Entity class are usually saved in columsn of the table, though certain attributes might specify relationships, in which case they may be saved in a different way. You will also notice that there are further annotations on the getter methods. These annotations describe whether an attribute is the primary key (id), or whether the attribute is nullable, and so on.

We will create a database structure similar to what we had in the previous exercise. This exercise has two Entity objects: SquareState (which is already shown above) and Square. We have provided the Square object. You will have to annotate it appropriately.

In the previous exercise, we had used the DAO pattern, and had created a ```BoardDao``` interface to define data access methods for the board. Then we created an implementation ```BoardDaoJDBC``` for that interface, to communicate with the database using JDBC. Finally we used ```BoardDaoJDBC``` in ```JDBCPersistenceStrategy```. The larger idea was to be able to create multiple implementations for different scenarios. We can create ```BooardDaoJPA``` and use it from ```JPAPersistenceStrategy``` (we would have to create this class too). However, there is a caveat. All the methods in ```BoardDao``` take a ```Connection``` argument. But in JPA, our code does not deal with ```Connection``` object at all. Oooops ! Bad Design ! We cannot utilize BoardDao for JPA without refactoring it. Unfortunately refactoring ```BoardDao```, means having to refactor it's existing subclasses and the classes that use them. See how a bad design decision can bite us ? We will not focus on refactoring that mistake for now. We just wanted to show you the impact of bad design. You are welcome to do it on your own. Remember you have unit tests to guide you along. 

In this exercise, we will implement the JPA code without even using DAO's. At the end of this exercise, you will have the JPA layer implemented without the DAO, and the JDBC layer with the Dao. We will then be able to compare and contract both approaches.

Since we use different ```PersistenceStrategy``` implementations to persist the board in different ways, the right thing to do is to create a ```JPAPersistenceStrategy```. Since we are not going to create any DAO objects for JPA, the ```JPAPersistenceStrategy``` will communicate with the EntityManager and Entity objects.

As we mentioned before there are two Entity classes: ```SquareState``` and ```Square```. We have annotated ```SquareState```, except for one annotation, which you will have to figure out and add to it. The Entity ```Square``` on the other hand has attributes, but no annotations. You will have to add all annotations to ```Square```. You also have to implement the methods ```save``` and ```load``` in ```JPAPersistenceStrategy```.

As always, start with running unit tests, and ensure that all unit tests pass, as you go along. Once you have all tests passing run ```UI.java``` and make sure you are able to persist the board using ```JPAPersistenceStrategy``` (be sure the software is not using ```JDBCPersistenceStrategy```, or ```FilePersistenceStrategy```)

<h2>Steps For This Project</h2>

 1. Run ```AllTests``` and ensure that 84 tests are run, out of which there are 5 failures.
 2. Add annotations to the Entity classes
 3. Implement empty methods (```save``` and ```load```) in ```JPAPersistenceStrategy```.
 4. Get all the unit tests to pass.
 5. Configure ```config.properties``` to use ```JPAPersistenceStrategy``` and verify that you can indeed save and load a board using the this strategy.


<h1>Section 11 Project 2</h1>

<h2>Overview</h2>

In the previous exercise, we wrote database code to persist the board in a relational database using JDBC. JDBC as you might have already realized makes our code database agnostic. JDBC itself is a bunch of interfaces (with the exception of the ConnectionManager class). Each database vendor provides an implementation in the form of a JDBC driver. This driver contains implementations of the various interfaces provided by the JDBC library. Isn't it awesome that we write our code to the interfaces, and by changing the underlying driver, url, etc, we can start communicating with another relational database. For example switching the database from MySql to PostGres is a simple matter of changing the JDBC driver, and url (along with login credentials). JDBC actually takes the write once run anywhere philosophy of Java to relational databases.

However, at some point of time people started disfavoring the use of SQL in Java code. Let's try to understand their rationale with an example. Imagine you want to make a query in your code. You would typically get a Connection to the database, then perpare a Statement, and then fire the query. The Statement would give you a ResultSet which you would iterate and possibly create simple data objects which would be given to the view to populate the screen with data. this is how most well layered applications were designed with JDBC. Or maybe if you wanted to insert some data that the user filled on a form on the screen, you would typically represent data from the form in some sort of simple data object. Using this data object you would generate a query and execute it on a Statement. If you think about it carefully, there is some redundancy here. We are converting simple data objects which the view can understand to queries and we also convert the data from a ResultSet into simple data objects. Much of this is plumbing code, which can be handled by a library if knew the database schema and how the attributes of a simple data object mapped to the columns of a table (assuming in most cases a simple data object might map to a table). This is what [ORM](http://stackoverflow.com/questions/1152299/what-is-an-object-relational-mapping-framework) (Object Relational Mapping) tools did. Some people feel that ORM tools have several [issues](http://seldo.com/weblog/2011/08/11/orm_is_an_antipattern), but by and large we feel that they are [very useful](http://stackoverflow.com/questions/179311/is-o-r-mapping-worth-it?rq=1), and do increase the productivity of a programmer. As a result many developers started using ORM frameworks like [Hibernate](http://www.codeguru.com/cpp/data/mfc_database/sqlserver/article.php/c10079/Understanding-Hibernate-ORM-for-JavaJ2EE.htm). 

At some point ORM as a concept became powerful, and useful enough to warrant a specification. Also EJB 2.0 was becoming very unpopular and needed a makeover into something more lightweight. Thus the [JPA](http://www.oracle.com/technetwork/java/javaee/tech/persistence-jsp-140049.html) specification was born. Using JPA we can work with objects and call methods on them to either persist them to a database or to get a list of objects as the result of a query. Since JPA is a specification, we can change the underlying implementation. Some popular JPA implementations are [Hibernate, OpenJPA, and TopLink](http://stackoverflow.com/questions/576659/jpa-implementations-which-one-is-the-best-to-use). 

In this exercise, we will write database code to the JPA API, and use [Hibernate](http://www.hibernate.org/) as the underlying JPA implementation. As a side note it is interesting that Hibernate in turn uses JDBC as the underlying technology. Isn't it fascinating that in effect our database code is going across 3 layers ?

Since, we now have model classes which use different technologies (earlier we used JDBC, and in this project we will use JPA), we have further split the ```model``` package into two sub-packages: ```com.diycomputerscience.minesweeper.model.jdbc```, and ```com.diycomputerscience.minesweeper.model.jpa```. Classes that you worked with in the previous exercise, such as ```JDBCPersistenceStrategy```, ```DBInit```, and ```BoardDaoJDBC``` have now been moved to the ```jdbc``` package. Classes for JPA will be in the ```jpa``` package.  

We have created a ```SquareState``` [Entity](http://stackoverflow.com/questions/9778659/what-are-jpa-entities) object to help you better understand JPA.

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

We will create a database structure similar to what we had in the previous exercise. This project needs to have two Entities: ```SquareState``` (which is already shown above) and ```Square```. This project already contains the ```Square``` class, but you will have to annotate it appropriately.

In the previous project, we had used the DAO pattern, and had created a ```BoardDao``` interface to define data access methods for the board. Then we created an implementation ```BoardDaoJDBC``` for that interface, to communicate with the database using JDBC. Finally we used ```BoardDaoJDBC``` in ```JDBCPersistenceStrategy```. The larger idea was to be able to create multiple implementations for different scenarios. The idea was to be able to create ```BooardDaoJPA```, and ```JPAPersistenceStrategy```, where we could use ```BooardDaoJPA``` in the latter. However, there is a caveat. Take a look at ```BoardDao```. All the it's methods take a ```Connection``` argument. But in JPA, our code does not need to deal with a ```Connection``` object at all. Oooops ! Bad Design ! We cannot utilize BoardDao for JPA without refactoring it. Unfortunately refactoring ```BoardDao```, means having to refactor it's existing implementations and the classes that use them. See how a bad design decision can bite us ? We will not focus on refactoring that mistake for now. We just wanted to show you the impact of bad design. You are welcome to do it on your own. Remember you have unit tests to guide you along. 

In this project, we will implement the JPA code without using DAO's. At the end of this exercise, you will have the JPA layer implemented without DAO's, and the JDBC layer with DAO's. We will then be able to compare and contract both approaches.

Since we use different ```PersistenceStrategy``` implementations to persist the board in different ways, the right thing to do is to create a ```JPAPersistenceStrategy```. Since we are not going to create any DAO objects for JPA, the ```JPAPersistenceStrategy``` will directly communicate with the ```EntityManager``` and ```Entity``` objects.

As we mentioned earlier, there are two Entity classes: ```SquareState``` and ```Square```. We have annotated ```SquareState```, except for one annotation, which you will have to figure out, and add to it. The Entity ```Square``` on the other hand has attributes, but no annotations. You will have to add all annotations to ```Square```. You also have to implement the methods ```save``` and ```load``` in ```JPAPersistenceStrategy```.

As always, start with running unit tests, and ensure that all unit tests pass, as you go along. Once you have all tests passing run ```UI.java``` and make sure you are able to persist the board using ```JPAPersistenceStrategy``` (be sure the software is not using ```JDBCPersistenceStrategy```, or ```FilePersistenceStrategy```)

<h2>Steps For This Project</h2>

 1. Run ```AllTests``` and ensure that 84 tests are run, out of which there are 5 failures.
 2. Add annotations to the Entity classes
 3. Implement empty methods (```save``` and ```load```) in ```JPAPersistenceStrategy```.
 4. Get all the unit tests to pass.
 5. Configure ```config.properties``` to use ```JPAPersistenceStrategy``` and verify that you can indeed save and load a board using the this strategy.


package com.devcrumb;

import com.devcrumb.dao.EventDao;
import com.devcrumb.dao.PersonDao;
import com.devcrumb.model.Event;
import com.devcrumb.model.Person;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Standalone application with Spring Data JPA, Hibernate and Maven
 * 
 * @author DevCrumb.com
 */
public class App {

	/*
	1. zainstalowac postgresa i postgisa: sudo apt-get install -y postgresql postgis
	2. zalogowac sie na usera postgres: sudo -u postgres psql postgres
	3. zmienic haslo usera postgres na pasujace do pliku persistence.xml: postgres=# \password postgres
	4. nadal bedac zalogowanym do bazy, zainstalowac dodatek: CREATE EXTENSION postgis;
	5. odpalic aplikacje
	 */

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		
		EventDao eventDao = context.getBean(EventDao.class);
		PersonDao personDao = context.getBean(PersonDao.class);
        EntityManager entityManager = context.getBean(EntityManager.class);

        jpaExample(eventDao, personDao);

        for(int i = 0; i < 5; i++)
        {
            System.out.println();
        }

        hqlExample(entityManager);

		context.close();
	}

    private static void jpaExample(EventDao eventDao, PersonDao personDao)
    {
        Event event1 = createEvent("Event 1", new Date(), "POINT(10 5)");
        Person person1 = new Person("John", "Doe");
        person1.setEvent(event1);
        event1.getPersons().add(person1);
        Long storedEventId = eventDao.save(event1).getId();

        Event event2 = createEvent("Event 1", new Date(), "POINT(15 10)");
        Person person2 = new Person("Marian", "Kowalski");
        person2.setEvent(event2);
        event2.getPersons().add(person2);
        Long storedEventId2 = eventDao.save(event2).getId();

        System.out.println("Count Event records: " + eventDao.count());

        List<Event> events = (List<Event>) eventDao.findAll();
        for (Event event : events) {
            System.out.println(event);
        }

        Event storedEvent = eventDao.getById(storedEventId);
        System.out.println(storedEvent.getPersons().get(0));

        Person peter = new Person("Peter", "Sagan");
        Person nasta = new Person("Nasta", "Kuzminova");

        // Add new Person records
        personDao.save(peter);
        personDao.save(nasta);

        // Count Person records
        System.out.println("Count Person records: " + personDao.count());

        // Print all records
        List<Person> persons = (List<Person>) personDao.findAll();
        for (Person person : persons) {
            System.out.println(person);
        }

        // Find Person by surname
        System.out.println("Find by surname 'Sagan': "	+ personDao.findBySurname("Sagan"));

        // Update Person
        nasta.setName("Barbora");
        nasta.setSurname("Spotakova");
        personDao.save(nasta);

        System.out.println("Find by id 2: " + personDao.findOne(2L));

        // Remove record from Person
        personDao.delete(2L);

        // And finally count records
        System.out.println("Count Person records: " + personDao.count());
    }

    private static void hqlExample(EntityManager entityManager)
    {
        Geometry filter = wktToGeometry("POINT(5 5)");
        Query query = entityManager.createQuery("select e from Event e where ST_Distance(e.location, :filter) > 10", Event.class);
        query.setParameter("filter", filter);
        System.out.println(query.getResultList());
    }

    private static Event createEvent(String title, Date theDate, String wktPoint) {
        Geometry geom = wktToGeometry(wktPoint);

        if (!geom.getGeometryType().equals("Point")) {
            throw new RuntimeException("Geometry must be a point. Got a " + geom.getGeometryType());
        }


        Event theEvent = new Event();
        theEvent.setTitle(title);
        theEvent.setDate(theDate);
        theEvent.setLocation((Point) geom);
        return theEvent;
    }
	
	private static Geometry wktToGeometry(String wktPoint) {
        WKTReader fromText = new WKTReader();
        Geometry geom = null;
        try {
            geom = fromText.read(wktPoint);
        } catch (ParseException e) {
            throw new RuntimeException("Not a WKT string:" + wktPoint);
        }
        return geom;
    }
}

package no.wis.myplace.server.datacollection.jobs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import no.wis.myplace.server.entity.Forecast;
import no.wis.myplace.server.entity.TextForecast;
import no.wis.myplace.server.entity.WeatherPlace;
import no.wis.myplace.server.entity.dao.JPAForecastDAOImpl;
import no.wis.myplace.server.entity.dao.JPATextForecastDAOImpl;
import no.wis.myplace.server.entity.dao.JPAWeatherPlaceDAOImpl;
import no.wis.myplace.server.util.JobUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Name: YrParser
 * @Description: FYLL INN
 * @Date: Feb 20, 2014
 */
public class YrParser implements Job {

    // Strings
    private String sunrise = "";
    private String sunset = "";
    private String lastUpdate = "";
    private String nextUpdate = "";
    private String dateTo = "";
    private String dateFrom = "";
    private String forecastBody = "";
    private String forecastTitle = "";
    private int period_nr;
    private int symbol;
    private double precipitation;
    private double wind_dir;
    private double wind_speed;
    private double temperature;

    // Boolean values
    private boolean isLUpd = false;
    private boolean isNUpd = false;
    private boolean isText = false;
    private boolean isTabular = false;
    private boolean isTitle = false;
    private boolean isBody = false;

    private final SimpleDateFormat dateTimeFromat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private WeatherPlace currentWeatherPlace; // NOT NEEDED

    private JPAForecastDAOImpl forecastDAOImpl; // NOT NEEDED
    private JPATextForecastDAOImpl textForecastDAOImpl; // NOT NEEDED

	// Parsing weather data from web and storing to database
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
	// Start logger
        long start = System.nanoTime();
        Logger logger = LoggerFactory.getLogger(YrParser.class);
        logger.info("started at " + new Date());

	// Create implementation classes for place, forecast and text forecast
        JPAWeatherPlaceDAOImpl weatherPlaceDAOImpl = new JPAWeatherPlaceDAOImpl(); // NOT NEEDED
        forecastDAOImpl = new JPAForecastDAOImpl();// NOT NEEDED
        textForecastDAOImpl = new JPATextForecastDAOImpl();// NOT NEEDED

	// Get all weather places
	 List<WeatherPlace> weatherPlaces = weatherPlaceDAOImpl.getAll(); // NOT NEEDED

	// Truncate forecast and text forecast tables in database
        forecastDAOImpl.truncate(); // NOT NEEDED
        textForecastDAOImpl.truncate(); // NOT NEEDED

	// Loop through all weather places
        for (WeatherPlace weatherPlace : weatherPlaces) { // NOT NEEDED
            currentWeatherPlace = weatherPlace; // NOT NEEDED

		// Establish connection
            logger.info("parsing " + weatherPlace.getUrl()); // NOT NEEDED
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("http://www.yr.no/place/Denmark/Capital/Copenhagen/varsel.xml");
            HttpResponse resp;

		// Get response from web source
            try {
                resp = client.execute(request);
            } catch (IOException ex) {
                logger.warn("could not execute request because " + ex.getMessage());
                continue;
            }

            if (resp == null || resp.getStatusLine().getStatusCode() != 200) {
                if (resp == null) {
                    logger.warn("cannot continue because HttpResponse is null");
                } else {
                    int statusCode = resp.getStatusLine().getStatusCode();
                    if (statusCode == 400) {
                        logger.error("skips parsing because server responds with BAD REQUEST");
                    } else if (statusCode == 404) {
                        logger.error("skips parsing because server responds with NOT FOUND");
                    } else {
                        logger.warn("skips parsing because server responds with statuscode " + resp.getStatusLine().getStatusCode());
                    }
                }
                continue;
            }

            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);

		// Read weather data
            try {
                XMLEventReader reader = factory.createXMLEventReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
                while (reader.hasNext()) {
                    XMLEvent e = reader.nextEvent();
                    processEvent(e);
                }
            } catch (IOException | IllegalStateException | XMLStreamException ex) {
                logger.warn("could not parse XML because " + ex.getMessage());
            }
        }
        forecastDAOImpl.end(); // NOT NEEDED
        textForecastDAOImpl.end(); // NOT NEEDED

        //Reschedule job for next update as given in XML-feed
        try {
            logger.info("done, found new updatetime (" + nextUpdate + "). "
                    + "Setting new trigger 15 minutes after this time.");

            DateTime newTriggerTime;
            try {
                newTriggerTime = new DateTime(dateTimeFromat.parse(nextUpdate).getTime()).plusMinutes(15);
            } catch (ParseException ex) {
                logger.warn("could not parse next update, setting new trigger in one hour.");
                newTriggerTime = new DateTime().plusHours(1);
            }

            DateTime newDate;

            if (newTriggerTime.isAfterNow()) {
                newDate = new DateTime(reSchedule(jec, newTriggerTime).getTime());
            } else {
                logger.info("Next update set in past, rescheduling 15m from now for retry.");
                newTriggerTime = new DateTime().plusMinutes(15);
                newDate = new DateTime(reSchedule(jec, newTriggerTime).getTime());
            }

            logger.info("rescheduled! Next fire time is " + newDate.toString());
        } catch (SchedulerException ex) {
            logger.error("Could not be rescheduled", ex);
            throw new JobExecutionException("Rescheduling error, refireing immediately", true);
        }
        logger.info("ended, took " + ((System.nanoTime() - start) / 1000000000) + "s.");
    }

    private Date reSchedule(JobExecutionContext jec, DateTime newTriggerTime) throws SchedulerException {

        int seconds = newTriggerTime.getSecondOfMinute();
        int minutes = newTriggerTime.getMinuteOfHour();
        int hours = newTriggerTime.getHourOfDay();

        Trigger oldTrigger = jec.getTrigger();
        char whitespace = ' ';
        StringBuilder cronExpression = new StringBuilder().append(seconds)
                .append(whitespace).append(minutes).append(whitespace)
                .append(hours).append(" ? * * *");

        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withSchedule(cronSchedule(cronExpression.toString())).build();
        return jec.getScheduler().rescheduleJob(oldTrigger.getKey(), newTrigger);
    }

    private void processEvent(XMLEvent xmle) {
        if (xmle.isStartElement()) {
            processStartElement(xmle.asStartElement());
        } else if (xmle.isCharacters()) {
            processCharacters(xmle.asCharacters());
        } else if (xmle.isEndElement()) {
            processEndElement(xmle.asEndElement());
        }
    }

	// Find name of element, set corresponding boolean variable to true, and find attribute value (if any)
    private void processStartElement(StartElement se) {
        QName qname = se.getName();
        String currentElement = qname.getLocalPart();
        switch (currentElement) {
            case "lastupdate":
                isLUpd = true;
                break;
            case "nextupdate":
                isNUpd = true;
                break;
            case "sun":
                Attribute rise = se.getAttributeByName(new QName("rise"));
                Attribute set = se.getAttributeByName(new QName("set"));
                if (rise != null) {
                    sunrise = rise.getValue();
                }
                if (set != null) {
                    sunset = set.getValue();
                }
                break;
            case "text":
                isText = true;
                break;
            case "tabular":
                isTabular = true;
                break;
            case "time":
                Attribute from = se.getAttributeByName(new QName("from"));
                Attribute to = se.getAttributeByName(new QName("to"));
                if (from != null) {
                    dateFrom = from.getValue();
                }
                if (to != null) {
                    dateTo = to.getValue();
                }
                Attribute perAtt = se.getAttributeByName(new QName("period"));
                if (perAtt != null) {
                    period_nr = Integer.parseInt(perAtt.getValue());
                }
                break;
            case "title":
                isTitle = true;
                break;
            case "body":
                isBody = true;
                break;
            case "symbol":
                Attribute symAtt = se.getAttributeByName(new QName("number"));
                if (symAtt != null) {
                    symbol = Integer.parseInt(symAtt.getValue());
                }
                break;
            case "precipitation":
                Attribute precAtt = se.getAttributeByName(new QName("value"));
                if (precAtt != null) {
                    precipitation = Double.parseDouble(precAtt.getValue());
                }
                break;
            case "windDirection":
                Attribute dirAtt = se.getAttributeByName(new QName("deg"));
                if (dirAtt != null) {
                    wind_dir = Double.parseDouble(dirAtt.getValue());
                }
                break;
            case "windSpeed":
                Attribute speedAtt = se.getAttributeByName(new QName("mps"));
                if (speedAtt != null) {
                    wind_speed = Double.parseDouble(speedAtt.getValue());
                }
                break;
            case "temperature":
                Attribute tempAtt = se.getAttributeByName(new QName("value"));
                if (tempAtt != null) {
                    temperature = Double.parseDouble(tempAtt.getValue());
                }
                break;
        }
    }

	// Find element text content and set to active element variable indicated by boolean value
    private void processCharacters(Characters ch) {
        if (ch.isWhiteSpace()) {
            return;
        }
        String str = ch.getData();

        if (isLUpd) {
            lastUpdate = str;
        } else if (isNUpd) {
            nextUpdate = str;
        } else if (isTitle) {
            forecastTitle = str;
        } else if (isBody) {
            forecastBody = str;
        }
    }

    private void processEndElement(EndElement ee) {
        QName name = ee.getName();

        String currentElement = name.getLocalPart();

	// Find element name, set corresponding boolean value to false and, if end of file, store weather data to database
        if (currentElement != null) {
            switch (currentElement) {
                case "lastupdate":
                    isLUpd = false;
                    break;
                case "nextupdate":
                    isNUpd = false;
                    break;
                case "text":
                    isText = false;
                    break;
                case "tabular":
                    isTabular = false;
                    break;
                case "time":
                    if (isText) {
			// Store text forecast to database
                        textForecastDAOImpl.merge(new TextForecast(new DateTime(dateFrom),
                                new DateTime(dateTo),
                                JobUtil.checkStringLength(forecastTitle, TextForecast.MAX_LENGTH_TITLE),
                                JobUtil.checkStringLength(forecastBody, TextForecast.MAX_LENGTH_TITLE),
                                currentWeatherPlace));
                    } else if (isTabular) {
			// Store forecast to database
                        forecastDAOImpl.merge(new Forecast(new DateTime(dateFrom),
                                new DateTime(dateTo), period_nr, symbol,
                                precipitation, wind_dir, wind_speed, temperature,
                                currentWeatherPlace));
                    }
                    break;
                case "title":
                    isTitle = false;
                    break;
                case "body":
                    isBody = false;
                    break;
            }
        }
    }
}

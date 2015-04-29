import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
 
public class YrParser{
 
	
	ArrayList<TabularForecast> forecasts = new ArrayList<TabularForecast>();
    private String dateTo = "";
    private String dateFrom = "";
    private int period_nr;
    private String symbol;
    private double precipitation;
    private double wind_dir;
    private double wind_speed;
    private double temperature;
    private double pressure;
 
    // Boolean values
    private boolean isTabular = false;
 
 
    public ArrayList<TabularForecast> execute(String link){
 

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(link);//http://www.yr.no/sted/Danmark/Hovedstaden/Moskva/varsel_time_for_time.xml");
            HttpResponse resp = null;
 
        // Get response from web source
            try {
                resp = client.execute(request);
            } catch (IOException ex) {
               
            }
 
            if (resp == null || resp.getStatusLine().getStatusCode() != 200) {
                if (resp == null) {
//                    logger.warn("cannot continue because HttpResponse is null");
                } else {
                    int statusCode = resp.getStatusLine().getStatusCode();
                    if (statusCode == 400) {
//                        logger.error("skips parsing because server responds with BAD REQUEST");
                    } else if (statusCode == 404) {
//                        logger.error("skips parsing because server responds with NOT FOUND");
                    } else {
//                        logger.warn("skips parsing because server responds with statuscode " + resp.getStatusLine().getStatusCode());
                    }
                }
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
//                logger.warn("could not parse XML because " + ex.getMessage());
            }
        
        System.out.println("\nSize of forecasts: "+forecasts.size());
        return forecasts;
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
            case "symbol":
                Attribute symAtt = se.getAttributeByName(new QName("name"));
                if (symAtt != null) {
                    symbol = symAtt.getValue();
                }
                switch(symbol){
                case "Delvis skyet":
                	symbol = "Partially Cloudy";
                	break;
                case "Lettskyet":
                	symbol = "Light Clouds";
                	break;
                case "Klarvær":
                	symbol = "Clear Skies";
                	break;
                case "Skyet":
                	symbol = "Cloudy";
                	break;
                case "Regnbyger":
                	symbol = "Showers";
                	break;
                case "Regn":
                	symbol = "Rainy";
                	break;
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
            case "pressure":
                Attribute pressAtt = se.getAttributeByName(new QName("value"));
                if (pressAtt != null) {
                    pressure = Double.parseDouble(pressAtt.getValue());
                }
                break;
        }
    }
 
    // Find element text content and set to active element variable indicated by boolean value
    private void processCharacters(Characters ch) {
        if (ch.isWhiteSpace()) {
            return;
        }
    }
 
    private void processEndElement(EndElement ee) {
        QName name = ee.getName();
 
        String currentElement = name.getLocalPart();
 
    // Find element name, set corresponding boolean value to false and, if end of file, store weather data to database
        if (currentElement != null) {
            switch (currentElement) {
                case "tabular":
                    isTabular = false;
                    break;
                case "time":
                    if (isTabular) {
                    	TabularForecast tbf = new TabularForecast
                    			(dateFrom, dateTo, period_nr, symbol, precipitation, wind_dir, wind_speed, (int)temperature, pressure);
                    	forecasts.add(tbf);
                    	//System.out.println(tbf.toString());
                    }
                    break;
            }
        }
    }
}
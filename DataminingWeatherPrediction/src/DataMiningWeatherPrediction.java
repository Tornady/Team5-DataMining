import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.james.mime4j.field.datetime.DateTime;


public class DataMiningWeatherPrediction {

	public static void main(String[] args) {
		YrParser yr = new YrParser();
		String cph = "http://www.yr.no/place/Denmark/Capital/Copenhagen/varsel.xml";
		String cphhbh = "http://www.yr.no/place/Denmark/Capital/Copenhagen/varsel_time_for_time.xml";
		String sof = "http://www.yr.no/place/Bulgaria/Sofia/Sofia/varsel.xml";
		String sofhbh = "http://www.yr.no/place/Bulgaria/Sofia/Sofia/varsel_time_for_time.xml";
		String stk = "http://www.yr.no/place/Sweden/Stockholm/Stockholm/varsel.xml";
		String stkhbh = "http://www.yr.no/place/Sweden/Stockholm/Stockholm/varsel_time_for_time.xml";
		String osl = "http://www.yr.no/place/Norway/Oslo/Oslo/Oslo/varsel.xml";
		String oslhbh = "http://www.yr.no/place/Norway/Oslo/Oslo/Oslo/varsel_time_for_time.xml";
		String ber = "http://www.yr.no/place/Germany/Berlin/Berlin/varsel.xml";
		String berhbh = "http://www.yr.no/place/Germany/Berlin/Berlin/varsel_time_for_time.xml";
		
		ArrayList<TabularForecast> stockholm = yr.execute(stk);
		ArrayList<TabularForecast> stockholmhbh = yr.execute(stkhbh);
		ArrayList<TabularForecast> oslo = yr.execute(osl);
		ArrayList<TabularForecast> oslohbh = yr.execute(oslhbh);
		ArrayList<TabularForecast> copenhagen = yr.execute(cph);
		ArrayList<TabularForecast> copenhagenhbh = yr.execute(cphhbh);
		ArrayList<TabularForecast> berlin = yr.execute(ber);
		ArrayList<TabularForecast> berlinhbh = yr.execute(berhbh);
		ArrayList<TabularForecast> sofia = yr.execute(sof);
		ArrayList<TabularForecast> sofiahbh = yr.execute(sofhbh);
		
		excelWriter exw = new excelWriter();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		String datestr = dateFormat.format(cal.getTime());
		
		exw.write(datestr+"cph", copenhagen);
		exw.write(datestr+"cphHbH", copenhagenhbh);
		exw.write(datestr+"stk", stockholm);
		exw.write(datestr+"stkHbH", stockholmhbh);
		exw.write(datestr+"osl", oslo);
		exw.write(datestr+"oslHbH", oslohbh);
		exw.write(datestr+"ber", berlin);
		exw.write(datestr+"berHbH", berlinhbh);
		exw.write(datestr+"sof", sofia);
		exw.write(datestr+"sofHbH", sofiahbh);
		
		//exw.write("test", forecasts);
		// #AwesomeCodes
	}
}
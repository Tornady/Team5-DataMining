import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class excelWriter {
	  //European countries use ";" as 
    //CSV separator because "," is their digit separator
	ArrayList<TabularForecast> forecasts;
	
	public excelWriter(){
		
	}
	
    private static final String CSV_SEPARATOR = ",";
    private static void writeToCSV(String name, ArrayList<TabularForecast> productList)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name+".csv"), "UTF-8"));
            StringBuffer firstline = new StringBuffer();
            firstline.append("Temperature");
            firstline.append(CSV_SEPARATOR);
            firstline.append("Forecast");
            firstline.append(CSV_SEPARATOR);
            firstline.append("WindSpeed");
            firstline.append(CSV_SEPARATOR);
            firstline.append("WindDirection");
            firstline.append(CSV_SEPARATOR);
            firstline.append("Precipitation");
            firstline.append(CSV_SEPARATOR);
            firstline.append("Pressure");
            firstline.append(CSV_SEPARATOR);
            firstline.append("From");
            firstline.append(CSV_SEPARATOR);
            firstline.append("To");
            bw.write(firstline.toString());
            bw.newLine();
            
            for (TabularForecast product : productList)
            {
            	
            	/*
            	  	this.from = from;
		        	this.to = to;
		        	this.period = period;
		        	this.symbol = symbol;
			        this.precipitation = precipitation;
			        this.direction = direction;
			        this.speed = speed;
			        this.temp = temp;
			        this.pressure = pressure;
            	 */
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(product.getTemp());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getSymbol());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getSpeed());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getDirection());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getPrecipitation());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getPressure());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getFrom());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(product.getTo());
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {System.out.println(e.getMessage());}
        catch (FileNotFoundException e){System.out.println(e.getMessage());}
        catch (IOException e){System.out.println(e.getMessage());}
    }
    
    public void write(String filename, ArrayList<TabularForecast> forecasts){
    	writeToCSV(filename, forecasts);
    }
    
}

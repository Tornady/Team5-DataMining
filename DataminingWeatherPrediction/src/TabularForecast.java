
public class TabularForecast {
 
    String from, to, symbol;
    int temp, period;
    double direction, speed, precipitation, pressure;
 
    public TabularForecast(String from, String to, int period, String symbol, double precipitation,
            double direction, double speed, int temp, double pressure) {
        this.from = from;
        this.to = to;
        this.period = period;
        this.symbol = symbol;
        this.precipitation = precipitation;
        this.direction = direction;
        this.speed = speed;
        this.temp = temp;
        this.pressure = pressure;
    }
 
    public void setPressure(double pressure){
    	this.pressure = pressure;
    }
    
    public double getPressure(){
    	return pressure;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
 
    public void setTo(String to) {
        this.to = to;
    }
 
    public void setTemp(int temp) {
        this.temp = temp;
    }
 
    public void setPeriod(int period) {
        this.period = period;
    }
 
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
 
    public void setDirection(double direction) {
        this.direction = direction;
    }
 
    public void setSpeed(double speed) {
        this.speed = speed;
    }
 
    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
 
    public String getFrom() {
        return from;
    }
 
    public String getTo() {
        return to;
    }
 
    public String getSymbol() {
        return symbol;
    }
 
    public int getTemp() {
        return temp;
    }
 
    public int getPeriod() {
        return period;
    }
 
    public double getPrecipitation() {
        return precipitation;
    }
 
    public double getDirection() {
        return direction;
    }
 
    public double getSpeed() {
        return speed;
    }
    
    public String toString(){
    	StringBuilder sb = new StringBuilder();
    	sb.append("Weather Forecast: "+ getFrom() + " - "+ getTo());
    	sb.append("\nWeather: "+ getSymbol()+".");
    	sb.append("\nTemperature: "+getTemp() + " deg C.");
    	sb.append("\nWindSpeed: "+getSpeed() + " m/s.");
    	sb.append("\nWind Direction: "+getDirection() + " degrees.");
    	sb.append("\nRain: "+getPrecipitation() + " mm.");
    	sb.append("\nPressure: "+getPressure() + " hPa.\n");
    	return sb.toString();
    }
 
}
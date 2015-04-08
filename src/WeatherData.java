
public class WeatherData {
	float Precipitation;
	float WindDirection;
	float WindSpeed;
	float Temperature;
	float Pressure;
	
	public float[] getAttributes() {
		return new float[] { Precipitation, WindDirection, WindSpeed, Temperature, Pressure };
	}
}

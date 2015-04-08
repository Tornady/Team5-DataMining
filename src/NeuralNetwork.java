import java.util.ArrayList;

public class NeuralNetwork {
	ArrayList<WeatherData> trainingList = new ArrayList<>();
	float[] inputLayer = new float[5];
	float[] hiddenLayer = new float[5];
	float[] outputLayer = new float[5];
	public void backpropagation() {
		Boolean running = true;
		Initialize(); 
		float[] errors = new float[5];
		while(running) { //terminating condition is not satisﬁed
			for(WeatherData d : trainingList) {
				float[] data = d.getAttributes();
				for(int i = 0; i < data.length; i++) //float j : d.getAttributes()) 
					inputLayer[i] = data[i]; // output of an input unit is its actual input value 
				/*for each hidden or output layer unit j { 
				Ij =Pi wijOi+bj; //compute the net input of unit j with respect to the previous layer, i 
				Oj = 1 1+e−Ij; //compute the output of each unit j 
				}*/
				//Backpropagate the errors: 
				for(int i = 0; i < outputLayer.length; i++) // compute the error 
					errors[i] = outputLayer[i] * (1 - outputLayer[i]) * 
					(data[i] - outputLayer[i]);
				for (int i = hiddenLayer.length - 1; i >= 0; i--)
					errors[i] = outputLayer[i] * (1 - outputLayer[i]);/* *
					sumk Errk wjk; // compute the error with respect to the next higher layer, k 
				for each weight wij in network { 
					1wij =(l)ErrjOi; // weight increment 
					wij =wij+1wij;// weight update 
				}
				for each bias bj in network { 
					1bj =(l)Errj; // bias increment 
					bj =bj+1bj;// bias update 
				}*/
			}
		}
	}
	private void Initialize() {
		 //Initialize all weights and biases in network
	}
}

package mapDiscovery;

import lejos.nxt.*;

public class Discovery {
	static int BLUE = 360;
	static int BLACK = 350;
	static int RED_L = 370;

	private static String color(int value) {
		if (value < BLUE)
			return "blue";
		if (value < BLACK)
			return "black";
		if (value < RED_L)
			return "red";
		return "white";
	}
	
	public static void main(String[] args) {
		DMap mapa = new DMap();

		// TODO TUTAJ BADAMY MAPE!LightSensor light;
		LightSensor light = new LightSensor(SensorPort.S4);
	    while(!Button.ESCAPE.isDown()) {
			int value = light.getNormalizedLightValue();
	        /*if (Button.RIGHT.isDown()) {*/
	        	String color = color(value);
	        	LCD.drawString(color, 0, 1);
	        /*} else {*/
		        LCD.drawInt(value, 0,0);
	        /*}*/
	        if (Button.LEFT.isDown()) {
	        }
		}

		mapa.printOnDisplay();
        
		while(!Button.RIGHT.isDown()) {
		}

	}

}

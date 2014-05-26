package mapDiscovery;

import lejos.nxt.*;
import lejos.robotics.navigation.DifferentialPilot;

public class Discovery {
	static int BLUE_L;
	static int BLUE_H;
	// BLACK is RED for MiniMapCre
	static int RED_L;
	static int RED_H;
	static int WHITE_L;
	static int WHITE_H;
	
	static int pos_x = -1;
	static int pos_y = 3;

	private static void setColorValues(LightSensor light) {
		String kolory[] = {"niebieski", "czerwony", "bialy"}; 
		int wartosci[] = new int[3];
		for (int i=0; i<3; i++) {
			LCD.clear();
        	LCD.drawString("Pokaz " + kolory[i], 0, 0);
        	LCD.drawString("I nacisnij prawy", 0, 1);
		    while(!Button.RIGHT.isDown()) {
				int value = light.getNormalizedLightValue();
		        LCD.drawInt(value, 0,2);
			}
		    while(!Button.RIGHT.isUp()) {}
			int value = light.getNormalizedLightValue();
			wartosci[i] = value;
		}
		BLUE_L = wartosci[0] - 20;
		int temp = wartosci[1] - wartosci[0];
		if (temp < 30){
			BLUE_H = wartosci[0] + (int)Math.floor(temp/2);
			RED_L = BLUE_H;
		} else {
		    BLUE_H = wartosci[0] + 15;
		    RED_L = wartosci[1] - 15;
		}
		temp = wartosci[2] - wartosci[1];
		if (temp < 30){
			RED_H = wartosci[1] + (int)Math.floor(temp/3);
			WHITE_L = RED_H;
		} else {
		    RED_H = wartosci[1] + 15;
		    WHITE_L = wartosci[2] - 15;
		}
		WHITE_H = wartosci[2] + 20;
	}
	
	private static String color(int value) {
		if ((value > BLUE_L)&&(value <= BLUE_H))
			return "blue";
		if ((value > RED_L)&&(value <= RED_H))
			return "red";
		if ((value > WHITE_L)&&(value <= WHITE_H))
			return "white";
		return "unknown";
	}
	
	private static DMap moveOneFront(DifferentialPilot pilot, DMap mapa) {
        pilot.travel(200, true);
        // Rozbić na dwa ruszania się + korygacja + sprawdzenie czy nie ma przeszkody
        return mapa;
	}
	
	public static void main(String[] args) {
		// TODO TUTAJ BADAMY MAPE!LightSensor light;
		LightSensor light = new LightSensor(SensorPort.S4);
	    DifferentialPilot pilot = new DifferentialPilot(56, 128, Motor.C, Motor.B);
	    pilot.setTravelSpeed(75.0f);
		
		/*setColorValues(light);
		
		LCD.clear();*/

		/*int iterator = 0;
	    LCD.clear();
		LCD.drawString("w " + WHITE_L + " " + WHITE_H, 0, 0);
		LCD.drawString("r " + RED_L + " " + RED_H, 0, 1);
		LCD.drawString("b " + BLUE_L + " " + BLUE_H, 0, 2);*/
	    while(!Button.ENTER.isDown()) {
		}
	    
		DMap mapa = new DMap();

		/*iterator = 0;
	    while(!Button.ESCAPE.isDown()) {
			int value = light.getNormalizedLightValue();

			if (iterator % 10 == 0)
			    LCD.clear();
        	String color = color(value);
        	LCD.drawString(color, 0, 1);
	        LCD.drawInt(value, 0,0);
	        iterator++;
		}*/
		
		/*1. Jeœli w (3,-1) - jedŸ do przodu o jeden.
		2. Wybierz losowy nieznany punkt (ale 1-najbli¿szy w manhatañskiej, 2-przód ma priorytet)
		3. Okreœl trasê do tego punktu.
		4. Wykonuj “kroki” do niego i koryguj swoje po³o¿enie przy granicach pól.
		5. Po wjechaniu na oczekiwane pole sprawdŸ kolor/przeszkodê, etc.
		6. Jeœli przeszkoda
		    a) sprawdŸ która i zapisz
		b) wycofaj na poprzednie pole
		7. Jeœli istniej¹ nieodkryte pola - przejdŸ do 2.
		8. W przeciwnym przypadku zakoñcz dzia³anie*/
		
		mapa = moveOneFront(pilot, mapa);

		while (mapa.haveUnknown()){
            way = mapa.findAWay(pos_x, pos_y); // W odległości manhatańskiej/taksówkowej
            mapa = goAWay(way, mapa);
		}

		mapa.printOnDisplay();
        
		while(!Button.RIGHT.isDown()) {
		}

	}

}

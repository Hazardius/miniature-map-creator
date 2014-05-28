package mapDiscovery;

import lejos.nxt.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.RangeFeatureDetector;

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
	static int orient = 0; // 0-N, 1-E, 2-S, 3-W

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
	
	private static DMap moveOneFront(DifferentialPilot pilot, LightSensor light, DMap mapa) {
		int new_val = light.getNormalizedLightValue();
		String start_color = color(new_val);
		int old_val = new_val;
		String new_color = start_color;
		pilot.forward();
		do {
			new_val = light.getNormalizedLightValue();
			new_color = color(new_val);
		} while (Math.abs(old_val - new_val) < 30);
		pilot.stop();
		correctPosition(pilot, light);
		pilot.travel(150); // skorygowac aby nie wjezdzac na obiekty!!
		int oldx = pos_x;
		int oldy = pos_y;
		switch (orient) {
		    case 0: pos_x++;
		    	    break;
		    case 1: pos_y++;
	    	        break;
		    case 2: pos_x--;
	    	        break;
		    case 3: pos_y--;
	    	        break;
		};
		if (mapa.isUnknown(pos_x, pos_y)) {
	        UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
	        float MAX_DISTANCE = 5.0f; // In centimeters
	        FeatureDetector fd = new RangeFeatureDetector(us, MAX_DISTANCE, 500);
	    	Feature result = fd.scan();
	    	if(result != null) {
	    		new_val = light.getNormalizedLightValue();
	    		if (new_val >= RED_L) {
	    			mapa.setField(pos_x, pos_y, 'H');
	    		} else {
	    			mapa.setField(pos_x, pos_y, 'P');
	    		}
	    		pilot.travel(-100.0f);
	    		pos_x = oldx;
	    		pos_y = oldy;
	        } else {
	    		new_color = color(light.getNormalizedLightValue());
	    		if (new_color == "blue") {
		        	mapa.setField(pos_x, pos_y, 'N');
	    		} else if (new_color == "red") {
		        	mapa.setField(pos_x, pos_y, 'C');
	    		} else {
	            	mapa.setField(pos_x, pos_y, 'B');
	    		}
	    		pilot.travel(50);
	        }
		} else {
			pilot.travel(50);
		}
        // Rozbić na dwa ruszania się + korygacja + sprawdzenie czy nie ma przeszkody
        return mapa;
	}

	private static void correctPosition(DifferentialPilot pilot,
			LightSensor light) {
		int start_val = light.getNormalizedLightValue();
		int now_val = start_val;
		pilot.rotateLeft();
		while (Math.abs(start_val - now_val) < 10) {
			now_val = light.getNormalizedLightValue();
		}
		pilot.stop();
		OdometryPoseProvider pp = new OdometryPoseProvider(pilot);
        Pose pose;
        pilot.rotateRight();
        do {
        	start_val = light.getNormalizedLightValue();
        } while (start_val == now_val);
        pose  = pp.getPose();
        pilot.rotate(pose.getHeading() / 2.0f);
	}

	private static DMap goAWay(String way, DifferentialPilot pilot, LightSensor light, DMap mapa) {
		do {
			char next_l = way.charAt(0);
			way = way.substring(1);
			if (next_l == 'r') {
				pilot.rotate(90.0f);
				orient = (orient + 1) % 4;
			} else if (next_l == 'l') {
				pilot.rotate(-90.0f);
				orient = (orient - 1);
				if (orient == -1) {
					orient = 3;
				}
			} else if (next_l == 'f') {
				mapa = moveOneFront(pilot, light, mapa);
			}
		} while (way.length() != 0);
		return mapa;
	}
	
	public static void main(String[] args) {
		LightSensor light = new LightSensor(SensorPort.S4);
	    DifferentialPilot pilot = new DifferentialPilot(56, 128, Motor.B, Motor.C);
	    pilot.setTravelSpeed(75.0f);
	    pilot.setRotateSpeed(10.0f);
		
		setColorValues(light);
		
		LCD.clear();

		int iterator = 0;
	    /*LCD.clear();
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
		
		mapa = moveOneFront(pilot, light, mapa);
		mapa.setField(pos_x, pos_y, 'S');

		while (mapa.haveUnknown()){
            String way = mapa.findAWay(pos_x, pos_y); // W odległości manhatańskiej/taksówkowej
        	LCD.drawString(way, 0, 4);
            mapa = goAWay(way, pilot, light, mapa);
		}

		mapa.printOnDisplay();
        
		while(!Button.RIGHT.isDown()) {
		}

	}

}

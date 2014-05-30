package mapDiscovery;

import lejos.nxt.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.*;
import lejos.robotics.objectdetection.*;

public class Discovery {
    static int BLUE_L;
    static int BLUE_H;
    static int RED_L;
    static int RED_H;
    static int BLACK_L;
    static int BLACK_H;
    static int WHITE_L;
    static int WHITE_H;
    
    static int pos_x = 3;
    static int pos_y = -1;
    static int orient = 0; // 0-N, 1-E, 2-S, 3-W
    
    static Pose nice_pose;

    private static void setColorValues(LightSensor light) {
        String kolory[] = {"bialy", "czarny", "niebieski", "czerwony"}; 
        int wartosci[] = new int[4];
        for (int i=0; i<4; i++) {
            LCD.clear();
            LCD.drawString("Pokaz 1 " + kolory[i], 0, 0);
            LCD.drawString("I nacisnij prawy", 0, 1);
            while(!Button.RIGHT.isDown()) {
                int value = light.getNormalizedLightValue();
                LCD.drawInt(value, 0,2);
            }
            while(!Button.RIGHT.isUp()) {}
            int v_min = light.getNormalizedLightValue();
            /*int v_max = v_min;*/
            /*int tab[] = new int[3];
            for (int j=1; j<4; j++) {
                LCD.clear();
                LCD.drawString("Pokaz " + (j + 1) + " " + kolory[i], 0, 0);
                while(!Button.RIGHT.isDown()) {
                    int next_v = light.getNormalizedLightValue();
                    LCD.drawInt(next_v, 0,2);
                }
                while(!Button.RIGHT.isUp()) {}
                int temp = light.getNormalizedLightValue();
                tab[j-1] = temp;
                /*if (temp > v_max) v_max = temp;
                else if (temp < v_min) v_min = temp;*/
            //}*/
            //wartosci[i] = Math.round((v_min + tab[0] + tab[1] + tab[2]) / 4);
            wartosci[i] = v_min;
            /*switch (i){
	            case 0: WHITE_L = v_min - 10;
	                    WHITE_H = v_max + 100;
	                    break;
	            case 1: BLACK_L = v_min;
	                    BLACK_H = v_max;
	                    break;
	            case 2: BLUE_L = v_min - 100;
	                    BLUE_H = v_max;
	                    break;
	            case 3: RED_L = v_min;
	                    RED_H = v_max;
	                    break;
            }*/
        }
        WHITE_L = wartosci[0];
        BLACK_L = wartosci[1];
        //BLUE_L = wartosci[2];
        RED_L = wartosci[3];
        /*if (RED_H < WHITE_L) {
        	WHITE_L = RED_H;
        }
        if (BLACK_H < RED_L) {
	        int diff = Math.abs(BLACK_H - RED_L);
	        BLACK_H = BLACK_H + Math.round(2 * diff / 3.0f) + 1;
	        RED_L = BLACK_H;
        }*/
    }
    
    private static String color(int value) {
    	int b_d, k_d, r_d, w_d;

    	//b_d = Math.abs(value - BLUE_L);
    	int min;// = b_d;
    	String ret_val = "blue";
    	k_d = Math.abs(value - BLACK_L);
    	//if (k_d < min) {
    		min = k_d;
    		ret_val = "black";
    	//}
    	r_d = Math.abs(value - RED_L);
    	if (r_d < min) {
    		min = r_d;
    		ret_val = "red";
    	}
    	w_d = Math.abs(value - WHITE_L);
    	if (w_d < min) {
    		min = w_d;
    		ret_val = "white";
    	}
    	return ret_val;

        /*if ((value > BLACK_L)&&(value <= BLACK_H))
            return "black";
        if ((value > BLUE_L)&&(value <= BLUE_H))
            return "blue";
        if ((value > WHITE_L)&&(value <= WHITE_H))
            return "white";
        if ((value > RED_L)&&(value <= RED_H))
            return "red";
        return "unknown";*/
    }
    
    private static String f_color(int value) {
    	String color = color(value);
    	if (color.contentEquals("black"))
    		return "blue";
    	return color;
    }
    
    private static DMap moveOneFront(DifferentialPilot pilot, LightSensor light, DMap mapa) {
        int new_val = light.getNormalizedLightValue();
        String start_color = color(new_val);
        String new_color = start_color;
        pilot.forward();
        int new_col_val;
        do {
        	new_col_val = light.getNormalizedLightValue();
            new_color = color(new_col_val);
        } while (!new_color.contentEquals("black"));
    	/*LCD.drawString(new_color, 0, 4);
        do {
        	new_col_val = light.getNormalizedLightValue();
            new_color = color(new_col_val);
        } while (new_color.contentEquals("black"));*/
        pilot.stop();
        if (start_color.contentEquals("black")){
        	pilot.travel(5);
        } else pilot.travel(30);
        correctPosition(pilot, light);
        //pilot.travel(100); // skorygowac aby nie wjezdzac na obiekty!!
        switch (orient) {
            case 0: pos_y++;
                    break;
            case 1: pos_x++;
                    break;
            case 2: pos_y--;
                    break;
            case 3: pos_x--;
                    break;
        };
        new_color = f_color(light.getNormalizedLightValue());
    	LCD.drawString(new_color, 0, 4);
        if (new_color.contentEquals("blue")) {
            mapa.setField(pos_x, pos_y, 'N');
        } else if (new_color.contentEquals("red")) {
            mapa.setField(pos_x, pos_y, 'C');
        } else {
            mapa.setField(pos_x, pos_y, 'B');
        }
        return mapa;
    }

    private static void correctPosition(DifferentialPilot pilot,
            LightSensor light) {
    	//pilot.travel(7);
        int start_val = light.getNormalizedLightValue();
        int now_val = start_val;
        String now_c = color(now_val);
        pilot.rotateLeft();
        do {
            now_val = light.getNormalizedLightValue();
            now_c = color(now_val);
        } while (!now_c.contentEquals("black"));
        pilot.stop();
        
        //pilot.rotate(5.0f);
        String out = color(light.getNormalizedLightValue());
        //pilot.rotate(-5.0f);
        OdometryPoseProvider pp = new OdometryPoseProvider(pilot);
        Pose pose;
        
        pilot.rotateRight();
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        do {
            now_val = light.getNormalizedLightValue();
            now_c = color(now_val);
        } while (!now_c.contentEquals(out));
        pose  = pp.getPose();
        
        double rotation = pose.getHeading() * -1.0f;
        if (!color(start_val).contentEquals("black")){
        	rotation = rotation / 2.0f;
        }
        
        pilot.rotate(rotation);

        pp = new OdometryPoseProvider(pilot);
        now_val = light.getNormalizedLightValue();
        now_c = color(now_val);
        start_val = now_val;
        pilot.forward();
        try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	now_val = light.getNormalizedLightValue();
    	now_c = color(now_val);
        while (!now_c.contentEquals("black")) {
        	now_val = light.getNormalizedLightValue();
        	now_c = color(now_val);
        }
        pilot.stop();
        pilot.travel((pp.getPose().getX() / -2.0f) + 30.0f);
        return;
    }

    private static DMap goAWay(String way, DifferentialPilot pilot, LightSensor light, DMap mapa) {
    	for (int i=0; i<way.length(); i++) {
            char next_l = way.charAt(i);
            if (next_l == 'r') {
                pilot.rotate(-90.0f);
                orient = (orient + 1) % 4;
            } else if (next_l == 'l') {
                pilot.rotate(90.0f);
                orient = (orient - 1);
                if (orient == -1) {
                    orient = 3;
                }
            } else if (next_l == 'f') {
                mapa = moveOneFront(pilot, light, mapa);
            }
        };
        return mapa;
    }
    
    public static void main(String[] args) throws Exception {
        LightSensor light = new LightSensor(SensorPort.S4);
        DifferentialPilot pilot = new DifferentialPilot(56, 128, Motor.C, Motor.B);
        pilot.setTravelSpeed(75.0f);
        pilot.setRotateSpeed(25.0f);
        
        setColorValues(light);
        
        LCD.clear();

        //int iterator = 0;
        /*LCD.clear();
        LCD.drawString("w " + WHITE_L + " " + WHITE_H, 0, 0);
        LCD.drawString("r " + RED_L + " " + RED_H, 0, 1);
        LCD.drawString("b " + BLUE_L + " " + BLUE_H, 0, 2);
        LCD.drawString("k " + BLACK_L + " " + BLACK_H, 0, 3);*/
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

        while (mapa.unknownCount() > 0){
        	LCD.drawInt(mapa.unknownCount(), 0, 7);
            String way = mapa.findAWay(pos_x, pos_y, orient); // W odległości manhatańskiej/taksówkowej

        	//LCD.drawString("" + pos_x + "x" + pos_y, 0, 3);
        	//LCD.drawString(way, 0, 4);
            mapa = goAWay(way, pilot, light, mapa);
        }

        mapa.printOnDisplay();

        Sound.beep();
        
        while(!Button.RIGHT.isDown()) {
        }

    }

}

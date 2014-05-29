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
        String kolory[] = {"niebieski", "czarny", "czerwony", "bialy"}; 
        int wartosci[] = new int[4];
        for (int i=0; i<4; i++) {
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
        BLUE_L = wartosci[0] - 30;
        int temp = wartosci[1] - wartosci[0];
        if (temp < 40){
            BLUE_H = wartosci[0] + (int)Math.floor(temp/2);
            BLACK_L = BLUE_H;
        } else {
            BLUE_H = wartosci[0] + 15;
            BLACK_L = wartosci[1] - 15;
        }
        temp = wartosci[2] - wartosci[1];
        if (temp < 40){
            BLACK_H = wartosci[1] + (int)Math.floor(temp/3);
            RED_L = BLACK_H;
        } else {
            BLACK_H = wartosci[1] + 15;
            RED_L = wartosci[2] - 15;
        }
        temp = wartosci[3] - wartosci[2];
        if (temp < 40){
            RED_H = wartosci[2] + (int)Math.floor(temp/3);
            WHITE_L = RED_H;
        } else {
            RED_H = wartosci[2] + 15;
            WHITE_L = wartosci[3] - 15;
        }
        WHITE_H = wartosci[3] + 30;
    }
    
    private static String color(int value) {
        if ((value > BLUE_L)&&(value <= BLUE_H))
            return "blue";
        if ((value > RED_L)&&(value <= RED_H))
            return "red";
        if ((value > BLACK_L)&&(value <= BLACK_H))
            return "black";
        if ((value > WHITE_L)&&(value <= WHITE_H))
            return "white";
        return "unknown";
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
        } while (Math.abs(new_col_val - new_val) < 20);
        pilot.stop();
        correctPosition(pilot, light);
        pilot.travel(100); // skorygowac aby nie wjezdzac na obiekty!!
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
        if (mapa.isUnknown(pos_x, pos_y)) {
            new_color = color(light.getNormalizedLightValue());
            if (new_color == "blue") {
                mapa.setField(pos_x, pos_y, 'N');
            } else if (new_color == "red") {
                mapa.setField(pos_x, pos_y, 'C');
            } else {
                mapa.setField(pos_x, pos_y, 'B');
            }
        }
        return mapa;
    }

    private static void correctPosition(DifferentialPilot pilot,
            LightSensor light) {
        int start_val = light.getNormalizedLightValue();
        int now_val = start_val;
        pilot.rotateLeft();
        // TUTAJ ZMIENIAC 1
        String start_c = color(start_val);
        String now_c = color(now_val);
        do {
            now_val = light.getNormalizedLightValue();
            now_c = color(now_val);
        } while (start_c.contentEquals(now_c));
        pilot.stop();
        OdometryPoseProvider pp = new OdometryPoseProvider(pilot);
        Pose pose;
        pilot.rotateRight();
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        do {
            start_val = light.getNormalizedLightValue();
        } while (Math.abs(start_val - now_val) == 0);
        // TUTAJ ZMIENIAC 2
        pose  = pp.getPose();
        pilot.rotate(pose.getHeading() / -2.0f);
        

		float diff=0;
		pose = navigator.getPose();
		if(kierunek == Kierunek.N || kierunek == Kierunek.S)
            diff = Math.round((pose.getX()-1)/dist) - (pose.getX()-1)/dist;
		else
			diff = Math.round((-pose.getY()-1)/dist) - (-pose.getY()-1)/dist;
        if(diff != 0)
		{
			navigator.travel(dist*diff);
                        //przejscie z X.eps na x i przeskalowanie na X - usuniecie .eps
			pose.setLocation(scale(reScale(pose.getX())), -scale(reScale(-pose.getY())));
			navigator.setPose(pose);
		}
    }

    private static DMap goAWay(String way, DifferentialPilot pilot, LightSensor light, DMap mapa) {
        do {
            char next_l = way.charAt(0);
            way = way.substring(1);
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
        } while (way.length() != 0);
        return mapa;
    }
    
    public static void main(String[] args) throws Exception {
        LightSensor light = new LightSensor(SensorPort.S4);
        DifferentialPilot pilot = new DifferentialPilot(56, 128, Motor.C, Motor.B);
        pilot.setTravelSpeed(75.0f);
        pilot.setRotateSpeed(10.0f);
        
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

        OdometryPoseProvider pp = new OdometryPoseProvider(pilot);
        nicepose = pp.getPose();
        
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
        	LCD.drawInt(mapa.unknownCount(), 0, 5);
    		Thread.sleep(1000);
            String way = mapa.findAWay(pos_x, pos_y, orient); // W odległości manhatańskiej/taksówkowej

        	LCD.drawString(way, 0, 4);
			Thread.sleep(1000);
            mapa = goAWay(way, pilot, light, mapa);
        }

        mapa.printOnDisplay();

        Sound.beep();
        
        while(!Button.RIGHT.isDown()) {
        }

    }

}

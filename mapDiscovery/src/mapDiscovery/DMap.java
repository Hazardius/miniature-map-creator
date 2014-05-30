package mapDiscovery;

import lejos.nxt.LCD;

public class DMap {
    private char[][] mapData;
    private int xDim, yDim;
    private boolean sSet, cSet, nSet;
    private int unknown;
    

    /*n - pole nieznane,
    S – pole startowe,
    B – pole bia³e,
    C – czerwone pole,
    N – niebieskie pole.*/
    
    public DMap() {
        this(4, 4);
    }
    
    public DMap(int x, int y) {
        xDim = x;
        yDim = y;
        mapData = new char[x][y];
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                mapData[i][j] = 'n';    // nil value
            }
        }
        unknown = xDim * yDim;
    }

    private void whiteAll() {
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                if (mapData[i][j] == 'n')
                    mapData[i][j] = 'B';
            }
        }
        unknown = 0;
    }
    
    public void setField(int xPos, int yPos, char value) {
        if ((xPos < xDim) && (yPos < yDim)) {
            mapData[xPos][yPos] = value;
            if (value == 'S') {
                sSet = true;
                cSet = false;
                nSet = false;
                unknown++;
            }
            if (value == 'C') cSet = true;
            if (value == 'N') nSet = true;
            if (sSet && cSet && nSet) whiteAll();
            else {
                unknown--;
            }
        }
    }
    
    public void printOnDisplay() {
        LCD.clear();
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                LCD.drawString("" + mapData[i][j], i, yDim - j - 1);
            }
        }
    }

    public boolean isUnknown(int pos_x, int pos_y) {
        return mapData[pos_x][pos_y] == 'n';
    }

	public int unknownCount() {
        return unknown;
	}
	
	public int knownCount() {
        return xDim * yDim - unknown;
	}

	public String findAWay(int pos_x, int pos_y, int orient) {
        switch (orient) {
	        case 0: if (pos_y == yDim - 1) {
        	        	return "lf";
        	        } else {
		        	    return "f";
        	        }
	        case 1: return "rr";
	        case 2: if (pos_y == 0) {
			        	return "rf";
			        } else {
		        	    return "f";
			        }
	        case 3: if (pos_y % 2 == 1) {
			        	return "lf";
			        } else {
		        	    return "rf";
			        }
        };
        return "";
	}
}

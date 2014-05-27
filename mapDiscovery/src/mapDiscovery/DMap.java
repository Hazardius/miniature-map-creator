package mapDiscovery;

import lejos.nxt.LCD;

public class DMap {
	private char[][] mapData;
	private int xDim, yDim;
	

    /*n - pole nieznane,
    S – pole startowe,
    B – pole bia³e,
    C – czerwone pole,
    N – niebieskie pole,
    P – niebieska pi³eczka,
    H – czerwony szeœcian.*/
	
	public DMap() {
		this(4, 4);
	}
	
	public DMap(int x, int y) {
		xDim = x;
		yDim = y;
	    mapData = new char[x][y];
	    for (int i = 0; i < xDim; i++) {
	    	for (int j = 0; j < yDim; j++) {
	    		mapData[i][j] = 'n';	// nil value
	    	}
	    }
	}
	
	public void setField(int xPos, int yPos, char value) {
		if ((xPos < xDim) && (yPos < yDim)) {
 			mapData[xPos][yPos] = value;
		}
	}
	
	public void printOnDisplay() {
		LCD.clear();
	    for (int i = 0; i < xDim; i++) {
	    	for (int j = 0; j < yDim; j++) {
	            LCD.drawString("" + mapData[i][j], i,j);
	    	}
	    }
	}
	
	private int heurestic_len(int one_x, int one_y, int two_x, int two_y) {
		return (Math.abs(one_x - two_x)+Math.abs(one_y - two_y));
	}

	public String findAWay(int pos_x, int pos_y) {
		String way = "";
		int min = 7;
		int[][] wybrane = new int[6][2];
		int ch_co = 0;
		for (int row=0; row < 4; row++) {
			for (int col=0; col < 4; col++) {
				int h_len = heurestic_len(row, col, pos_x, pos_y);
				if (h_len == min) {
					wybrane[ch_co][0] = row;
					wybrane[ch_co][1] = col;
					ch_co++;
				} else if (h_len < min) {
					wybrane = new int[5][2];
					wybrane[0][0] = row;
					wybrane[0][1] = col;
					min = h_len;
					ch_co = 1;
				}
			}
		}
		// W tym momencie mamy listê "najbli¿szych pól" w tabelce wybrane!
		return way;
	}

	public boolean haveUnknown() {
	    for (int i = 0; i < xDim; i++) {
	    	for (int j = 0; j < yDim; j++) {
	    		if (mapData[i][j] == 'n')
	    			return true;
	    	}
	    }
		return false;
	}
}

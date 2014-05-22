package mapDiscovery;

import lejos.nxt.LCD;

public class DMap {
	private char[][] mapData;
	private int xDim, yDim;
	
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
}

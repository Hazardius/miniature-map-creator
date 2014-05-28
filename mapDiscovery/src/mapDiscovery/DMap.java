package mapDiscovery;

import lejos.nxt.LCD;

public class DMap {
    private char[][] mapData;
    private int xDim, yDim;
    private boolean sSet, cSet, nSet;
    

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
    }

    private void whiteAll() {
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                if (mapData[i][j] = 'n')
                    mapData[i][j] = 'B';
            }
        }
    }
    
    public void setField(int xPos, int yPos, char value) {
        if ((xPos < xDim) && (yPos < yDim)) {
            mapData[xPos][yPos] = value;
            if (value == 'S') {
                sSet = true;
                cSet = false;
                nSet = false;
            }
            if (value == 'C') cSet = true;
            if (value == 'N') nSet = true;
            if (sSet && cSet && nSet) whiteAll();
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
        return (Math.abs(one_x - two_x) + Math.abs(one_y - two_y));
    }

    public String wayTo(int pos_x, int pos_y, int jx, int jy, int orient) {
        String way = "";
        int cpx = pos_x;
        int cpy = pos_y;
        int cpo = orient;
        while ((cpx != jx) || (cpy != jy)) {
            switch (cpo) {
                case 0: if (cpx == jx) {
                            if (cpy < jy) {
                                way += "f";
                                cpy++;
                            } else {
                                way += "rr";
                                cpo = 2;
                            }
                        } else if (cpy == jy) {
                            if (cpx > jx) {
                                way += "l";
                                cpo = 3;
                            } else {
                                way += "r";
                                cpo = 1;
                            }
                        } else {
                            if (cpy < jy) {
                                way += "f";
                                cpy++;
                            } else {
                                if (cpx < jx) {
                                    way += "r";
                                    cpo = 1;
                                } else {
                                    way += "l";
                                    cpo = 3;
                                }
                            }
                        }
                        break;
                case 1: if (cpy == jy) {
                            if (cpx < jx) {
                                way += "f";
                                cpx++;
                            } else {
                                way += "rr";
                                cpo = 3;
                            }
                        } else if (cpx == jx) {
                            if (cpy > jy) {
                                way += "r";
                                cpo = 2;
                            } else {
                                way += "l";
                                cpo = 0;
                            }
                        } else {
                            if (cpx < jx) {
                                way += "f";
                                cpx++;
                            } else {
                                if (cpy < jy) {
                                    way += "l";
                                    cpo = 0;
                                } else {
                                    wax += "r";
                                    cpo = 2;
                                }
                            }
                        }
                        break;
                case 2: if (cpx == jx) {
                            if (cpy > jy) {
                                way += "f";
                                cpy++;
                            } else {
                                way += "rr";
                                cpo = 0;
                            }
                        } else if (cpy == jy) {
                            if (cpx > jx) {
                                way += "r";
                                cpo = 3;
                            } else {
                                way += "l";
                                cpo = 1;
                            }
                        } else {
                            if (cpy > jy) {
                                way += "f";
                                cpy++;
                            } else {
                                if (cpx < jx) {
                                    way += "l";
                                    cpo = 1;
                                } else {
                                    way += "r";
                                    cpo = 3;
                                }
                            }
                        }
                        break;
                case 3: if (cpy == jy) {
                            if (cpx > jx) {
                                way += "f";
                                cpy++;
                            } else {
                                way += "rr";
                                cpo = 1;
                            }
                        } else if (cpx == jx) {
                            if (cpy > jy) {
                                way += "l";
                                cpo = 2;
                            } else {
                                way += "r";
                                cpo = 0;
                            }
                        } else {
                            if (cpx > jx) {
                                way += "f";
                                cpy++;
                            } else {
                                if (cpy < jy) {
                                    way += "r";
                                    cpo = 0;
                                } else {
                                    way += "l";
                                    cpo = 2;
                                }
                            }
                        }
                        break;
            };
        };
        return way;
    }

    public String findAWay(int pos_x, int pos_y, int orient) {
        int min = 7;
        int[][] wybrane = new int[6][2];
        int ch_co = 0;
        for (int row=0; row < 4; row++) {
            for (int col=0; col < 4; col++) {
                int h_len = heurestic_len(col, row, pos_x, pos_y);
                if (h_len == min) {
                    wybrane[ch_co][0] = col;
                    wybrane[ch_co][1] = row;
                    ch_co++;
                } else if (h_len < min) {
                    wybrane = new int[5][2];
                    wybrane[0][0] = col;
                    wybrane[0][1] = row;
                    min = h_len;
                    ch_co = 1;
                }
            }
        }
        int jx, jy;
        int max = -1;
        for (int wyb=0; wyb<ch_co; wyb++) {
            int pv = 0;
            if (wybrane[wyb][0] == pos_x) {
                pv += 2;
            } else if ((wybrane[wyb][0] < pos_x) && (orient == 3)) {
                pv++;
            } else if ((wybrane[wyb][0] > pos_x) && (orient == 1)) {
                pv++;
            }
            if (wybrane[wyb][1] == pos_y) {
                pv += 2;
            } else if ((wybrane[wyb][1] < pos_y) && (orient == 2)) {
                pv++;
            } else if ((wybrane[wyb][1] > pos_y) && (orient == 0)) {
                pv++;
            }
            if (max < pv) {
                max = pv;
                jx = wybrane[wyb][0];
                jy = wybrane[wyb][1];
            }
        }
        return wayTo(pos_x, pos_y, jx, jy, orient);
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

    public boolean isUnknown(int pos_x, int pos_y) {
        if (mapData[pos_x][pos_y] == 'n')
            return true;
        return false;
    }
}

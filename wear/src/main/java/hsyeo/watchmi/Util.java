package hsyeo.watchmi;

public class Util {

    int level;
    int DetermineLevel(float x, float y, int base, int gap){
        int lvl1 = base;
        int lvl2 = lvl1 + gap;
        int lvl3 = lvl2 + gap;
        int lvl4 = lvl3 + gap;
        // don't use squareroot coz it is slow
        if ((x*x+y*y) < lvl1*lvl1)
            level = 0;
        else if ((x*x+y*y) > lvl1*lvl1 && (x*x+y*y) < lvl2*lvl2)
            level = 1;
        else if ((x*x+y*y) > lvl2*lvl2 && (x*x+y*y) < lvl3*lvl3)
            level = 2;
        else if ((x*x+y*y) > lvl3*lvl3 && (x*x+y*y) < lvl4*lvl4)
            level = 3;
        else if ((x*x+y*y) > lvl4*lvl4)
            level = 4;

        return level;
    }

    //TODO: improve this hardcoded piece of ...
    String globalPos = "";
    int direction = 0;
    int one, two;
    int DetermineTouchPos(float x, float y, boolean isRound){
        if (isRound) {
            one = 100; two = 220;
        } else {
            one = 93; two = 186;
        }

//region classify touch region based on touch location
        if (x < one && y < one) {
            globalPos = "Top Left";
            direction = 8;
        } else if (x > one && x < two && y < one) {
            globalPos = "Top Mid";
            direction = 1;
        } else if (x > 186 && y < one) {
            globalPos = "Top Right";
            direction = 2;
        }
        else if (x < one && y > one && y < two) {
            globalPos = "Left";
            direction = 7;
        } else if (x > one && x < 186 && y > one && y < two) {
            globalPos = "Mid";
            direction = 9;
        } else if (x > two && y > one && y < two) {
            globalPos = "Right";
            direction = 3;
        }
        else if (x < one && y > two) {
            globalPos = "Bottom Left";
            direction = 6;
        } else if (x > one && x < two && y > two) {
            globalPos = "Bottom Mid";
            direction = 5;
        } else if (x > two && y > two) {
            globalPos = "Bottom Right";
            direction = 4;
        }
//endregion
        return direction;
    }
}

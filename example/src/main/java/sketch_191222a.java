import processing.core.PApplet;
import processing.core.PConstants;

public class sketch_191222a extends PApplet {

public void setup() {
  
}

public void draw() {
  ellipse(0,0,100,100);
}

public void settings() {  size(500,500, PConstants.P3D); }

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_191222a" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

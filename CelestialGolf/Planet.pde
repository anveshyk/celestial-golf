class Planet {

  static final int MIN_DIAMETER = 150; //150
  static final int MAX_DIAMETER = 275; //350 then 250 then 275
  static final int MIN_RGB_VALUE = 137; //125, then 150

  static final float FLAG_STICK_LEN = 35;
  static final int STICK_WIDTH = 4;
  static final float FLAG_HEIGHT = 14;
  static final float FLAG_WIDTH = 18;
  static final int HOLE_WIDTH = 23;
  static final float HOLE_FRACTION = 0.925;
  static final float HOLE_OFFSET = 8; //distance hole is down from circumference of planet

  // potential for moving planets later so velocity and acceleration needed?!?!?
  PVector position;
  int diameter;
  color col;

  // Flag/green attributes
  boolean green; //green means it has the flag, i.e. green in golf being the area of the hole with the flag/hole on it
  float flagAngle;
  PVector flagTip; //actually not tip of flag, tip of stick!
  PVector flagBase;
  PVector hole;

  Planet(int diameter, int x, int y) {
    position = new PVector(x, y);
    this.diameter = diameter;
    col = color(int(random(MIN_RGB_VALUE, 255)), int(random(MIN_RGB_VALUE, 255)), int(random(MIN_RGB_VALUE, 255)));
    green = false;
  }

  void draw() {

    if (green) {

      // planet
      noStroke();
      fill(col);
      ellipseMode(CENTER);
      circle(position.x, position.y, diameter);

      // hole
      /*
      stroke(30, 35, 45);
      strokeCap(SQUARE); //project
      strokeWeight(HOLE_WIDTH);
      line(position.x, position.y, flagBase.x, flagBase.y); // hole instead of flagbase
      */
      fill(25, 24, 29);
      //fill(30, 35, 45);
      circle(hole.x, hole.y, HOLE_WIDTH);

    }

    else {

      noStroke();
      ellipseMode(CENTER);
      fill(col);
      circle(position.x, position.y, diameter);

    }
  }

  void drawFlag() {

    if (green) {

      //draw flag

      noStroke();
      fill(208, 67, 52);

      pushMatrix();
      translate(flagTip.x, flagTip.y);
      rotate(flagAngle);
      rectMode(CORNER);
      rect(-(FLAG_HEIGHT), 0, FLAG_HEIGHT, FLAG_WIDTH, 3);
      popMatrix();


      // flag stick
      stroke(160, 82, 80);
      strokeCap(PROJECT);
      strokeWeight(STICK_WIDTH);
      line(flagTip.x, flagTip.y, flagBase.x, flagBase.y);
    }
  }

  //makes green - creates hole location
  void makeGreen() {
    green = true;
    flagAngle = random(TWO_PI);

    while(!validFlag(flagAngle)) {
      flagAngle = random(TWO_PI);
    }

    // get top of hole pos
    hole = PVector.sub(flagBase, position);
    float offset = hole.mag() - HOLE_OFFSET;
    hole.normalize();
    hole.mult(offset);
    hole.add(position);

  }

  boolean validFlag(float angle) {
    PVector tip = PVector.fromAngle(angle);
    PVector base = tip.copy();

    tip.mult(float(diameter)/2 + FLAG_STICK_LEN); //flag vector
    tip.add(position.copy());
    flagTip = tip;

    base.mult(float(diameter)/2);
    base.add(position.copy());
    flagBase = base;

    return inScreen(tip);
  }

  boolean inScreen(PVector pos) {
    return (pos.x < width && pos.x > 0 && pos.y < height && pos.y > 0);
  }

}

class Star {
  // simple object to hold a star in the backgrounds position and diamter (perhaps colour later on)

  //could perhaps have stars of different colours, or slightly change in size, or in colour

  //could have star be actual star shape?!!??!!!?!?!!

  //stars in actual background of inspo is faded around the edges - anyway to replicate this??
  // add slight opacity??

  static final float MIN_DIAMETER = 1;
  static final float MAX_DIAMETER = 9; //originally 8
  static final float BORDER_WIDTH = 0.5;
  static final int TWINKLE_ODDS = 400;

  int diameter;
  PVector position;

  Star() {
    position = new PVector(random(width), random(height));
    diameter = int(random(MIN_DIAMETER, MAX_DIAMETER));
  }

  void draw() {
    stroke(153, 153, 157);
    strokeWeight(BORDER_WIDTH);
    ellipseMode(CENTER);
    fill(228, 227, 232);
    //if (int(random(7)) == 2) stroke(215, 215, 215);

    int prevD = diameter;
    if (int(random(TWINKLE_ODDS)) == 1) diameter+= 4;

    circle(position.x, position.y, diameter);
    diameter = prevD;
  }
}

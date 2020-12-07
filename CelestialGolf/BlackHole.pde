class BlackHole {

  //static constants
  static final int DIAMETER = 150;
  static final int MAX = 150;

  // attributes
  PVector position;

  BlackHole(int x, int y) {
    position = new PVector(x, y);
  }

  void draw() {
    stroke(255);
    strokeWeight(1);
    ellipseMode(CENTER);
    fill(0);
    circle(position.x, position.y, DIAMETER);
  }

}

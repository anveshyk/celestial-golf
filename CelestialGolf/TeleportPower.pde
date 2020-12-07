class TeleportPower {

  // static constants
  static final int ORBIT_HEIGHT = 60;
  static final int POSITION_NO = 800; // number of position Pectors that make up the orbit, the more the slower the orbit
  static final float DIAMETER = 25;

  // attributes
  //Planet planet;
  int orbitRadius;
  float angle; //in radians angle inbetween each succesive orbit point
  ArrayList<PVector> orbit = new ArrayList<PVector>(); // PVector positions that make up circumference of orbit
  int index;
  boolean used = false;

  TeleportPower(Planet planet) {
    //this.planet = planet;
    orbitRadius = planet.diameter/2 + ORBIT_HEIGHT;
    index = 0;
    angle = TWO_PI / POSITION_NO;

    for (int i = 0; i < POSITION_NO; i++) {
      PVector position = PVector.fromAngle(i * angle);
      position.mult(orbitRadius);
      position.add(planet.position);
      orbit.add(position);
    }
  }

  void draw() {
    if (!used) {
      if (index >= orbit.size()) index = 0;

      noStroke();
      ellipseMode(CENTER);
      fill(150,150,150);
      PVector position = orbit.get(index);
      circle(position.x, position.y, DIAMETER);

      // making crescent
      //ellipseMode(CORNERS);
      fill(25, 24, 29);
      circle(position.x+DIAMETER/2-1, position.y, DIAMETER);

      index++;
    }
  }

}

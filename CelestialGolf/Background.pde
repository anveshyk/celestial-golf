class Background {

  static final int STAR_NO = 50;

  ArrayList<Star> stars = new ArrayList<Star>();

  Background() {
    for (int i = 0; i < STAR_NO; i++) {
      stars.add(new Star());
    }
  }

  void draw() {
    background(25, 24, 29);

    for (Star star : stars) {
      star.draw();
    }
  }
}

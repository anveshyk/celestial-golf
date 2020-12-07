import processing.sound.*;

SoundFile holedSound;
SoundFile hitShot;
SoundFile bounce;
SoundFile music;
SoundFile intro;

PFont font;

static final float PLANET_BUFFER = 150; //200 //buffer enforced between planets when choosing initial positions
static final int PLANET_BUFFER2 = 80; //buffer enforced for planet positions from sides of screen
static final int MAX_HOLE_NO = 9;
static final int DELAY = 2000; // time inbetween beong holed and new hole
static final int MAX_PLANET_NO = 7;

ArrayList<Planet> planets;
Ball ball;
TeleportPower teleport;
ExtraShotsPower extraShots;
BlackHole blackHole;
Planet green; //planet w hole on it
Planet tee; //planet where bal starts
PImage starImg;
Background bg;
//PImage bg
int planetNo = 8; //5

int par;
int score = 0;
int shots = 0;
int holeNo = 0; //essentially level no

int time = millis();

int textOpacity = 255;

int xStart, yStart, xEnd, yEnd, xCurrent, yCurrent;
PVector target, dragVector;

boolean playedHoledSound = true;
boolean doneIntro = false;
boolean doneHoleIntro = false;

void setup() {

  size(1500, 1000); //1200 800
  //bg = loadImage("space2.jpg");
  starImg = loadImage("star.png");
  cursor(starImg);

  font = loadFont("NanumPen-180.vlw");

  bg = new Background();

  setupNewHole();

  String musicPath = sketchPath("space_music.wav");
  music = new SoundFile(this, musicPath);
  music.amp(0.75);

  String holedPath = sketchPath("holed.wav");
  holedSound = new SoundFile(this, holedPath);
  holedSound.amp(1);

  String shotPath = sketchPath("shot.wav");
  hitShot = new SoundFile(this, shotPath);
  hitShot.amp(0.6);

  String bouncePath = sketchPath("bounce.wav");
  bounce = new SoundFile(this, bouncePath);
  bounce.amp(0.5);

  String introPath = sketchPath("intro.wav");
  intro = new SoundFile(this, introPath);
  intro.amp(0.075);

  dragVector = new PVector(0f, 0f);

  time = millis();
}

void draw() {

  // display title
  if (!doneIntro) {
    doIntro();
  }


  // detect if level over (holed) - setup new level or end game
  if (holeNo > 0 && ball.holed) {
      holed();
  }

  // else, progress level
  else if (doneIntro) {
    progressFrame();

    if (!doneHoleIntro && shots == 0) {
      holeIntro();
    }
  }

}

// When mouse is pressed, store x, y coords (taken from studres example)
void mousePressed() {
  //technically allows you to start your next powering up when the ball is not at rest
  xStart = mouseX;
  yStart = mouseY;
}

void finish() {

}

void holed() {
  if (!playedHoledSound) {
    playedHoledSound = true;
    holedSound.play();
    time = millis();
    score += (shots - par);
  }
  else {
    if (millis()-time > DELAY) {
      setupNewHole();
    }
  }

  progressFrame();
}

void doIntro() {
  //handling title
  bg.draw();
  intro.play();

  textAlign(CENTER, CENTER);
  textFont(font, 230);
  noStroke();
  fill(255, textOpacity);
  text("interstellar golf", width/2, height/2); //interstellar, space, cosmos, astro //CELESTIAL

  textOpacity--;

  if (textOpacity == 0) {
    doneIntro = true;
    textOpacity = 255;
  }
}

// When mouse is released create new vector relative to stored x, y coords (adapted from studres example)
void mouseReleased() {

  xEnd = mouseX;
  yEnd = mouseY;

  // only allow shots if ball at rest and mouse hasbeen dragged
  if (ball.velocity.mag() == 0 && dragVector.mag() != 0) {
    hitShot.play();
    shots++;
    ball.velocity = new PVector((xStart - xEnd)/20f, (yStart - yEnd)/20f);
  }

  dragVector.set(0f, 0f);
  target = null;
}

// used to create power slider arrow
void mouseDragged() {
  int xCurrent = mouseX;
  int yCurrent = mouseY;
  dragVector = new PVector((xStart - xCurrent), (yStart - yCurrent));
  target = PVector.add(ball.position, dragVector);
}

void setupNewHole() {

  playedHoledSound = false;
  shots = 0;
  holeNo++;
  doneHoleIntro = false;

  textOpacity = 255;


  planets = new ArrayList<Planet>();
  int planetNo = int(random(2, MAX_PLANET_NO));
  for (int i = 0; i < planetNo; i++) {
    makePlanet();
  }

  chooseGreenAndTee();
  green.makeGreen();

  blackHole = null;
  if (holeNo % 2 == 0) makeBlackhole();

  // choose random planet for teleport - not green - allow tee
  int randPlanetNo = int(random(planetNo));
  while (planets.get(randPlanetNo) == green) {
    randPlanetNo = int(random(planetNo));
  }
  teleport = new TeleportPower(planets.get(randPlanetNo));

  // choose random planet for losing shots power up
  randPlanetNo = int(random(planetNo));
  extraShots = new ExtraShotsPower(planets.get(randPlanetNo));

  initBall();

  //calculating par
  par = getPar();

}

int getPar() {
  float dist = green.position.dist(tee.position);
  return int(dist/300) + 1;
}

void holeIntro() {

  //intro.play();

  textAlign(CENTER, CENTER);
  textFont(font, 230);
  noStroke();
  fill(255, textOpacity);
  text(("Hole " + holeNo), width/2, height/2); //interstellar, space, cosmos, astro //CELESTIAL

  textOpacity--;

  if (textOpacity == 0) {
    doneHoleIntro = true;
  }
}

void progressFrame() {

  //background(25, 24, 29);  //bg, 25, 20, (33, 30, 31)
  bg.draw();

  teleport.draw();
  extraShots.draw();

  green.drawFlag();

  for (Planet planet : planets) {
    planet.draw();
  }

  if (blackHole != null) {
    blackHole.draw();
  }

  // draw power arrow
  if (mousePressed && target != null && ball.velocity.mag() == 0) {
    // find PVector of end of power line from ball - so imverse of dragged distance starting from ball
    //stroke(255);
    //stroke(200);
    stroke(228, 227, 232);
    //strokeCap(PROJECT);
    strokeCap(ROUND);
    strokeWeight(dragVector.mag() / 20);
    line(ball.position.x, ball.position.y, target.x, target.y);
  }

  ball.draw();
  ball.integrate(planets, this);

  // text for hole no, par, current shots, current score??
  if (!doneHoleIntro) {
    holeIntro();
  }

  drawText();


  // background music
  if (!intro.isPlaying() && !music.isPlaying()) {
    music.loop();
  }

}

void drawText() {
  textAlign(CENTER, CENTER);
  textFont(font, 40);
  noStroke();
  fill(255);
  text(("par " + par), 49, 25);
  text(("shots: " + shots), width/2, 25);
  text(("score: " + score), width - 70, 25);
}

// chooses green and tee (ball init position planet)
// by going through all pairs
void chooseGreenAndTee() {
  float maxDistance = 0; //max value
  for (int i = 0; i < planets.size()-1; i++) {
    for (int j = i+1; j < planets.size(); j++) {

      Planet possGreen = planets.get(i);
      Planet possTee = planets.get(j);
      float distance = possGreen.position.dist(possTee.position); //could subtract other stuff??

      if (distance > maxDistance) {
        maxDistance = distance;
        green = possGreen;
        tee = possTee;
      }

    }
  }
}

// randomly places the ball in space, ensuring not on planet
void initBall() {

  float offset = 30;

  // get position near tee
  PVector centre = new PVector(width/2, height/2);
  PVector initPos = PVector.sub(centre, tee.position);
  float dist = tee.diameter/2 + offset;
  initPos.normalize();
  initPos.mult(dist);
  initPos.add(tee.position);

  ball = new Ball(initPos.x, initPos.y, 0.11f, 0.11f, 0f, 0f);
}

void makePlanet() {

  boolean valid = false;
  int x = 0;
  int y = 0;
  int diameter = 0;

  //int count = 0;

  while (!valid) {

    x = int(random(0+PLANET_BUFFER2, width-PLANET_BUFFER2));
    y = int(random(0+PLANET_BUFFER2, height-PLANET_BUFFER2));
    diameter = int(random(Planet.MIN_DIAMETER, Planet.MAX_DIAMETER));

    valid = true;

    for (Planet planet : planets) {
      if (bodiesOverlap(planet, x, y, diameter, PLANET_BUFFER)) {
        valid = false;
        break;
      }
    }

  }

  planets.add(new Planet(diameter, x, y));

}

void makeBlackhole() {
  boolean valid = false;
  int x = 0;
  int y = 0;
  int diameter = 150;

  //int count = 0;

  while (!valid) {

    x = int(random(0+PLANET_BUFFER2, width-PLANET_BUFFER2));
    y = int(random(0+PLANET_BUFFER2, height-PLANET_BUFFER2));

    valid = true;

    for (Planet planet : planets) {
      if (bodiesOverlap(planet, x, y, diameter, PLANET_BUFFER)) {
        valid = false;
        break;
      }
    }
  }

  blackHole = new BlackHole(x, y);
}

// checks if any planet overlaps with any given circular body attributes,
boolean bodiesOverlap(Planet planet, int x, int y, int diameter, float buffer) {
  PVector newPos = new PVector(x, y);
  float minDist = (float(diameter)/2 + float(planet.diameter)/2) + buffer;

  return newPos.dist(planet.position) < minDist;
}

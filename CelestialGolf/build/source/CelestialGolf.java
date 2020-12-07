import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CelestialGolf extends PApplet {



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

public void setup() {

   //1200 800
  //bg = loadImage("space2.jpg");
  starImg = loadImage("star.png");
  cursor(starImg);

  font = loadFont("NanumPen-180.vlw");

  bg = new Background();

  setupNewHole();

  String musicPath = sketchPath("space_music.wav");
  music = new SoundFile(this, musicPath);
  music.amp(0.75f);

  String holedPath = sketchPath("holed.wav");
  holedSound = new SoundFile(this, holedPath);
  holedSound.amp(1);

  String shotPath = sketchPath("shot.wav");
  hitShot = new SoundFile(this, shotPath);
  hitShot.amp(0.6f);

  String bouncePath = sketchPath("bounce.wav");
  bounce = new SoundFile(this, bouncePath);
  bounce.amp(0.5f);

  String introPath = sketchPath("intro.wav");
  intro = new SoundFile(this, introPath);
  intro.amp(0.075f);

  dragVector = new PVector(0f, 0f);

  time = millis();
}

public void draw() {

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
public void mousePressed() {
  //technically allows you to start your next powering up when the ball is not at rest
  xStart = mouseX;
  yStart = mouseY;
}

public void finish() {

}

public void holed() {
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

public void doIntro() {
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
public void mouseReleased() {

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
public void mouseDragged() {
  int xCurrent = mouseX;
  int yCurrent = mouseY;
  dragVector = new PVector((xStart - xCurrent), (yStart - yCurrent));
  target = PVector.add(ball.position, dragVector);
}

public void setupNewHole() {

  playedHoledSound = false;
  shots = 0;
  holeNo++;
  doneHoleIntro = false;

  textOpacity = 255;


  planets = new ArrayList<Planet>();
  int planetNo = PApplet.parseInt(random(2, MAX_PLANET_NO));
  for (int i = 0; i < planetNo; i++) {
    makePlanet();
  }

  chooseGreenAndTee();
  green.makeGreen();

  blackHole = null;
  if (holeNo % 2 == 0) makeBlackhole();

  // choose random planet for teleport - not green - allow tee
  int randPlanetNo = PApplet.parseInt(random(planetNo));
  while (planets.get(randPlanetNo) == green) {
    randPlanetNo = PApplet.parseInt(random(planetNo));
  }
  teleport = new TeleportPower(planets.get(randPlanetNo));

  // choose random planet for losing shots power up
  randPlanetNo = PApplet.parseInt(random(planetNo));
  extraShots = new ExtraShotsPower(planets.get(randPlanetNo));

  initBall();

  //calculating par
  par = getPar();

}

public int getPar() {
  float dist = green.position.dist(tee.position);
  return PApplet.parseInt(dist/300) + 1;
}

public void holeIntro() {

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

public void progressFrame() {

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

public void drawText() {
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
public void chooseGreenAndTee() {
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
public void initBall() {

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

public void makePlanet() {

  boolean valid = false;
  int x = 0;
  int y = 0;
  int diameter = 0;

  //int count = 0;

  while (!valid) {

    x = PApplet.parseInt(random(0+PLANET_BUFFER2, width-PLANET_BUFFER2));
    y = PApplet.parseInt(random(0+PLANET_BUFFER2, height-PLANET_BUFFER2));
    diameter = PApplet.parseInt(random(Planet.MIN_DIAMETER, Planet.MAX_DIAMETER));

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

public void makeBlackhole() {
  boolean valid = false;
  int x = 0;
  int y = 0;
  int diameter = 150;

  //int count = 0;

  while (!valid) {

    x = PApplet.parseInt(random(0+PLANET_BUFFER2, width-PLANET_BUFFER2));
    y = PApplet.parseInt(random(0+PLANET_BUFFER2, height-PLANET_BUFFER2));

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
public boolean bodiesOverlap(Planet planet, int x, int y, int diameter, float buffer) {
  PVector newPos = new PVector(x, y);
  float minDist = (PApplet.parseFloat(diameter)/2 + PApplet.parseFloat(planet.diameter)/2) + buffer;

  return newPos.dist(planet.position) < minDist;
}
class Background {

  static final int STAR_NO = 50;

  ArrayList<Star> stars = new ArrayList<Star>();

  Background() {
    for (int i = 0; i < STAR_NO; i++) {
      stars.add(new Star());
    }
  }

  public void draw() {
    background(25, 24, 29);

    for (Star star : stars) {
      star.draw();
    }
  }
}
class Ball {
  //treated as a point mass

  // damping factor to simulate inelastic collision / impulse w planet
  static final float WALL_DAMPING = 0.75f; //0.75
  static final float DAMPING = 0.5f; // 0.5
  static final float FRICTION = 0.92f; // 0.9 then 0.92 then 0.95 then 0.92
  static final int DIAMETER = 10; //10
  //static final float GRAV_CONST = 6.673 * pow(1, -11);
  static final float FRACTION = 0.25f; //was 0.1, 0.2 0.5, then 0.25
  static final float MIN_VELOCITY = 0.1f;
  static final float MAX_VELOCITY = 1000000000;
  static final int TRAIL_NO = 20;
  static final int MIN_COLLISION_TIME = 100; // min time allowed between bounce noise being played
  static final float SURFACE_MAX_DISTANCE = 2; // the max distance value allowed between ball and planet that still classifies it as being on the planet

  // could have boolean for ball being at rest
  //boolean atRest = false;
  boolean holed = false; // helps w handling of when ball was in hole previously but is now out of hole

  int collisionTime; //time in millis of last bounce noise

  PVector position, velocity, acceleration;
  ArrayList<PVector> trail = new ArrayList<PVector>();

  float prevVelocity = MAX_VELOCITY;
  Planet prevPlanet = null;

  // Otherwise do something more interesting
  Ball(float x, float y, float xVel, float yVel, float xAcc, float yAcc) {
    position = new PVector(x, y);
    velocity = new PVector(xVel, yVel);
    acceleration = new PVector(xAcc, yAcc);
    collisionTime = millis();
  }

  // update position and velocity
  public void integrate(ArrayList<Planet> planets, CelestialGolf main) {


    // Updating trail

    if (trail.size() >= TRAIL_NO) {
      trail.remove(0);
    }
    trail.add(position.copy());



    // Calculate new acceleration, velcoity, position - sum ll accelerations / forces from all planets

    acceleration.set(0f, 0f);

    if (velocity.mag() > 0) {

      for (Planet planet : planets) {

        // relative mass of planet - volume = 4/3 * PI * radius^3
        float m = pow((PApplet.parseFloat(planet.diameter)/2), 2);
        //changed to squared rather than true mass of cubed to slightly reduced emphasis of bigger planets - so not really using mass, using relative to circle area

        // distance from planet
        float r = position.dist(planet.position);
        // magnitude of acceleration vector
        float aMag = m / pow(r, 2); // changed from true value of 2 to 3 to put more emphasis on closer planets

        // finding normal of vector from ball to planet centre
        PVector a = PVector.sub(planet.position, position);
        a.normalize();

        //finding acceleration vector, applying constant to scale down realtively to work in my game
        a.mult(aMag*FRACTION);

        // Only applying a of planet bal is on (to prevent weird rolling effect)
        if (distanceBetween(planet) < SURFACE_MAX_DISTANCE) {
          acceleration.set(a);
          break;
        }

        //adding to total sum acceleration
        acceleration.add(a);

      }

      if (main.blackHole != null) {
        // relative mass of planet - volume = 4/3 * PI * radius^3
        float m = pow((150/2), 2);
        //changed to squared rather than true mass of cubed to slightly reduced emphasis of bigger planets - so not really using mass, using relative to circle area

        // distance from planet
        float r = position.dist(main.blackHole.position);
        // magnitude of acceleration vector
        float aMag = m / pow(r, 2); // changed from true value of 2 to 3 to put more emphasis on closer planets

        // finding normal of vector from ball to planet centre
        PVector a = PVector.sub(main.blackHole.position, position);
        a.normalize();

        //finding acceleration vector, applying constant to scale down realtively to work in my game
        a.mult(aMag*FRACTION);
        acceleration.add(a);
      }

      //velocity.add(acceleration);
      position.add(velocity);
      velocity.add(acceleration);
    }


    // collision detection w black hole, powers

    TeleportPower tele = main.teleport;
    if (!tele.used && tele.orbit.get(tele.index-1).dist(position) <= (DIAMETER + TeleportPower.DIAMETER)/2f) {
      tele.used = true;



      // get position near green
      float offset = 15;
      PVector centre = new PVector(width/2, height/2);
      PVector initPos = PVector.sub(centre, main.green.position);
      float dist = main.green.diameter/2 + offset;
      initPos.normalize();
      initPos.mult(dist);
      initPos.add(main.green.position);

      ball = new Ball(initPos.x, initPos.y, 0.11f, 0.11f, 0f, 0f);
    }


    ExtraShotsPower power = main.extraShots;
    if (!power.used && power.orbit.get(power.index-1).dist(position) <= (DIAMETER + ExtraShotsPower.DIAMETER)/2f) {
      power.used = true;
      main.score -= 3;
    }


    if (main.blackHole != null && main.blackHole.position.dist(position) <= (DIAMETER + BlackHole.DIAMETER)/2f) {
      main.shots++;

      boolean valid = false;
      int x = 0;
      int y = 0;

      while (!valid) {

        x = PApplet.parseInt(random(width));
        y = PApplet.parseInt(random(height));

        valid = true;

        for (Planet planet : planets) {
          if (main.bodiesOverlap(planet, x, y, Ball.DIAMETER, 0) || new PVector(x, y).dist(main.blackHole.position) <= 80) {
            valid = false;
          }
        }

      }

      ball = new Ball(x, y, velocity.x, velocity.y, 0f, 0f);
    }

    // Fix position if inside planet - picks closest point but really should do interplation on past (trail) position

    for (Planet planet : planets) {

      if (distanceBetween(planet) < 0 && !ballInHole(planet)) {

        PVector radius = PVector.sub(trail.get(trail.size()-1), planet.position);
        radius.normalize();
        radius.mult(PApplet.parseFloat(planet.diameter)/2 + PApplet.parseFloat(DIAMETER)/2);

        PVector point = PVector.add(radius, planet.position);
        ball.position = point;

        // aply damping to mimic speed being killed when going in hole but coming back out?
        // only do above if detected to be in hole
        // could also detect if in hole

      }
    }



    // Bounce off the edge of the screen (impulse - inelastic collision) - ensure going in reverse direction and plays bounce sound

    float radius = PApplet.parseFloat(DIAMETER)/2;
    if (velocity.mag() > 0.1f && (position.x <= 0 + radius || position.x >= width - radius || position.y <= 0 + radius || position.y >= height - radius)) {

      int time = millis();
      if ((millis() - collisionTime) > MIN_COLLISION_TIME) {
        main.bounce.play();
        collisionTime = time;
      }

      if (position.x <= 0 + radius) {
        velocity.x = abs(velocity.x) * DAMPING;
        position.x = 0 + radius;
      }
      else if (position.x >= width - radius) {
        velocity.x = -abs(velocity.x) * DAMPING;
        position.x = width - radius;
      }

      if (position.y <= 0 + radius) {
        velocity.y = abs(velocity.y) * DAMPING;
        position.y = 0 + radius;
      }
      else if (position.y >= height - radius) {
        velocity.y = -abs(velocity.y) * DAMPING;
        position.y = height - radius;
      }

      //velocity.mult(WALL_DAMPING);

    }





    // Planet collision detection and impulse - apply impulse to bounce off planet - inelastic collision / friction
    // Also detects rolling - do I need to differentiate or treat them the same?


      for (Planet planet : planets) {


        // Collision impulse
        // if I am not manually setting ball to rest, then I may have to check for a velcity value just above 0 rather than 0
        if (velocity.mag() > 0f && distanceBetween(planet) < 0.05f && !ballInHole(planet)/* && !inHole*/) { //0.05

          // get speed (magnitude of velocity)
          float magnitude = velocity.mag();

          // nornalised incidence vector
          PVector incidence = PVector.mult(velocity, -1);
          incidence.normalize();

          //calculate normal (normalised vector in direction of planet centre to collision point)
          PVector normal = PVector.sub(position, planet.position);
          normal.normalize();

          // calculate dot product of incident vector and normal
          float dot = incidence.dot(normal);

          // calculate reflection vector
          // assign reflection vector to direction vector and apply intial speed but dampened
          velocity.set(2*normal.x*dot - incidence.x, 2*normal.y*dot - incidence.y, 0);
          velocity.mult(magnitude); // did also apply damping here previously


          // only apply to upwards normal component

          float angle = PVector.angleBetween(velocity, normal);

          // 'y' component (relative to planet impact point)
          float yMag = velocity.mag() * cos(angle);
          PVector yVelocity = PVector.mult(normal, yMag);

          // 'x' / tangent component
          PVector xVelocity = PVector.sub(velocity, yVelocity);

          // apply resistance forces
          yVelocity.mult(DAMPING);
          xVelocity.mult(FRICTION);

          velocity = PVector.add(yVelocity, xVelocity);




          // play bounce sound on collision
          // need to detect collision vs rolling if so (using x/tangent y components)
          int time = millis();
          if (yVelocity.mag() > 0.5f && (time - collisionTime) > MIN_COLLISION_TIME) {
            main.bounce.play();
            collisionTime = time;
          }







          // Rest - detecting / enforcing ball to come to rest (otherwise it bounces forever)

          // either compare to previous position - if distance below v small value then at rest.
          // or calculate tangent velocity and also if below small threshold? - will also need y velcoity

          if (velocity.mag() < 0.5f) {
            velocity.set(0f, 0f);
          }

          /*
          if (xVelocity.mag() < 0.2) {
            velocity.set(0f, 0f);
          } */

          /*
          if (position.dist(trail.get(trail.size()-1)) < 0.1) {
            velocity.set(0f, 0f);
          }*/

          // can exit now as not possible for ball to collide with more than 1 planet at once
          break;
        }




        // Ball in hole????

        else if (ballInHole(planet)) {

          if (bottomOfHole(planet)) {
            position.set(planet.hole.copy());
            velocity.set(0f, 0f);

            holed = true;
          }

        }

        // ball was in hole in previous frame but is not any more
        // impart impulse
        /*
        else if (inHole) {
          position = trail.get(trail.size()-1); //what if this prev position not in hole either???? - need to guarantee, possible glitches, could respawn in centre of hole??? as hacky fix
          //velocity.mult(-1);
          //velocity.mult(DAMPING);

          //if (velocity.mag() < 0.5) {
            velocity.set(0f, 0f);
          //}
        }*/
        // need to remember to include possibility of ball not being in hole at some point again ?!?!?!?

      }

  }

  public boolean ballInHole(Planet planet) {
    //uses updated position before drawn to screen for this method
    //no need to check for ball being on surface or below as done before this method called?

    if (!planet.green) return false;

    if (distanceBetween(planet) > 0f) return false;

    return (position.dist(green.hole) - (PApplet.parseFloat(DIAMETER)/2 + PApplet.parseFloat(green.HOLE_WIDTH)/2) < 0);
  }

  public boolean bottomOfHole(Planet green) {
    // if below halfway down hole
    float distanceFromSurface = PApplet.parseFloat(green.diameter/2) - position.dist(green.position);
    return (distanceFromSurface > PApplet.parseFloat(Planet.HOLE_WIDTH)/2);
  }

  public void draw() {

    noStroke();

    fill(255);
    ellipseMode(CENTER);
    circle(position.x, position.y, DIAMETER);

    // Trail - only drawn if velocity above certain value

    for (PVector pos : trail) {
      fill(255, 75);
      circle(pos.x, pos.y, DIAMETER);
    }
  }

  public float distanceBetween(Planet planet) {
    return (planet.position.dist(position) - PApplet.parseFloat(planet.diameter)/2 - PApplet.parseFloat(DIAMETER)/2);
  }



}
class BlackHole {

  //static constants
  static final int DIAMETER = 150;
  static final int MAX = 150;

  // attributes
  PVector position;

  BlackHole(int x, int y) {
    position = new PVector(x, y);
  }

  public void draw() {
    stroke(255);
    strokeWeight(1);
    ellipseMode(CENTER);
    fill(0);
    circle(position.x, position.y, DIAMETER);
  }

}
class ExtraShotsPower {

  // static constants
  static final int ORBIT_HEIGHT = 25;
  static final int POSITION_NO = 600; // number of position Pectors that make up the orbit, the more the slower the orbit
  static final float DIAMETER = 15; //15

  // attributes
  //Planet planet;
  int orbitRadius;
  float angle; //in radians angle inbetween each succesive orbit point
  ArrayList<PVector> orbit = new ArrayList<PVector>(); // PVector positions that make up circumference of orbit
  int index;
  boolean used = false;

  ExtraShotsPower(Planet planet) {
    //this.planet = planet;
    orbitRadius = planet.diameter/2 + ORBIT_HEIGHT;
    index = 0;
    angle = TWO_PI / POSITION_NO;

    for (int i = 0; i < POSITION_NO; i++) {
      PVector position = PVector.fromAngle(i * angle * -1); // - 1 reverses direction
      position.mult(orbitRadius);
      position.add(planet.position);
      orbit.add(position);
    }
  }

  public void draw() {
    if (!used) {  
      if (index >= orbit.size()) index = 0;

      noStroke();
      fill(255,215,0);
      ellipseMode(CENTER);
      PVector position = orbit.get(index);
      circle(position.x, position.y, DIAMETER);

      index++;
    }
  }

}
class Planet {

  static final int MIN_DIAMETER = 150; //150
  static final int MAX_DIAMETER = 275; //350 then 250 then 275
  static final int MIN_RGB_VALUE = 137; //125, then 150

  static final float FLAG_STICK_LEN = 35;
  static final int STICK_WIDTH = 4;
  static final float FLAG_HEIGHT = 14;
  static final float FLAG_WIDTH = 18;
  static final int HOLE_WIDTH = 23;
  static final float HOLE_FRACTION = 0.925f;
  static final float HOLE_OFFSET = 8; //distance hole is down from circumference of planet

  // potential for moving planets later so velocity and acceleration needed?!?!?
  PVector position;
  int diameter;
  int col;

  // Flag/green attributes
  boolean green; //green means it has the flag, i.e. green in golf being the area of the hole with the flag/hole on it
  float flagAngle;
  PVector flagTip; //actually not tip of flag, tip of stick!
  PVector flagBase;
  PVector hole;

  Planet(int diameter, int x, int y) {
    position = new PVector(x, y);
    this.diameter = diameter;
    col = color(PApplet.parseInt(random(MIN_RGB_VALUE, 255)), PApplet.parseInt(random(MIN_RGB_VALUE, 255)), PApplet.parseInt(random(MIN_RGB_VALUE, 255)));
    green = false;
  }

  public void draw() {

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

  public void drawFlag() {

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
  public void makeGreen() {
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

  public boolean validFlag(float angle) {
    PVector tip = PVector.fromAngle(angle);
    PVector base = tip.copy();

    tip.mult(PApplet.parseFloat(diameter)/2 + FLAG_STICK_LEN); //flag vector
    tip.add(position.copy());
    flagTip = tip;

    base.mult(PApplet.parseFloat(diameter)/2);
    base.add(position.copy());
    flagBase = base;

    return inScreen(tip);
  }

  public boolean inScreen(PVector pos) {
    return (pos.x < width && pos.x > 0 && pos.y < height && pos.y > 0);
  }

}
class Star {
  // simple object to hold a star in the backgrounds position and diamter (perhaps colour later on)

  //could perhaps have stars of different colours, or slightly change in size, or in colour

  //could have star be actual star shape?!!??!!!?!?!!

  //stars in actual background of inspo is faded around the edges - anyway to replicate this??
  // add slight opacity??

  static final float MIN_DIAMETER = 1;
  static final float MAX_DIAMETER = 9; //originally 8
  static final float BORDER_WIDTH = 0.5f;
  static final int TWINKLE_ODDS = 400;

  int diameter;
  PVector position;

  Star() {
    position = new PVector(random(width), random(height));
    diameter = PApplet.parseInt(random(MIN_DIAMETER, MAX_DIAMETER));
  }

  public void draw() {
    stroke(153, 153, 157);
    strokeWeight(BORDER_WIDTH);
    ellipseMode(CENTER);
    fill(228, 227, 232);
    //if (int(random(7)) == 2) stroke(215, 215, 215);

    int prevD = diameter;
    if (PApplet.parseInt(random(TWINKLE_ODDS)) == 1) diameter+= 4;

    circle(position.x, position.y, diameter);
    diameter = prevD;
  }
}
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

  public void draw() {
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
  public void settings() {  size(1500, 1000); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CelestialGolf" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

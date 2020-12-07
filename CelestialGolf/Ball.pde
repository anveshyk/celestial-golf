class Ball {
  //treated as a point mass

  // damping factor to simulate inelastic collision / impulse w planet
  static final float WALL_DAMPING = 0.75; //0.75
  static final float DAMPING = 0.5; // 0.5
  static final float FRICTION = 0.92; // 0.9 then 0.92 then 0.95 then 0.92
  static final int DIAMETER = 10; //10
  //static final float GRAV_CONST = 6.673 * pow(1, -11);
  static final float FRACTION = 0.25; //was 0.1, 0.2 0.5, then 0.25
  static final float MIN_VELOCITY = 0.1;
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
  void integrate(ArrayList<Planet> planets, CelestialGolf main) {


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
        float m = pow((float(planet.diameter)/2), 2);
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

        x = int(random(width));
        y = int(random(height));

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
        radius.mult(float(planet.diameter)/2 + float(DIAMETER)/2);

        PVector point = PVector.add(radius, planet.position);
        ball.position = point;

        // aply damping to mimic speed being killed when going in hole but coming back out?
        // only do above if detected to be in hole
        // could also detect if in hole

      }
    }



    // Bounce off the edge of the screen (impulse - inelastic collision) - ensure going in reverse direction and plays bounce sound

    float radius = float(DIAMETER)/2;
    if (velocity.mag() > 0.1 && (position.x <= 0 + radius || position.x >= width - radius || position.y <= 0 + radius || position.y >= height - radius)) {

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
        if (velocity.mag() > 0f && distanceBetween(planet) < 0.05 && !ballInHole(planet)/* && !inHole*/) { //0.05

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
          if (yVelocity.mag() > 0.5 && (time - collisionTime) > MIN_COLLISION_TIME) {
            main.bounce.play();
            collisionTime = time;
          }







          // Rest - detecting / enforcing ball to come to rest (otherwise it bounces forever)

          // either compare to previous position - if distance below v small value then at rest.
          // or calculate tangent velocity and also if below small threshold? - will also need y velcoity

          if (velocity.mag() < 0.5) {
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

  boolean ballInHole(Planet planet) {
    //uses updated position before drawn to screen for this method
    //no need to check for ball being on surface or below as done before this method called?

    if (!planet.green) return false;

    if (distanceBetween(planet) > 0f) return false;

    return (position.dist(green.hole) - (float(DIAMETER)/2 + float(green.HOLE_WIDTH)/2) < 0);
  }

  boolean bottomOfHole(Planet green) {
    // if below halfway down hole
    float distanceFromSurface = float(green.diameter/2) - position.dist(green.position);
    return (distanceFromSurface > float(Planet.HOLE_WIDTH)/2);
  }

  void draw() {

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

  float distanceBetween(Planet planet) {
    return (planet.position.dist(position) - float(planet.diameter)/2 - float(DIAMETER)/2);
  }



}

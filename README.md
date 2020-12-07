# celestial-golf
2D golf game set in space made with Processing Java. Each level is procedurally generated, and your shots are affected by each planet's gravity.

![alt text](https://github.com/[username]/[reponame]/blob/[branch]/image.jpg?raw=true)

RUN INSTRUCTIONS

- download Processing from https://processing.org/download/
- in Processing, open the folder CelestialGolf/
- Sketch tab -> Import Library -> Add Library -> search "sound" -> install "Sound - The Processing Foundation"
- select Play button (top left corner)

HOW TO PLAY

- golf ball spawns on random planet, within a randomly generated course/planet layout
- goal is to get the ball into the cup (which is marked by a flag and located on a different planet) in the least number of shots
- infinite number of holes/rounds
- drag mouse to adjust power
- ball's flight through space is affected by gravity of all planets, those bigger and closer will have more of an effect

OBSTACLES & POWER UPS

- black hole teleports you to a random location and costs an extra shot
- crescent moon teleports you to the planet with the flag
- yellow power up subtracts a shot ("mulligan")

NOTES

- window size (1500x1000) unfortunately not adjustable unless you alter it in the code yourself

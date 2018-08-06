package com.giantrainbow.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.parameter.CompoundParameter;
import java.util.Random;

@LXCategory(LXCategory.FORM)
public class SpinnyDisks extends CanvasPattern2D {
  public final CompoundParameter speedKnob =
      new CompoundParameter("Speed", 1, 20).setDescription("Speed.");

  Ball balls[];
  double elapsed;

  public SpinnyDisks(LX lx) {
    super(lx);

    this.elapsed = 0;
    this.balls = new Ball[100];

    Random rnd = new Random();

    for (int i = 0; i < balls.length; i++) {
      balls[i] = new Ball();
      balls[i].X = lx.model.xMin + (lx.model.xMax - lx.model.xMin) * rnd.nextFloat();
      balls[i].Y = lx.model.yMin + (lx.model.yMax - lx.model.yMin) * rnd.nextFloat();
      balls[i].R = 10 * rnd.nextFloat();
      balls[i].S = rnd.nextFloat();
    }

    addParameter(speedKnob);
    speedKnob.setValue(5);
  }

  public void draw(double deltaMs) {
    elapsed += deltaMs;
    for (Ball ball : balls) {
      ball.draw();
    }
  }

  public class Ball {
    float X;
    float Y;
    float R;
    float S;

    void draw() {
      float position = (float) (speedKnob.getValue() * S * elapsed / 10000);
      canvas.circle(X, Y, R, position);
    }
  };
}

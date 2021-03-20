#version 150

#define SAMPLER0 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER1 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER2 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER3 sampler2D // sampler2D, sampler3D, samplerCube

uniform SAMPLER0 iChannel0; // image/buffer/sound    Sampler for input textures 0
uniform SAMPLER1 iChannel1; // image/buffer/sound    Sampler for input textures 1
uniform SAMPLER2 iChannel2; // image/buffer/sound    Sampler for input textures 2
uniform SAMPLER3 iChannel3; // image/buffer/sound    Sampler for input textures 3

uniform vec3  iResolution;           // image/buffer          The viewport resolution (z is pixel aspect ratio, usually 1.0)
uniform float iTime;                 // image/sound/buffer    Current time in seconds
uniform float iTimeDelta;            // image/buffer          Time it takes to render a frame, in seconds
uniform int   iFrame;                // image/buffer          Current frame
uniform float iFrameRate;            // image/buffer          Number of frames rendered per second
uniform vec4  iMouse;                // image/buffer          xy = current pixel coords (if LMB is down). zw = click pixel
uniform vec4  iDate;                 // image/buffer/sound    Year, month, day, time in seconds in .xyzw
uniform float iSampleRate;           // image/buffer/sound    The sound sample rate (typically 44100)
uniform float iChannelTime[4];       // image/buffer          Time for channel (if video or sound), in seconds
uniform vec3  iChannelResolution[4]; // image/buffer/sound    Input texture resolution for each channel
uniform vec4  U1;
uniform vec4  U2;
#ifdef GL_ES
precision mediump float;
#endif


#define ITER 128

float iDuration = iMouse.x * 10.;
float lTime = 0.;


vec3 gradient(in float r) {	
    r = 4.0 * tanh(0.1 * r); // 0.1
    vec3 rainbow = 0.06 - 0.5 * cos(r + vec3(1.229*U1.x, U1.y*0.365, U1.z*3.707)); //vec3(4.071, -0.630, 0.356));  // 0.5
    return rainbow;
}

vec4 fractal(vec2 z, vec2 c) {
  // Computes colouring at point z of the Julia set with parameter c.
  for (int i = 0; i < ITER; ++i) {
    z = vec2(
      z.x*z.x - z.y*z.y + c.x,
      2.0 * z.x*z.y + c.y
    );
    float distSqr = dot(z, z);
    if (distSqr > 16.0) {
      return vec4(gradient(float(i) + 1.0 - log2(log(distSqr) / 2.0)), 1.);
    }
  }
  return vec4(0.0, 0.0, 0.0, 1.0);
}

vec2 cardioid(in float t) {
  // Computes the path around the main cardioid of the Mandelbrot set.
  t = 6.2831853 * lTime / iDuration;
  vec2 c = vec2(-cos(t), -sin(t));
  c *= (5.*iMouse.z + cos(t)) / (5. * iMouse.y);
  c.x += 0.2499;
  return c;
}


void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    vec2 uv = fragCoord / iResolution.xy;
    uv = 2.0 * (uv - 0.5);
    float gTime = mod(iTime, 20.);
    lTime = mod(gTime, 10.);

    if (gTime > 10.) {
      lTime = 20. - gTime;
    }

    vec2 c = cardioid(lTime);
    fragColor = fractal(uv, c);
}
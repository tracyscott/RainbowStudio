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

#ifdef GL_ES
precision mediump float;
#endif

#define M_PI 3.1415926

 /* Simplex Noise
 * The MIT License
 * Copyright © 2013 Nikita Miropolskiy
 * https://www.shadertoy.com/view/XsX3zB
 */

/* discontinuous pseudorandom uniformly distributed in [-0.5, +0.5]^3 */
vec3 random3(vec3 c) {
  float j = 4096.0*sin(dot(c,vec3(17.0, 59.4, 15.0)));
  vec3 r;
  r.z = fract(512.0*j);
  j *= .125;
  r.x = fract(512.0*j);
  j *= .125;
  r.y = fract(512.0*j);
  return r-0.5;
}

/* skew constants for 3d simplex functions */
const float F3 =  0.3333333;
const float G3 =  0.1666667;

/* 3d simplex noise */
float simplex3d(vec3 p) {
  /* 1. find current tetrahedron T and it's four vertices */
  /* s, s+i1, s+i2, s+1.0 - absolute skewed (integer) coordinates of T vertices */
  /* x, x1, x2, x3 - unskewed coordinates of p relative to each of T vertices*/

  /* calculate s and x */
  vec3 s = floor(p + dot(p, vec3(F3)));
  vec3 x = p - s + dot(s, vec3(G3));

  /* calculate i1 and i2 */
  vec3 e = step(vec3(0.0), x - x.yzx);
  vec3 i1 = e*(1.0 - e.zxy);
  vec3 i2 = 1.0 - e.zxy*(1.0 - e);

  /* x1, x2, x3 */
  vec3 x1 = x - i1 + G3;
  vec3 x2 = x - i2 + 2.0*G3;
  vec3 x3 = x - 1.0 + 3.0*G3;

  /* 2. find four surflets and store them in d */
  vec4 w, d;

  /* calculate surflet weights */
  w.x = dot(x, x);
  w.y = dot(x1, x1);
  w.z = dot(x2, x2);
  w.w = dot(x3, x3);

  /* w fades from 0.6 at the center of the surflet to 0.0 at the margin */
  w = max(0.6 - w, 0.0);

  /* calculate surflet components */
  d.x = dot(random3(s), x);
  d.y = dot(random3(s + i1), x1);
  d.z = dot(random3(s + i2), x2);
  d.w = dot(random3(s + 1.0), x3);

  /* multiply d by w^4 */
  w *= w;
  w *= w;
  d *= w;

  /* 3. return the sum of the four surflets */
  return dot(d, vec4(52.0));
}

float simplex3d_fractal(vec3 m) {
  return   0.5333333*simplex3d(m) 
          +0.2666667*simplex3d(2.0*m)
          +0.1333333*simplex3d(4.0*m)
          +0.0666667*simplex3d(8.0*m);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  vec2 uv = fragCoord.xy / iResolution.xy;
  uv = uv * 2. -1.;
  //uv.y = uv.y * 4. - 2.;
  vec2 p = fragCoord.xy/iResolution.x;
  vec3 p3 = vec3(p, iTime*iMouse.w);

  float intensity = 10.0 * iMouse.y * simplex3d_fractal(vec3(p3*12.0+12.0));

  float t = clamp((uv.x * -uv.x * 0.16) + 0.15, 0., 1.);
  float y = abs(intensity * -t + uv.y);

  float g = pow(y, 0.8 * iMouse.z);

  // TODO(tracy): Set this numbers based on hue passed in iMouse.x
  vec3 col = vec3(2.70, 1.8, 1.2); 
  col = vec3(1.70, 1.48, 1.78);
  col = col * -g + col;
  col = col * col;
  col = col * col;

  float thickness = 40.0 * iMouse.x;
  col *= thickness;

  fragColor.rgb = col;
  fragColor.w = 1.;
}

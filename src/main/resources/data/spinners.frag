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

// From Book of Shaders website.

#define PI 3.14159265358979323846
#define TWO_PI 6.28318530717958647693


vec2 rotate2D (vec2 _st, float _angle) {
  _st -= 0.5;
  _st =  mat2(cos(_angle),-sin(_angle),
              sin(_angle),cos(_angle)) * _st;
  _st += 0.5;
  return _st;
}

vec2 tile (vec2 _st, float _zoom) {
  _st *= _zoom;
  return fract(_st);
}

vec2 rotateTile(vec2 _st){
    _st *= 2.0;

    float index = 0.0;
    if (fract(_st.x * 0.5) > 0.5){
        index += 1.0;
    }
    if (fract(_st.y * 0.5) > 0.5){
        index += 2.0;
    }

    _st = fract(_st);

    if(index == 1.0){
        _st = rotate2D(_st,PI*0.5);
    } else if(index == 2.0){
        _st = rotate2D(_st,PI*-0.5);
    } else if(index == 3.0){
        _st = rotate2D(_st,PI);
    }

    return _st;
}

// Based on https://www.shadertoy.com/view/4sSSzG
float triangle (vec2 _st,
                vec2 _p0, vec2 _p1, vec2 _p2,
                float _smoothness) {
  vec3 e0, e1, e2;

  e0.xy = normalize(_p1 - _p0).yx * vec2(+1.0, -1.0);
  e1.xy = normalize(_p2 - _p1).yx * vec2(+1.0, -1.0);
  e2.xy = normalize(_p0 - _p2).yx * vec2(+1.0, -1.0);

  e0.z = dot(e0.xy, _p0) - _smoothness;
  e1.z = dot(e1.xy, _p1) - _smoothness;
  e2.z = dot(e2.xy, _p2) - _smoothness;

  float a = max(0.0, dot(e0.xy, _st) - e0.z);
  float b = max(0.0, dot(e1.xy, _st) - e1.z);
  float c = max(0.0, dot(e2.xy, _st) - e2.z);

  return smoothstep(_smoothness * 2.0,
                    1e-7,
                    length(vec3(a, b, c)));
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    vec2 st = fragCoord/iResolution.xy;

    st = tile(st,4.0);
    //st = rotateTile(st);

    float pattern = 0.0;

    st = rotate2D(st,-PI*iTime*iMouse.z * 2.0);
    float audioHz = iMouse.x;
    float audioMultiply = iMouse.y * 16.0;
    float yPertubation = audioMultiply * texture (iChannel0, vec2 (audioHz)).x;

    pattern =   triangle(st,
                         vec2(0.620,0.500+yPertubation),
                         vec2(0.890,0.380-yPertubation),
                         vec2(0.570,0.820+yPertubation),
                         0.001);

    vec3 color = vec3(pattern);

    fragColor = vec4(color,1.0);
}

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
// Author @patriciogv - 2015
// Title: Matrix

float random(in float x){ return fract(sin(x)*43758.5453); }
float random(in vec2 st){ return fract(sin(dot(st.xy ,vec2(12.9898,78.233))) * 43758.5453); }

float randomChar(vec2 outer,vec2 inner){
    float grid = 5.;
    vec2 margin = vec2(-0.210,-0.260);
    vec2 borders = step(margin,inner)*step(margin,1.000-inner);
    vec2 ipos = floor(inner*grid);
    vec2 fpos = fract(inner*grid);
    return step(0.900,random(outer*62.024+ipos)) * borders.x * borders.y * step(0.01,fpos.x) * step(0.01,fpos.y);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    vec2 st = fragCoord.st/iResolution.xy;
    vec2 uv = fragCoord/iResolution.xy;
    st.y *= iResolution.y/iResolution.x;
    vec3 color = vec3(0.0);

    float rows = 20.0 * iMouse.x;
    // rows = 3.0;
    // rows = 12.0;
    // rows = 24.0;
    vec2 ipos = floor(st*rows);
    vec2 fpos = fract(st*rows);

    ipos += vec2(0.,floor(iTime*20.*iMouse.y*random(ipos.x+1.072)));

    float pct = 1.0;
    pct *= randomChar(ipos,fpos);
    // pct *= random(ipos);

    color = vec3(pct);
    // Multiply the flag color.
    if (uv.y >= 0.0 && uv.y <=1./6.) {
      color *= vec3(0.458,0.0274,0.529);
    } else if (uv.y <= 2./6.) {
      color *= vec3(0.0, 0.302, 1.0);
    } else if (uv.y <= 3./6.) {
      color *= vec3(0.0, 0.502, 0.149);
    } else if (uv.y <= 4./6.) {
      color *= vec3(1.0, 0.929, 0.0);
    } else if (uv.y <= 5./6.) {
      color *= vec3(1.0, 0.549, 0.0);
    } else if (uv.y <= 1.0) {
      color *= vec3(0.894, 0.0118, 0.0118);
    }

    fragColor = vec4( color , 1.0);
}


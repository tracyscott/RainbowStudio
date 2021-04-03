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

// iMouse.w 0.3
// U1.x 0.2

float distFromMiddle(float n){
    return abs(.5-n);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;

    // move image right, flip left horizontally
    if (uv.x < .5) {
      uv.x = -uv.x+.5;
    } else {
      uv.x = uv.x-.5;
    }

   // Time varying pixel color
   float t = iTime/5.;
   vec3 col = vec3(iMouse.x,
                   iMouse.y,
                   iMouse.z);

    // Apply FFT
    float s = texture(iChannel3, (uv.xy*.06)).r;
    col *= s/(iMouse.w * distFromMiddle(uv.y));
    col *= U1.x;

    // Output to screen
   fragColor = vec4(col, 1.);
}

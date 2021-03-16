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

// Based on example from:
// Created by inigo quilez - iq/2014
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.

// Modified for RainbowStudio audio visuals.

// iMouse.x = Block size
// iMouse.y = Block frequency
// iMouse.z = Blinky frequency
// iMouse.w = Color shift frequency.
// U1.x = Block fill ratio
// U1.y = Smoothstep cut off, block sharpness. can turn blocks into small color spheres with low U1.x
void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2  px = 16.0*iMouse.x*(-iResolution.xy + 8.0*iMouse.y*fragCoord.xy) / iResolution.y;
    
    float id = 0.5 + 0.5*cos(iTime*20.*iMouse.z + sin(dot(floor(px+0.5),vec2(113.1,17.81)))*43758.545);
    
    vec3  co = 0.5 + 0.5*cos(iTime*20.*iMouse.w + 3.5*id + vec3(0.0,1.57,3.14) );
    
    vec2  pa = smoothstep( 0.0, U1.y * 2., id*(0.5 + 5.0 * U1.x *cos(6.2831*px)) );  // was 0.2, so 0.1 U1.y nominal
    
    fragColor = vec4( co*pa.x*pa.y, 1.0 );
}
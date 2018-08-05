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

float hz(float hz)
{
    float u = hz/11000.0;
    return texture(iChannel0,vec2(u,0.25)).x;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord.xy / iResolution.xy;

    float v1 = 0.02 + 0.4*hz(100.0);
    float v2 = 0.02 + 0.4*hz(500.0);
    float v3 = 0.02 + 0.4*hz(1000.0);
    float v4 = 0.02 + 0.4*hz(2000.0);
    uv.x = 4 * 3.14 * uv.x;

    vec3 col = vec3(0.0, 0.0, 0.0);
    float v1y = 1.0 - uv.y - 1.0 + 2.2 * v1;
    float v2y = 1.0 - uv.y - 1.0 + 2.2 * v2;
    float v3y = 1.0 - uv.y - 1.0 + 2.2 * v3;
    float v4y = 1.0 - uv.y - 1.0 + 2.2 * v4;
    float width = iMouse.x*4.0 * 0.036;
    float colorIntensity = iMouse.z;
    col += vec3(colorIntensity,0.0,colorIntensity) * abs(width/v1y) * v1;
    col += vec3(colorIntensity,colorIntensity,0.0) * abs(width/v2y) * v2;
    col += vec3(0.0,colorIntensity, colorIntensity) * abs(width/v3y) * v3;
    col += vec3(0.0,0.0,colorIntensity) * abs(width/v4y) * v4;

    float uvy2 = 0.4*iTime-uv.y;
    float kickHz = 70.0;
    float kickHzVal = hz(kickHz);
    float kickMultiplier = 16.0 * iMouse.y * kickHzVal * kickHzVal;
    float a1 = 10.0*kickHzVal *
        sin(50.0*uv.x)*cos(50.0*uvy2)/((10.0 / kickMultiplier) *cos(3.14 * uv.x));
    col += vec3(1.0,0.0,0.0) * a1 * 0.1;

    fragColor = vec4(col,1.0);
}

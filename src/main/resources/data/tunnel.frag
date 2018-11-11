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
uniform vec4  iKnobs;                // More parameters from LX Studio.

void mainImage( out vec4 fragColor, in vec2 pt )
{
    float timeBump = iMouse.x;
    float timeScale = 10.0 * iMouse.x;
    float imgScale = iMouse.y * 6.0;
    float coordScale = iMouse.z * 0.5;
    pt = coordScale*(pt.xy / iResolution.xy - 0.5)*vec2(iResolution.x/iResolution.y,1);
    float yScale = iMouse.w;
    pt.y = pt.y * 4.0 * yScale;
    float rInv = 1./(imgScale * abs(pt.x) + abs(pt.y)); //(3.0*length(pt)); // (100.0*abs(pt.y)); //(3.0*length(pt)); // length(pt);
    float xBrightness = 1.0 * (1.0 + abs(pt.x));
    /* Add a darkened diamond at the top to minimize oversaturation, good for fractal textures.
    if (2.0 * abs(pt.x) + 1.0 * abs(pt.y) < 0.6) {
        xBrightness = 0.1 + abs(pt.x);
    }
    */
    //pt = pt * rInv - vec2(rInv + (iTime+timeBump)*2.0,0.5);
    //pt = pt * rInv - vec2(rInv + (iTime*timeScale)*2.0,0.5);
    pt = pt * rInv - vec2(rInv + (timeScale)*2.0,0.5);
    fragColor = 6.0* xBrightness * texture(iChannel1,pt)*rInv/2.;
}

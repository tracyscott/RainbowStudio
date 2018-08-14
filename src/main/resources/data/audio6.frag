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

void mainImage( out vec4 o,  vec2 i )
{
    vec3 r=vec3(1,0,0);
    vec3 y=vec3(1,1,0);
    vec3 g=vec3(0,1,0);
    vec3 b=vec3(0,0,1);
    vec3 ca[19];
    ca[1]=b*2./6.+r*4./6.;
    ca[2]=b*1./6.+r*5./6.;
    ca[3]=r;
    ca[4]=r*5./6.+y/6.;
    ca[5]=r*4./6.+y*2./6.;
    ca[6]=r*3./6.+y*3./6.;
    ca[7]=r*2./6.+y*4./6.;
    ca[8]=r*1./6.+y*5./6.;
    ca[9]=y;
    ca[10]=y*2./3.+g*1./3.;
    ca[11]=y*1./3.+g*2./3.;
    ca[12]=g;
    ca[13]=g*2./3.+b*1./3.;
    ca[14]=g*1./3.+b*2./3.;
    ca[15]=b;
    ca[16]=b*5./6.+r*1./6.;
    ca[17]=b*4./6.+r*2./6.;
    ca[18]=b*3./6.+r*3./6.;
    
    vec2 uv = (i - .01*iResolution.xy)/(iResolution.y); //(i - .5*iResolution.xy)/iResolution.y;
    
    uv.y = uv.y - 0.4;
    uv.y = uv.y * 0.06;
    uv = 0.3 * uv;
    uv.x = uv.x + 0.1;
    uv.y = uv.y * 0.7;
    uv.y += 0.01 * sin(20.0*cos(5.0*uv.x)/sin(iTime));
    uv.x = uv.x * 4.0;
    int c=0;
    float bandDensity = 80.;
    float bandIntensity = 0.148;
    for (float x=0.; x<bandDensity;x+=4./bandDensity )
    {       
        c+=1;
        vec4 color=vec4(ca[c%18+1],0);
        float fftVal = texture(iChannel0, vec2(80.0-x/160.0, 0.)).r * 2.0;
        fftVal = 10.0 * iMouse.x * fftVal;
        float len = length(uv/8.0 - .1 * x * vec2(4.0, 0));
        len = len * 30.0;
        o+=color*vec4(fftVal*bandIntensity/len);
    }
    
    o+=vec4(0,0,0,0);
}

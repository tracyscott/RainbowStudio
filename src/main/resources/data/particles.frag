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
const int nParticles = 50;
const float size = 0.028;
const float softness = 240.0;
const vec4 bgColor = vec4(0.0,0.0,0.0,1.0);

float random (int i){
 return fract(sin(float(i)*43.0)*4790.234);   
}

float softEdge(float edge, float amt){
    return clamp(1.0 / (clamp(edge, 1.0/amt, 1.0)*amt), 0.,1.);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{

    vec2 uv = fragCoord/iResolution.xy;
    float aspect = iResolution.x / iResolution.y;
	uv.x *= aspect;

    fragColor = bgColor;
    
    
    float np = float(nParticles);
    for(int i = 0; i< nParticles; i++){
        vec2 tc = uv;
        
        float r = random(i);
        float r2 = random(i+nParticles);
        float r3 = random(i+nParticles*2);
 
        tc.x -= sin(iTime*0.125 + r*30.0)*r*20.0;
        tc.y -= cos(iTime*0.125 + r*40.0)*r2*0.5;
        
        float audioHz = iMouse.x;
        float audioMultiply = iMouse.y * 2.0;
        float yPertubation = audioMultiply * texture (iChannel0, vec2 (audioHz)).x;
        tc.y += yPertubation;

        float l = length(tc - vec2(aspect*0.5, 0.5)) - r*size*iMouse.w*64.0;
        
        vec4 orb = vec4(r, r2, r3, softEdge(l, softness * iMouse.z));
        
        orb.rgb *= 1.4; // boost it
        
        fragColor = mix(fragColor, orb, orb.a);
    }
    
}
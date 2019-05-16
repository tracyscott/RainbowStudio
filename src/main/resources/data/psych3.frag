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

float pattern0(vec2 p, float time) { return (sin((abs(20. * iMouse.x *p.y))*2.0+time*40.0*iMouse.y)+1.0)/(2.0*iMouse.z); }
float pattern1(vec2 p, float time) { return (sin(iMouse.w * length(p)*1.0+abs(sin(atan(p.y* 10.0 * iTime * iMouse.y,p.x * 10.0 * iTime * iMouse.y)*10.0*iMouse.z+iMouse.y*time*4.0)*0.5*length(p)*5.0)+time*10.0*iMouse.y)+1.0)/2.0; }
float pattern2(vec2 p, float time) { return sin(atan(p.y,p.x)*20.0+time*20.0); }
/*
float pattern0(vec2 p, float time) { return (sin((abs(p.y))*5.0+time*10.0)+1.0)/2.0; }
float pattern1(vec2 p, float time) { return (sin(length(p)*50.0+abs(sin(atan(p.y,p.x)*10.0+time*4.0)*length(p)*5.0)+time*10.0)+1.0)/2.0; }
float pattern2(vec2 p, float time) { return sin(atan(p.y,p.x)*20.0+time*20.0); }
*/

int getPosition(vec2 p) {
	if (p.y < -0.8 && abs(p.x) < 0.3) {
		if (p.x < -0.1) return 0;
		else if (p.x > 0.1) return 2;
	}
	return 1;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ) {
	vec2 p = (2.0*fragCoord.xy-iResolution.x)/iResolution.y;
	int mp = 1;
	int pp = getPosition(p);
	
	
	float p0,p1;
	
    p.x = sin(50.0* p.x * iTime * iMouse.y * 0.05f * iMouse.w);
	p0 = pattern1(p,-iTime);
	p1 = pattern1(p, iTime);
	
    
	float s = mix(p0,p1,smoothstep(0.9,0.55,length(p)));
	fragColor = vec4(vec3(s),1.0);
}

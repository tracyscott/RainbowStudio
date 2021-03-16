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
#ifdef GL_ES
precision mediump float;
#endif
 
const int nParticles = 0;
const float size = 0.028;
const float softness = 240.0;
const vec4 bgColor = vec4(0.0,0.0,0.0,1.0);


// U2x particle length anti-distortion nominator multiplier 1. nominal
// U2y particle length anti-distorition denominator multiplier 1. nominal
// U2z twinkle amount modulator. 0.1 nominal.
// U2w star brightness.  0.5 nominal.

// Increase pass count for a denser effect
#define PASS_COUNT 8

float fBrightness = 10. * U2.w; // 2.5;

// Number of angular segments
float fSteps = 121.0;

float fParticleSize = 0.01;
float fParticleLength = 0.5 / 60.0;

// Min and Max star position radius. Min must be present to prevent stars too near camera
float fMinDist = 0.8;
float fMaxDist = 5.0;

float fRepeatMin = 1.0;
float fRepeatMax = 2.0;

// fog density
float fDepthFade = 0.8;


float Random(float x)
{
	return fract(sin(x * 123.456) * 23.4567 + sin(x * 345.678) * 45.6789 + sin(x * 456.789) * 56.789);
}

vec3 GetParticleColour( const in vec3 vParticlePos, const in float fParticleSize, const in vec3 vRayDir )
{		
	vec2 vNormDir = normalize(vRayDir.xy);
	float d1 = dot(vParticlePos.xy, vNormDir.xy) / length(vRayDir.xy);
	vec3 vClosest2d = vRayDir * d1;
	
	vec3 vClampedPos = vParticlePos;
	
	vClampedPos.z = clamp(vClosest2d.z, vParticlePos.z - fParticleLength, vParticlePos.z + fParticleLength);
	
	float d = dot(vClampedPos, vRayDir);
	
	vec3 vClosestPos = vRayDir * d;
	
	vec3 vDeltaPos = vClampedPos - vClosestPos;	
		
	float fClosestDist = length(vDeltaPos) / fParticleSize;
	float fShade = clamp(1.0 - fClosestDist, 0.0, 1.0);
	
	if (d<3.0)
	{
		fClosestDist = max(abs(vDeltaPos.x),abs(vDeltaPos.y)) / fParticleSize;
		float f = clamp(1.0 - 0.8*fClosestDist, 0.0, 1.0);
		fShade += f*f*f*f;
		fShade *= fShade;
	}
	
	fShade = fShade * exp2(-d * fDepthFade) * fBrightness;
	return vec3(fShade);
}

vec3 GetParticlePos( const in vec3 vRayDir, const in float fZPos, const in float fSeed )
{
	float fAngle = atan(vRayDir.x, vRayDir.y);
	float fAngleFraction = fract(fAngle / (3.14 * 2.0));
	
	float fSegment = floor(fAngleFraction * fSteps + fSeed) + 0.5 - fSeed;
	float fParticleAngle = fSegment / fSteps * (3.14 * 2.0);

	float fSegmentPos = fSegment / fSteps;
	float fRadius = fMinDist + Random(fSegmentPos + fSeed) * (fMaxDist - fMinDist);
	
	float tunnelZ = vRayDir.z / length(vRayDir.xy / fRadius);
	
	tunnelZ += fZPos;
	
	float fRepeat = fRepeatMin + Random(fSegmentPos + 0.1 + fSeed) * (fRepeatMax - fRepeatMin);
	
	float fParticleZ = (ceil(tunnelZ / fRepeat) - 0.1) * fRepeat - fZPos;
	
	return vec3( sin(fParticleAngle) * fRadius, cos(fParticleAngle) * fRadius, fParticleZ );
}

vec3 Starfield( const in vec3 vRayDir, const in float fZPos, const in float fSeed )
{	
	vec3 vParticlePos = GetParticlePos(vRayDir, fZPos, fSeed);
	
	return GetParticleColour(vParticlePos, fParticleSize, vRayDir);	
}

vec3 RotateX( const in vec3 vPos, const in float fAngle )
{
    float s = sin(fAngle); float c = cos(fAngle);
    return vec3( vPos.x, c * vPos.y + s * vPos.z, -s * vPos.y + c * vPos.z);
}

vec3 RotateY( const in vec3 vPos, const in float fAngle )
{
    float s = sin(fAngle); float c = cos(fAngle);
    return vec3( c * vPos.x + s * vPos.z, vPos.y, -s * vPos.x + c * vPos.z);
}

vec3 RotateZ( const in vec3 vPos, const in float fAngle )
{
    float s = sin(fAngle); float c = cos(fAngle);
    return vec3( c * vPos.x + s * vPos.y, -s * vPos.x + c * vPos.y, vPos.z);
}

// Simplex Noise by IQ
vec2 hash( vec2 p )
{
	p = vec2( dot(p,vec2(127.1,311.7)),
			  dot(p,vec2(269.5,183.3)) );

	return -1.0 + 2.0*fract(sin(p)*43758.5453123);
}

float noise( in vec2 p )
{
    const float K1 = 0.366025404; // (sqrt(3)-1)/2;
    const float K2 = 0.211324865; // (3-sqrt(3))/6;

	vec2 i = floor( p + (p.x+p.y)*K1 );
	
    vec2 a = p - i + (i.x+i.y)*K2;
    vec2 o = (a.x>a.y) ? vec2(1.0,0.0) : vec2(0.0,1.0); //vec2 of = 0.5 + 0.5*vec2(sign(a.x-a.y), sign(a.y-a.x));
    vec2 b = a - o + K2;
	vec2 c = a - 1.0 + 2.0*K2;

    vec3 h = max( 0.5-vec3(dot(a,a), dot(b,b), dot(c,c) ), 0.0 );

	vec3 n = h*h*h*h*vec3( dot(a,hash(i+0.0)), dot(b,hash(i+o)), dot(c,hash(i+1.0)));

    return dot( n, vec3(70.0) );
	
}

const mat2 m = mat2( 0.80,  0.60, -0.60,  0.80 );

float fbm4( in vec2 p )
{
    float f = 0.0;
    f += 0.5000*noise( p ); p = m*p*2.02;
    f += 0.2500*noise( p ); p = m*p*2.03;
    f += 0.1250*noise( p ); p = m*p*2.01;
    f += 0.0625*noise( p );
    return f;
}

float marble(in vec2 p)
{
	return cos(p.x+fbm4(p));
}

float dowarp ( in vec2 q, out vec2 a, out vec2 b )
{
	float ang=0.;
	ang = 1.2345 * sin (33.33); //0.015*iTime);
	mat2 m1 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));
	ang = 0.2345 * sin (66.66); //0.021*iTime);
	mat2 m2 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));

	a = vec2( marble(m1*q), marble(m2*q+vec2(1.12,0.654)) );

	ang = 0.543 * cos (13.33); //0.011*iTime);
	m1 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));
	ang = 1.128 * cos (53.33); //0.018*iTime);
	m2 = mat2(cos(ang), -sin(ang), sin(ang), cos(ang));

	b = vec2( marble( m2*(q + a)), marble( m1*(q + a) ) );
	
	return marble( q + b +vec2(0.32,1.654));
}

float random (int i){
 return fract(sin(float(i)*43.0)*4790.234);   
}

float softEdge(float edge, float amt){
    float val = clamp(1.0 / (clamp(edge, 1.0/amt, 1.0)*amt), 0.,0.7);
    return val*val*val*val;
}

float circle(in vec2 _st, in float _radius){
    vec2 dist = _st-vec2(0.480,0.500);
	return 1.000-smoothstep(_radius-(_radius*1.682),
                         _radius+(_radius*1.034),
                         dot(dist/0.5,dist*3.)*6.0);
}

// -----------------------------------------------

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
 	vec2 uv = fragCoord.xy / iResolution.xy;
	vec2 q = 2.*uv-1.;
	q.y *= iResolution.y/iResolution.x;
	
	// camera	
	vec3 rd = normalize(vec3( 1.5*q.x, 1.5*q.y, 1.0 ));
	vec3 euler = vec3(
		sin(iTime * 0.2) * 0.625,
		cos(iTime * 0.1) * 0.625,
		iTime * 0.1 + sin(iTime * 0.3) * 0.5);
	
	// Nebulae Background
	float pi = 3.141592654;
	q.x = 0.5 + atan(rd.z, rd.x)/(2.*pi);
	q.y = 0.5 - asin(rd.y)/pi + 0.512;// + 0.001*iTime;
	q *= 2.34;
	
	vec2 wa = vec2(0.);
	vec2 wb = vec2(0.);
	float f = dowarp(q, wa, wb);
	f = 0.5+0.5*f;
	
	vec3 col = vec3(f);
	float wc = 0.;
	wc = f;
	col = vec3(wc, wc*wc, wc*wc*wc);
	wc = abs(wa.x);
	col -= vec3(wc*wc, wc, wc*wc*wc);
	wc = abs(wb.x);
	col += vec3(wc*wc*wc, wc*wc, wc);
	col *= 0.7;
	//col.x = pow(col.x, 2.18);
	//col.z = pow(col.z, 1.88);
	col = smoothstep(0., 1., col);
	col = 0.5 - (1.4*col-0.7)*(1.4*col-0.7);
	col = 0.75*sqrt(col);
	col *= 1. - 0.5*fbm4(8.*q);
	col = clamp(col, 0., 1.);
	float nebulaBright = 0.5f;
	col *= nebulaBright;
	// StarField
	float fShade = 0.0;
	float a = 0.2;
	float b = 10.0;
	float c = 1.0;
	float fZPos = 5.0;// + iTime * c + sin(iTime * a) * b;
	float fSpeed = 0.; //c + a * b * cos(a * iTime);
	
	// Twinkle parameters.
	float twinkleSpeed = 10.;
	float twinklePhase = 3.14 * mod(uv.x, 0.1);
	float twinkleAmount = (10. * U2.z) * 0.001 * sin(iTime*5. + 5.*twinklePhase); // * 10.0* mod(uv.x, 0.2);
	//fParticleLength = 0.006 + twinkleAmount * sin(iTime*twinkleSpeed);
	fParticleSize = fParticleSize + twinkleAmount * sin(iTime*twinkleSpeed);
	
	float fSeed = 0.0;
	
	vec3 vResult = vec3(0.);
	
	// Adjust particle length depending on horizontal position to account for distortion
	// on the ends of the Rainbow.
	fParticleLength = U2.x*fParticleLength/(5. * U2.y * abs(uv.x - 0.5));


	vec3 red = vec3(0.7,0.4,0.3);
	vec3 blue = vec3(0.3,0.4,0.7);
	vec3 tint = vec3(0.);
	float ti = 1./float(PASS_COUNT-1);
	float t = 0.;
	for(int i=0; i<PASS_COUNT; i++)
	{
		tint = mix(red,blue,t);
		vResult += 1.1*tint*Starfield(rd, fZPos, fSeed);
		t += ti;
		fSeed += 1.234;
	}
	
	col += sqrt(vResult); //sqrt(vResult);
	fragColor = vec4( col, 1.0 );

    // Shooting stars are a couple of SDF circles on sine/cosine loops.
	vec2 circlePos = uv;
	circlePos.x = uv.x + 16. * sin(iTime/8.);
	circlePos.x /= 0.5;
	circlePos.y = uv.y + 8. * sin(iTime);
    vec3 circleColor = vec3(circle(circlePos,0.010));
    vec2 circlePos2 = uv;
    circlePos2.x = uv.x + 16. * cos(iTime/8.) - 0.5;
	circlePos2.x /= 0.5;
	circlePos2.y = uv.y + 8. * sin(iTime);
    vec3 circleColor2 = vec3(circle(circlePos2, 0.010));
 
    fragColor += vec4(circleColor, 1.0);
    fragColor += vec4(circleColor2, 1.0);
}
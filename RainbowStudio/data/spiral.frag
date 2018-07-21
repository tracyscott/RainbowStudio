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

float texture(vec2 co) {
	float leftTravelDistance = 2.0;  // Try 20.0
	co.x += leftTravelDistance * iMouse.x;
	// This is expected to be hooked to a bipolar modulator centered on 0.5
	// If iMouse.y = 0.5, bend amount is 0.  Negative bend amount bends right,
	// positive bends left.  Try commenting out the co.x += leftTravelDistance * iMouse.x
	// above to see bend action independentally. 
	float bendAmt = iMouse.y - 0.5;
	float dotVectorXCoord =  1.0; 
	// Try commenting out the line above and commenting this line below. iMouse.x is moving dynamically
	// between 0.0 and 1.0.
	//float something = iMouse.x ;
	float dotVectorYCoord = iMouse.y - 0.5;
	// Perform a dot product of our x,y coordinate and (1.0, y)
    float dotty = dot(co.xy , vec2(dotVectorXCoord, dotVectorYCoord));

    // Increasing this increases the number of blades.  Try changing this number.
	float bladeMultiplier = 2.0;
	// Uncomment this line and turn Knob 3
	// bladeMultiplier = 10.0 * iMouse.z;
	dotty *= bladeMultiplier;
	// Return just the 0...1 part of our computation.
	float f = fract(dotty);
	// Try just returning the value of dotty.
	return f;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 position = fragCoord/iResolution.xx;
    float replicas = 10.0;  // Try changing this to 1.
    // Multiplying against your position makes copies.
    position *= replicas;
    float r = texture(position);

    vec3 color = vec3( r, r, r);
	fragColor = vec4(color,1.0);
}

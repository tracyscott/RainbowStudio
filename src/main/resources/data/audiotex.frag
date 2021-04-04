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

// iMouse.x = 0.2 nominal.  This is vertical displacement multiplier.  It is
// multiplied times 5. so above .2 and it starts tiling the texture.
// iMouse.y Non-displacement threshold.  If the displacement value is below this
// then don't displace.  This can be used to prevent the image from twitching when
// nothing is happening.  But it can also produce a little bit of visual tearing
// at the threshold value.
// iMouse.z If this is > 0.5 we will use the whole texture.  If it is less
// than or equal to, then we will use the left side of texture mirrored.  This
// mode is helpful for non-symmetric images since it makes them symmetric.

void mainImage( out vec4 fragColor, in vec2 pt )
{
    vec2 position = pt/iResolution.xy;
    vec2 originalPos = position;
    // move image right, flip left horizontally
    if (position.x < .5) {
      position.x = -position.x+.5;
    } else {
      position.x = position.x-.5;
    }

    vec2 c = position; //pt / iResolution.xy;
    c.x *= 0.3;  // Scale it in the X direction for a better visual fit.
    float mag = texture(iChannel0, vec2(0.2*c.x, 0.5*c.y)).r;
    if (mag < iMouse.y) {
    	mag = 0.;
    }
    if (iMouse.z > 0.5) {
    	position.x = originalPos.x; 
    } 
    position.y = position.y + mag * 5.0 * iMouse.x;
    fragColor = texture(iChannel1, position);
    fragColor *= fragColor;
}

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


#ifdef GL_ES
precision mediump float;
#endif

#define M_PI 3.1415926

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float getDegree(vec2 position, float offset, float om, float time) {
	float degree = atan(position.y, position.x) + offset;
	degree = (degree + M_PI) / M_PI;
	degree = fract(time / 5.0 + degree / 2.0 + om);
	degree = degree > 0.5 ? 1.0 - (degree - 0.5) * 2.0: degree * 2.0;
	return degree;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ) {

	vec2 position = ( fragCoord.xy / iResolution.xy );
	
	vec2 uv = (position - 0.5) * 2.0;
	
	float power = iMouse.x * 3.0;
	float powermul = pow(10.0, power);
	float mul = 3.0 * powermul;
	
	position = fract(position * mul); // + fract(-position * mul)) / 2.0;
	position -= 0.5;
	position *= 30.0;  // 30.0
	
	// With these commented out, there are interesting negative spaces movement
	position.x = position.x > 0.5 ? 1.0 - position.x : position.x;
	position.y = position.y > 0.5 ? 1.0 - position.y : position.y;
	
	position.x *= iResolution.x / iResolution.y;
	float inverseDensity = 0.1;
	float om = getDegree(uv, inverseDensity * pow(length(uv), 1.0) * 1.0, 0.0, 0.0);
	float v = 0.0;
	vec3 color = vec3(0.0);
    int xx = 3;
    int yy = 3;
    float c = float(xx * yy);
    for (int x = 0; x < xx; x++) {
        for (int y = 0; y < yy; y++) {
            float th = 1.5 * M_PI * float(x * yy + y) / c;
            vec2 p = position * mat2(cos(th), -sin(th), -sin(th), cos(th));
            p += vec2(float(x), float(y)) / vec2(float(xx), float(yy));
            v = getDegree(p, 0.0, om, iTime * iMouse.y * 5.0);

            vec2 co = floor(uv * mul) * float(x * yy + y) / c; // floor(uv * mul) * float(x * yy + y) / c;
            float r = rand(co * 13.342354);
            float g = rand(co * 4324.23423432);
            float b = rand(co * 14.314);

            color += vec3(r, g, b) / (v * c * 20.0);
        }
    }
	
	fragColor = vec4(color, 1.0);

}

#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;
uniform vec2 downscaledResolution;
uniform sampler2D downscaledFramebuffer;
uniform int windowSize;
uniform float threshold;

void main() {
    float w = 1.0 / resolution.x;
    float h = 1.0 / resolution.y;
    float wDownscaled = 1.0 / downscaledResolution.x;
    float hDownscaled = 1.0 / downscaledResolution.y;

    vec2 uv = gl_FragCoord.xy / resolution;


    vec4 binarized = texture2D(framebuffer, uv);
    vec4 avg = texture2D(downscaledFramebuffer, uv);

    float thresholdedAvg = avg.x * (100.0-threshold)/100.0;

    gl_FragColor = vec4(vec3(step(thresholdedAvg, binarized.x)), 1.0);
}

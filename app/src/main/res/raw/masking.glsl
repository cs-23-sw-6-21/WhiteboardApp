#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 samplerResolution;
uniform vec2 maskResolution;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D mask;

void main() {
    vec2 samplersUV = gl_FragCoord.xy / samplerResolution;
    vec2 maskUV = gl_FragCoord.xy / maskResolution;

    vec4 col1 = texture2D(sampler1, samplersUV);
    vec4 col2 = texture2D(sampler2, samplersUV);
    vec4 mask = texture2D(mask, samplersUV);

    float amask = step(0.1, mask.z);

    gl_FragColor = col2 * amask + (1.0-amask) * col1;
}
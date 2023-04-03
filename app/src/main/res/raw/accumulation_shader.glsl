#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;

uniform sampler2D framebuffer;
uniform sampler2D oldAccumulator;

void main() {
    vec2 samplersUV = gl_FragCoord.xy / resolution;

    vec4 col1 = texture2D(framebuffer, samplersUV);
    vec4 col2 = texture2D(oldAccumulator, samplersUV);


    float add = col2.x + 0.2 * col1.x - 0.2 * (1.0-col1.x);


    gl_FragColor = vec4(add, add, add, 1.0);
}
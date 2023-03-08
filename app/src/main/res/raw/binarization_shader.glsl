#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 binarized = step(vec4(0.5), texture2D(framebuffer, uv));

    gl_FragColor = binarized;
}

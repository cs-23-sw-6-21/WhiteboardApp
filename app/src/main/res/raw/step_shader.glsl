#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 color = texture2D(framebuffer, uv);

    float value = 1.0 - step(0.5, color.a);

    gl_FragColor = vec4(color.xyz * value + vec3(1.0 - value), 1.0);
}

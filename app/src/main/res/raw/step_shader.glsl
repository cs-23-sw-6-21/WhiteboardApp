#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 color = texture2D(framebuffer, uv);

    vec3 value = step(vec3(0.5), color.xyz);

    gl_FragColor = vec4(value, color.a);
}

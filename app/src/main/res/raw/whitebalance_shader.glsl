#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D rawInput;
uniform sampler2D backgroundColor;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 color1 = texture2D(rawInput, uv);
    vec4 color2 = texture2D(backgroundColor, uv);

    gl_FragColor = vec4(vec3(1.0) - (color1.xyz - color2.xyz), 1.0);
}

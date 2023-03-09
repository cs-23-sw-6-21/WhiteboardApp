#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D source_texture;


varying vec2 v_TexCoordinate;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 color = texture2D(source_texture, v_TexCoordinate);

    gl_FragColor = color;
}
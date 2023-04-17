#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    // manipulate UVs so it is correctly oriented in the segmentor
    // TODO: take phone orientation into account. Right now it is harcoded for left handed landscape orientation.
    vec2 uvFlipped = vec2(uv.y, uv.x);

    // Put red channel into all colour channels just to be nice.
    gl_FragColor = vec4(texture2D(framebuffer, uvFlipped).xxx, 1.0);
}

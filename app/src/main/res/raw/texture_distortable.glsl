#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D source_texture;


varying vec3 v_TexCoordinate;

void main() {
    vec2 uv = v_TexCoordinate.xy / resolution;

    vec4 color = texture2D(source_texture, vec2(v_TexCoordinate.x / v_TexCoordinate.z, v_TexCoordinate.y / v_TexCoordinate.z));
    //vec4 color = texture2D(source_texture, vec2(v_TexCoordinate.x, v_TexCoordinate.y);
    //color = vec4(v_TexCoordinate, 1.0);

    gl_FragColor = color;
}
attribute vec4 position;

attribute vec2 a_TexCoordinate;
varying vec2 v_TexCoordinate;
void main() {
    v_TexCoordinate = a_TexCoordinate;
    gl_Position = vec4(position.x*0.2, position.y * 0.2, position.z, position.w)
        - vec4(0.6, 0.6, 0.0, 0.0)
        + vec4(0.0, -position.x * 0.04, 0.0, 0.0);
}
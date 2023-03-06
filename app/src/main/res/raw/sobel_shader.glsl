#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform sampler2D framebuffer;

float map(float value, float min1, float max1, float min2, float max2){
    return min2+(value-min1)*(max2-min2)/(max1-min1);
}

void make_kernel(inout vec4 n[9], sampler2D tex, vec2 coord) {
    float w = 1.0 / resolution.x;
    float h = 1.0 / resolution.y;

    n[0] = texture2D(tex, coord + vec2( -w, -h));
    n[1] = texture2D(tex, coord + vec2(0.0, -h));
    n[2] = texture2D(tex, coord + vec2(  w, -h));
    n[3] = texture2D(tex, coord + vec2( -w,0.0));
    n[4] = texture2D(tex, coord);
    n[5] = texture2D(tex, coord + vec2(  w,0.0));
    n[6] = texture2D(tex, coord + vec2( -w,  h));
    n[7] = texture2D(tex, coord + vec2(0.0,  h));
    n[8] = texture2D(tex, coord + vec2(  w,  h));
}

void main() {
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 n[9];
    make_kernel(n, framebuffer, uv);

    vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
    vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);

    // The values are between -1.0 and 1.0, but you can only have a value between 0.0 and 1.0 in
    // textures so we map our -1.0 to 1.0 range to 0.0 to 1.0
    // The stage that uses this sobel operator should map the 0.0 to 1.0 range back to -1.0 to 1.0
    float x = map(sobel_edge_h.r, -1.0, 1.0, 0.0, 1.0);
    float y = map(sobel_edge_v.r, -1.0, 1.0, 0.0, 1.0);

    // Output x and y directions in r and b color channels
    gl_FragColor = vec4(x, y, 0.0, 1.0);
}
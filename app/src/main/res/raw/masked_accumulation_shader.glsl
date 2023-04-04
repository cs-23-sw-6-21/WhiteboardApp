#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;

uniform sampler2D framebuffer;
uniform sampler2D oldAccumulator;
uniform sampler2D mask;

void main() {
    vec2 samplersUV = gl_FragCoord.xy / resolution;

    vec4 col1 = texture2D(framebuffer, samplersUV);
    vec4 col2 = texture2D(oldAccumulator, samplersUV);
    vec4 mask = texture2D(mask, samplersUV);

    float binaryInput = step(0.99, col1.x);
    float maskValue = step(0.99, mask.x);

    float add = (col2.a + (maskValue) * (0.2 * binaryInput - 0.2 * (1.0-binaryInput)));
    //float add = step(0.99, mask.x);

    gl_FragColor = vec4(col1.xyz * maskValue + col2.xyz * (1.0-maskValue), add);
    //gl_FragColor = vec4(vec3(maskValue), 1.0);
}
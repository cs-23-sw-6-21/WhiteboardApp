#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;

uniform sampler2D segmentation;
uniform sampler2D oldAccumulator;

void main() {
    vec2 samplersUV = gl_FragCoord.xy / resolution;

    vec4 col1 = texture2D(segmentation, samplersUV);
    vec4 col2 = texture2D(oldAccumulator, samplersUV);


    float segmentMap = 1.0 - step(0.05, col1.z);


    float add = (col2.x + 0.1) * segmentMap;


    // annoying hack because the segmentor outputs a slightly blue color for no reason at all.
    //float actualmask = step(0.1, mask.x + mask.y + mask.z);

    gl_FragColor = vec4(add, 0.0, 0.0, add);
}
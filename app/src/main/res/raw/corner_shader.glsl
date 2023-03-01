#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform vec2 resolution;
uniform vec2 offset;

void main() {
   vec2 uv = (gl_FragCoord.xy - offset-(resolution/2.0))/(resolution);

   if(sqrt(uv.x*uv.x+uv.y*uv.y)>0.5){
      gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
   }
   else {
      gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
   }


}

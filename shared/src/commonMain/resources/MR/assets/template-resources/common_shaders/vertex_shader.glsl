attribute vec4 aPosition;
uniform mat4 uMVPMatrix;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord0;
uniform mat4 uTextureMatrix0;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTextureCoord0 = (uTextureMatrix0 * aTextureCoord).xy;
}
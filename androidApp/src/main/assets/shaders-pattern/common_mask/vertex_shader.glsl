attribute vec4 aPosition;
uniform mat4 uMVPMatrix;
attribute vec4 aTextureCoord;

/*headerGroup*/

void main() {
    gl_Position = uMVPMatrix * aPosition;
    /*bodyGroup*/
}
#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord0;
uniform samplerExternalOES sTexture0;

vec4 clampToBorder(vec2 textureCoord);

void main() {
    gl_FragColor = clampToBorder(vTextureCoord0);
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0); // Default color
    } else {
        return texture2D(sTexture0, textureCoord);
    }
}
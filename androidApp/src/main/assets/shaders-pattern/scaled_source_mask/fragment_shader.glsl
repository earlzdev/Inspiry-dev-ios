#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source texture
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;// Source

// Mask texture atlas
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;// Template mask

vec4 clampToBorder(vec2 textureCoord);

void main() {
    vec4 templateMask = texture2D(sTexture1, vTextureCoord1);
    vec4 source = clampToBorder(vTextureCoord_0_);

    float mask = 1.0;

    if (templateMask.a == 0.0)
    mask = 0.0;

    vec2 scaledSourceCoord = vTextureCoord_0_ * vec2(0.9, 0.9) + vec2(0.05, 0.05);
    vec4 scaledSource = texture2D(sTexture_0_, scaledSourceCoord);

    scaledSource.r -= (1.0 - templateMask.r) * templateMask.a;
    scaledSource.g -= (1.0 - templateMask.g) * templateMask.a;
    scaledSource.b -= (1.0 - templateMask.b) * templateMask.a;

    vec4 result = mix(source, scaledSource, mask);

    gl_FragColor = result;
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}
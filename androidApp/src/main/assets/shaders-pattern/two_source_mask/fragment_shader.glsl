#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source0
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;

// Source1
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;

// Mask
uniform samplerExternalOES sTexture_2_;
varying vec2 vTextureCoord_2_;

void main() {
    vec4 templateMask = texture2D(sTexture_2_, vTextureCoord_2_);
    float brightness = (0.299 * templateMask.r + 0.587 * templateMask.g + 0.114 * templateMask.b);

    vec4 source0 = texture2D(sTexture_0_, vTextureCoord_0_);
    vec4 source1 = texture2D(sTexture_1_, vTextureCoord_1_);

    gl_FragColor = mix(source1, source0, brightness);
}
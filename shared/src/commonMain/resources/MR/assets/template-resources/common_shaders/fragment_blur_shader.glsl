#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord0;
uniform samplerExternalOES sTexture0;
uniform vec2 uBlurSize0;

vec4 blur9(vec2 textureCoord, vec2 direction);

void main() {
    gl_FragColor = blur9(vTextureCoord0, vec2(0.0, 1.0));
}

vec4 blur9(vec2 textureCoord, vec2 direction) {
    vec4 color = vec4(0);
    vec2 offset1 = vec2(1.3846153846) * direction;
    vec2 offset2 = vec2(3.2307692308) * direction;
    vec2 tc = vTextureCoord0;
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture0, tc) * 0.2270270270;
    } else {
        return vec4(1.0, 1.0, 1.0, 1.0);
    }
    tc = textureCoord + (offset1 * direction * uBlurSize0);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture0, tc) * 0.3162162162;
    }
    tc = textureCoord - (offset1 * direction * uBlurSize0);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture0, tc) * 0.3162162162;
    }
    tc = textureCoord + (offset2 * direction * uBlurSize0);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture0, tc) * 0.0702702703;
    }
    tc = textureCoord - (offset2 * direction * uBlurSize0);
    if (tc.x >= 0.0 && tc.x <= 1.0 && tc.y >= 0.0 && tc.y <= 1.0) {
        color += texture2D(sTexture0, tc) * 0.0702702703;
    }
    return color;
}
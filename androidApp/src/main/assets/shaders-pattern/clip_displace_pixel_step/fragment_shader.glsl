#extension GL_OES_EGL_image_external : require
precision mediump float;
const float DISPLACEMENT_PIXEL_STEP_X = -40.0;
const float DISPLACEMENT_PIXEL_STEP_Y = -5.0;

// Source texture
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;// Source
uniform vec2 uPixelSize_0_;

// Mask texture atlas
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_2_;// Overlay mask
varying vec2 vTextureCoord_3_;// Alpha mask
varying vec2 vTextureCoord_4_;// Blend mask
varying vec2 vTextureCoord_5_;// Displacement mask

vec4 blendOverlay(vec4 source, vec4 blendMask);
vec2 displacement(vec2 textureCoord, vec2 pixelSize, vec2 displaceMask, float maxPixelOffsetX, float maxPixelOffsetY);
vec4 clampToBorder(vec2 textureCoord);

void main() {
    vec2 displacementMask = texture2D(sTexture_1_, vTextureCoord_5_).xy;
    vec2 coord = displacement(vTextureCoord_0_, uPixelSize_0_, displacementMask, DISPLACEMENT_PIXEL_STEP_X, DISPLACEMENT_PIXEL_STEP_Y);
    vec4 source = clampToBorder(coord);
    vec4 blendMask = texture2D(sTexture_1_, vTextureCoord_4_);
    source = blendOverlay(source, blendMask);
    vec4 overlayMask = texture2D(sTexture_1_, vTextureCoord_2_);
    float alpha = texture2D(sTexture_1_, vTextureCoord_3_).r;
    gl_FragColor = mix(source, overlayMask, alpha);
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0); // Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}

float blendOverlay(float source, float blendMask) {
    if (source < 0.5) return 2.0 * source * blendMask;
    else return 1.0 - 2.0 * (1.0 - source) * (1.0 - blendMask);
}

vec4 blendOverlay(vec4 source, vec4 blendMask) {
    float r = blendOverlay(source.r, blendMask.r);
    float g = blendOverlay(source.g, blendMask.g);
    float b = blendOverlay(source.b, blendMask.b);
    return vec4(r, g, b, 1.0);
}

vec2 displacement(vec2 textureCoord, vec2 pixelSize, vec2 offset, float maxPixelOffsetX, float maxPixelOffsetY) {
    vec2 newTextureCoord = textureCoord + vec2(maxPixelOffsetX, maxPixelOffsetY) * offset * pixelSize;
    if (newTextureCoord.x > 1.0 || newTextureCoord.x < 0.0) {
        newTextureCoord.x = textureCoord.x;
    }
    if (newTextureCoord.y > 1.0 || newTextureCoord.y < 0.0) {
        newTextureCoord.y = textureCoord.y;
    }
    return newTextureCoord;
}

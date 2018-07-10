#version 330

uniform float fogRange;
uniform vec3 ambientLight;

smooth in vec4 fragColor;
smooth in float cameraDistance;
out vec4 outputColor;

void main()
{
    float fogObscurity = cameraDistance / fogRange;
    fogObscurity = max(0.0, min(1.0, fogObscurity * fogObscurity));

    outputColor = ((1 - fogObscurity) * fragColor) + vec4(fogObscurity * ambientLight, 1.0);
}
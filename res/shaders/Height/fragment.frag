#version 330

in vec3 mVertexNormal;
in vec3 mVertexPosition;

// in model space
uniform vec3 cameraPosition;

out vec4 outputColor;

float sigmoid(float val){
    return val / (1 + abs(val));
}

void main() {
// diffuse component
    float intensity = max(0.0, dot(mVertexNormal, normalize(cameraPosition - mVertexPosition.xyz)));

    float height = sigmoid(mVertexPosition.z / 10);

    float r = -abs(height) + 1.0;
    float g = max(min(-height, 1.0), 0.0);
    float b = max(min(height, 1.0), 0.0);
    vec3 color = vec3(r, g, b);

    outputColor = vec4(color * intensity, 1.0);
}
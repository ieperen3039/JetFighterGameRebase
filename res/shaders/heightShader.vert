#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 vertexNormal;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

smooth out vec4 fragColor;

void main()
{
    vec4 mPosition = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mPosition;
    vec3 mvNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;

    vec3 cameraPosition = normalize(-position); //vector towards light source
    // diffuse component
    float intensity = max(0.0, dot(mvNormal, cameraPosition));
    color = vec3(1, 0, mPosition.z);
    fragColor = vec4(color * intensity, 1);
}
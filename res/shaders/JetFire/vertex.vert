// @author Geert van Ieperen

#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in float deltaTime

// position of the vertex
out vec3 mVertexPosition;



void main() {
	
	vec4 mPosition = modelMatrix * vec4(position, 1.0);
    gl_Position = viewProjectionMatrix * mPosition;
	
	mVertexNormal = normalize(normalMatrix * vertexNormal);
    mVertexPosition = mPosition.xyz;
}
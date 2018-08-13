#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 vertexNormal;

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

struct PointLight
{
    vec3 color;
    // light position in model coordinates.
    vec3 mPosition;
    float intensity;
};

const int MAX_POINT_LIGHTS = 10;

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform vec3 ambientLight;
// in model coordinates
uniform vec3 cameraPosition;

uniform mat4 modelMatrix;
uniform mat4 viewProjectionMatrix;
uniform mat3 normalMatrix;

smooth out vec4 fragColor;
smooth out float cameraDistance;

// P, N, eye and light.mPosition in model-space
vec3 calculateLighting(vec3 P, vec3 N, vec3 eye, PointLight light){

    vec3 result = vec3(0.0, 0.0, 0.0);

	vec3 lightDirection = normalize(light.mPosition.xyz - P); //vector towards light source
    // diffuse component
    float intensity = max(0.0, dot(N, lightDirection));
    result += intensity * light.color * material.diffuse.xyz;

	vec3 reflection = (reflect(lightDirection, N));
	vec3 virtualLightPosition = normalize(-reflection);

	// specular component
    float shine = pow( max(0.0, dot(virtualLightPosition, normalize(eye))), material.reflectance);
    //float shine = pow( max(0.0, dot(N, HalfAngle) ), mat.shininess );
    result += shine * light.color;


	return result;
}

void main()
{
    // opaque black
    vec4 modelPosition4 = modelMatrix * vec4(position, 1.0);
    gl_Position = viewProjectionMatrix * modelPosition4;

    vec3 mPosition = modelPosition4.xyz;
    vec3 mNormal = normalize(normalMatrix * vertexNormal);

    vec3 diffuseSpecularComponent = vec3(0.0, 0.0, 0.0);
    for (int i=0; i < MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0 ) {
            diffuseSpecularComponent += calculateLighting(mPosition, mNormal, cameraPosition, pointLights[i]);
        }
    }

    cameraDistance = length(mPosition.xyz - cameraPosition);

    fragColor = material.ambient * vec4(ambientLight, 1.0) + vec4(diffuseSpecularComponent, material.diffuse.w);
}
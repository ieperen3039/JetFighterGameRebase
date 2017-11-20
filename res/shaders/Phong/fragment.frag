#version 330

in vec3 mvVertexNormal;
in vec3 mvVertexPosition;

out vec4 fragColor;

struct PointLight
{
    vec3 color;
    // light position in view coordinates.
    vec3 position;
    float intensity;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

const int MAX_POINT_LIGHTS = 10;

uniform float specularPower;
uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform vec3 camera_pos;
uniform int shadowed;
uniform int blackAsAlpha;
uniform vec3 ambientLight;

vec4 materialColor;
vec4 diffuseC;
vec4 speculrC;

vec4 calcLightcolor(vec3 light_color, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffusecolor = vec4(0, 0, 0, 0);
    vec4 speccolor = vec4(0, 0, 0, 0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffusecolor = diffuseC * vec4(light_color, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(camera_pos - position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    speccolor = speculrC * light_intensity  * specularFactor * material.reflectance * vec4(light_color, 1.0);

    return (diffusecolor + speccolor);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_color = calcLightcolor(light.color, light.intensity, position, to_light_dir, normal);

    return light_color;
}

void main()
{
    materialColor = material.ambient;

    vec4 diffuseSpecularComponent = vec4(0.0, 0.0, 0.0, 0.0);

    if (shadowed == 1) {
        for (int i=0; i < MAX_POINT_LIGHTS; i++) {
            if (pointLights[i].intensity > 0 ) {
                diffuseSpecularComponent += calcPointLight(pointLights[i], mvVertexPosition, mvVertexNormal);
            }
        }

        fragColor = materialColor * vec4(ambientLight, 1) + diffuseSpecularComponent;
    } else {
        if (blackAsAlpha == 1) {
            vec3 colorstart = materialColor.xyz * vec4(ambientLight, 1).xyz;
            fragColor = vec4(colorstart.x, colorstart.y, colorstart.z, 0.0);
        } else {
            fragColor = materialColor * vec4(ambientLight, 1);
        }
    }
}
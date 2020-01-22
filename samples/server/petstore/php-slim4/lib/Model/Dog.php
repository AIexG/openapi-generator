<?php

/**
 * Dog
 *
 * PHP version 7.1
 *
 * @package OpenAPIServer\Model
 * @author  OpenAPI Generator team
 * @link    https://github.com/openapitools/openapi-generator
 */

/**
 * NOTE: This class is auto generated by the openapi generator program.
 * https://github.com/openapitools/openapi-generator
 */
namespace OpenAPIServer\Model;

use OpenAPIServer\Interfaces\ModelInterface;

/**
 * Dog
 *
 * @package OpenAPIServer\Model
 * @author  OpenAPI Generator team
 * @link    https://github.com/openapitools/openapi-generator
 */
class Dog implements ModelInterface
{
    private const MODEL_SCHEMA = <<<'SCHEMA'
{
  "allOf" : [ {
    "$ref" : "#/components/schemas/Animal"
  }, {
    "$ref" : "#/components/schemas/Dog_allOf"
  } ]
}
SCHEMA;

    /** @var string $className */
    private $className;

    /** @var string $color */
    private $color;

    /** @var string $breed */
    private $breed;

    /**
     * Returns model schema.
     *
     * @param bool $assoc When TRUE, returned objects will be converted into associative arrays. Default FALSE.
     *
     * @return array
     */
    public static function getOpenApiSchema($assoc = false)
    {
        return json_decode(static::MODEL_SCHEMA, $assoc);
    }
}

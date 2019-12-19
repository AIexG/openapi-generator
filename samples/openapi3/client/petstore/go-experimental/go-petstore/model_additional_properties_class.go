/*
 * OpenAPI Petstore
 *
 * This spec is mainly for testing Petstore server and contains fake endpoints, models. Please do not use this for any other purpose. Special characters: \" \\
 *
 * API version: 1.0.0
 * Generated by: OpenAPI Generator (https://openapi-generator.tech)
 */

package openapi

import (
	"bytes"
	"encoding/json"
)

// AdditionalPropertiesClass struct for AdditionalPropertiesClass
type AdditionalPropertiesClass struct {
	MapProperty *map[string]string `json:"map_property,omitempty"`
	MapOfMapProperty *map[string]map[string]string `json:"map_of_map_property,omitempty"`
}

// GetMapProperty returns the MapProperty field value if set, zero value otherwise.
func (o *AdditionalPropertiesClass) GetMapProperty() map[string]string {
	if o == nil || o.MapProperty == nil {
		var ret map[string]string
		return ret
	}
	return *o.MapProperty
}

// GetMapPropertyOk returns a tuple with the MapProperty field value if set, zero value otherwise
// and a boolean to check if the value has been set.
func (o *AdditionalPropertiesClass) GetMapPropertyOk() (map[string]string, bool) {
	if o == nil || o.MapProperty == nil {
		var ret map[string]string
		return ret, false
	}
	return *o.MapProperty, true
}

// HasMapProperty returns a boolean if a field has been set.
func (o *AdditionalPropertiesClass) HasMapProperty() bool {
	if o != nil && o.MapProperty != nil {
		return true
	}

	return false
}

// SetMapProperty gets a reference to the given map[string]string and assigns it to the MapProperty field.
func (o *AdditionalPropertiesClass) SetMapProperty(v map[string]string) {
	o.MapProperty = &v
}

// GetMapOfMapProperty returns the MapOfMapProperty field value if set, zero value otherwise.
func (o *AdditionalPropertiesClass) GetMapOfMapProperty() map[string]map[string]string {
	if o == nil || o.MapOfMapProperty == nil {
		var ret map[string]map[string]string
		return ret
	}
	return *o.MapOfMapProperty
}

// GetMapOfMapPropertyOk returns a tuple with the MapOfMapProperty field value if set, zero value otherwise
// and a boolean to check if the value has been set.
func (o *AdditionalPropertiesClass) GetMapOfMapPropertyOk() (map[string]map[string]string, bool) {
	if o == nil || o.MapOfMapProperty == nil {
		var ret map[string]map[string]string
		return ret, false
	}
	return *o.MapOfMapProperty, true
}

// HasMapOfMapProperty returns a boolean if a field has been set.
func (o *AdditionalPropertiesClass) HasMapOfMapProperty() bool {
	if o != nil && o.MapOfMapProperty != nil {
		return true
	}

	return false
}

// SetMapOfMapProperty gets a reference to the given map[string]map[string]string and assigns it to the MapOfMapProperty field.
func (o *AdditionalPropertiesClass) SetMapOfMapProperty(v map[string]map[string]string) {
	o.MapOfMapProperty = &v
}

type NullableAdditionalPropertiesClass struct {
	Value AdditionalPropertiesClass
	ExplicitNull bool
}

func (v NullableAdditionalPropertiesClass) MarshalJSON() ([]byte, error) {
    switch {
    case v.ExplicitNull:
        return []byte("null"), nil
    default:
		return json.Marshal(v.Value)
	}
}

func (v *NullableAdditionalPropertiesClass) UnmarshalJSON(src []byte) error {
	if bytes.Equal(src, []byte("null")) {
		v.ExplicitNull = true
		return nil
	}

	return json.Unmarshal(src, &v.Value)
}

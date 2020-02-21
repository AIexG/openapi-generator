package org.openapitools.codegen.languages;

import java.io.*;
import java.io.File;

import java.util.*;

import org.openapitools.codegen.*;
import org.openapitools.codegen.utils.ModelUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.parameters.Parameter;

import com.google.common.base.CaseFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CppDllServerCodegen extends CppRestbedServerCodegen
{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CppDllServerCodegen.class);

    public CppDllServerCodegen()
    {
        super();

        outputFolder = "";
        apiTemplateFiles.clear();
        modelTemplateFiles.clear();

        embeddedTemplateDir = templateDir = "cpp-dll-server";

        supportingFiles.clear();
        supportingFiles.add(new SupportingFile("header.mustache", outputFolder, "CppDllServer.h"));
        supportingFiles.add(new SupportingFile("source.mustache", outputFolder, "CppDllServer.cpp"));
        
        cliOptions.clear();
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     */
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator. This will be used by the
     * generator to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "cpp-dll-server";
    }

    /**
     * Returns human-friendly help for the generator. Provide the consumer with
     * help tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a C++ API for DLL use.";
    }

    /**
     * Location to write model files. You can use the modelPackage() as defined
     * when the class is instantiated
     * @return Path of the model folder
     */
    @Override
    public String modelFileFolder() {
        return (outputFolder + "/" + apiPackage).replace("/", File.separator);
    }
    
    /**
     * Location to write api files. You can use the apiPackage() as defined when
     * the class is instantiated
     * @return Path of the api folder
     */
    @Override
    public String apiFileFolder() {
        return (outputFolder + "/" + modelPackage).replace("/", File.separator);
    }

    /**
     * Turn name into UPPER_UNDERSCORE if not done already.
     * @param name a String.
     * @return String in upper underscore or upper snakecase
     */
    public String toUpperModelName(String name) {
        if(name.matches("^[A-Z](?:_|[A-Z]+)+[A-Z]$")) {
            return name;
        } else {
            return sanitizeName(CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name));
        }
    }

    /**
     * Utility for sorting model names with a previously generated adjacency graph
     * 
     * @param startNode starting node, ideally a root node
     * @param vecVisited vector fo all visited nodes
     * @param listSorted modifyable list for placing sorted nodes
     * @param mapEdges directed adjacency graph of all used edges
     */
    public void topologicalSortUtil(String startNode, Vector<String> vecVisited, LinkedList<String> listSorted, Map<String, Set<String>> mapEdges)
    {
        vecVisited.add(startNode);
        // Recur for all the vertices adjacent to this vertex 
        if (mapEdges.get(startNode) != null) {
            for (String node : mapEdges.get(startNode)) {
                if (!vecVisited.contains(node)) {
                    topologicalSortUtil(node, vecVisited, listSorted, mapEdges);
                }
            }
        }
        // Push current vertex to stack which stores result 
        listSorted.addLast(startNode);
    }
        
    /**
     * Sorts all codegen object models by their depenencies and prefixes the mapping
     * keys with a number to allow for automatic alphabetical ordering when returned.
     * 
     * @param objs current state of codegen object model.
     * @return modified state of the codegen object model.
     */
    public Map<String, Object> topologicalSort(Map<String, Object> objs)
    {
        if (objs.isEmpty()) {
            LOGGER.error("Could not read models. Is the OAS valid?");
            return objs;
        }

        List<String> allSchemas = ModelUtils.getAllUsedSchemas(this.openAPI);
        
        // find all connected models as nodes and build an adjacency graph 
        Map<String, Set<String>> mapEdges = new HashMap<String, Set<String>>();
        for (String modelName : objs.keySet()) {
            Map<String, Object> obj = (Map<String, Object>) objs.get(modelName);
            List<Map<String, Object>> allModels = (List<Map<String, Object>>) obj.get("models");
            for (Map<String, Object> models : allModels) {

                if (models.get("model") instanceof CodegenModel) {
                    CodegenModel model = (CodegenModel) models.get("model");
                    
                    Set<String> tmpTargetNodes = new TreeSet<>();
                    for (CodegenProperty cp : model.getVars()) {
                        // get all models used in the specification
                        if (cp.isModel) {
                            tmpTargetNodes.add(cp.openApiType);
                        } else if (cp.isModelContainer) {
                            tmpTargetNodes.add(cp.items.openApiType);
                        }
                    }
                    if (!tmpTargetNodes.isEmpty()) {
                        mapEdges.put(modelName, tmpTargetNodes);
                    }
                }
            }
        }

        // determine which nodes are start and/or end nodes
        Map<String, Integer> mapUsedNodes = new HashMap<>();
        for (String startNode : mapEdges.keySet()) {
            if (mapEdges.get(startNode).size() > 0) {
                if (!mapUsedNodes.containsKey(startNode)) {
                    mapUsedNodes.put(startNode, 0);
                } else {
                    mapUsedNodes.put(startNode, mapUsedNodes.get(startNode) + 1);
                }
            }
            
            for(String targetNode : mapEdges.get(startNode)) {
                if (!mapUsedNodes.containsKey(targetNode)) {
                    mapUsedNodes.put(targetNode, 1);
                } else {
                    mapUsedNodes.put(targetNode, mapUsedNodes.get(targetNode) + 1);
                }
            }
        }

        // determine which nodes can be used as root
        List<String> listRootNodes = new ArrayList<>();
        for (String node : mapUsedNodes.keySet()) {
            if (mapUsedNodes.get(node) == 0) {
                listRootNodes.add(node);
            }
        }

        if (listRootNodes.size() == 0) {
            LOGGER.error("No root schema found.");
        }

        // trigger sorting algorithm for every root node, put model names into a stack
        LinkedList<String> listSorted = new LinkedList<>();
        Vector<String> vecVisited = new Vector<>();
        for (String node : listRootNodes) {
            if (!vecVisited.contains(node)) {
                topologicalSortUtil(node, vecVisited, listSorted, mapEdges);
            }
        }
        // also push the unused models into the stack, for completeion reasons
        for (String modelName : objs.keySet()) {
            if (!vecVisited.contains(modelName)) {
                vecVisited.add(modelName);
                listSorted.push(modelName);
            }
        }
        
        // empty the stack and put the sorted models into the acutal object
        Map<String, Object> sortedObjs = new TreeMap<String, Object>();

        // add leading zeros to allow for automated alphabetical sorting
        LOGGER.debug("Prefixing numbers to model keys for sorting.");
        String formatted = "%0" + (int)(objs.size()*0.1) + "d";
        for (String node : listSorted) {
            sortedObjs.put(
                String.format(Locale.US, formatted, sortedObjs.size()) + "_" + node, 
                objs.get(node)
            );
        }

        return sortedObjs;
    }

    /**
     * Invoked by DefaultGenerator after all models have been processed.
     * Sorts all models in topological order by their dependencies.
     * Determines all used ENUMS for templating usage.
     *
     * @param objs current state of codegen object model.
     * @return modified state of the codegen object model.
     */
    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        objs = super.postProcessAllModels(objs);
        Map<String, Object> allProcessedModels = new TreeMap<String, Object>(objs);

        // clean models of unused composed schemas
        Iterator<Map.Entry<String, Object>> itrObjs = allProcessedModels.entrySet().iterator();
        while (itrObjs.hasNext()) {
            Map.Entry<String, Object> so = itrObjs.next();
            if (so.getKey().endsWith("_allOf") || so.getKey().endsWith("_oneOf") || so.getKey().endsWith("_anyOf")) {
                itrObjs.remove();
            }
        }

        allProcessedModels = topologicalSort(allProcessedModels);

        // Get all Enums and ignore duplicate keys
        Map<String, CodegenModel> allModels = getAllModels(allProcessedModels);
        Map<String, List<String>> mapUniqueEnums = new HashMap<String, List<String>>();
        for (CodegenModel cm : allModels.values()) {
            for (CodegenProperty cp : cm.getVars()) {
                if(cp.isEnum && mapUniqueEnums.get(cp.enumName) == null) {
                    mapUniqueEnums.put(cp.enumName, cp._enum);
                }
            }
        }
        
        // write all enums into a key value pair for template usage
        List<Object> listUniqueEnums = new ArrayList<Object>();
        for (Map.Entry<String, List<String>> ue : mapUniqueEnums.entrySet() ) {
            Map<String, Object> mapUniqueEnum = new HashMap<String, Object>();
            mapUniqueEnum.put("values", ue.getValue());
            mapUniqueEnum.put("name", ue.getKey());
            listUniqueEnums.add(mapUniqueEnum);
            
        }

        additionalProperties.put("hasEnums", (!listUniqueEnums.isEmpty() ? true : false));
        additionalProperties.put("uniqueEnums", listUniqueEnums);

        return allProcessedModels;
    }

    /**
     * Post-process any model and its properties, turn classVarName and complexType
     * into upper snake case
     * 
     * @param model A CodegenModel
     * @param property A CodegenProperty
     */
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        model.classVarName = toUpperModelName(model.classVarName);
        
        if (property.isModel || property.isModelContainer) {
            property.complexType = toUpperModelName(property.complexType);
            
            if (property.items != null) {
                property.items.complexType = toUpperModelName(property.items.complexType);
            }
            
            if (property.mostInnerItems != null) {
                property.mostInnerItems.complexType = toUpperModelName(property.mostInnerItems.complexType);
            }
        }
    }
}

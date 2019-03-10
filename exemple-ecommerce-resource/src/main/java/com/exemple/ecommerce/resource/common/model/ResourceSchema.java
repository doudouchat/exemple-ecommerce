package com.exemple.ecommerce.resource.common.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(name = "resource_schema")
public class ResourceSchema implements Serializable {

    private static final long serialVersionUID = 1L;

    @PartitionKey
    @Column(name = "app")
    private String application;

    @ClusteringColumn
    private String resource;

    @ClusteringColumn(1)
    private String version;

    @Column
    private byte[] content;

    @Column(name = "filter")
    private Set<String> filters = Collections.emptySet();

    @Column(name = "transform")
    private Map<String, Set<String>> transforms = Collections.emptyMap();

    @Column(name = "rule")
    private Map<String, Set<String>> rules = Collections.emptyMap();

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getContent() {
        return ObjectUtils.clone(content);
    }

    public void setContent(byte[] content) {
        this.content = ObjectUtils.clone(content);
    }

    public Set<String> getFilters() {
        return new HashSet<>(filters);
    }

    public void setFilters(Set<String> filters) {
        this.filters = new HashSet<>(filters);
    }

    public Map<String, Set<String>> getTransforms() {
        return transforms;
    }

    public void setTransforms(Map<String, Set<String>> transforms) {
        this.transforms = transforms;
    }

    public Map<String, Set<String>> getRules() {
        return rules;
    }

    public void setRules(Map<String, Set<String>> rules) {
        this.rules = rules;
    }

}

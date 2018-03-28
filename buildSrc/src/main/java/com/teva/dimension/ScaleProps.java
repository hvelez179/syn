package com.teva.dimension;

/**
 * This class holds the scaling properties for a output resource directory for the DimensionScalerTask.
 *
 * The DimensionScalerTask is not part of RespiratoryApp's source.  It is only used by the build.
 */
public class ScaleProps {
    public String name;
    public float value;

    public ScaleProps(String name, float value) {
        this.name = name;
        this.value = value;
    }
}

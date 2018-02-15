package org.radarcns.domain.restapi.format;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Acceleration {


    /**
     * Acceleration in the x-axis (g).
     */
    @JsonProperty
    public java.lang.Object x;
    /**
     * Acceleration in the y-axis (g).
     */
    @JsonProperty
    public java.lang.Object y;
    /**
     * Acceleration in the z-axis (g).
     */
    @JsonProperty
    public java.lang.Object z;

    /**
     * Default constructor.  Note that this does not initialize fields to their default values from
     * the schema.  If that is desired then one should use <code>newBuilder()</code>.
     */
    public Acceleration() {
    }

    /**
     * All-args constructor.
     *
     * @param x Acceleration in the x-axis (g).
     * @param y Acceleration in the y-axis (g).
     * @param z Acceleration in the z-axis (g).
     */
    public Acceleration(java.lang.Object x, java.lang.Object y, java.lang.Object z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Object getX() {
        return x;
    }

    public void setX(Object x) {
        this.x = x;
    }

    public Object getY() {
        return y;
    }

    public void setY(Object y) {
        this.y = y;
    }

    public Object getZ() {
        return z;
    }

    public void setZ(Object z) {
        this.z = z;
    }
}
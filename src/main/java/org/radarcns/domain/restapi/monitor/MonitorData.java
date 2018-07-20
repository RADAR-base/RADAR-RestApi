package org.radarcns.domain.restapi.monitor;

import java.util.Objects;

import org.radarcns.domain.restapi.header.MonitorHeader;

public class MonitorData {

    private MonitorHeader header;

    private Object data;

    public MonitorData() {

    }

    public MonitorData(MonitorHeader monitorHeader, Object data) {
        this.header = monitorHeader;
        this.data = data;
    }

    public MonitorHeader getHeader() {
        return header;
    }

    public void setHeader(MonitorHeader header) {
        this.header = header;
    }

    public Object getData() {
        return data;
    }

    public MonitorData header(MonitorHeader monitorHeader) {
        this.header = monitorHeader;
        return this;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public MonitorData data(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MonitorData that = (MonitorData) o;
        return Objects.equals(header, that.header) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(header, data);
    }

    @Override
    public String toString() {
        return "MonitorData{" + "header=" + header + ", data=" + data + '}';
    }
}

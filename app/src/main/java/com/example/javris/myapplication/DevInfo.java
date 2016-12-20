package com.example.javris.myapplication;

import java.io.Serializable;
import java.util.Locale;


public class DevInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String macAddress;

    public DevInfo() {

    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof DevInfo && (this == o || macAddress.equals(((DevInfo) o).getMacAddress()));
    }

    DevInfo(String name, String macAddress) {
        super();
        this.name = name;
        this.macAddress = macAddress;
        if (this.macAddress != null) {
            this.macAddress = this.macAddress.toUpperCase(Locale.US);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        if (this.macAddress != null) {
            this.macAddress = this.macAddress.toUpperCase(Locale.US);
        }
    }

    @Override
    public String toString() {
        return "DevInfo [name=" + name + ", macAddress=" + macAddress + "]";
    }

}

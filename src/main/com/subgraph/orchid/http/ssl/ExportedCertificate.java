package com.subgraph.orchid.http.ssl;

public class ExportedCertificate {
    private final String extension;
    private final String name;
    private final String path;

    private ExportedCertificate(String path, String name, String extension){
        this.extension = extension;
        this.name = name;
        this.path = path;
    }

    public static ExportedCertificate getInstance(String path, String name, String extension){
        return new ExportedCertificate(path, name, extension);
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}